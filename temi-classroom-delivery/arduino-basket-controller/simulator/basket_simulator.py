#!/usr/bin/env python3
"""Local HTTP simulator for the ESP32 basket protocol.

The simulator deliberately mirrors the firmware's safety gates and has no external
dependencies. Set BASKET_DEVICE_TOKEN before starting it; an empty token is refused.
"""

from __future__ import annotations

import argparse
import json
import os
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any


PROTOCOL_VERSION = 1
MAX_BODY_BYTES = 8 * 1024
MAX_ID_LENGTH = 64


class BasketState:
    def __init__(self, device_id: str, firmware_version: str) -> None:
        self.device_id = device_id
        self.firmware_version = firmware_version
        self.started_at = time.monotonic()
        self.sequence = 0
        self.door = "CLOSED"
        self.lock = "LOCKED"
        self.sensor = "OK"
        self.weight_grams: int | None = 0
        self.load_state = "OK"
        self.mission: dict[str, Any] | None = None
        self.last_request_id = ""
        self.last_response: tuple[int, dict[str, Any]] | None = None
        self.lock_guard = threading.Lock()

    def status(self) -> dict[str, Any]:
        return {
            "protocolVersion": PROTOCOL_VERSION,
            "deviceId": self.device_id,
            "firmwareVersion": self.firmware_version,
            "sequence": self.sequence,
            "uptimeMs": int((time.monotonic() - self.started_at) * 1000),
            "door": self.door,
            "lock": self.lock,
            "sensor": self.sensor,
            "weightGrams": self.weight_grams,
            "loadState": self.load_state,
        }


class BasketHandler(BaseHTTPRequestHandler):
    state: BasketState
    device_token: str

    def do_GET(self) -> None:  # noqa: N802 - BaseHTTPRequestHandler API
        if self.path != "/api/v1/basket/status":
            self.send_error(404)
            return
        if not self.authorized():
            self.error(401, "unauthorized", "바구니 장치 토큰이 올바르지 않습니다.")
            return
        with self.state.lock_guard:
            self.success({"status": self.state.status()})

    def do_POST(self) -> None:  # noqa: N802 - BaseHTTPRequestHandler API
        if self.path != "/api/v1/basket/commands":
            self.send_error(404)
            return
        if not self.authorized():
            self.error(401, "unauthorized", "바구니 장치 토큰이 올바르지 않습니다.")
            return
        length_header = self.headers.get("Content-Length", "")
        try:
            length = int(length_header)
        except ValueError:
            self.error(400, "invalid_length", "Content-Length가 올바르지 않습니다.")
            return
        if length < 0 or length > MAX_BODY_BYTES:
            self.error(413, "request_too_large", "요청 본문이 너무 큽니다.")
            return
        try:
            body = json.loads(self.rfile.read(length).decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError):
            self.error(400, "invalid_json", "올바른 JSON 객체를 보내 주세요.")
            return
        if not isinstance(body, dict):
            self.error(400, "invalid_json", "JSON 객체가 필요합니다.")
            return

        with self.state.lock_guard:
            request_id = self.required_text(body, "requestId")
            if request_id is None:
                return
            if request_id == self.state.last_request_id and self.state.last_response is not None:
                status_code, response = self.state.last_response
                self.write_json(status_code, response)
                return
            result = self.execute(body)
            if result[0] < 300:
                response = {
                    "success": True,
                    "data": {"requestId": request_id, "status": self.state.status()},
                }
            else:
                response = result[1]
            self.state.last_request_id = request_id
            self.state.last_response = (result[0], response)
            self.write_json(result[0], response)

    def execute(self, body: dict[str, Any]) -> tuple[int, dict[str, Any]]:
        if body.get("protocolVersion") != PROTOCOL_VERSION:
            return self.failure(400, "unsupported_protocol", "지원하지 않는 프로토콜 버전입니다.")
        command = body.get("command")
        if command not in {"PREPARE_MISSION", "UNLOCK", "LOCK", "SAFE_STATE"}:
            return self.failure(400, "invalid_command", "허용되지 않은 바구니 명령입니다.")
        if command == "SAFE_STATE":
            if self.state.door != "CLOSED":
                return self.failure(409, "door_open", "문이 열려 있어 안전 잠금을 확인할 수 없습니다.")
            self.state.mission = None
            self.state.lock = "LOCKED"
            self.state.sequence += 1
            return 200, {}

        mission_id = self.required_text(body, "missionId")
        stop_id = self.required_text(body, "stopId")
        if mission_id is None or stop_id is None:
            return 400, {"success": False, "error": {"code": "invalid_mission", "message": "missionId와 stopId가 필요합니다."}}
        if command in {"PREPARE_MISSION", "UNLOCK"}:
            quantity = body.get("expectedQuantity")
            if not isinstance(quantity, int) or isinstance(quantity, bool) or quantity <= 0:
                return self.failure(400, "invalid_quantity", "expectedQuantity는 1 이상이어야 합니다.")

        if command == "PREPARE_MISSION":
            if not self.safe_for_prepare():
                return self.failure(409, "unsafe_state", "바구니가 준비 가능한 안전 상태가 아닙니다.")
            self.state.mission = {
                "missionId": mission_id,
                "stopId": stop_id,
                "expectedQuantity": body["expectedQuantity"],
            }
            self.state.sequence += 1
            return 200, {}

        if self.state.mission is None or self.state.mission.get("missionId") != mission_id or self.state.mission.get("stopId") != stop_id:
            return self.failure(409, "mission_mismatch", "준비된 배부 작업과 일치하지 않습니다.")

        if command == "UNLOCK":
            if self.state.mission.get("expectedQuantity") != body["expectedQuantity"]:
                return self.failure(409, "mission_mismatch", "준비된 수량과 일치하지 않습니다.")
            if not self.safe_for_unlock():
                return self.failure(409, "unsafe_state", "문·잠금·센서 상태가 안전하지 않아 열지 않습니다.")
            self.state.lock = "UNLOCKED"
            self.state.sequence += 1
            return 200, {}

        if self.state.door != "CLOSED":
            return self.failure(409, "door_open", "문이 열려 있어 잠글 수 없습니다.")
        self.state.lock = "LOCKED"
        self.state.mission = None
        self.state.sequence += 1
        return 200, {}

    def safe_for_prepare(self) -> bool:
        return (
            self.state.door == "CLOSED"
            and self.state.lock == "LOCKED"
            and self.state.sensor == "OK"
            and self.state.load_state == "OK"
        )

    def safe_for_unlock(self) -> bool:
        return self.safe_for_prepare() and self.state.mission is not None

    def required_text(self, body: dict[str, Any], field: str) -> str | None:
        value = body.get(field)
        if not isinstance(value, str) or not value.strip() or len(value.strip()) > MAX_ID_LENGTH:
            self.error(400, "invalid_field", f"{field}가 올바르지 않습니다.")
            return None
        return value.strip()

    def authorized(self) -> bool:
        expected = f"Bearer {self.device_token}"
        provided = self.headers.get("Authorization", "")
        return bool(self.device_token) and provided == expected

    def success(self, data: dict[str, Any]) -> None:
        self.write_json(200, {"success": True, "data": data})

    def error(self, status: int, code: str, message: str) -> None:
        self.write_json(status, {"success": False, "error": {"code": code, "message": message}})

    @staticmethod
    def failure(status: int, code: str, message: str) -> tuple[int, dict[str, Any]]:
        return status, {"success": False, "error": {"code": code, "message": message}}

    def write_json(self, status: int, payload: dict[str, Any]) -> None:
        encoded = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.end_headers()
        self.wfile.write(encoded)

    def log_message(self, fmt: str, *args: Any) -> None:
        print(f"[{self.log_date_time_string()}] {self.address_string()} {fmt % args}")


def main() -> None:
    parser = argparse.ArgumentParser(description="ESP32 basket HTTP simulator")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8788)
    parser.add_argument("--device-id", default="basket-01")
    parser.add_argument("--firmware-version", default="simulator-0.1.0")
    args = parser.parse_args()
    token = os.environ.get("BASKET_DEVICE_TOKEN", "").strip()
    if not token:
        raise SystemExit("BASKET_DEVICE_TOKEN 환경 변수를 먼저 설정하세요.")

    state = BasketState(args.device_id, args.firmware_version)

    class BoundHandler(BasketHandler):
        pass

    BoundHandler.state = state
    BoundHandler.device_token = token
    server = ThreadingHTTPServer((args.host, args.port), BoundHandler)
    print(f"basket simulator listening on http://{args.host}:{args.port}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("stopping")
    finally:
        server.server_close()


if __name__ == "__main__":
    main()

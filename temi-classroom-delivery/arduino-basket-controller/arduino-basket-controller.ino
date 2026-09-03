/*
 * temi classroom delivery basket controller
 *
 * Arduino IDE / ESP32 Wi-Fi HTTP+JSON MVP.
 * Install ArduinoJson and ESP32Servo from the Arduino IDE library manager.
 *
 * This is a LAN prototype. It intentionally fails closed when a sensor is
 * unknown and starts locked after every reboot. Do not expose this HTTP server
 * to the Internet or use the NVIDIA NIM key as DEVICE_TOKEN.
 */

#include <Arduino.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>
#include <WebServer.h>
#include <WiFi.h>
#include "arduino_secrets.h"

const char* WIFI_SSID = TEMI_WIFI_SSID;
const char* WIFI_PASSWORD = TEMI_WIFI_PASSWORD;
const char* DEVICE_ID = TEMI_DEVICE_ID;
const char* DEVICE_TOKEN = TEMI_DEVICE_TOKEN;
const char* FIRMWARE_VERSION = "0.1.0";

// Bench-only switch. Keep 0 for any robot or physical basket test.
#ifndef BASKET_TEST_MODE
#define BASKET_TEST_MODE 0
#endif

constexpr int LOCK_SERVO_PIN = 18;
constexpr int DOOR_SENSOR_PIN = 19;
constexpr int LOCKED_ANGLE = 20;
constexpr int UNLOCKED_ANGLE = 95;
constexpr size_t MAX_BODY_BYTES = 8 * 1024;
constexpr size_t MAX_ID_LENGTH = 64;
constexpr int PROTOCOL_VERSION = 1;

WebServer server(80);
Servo lockServo;
unsigned long bootMillis = 0;
unsigned long sequenceNumber = 0;
bool commandedLocked = true;
String preparedMissionId;
String preparedStopId;
int preparedQuantity = 0;
String lastRequestId;
String lastResponse;
int lastResponseCode = 0;

bool secureTokenEquals(const String& provided) {
  const String expected = String("Bearer ") + DEVICE_TOKEN;
  if (strlen(DEVICE_TOKEN) == 0 || String(DEVICE_TOKEN) == "REPLACE_WITH_PER_DEVICE_BEARER_TOKEN") return false;
  if (provided.length() != expected.length()) return false;
  uint8_t difference = 0;
  for (size_t i = 0; i < expected.length(); ++i) {
    difference |= static_cast<uint8_t>(provided[i] ^ expected[i]);
  }
  return difference == 0;
}

bool authorized() {
  if (!server.hasHeader("Authorization")) return false;
  return secureTokenEquals(server.header("Authorization"));
}

String doorState() {
  // INPUT_PULLUP + closed reed switch to GND means CLOSED.
  if (DOOR_SENSOR_PIN < 0) return "UNKNOWN";
  return digitalRead(DOOR_SENSOR_PIN) == LOW ? "CLOSED" : "OPEN";
}

String sensorState() {
  return doorState() == "UNKNOWN" ? "UNKNOWN" : "OK";
}

String loadState() {
#if BASKET_TEST_MODE
  return "OK";
#else
  // HX711/로드셀 판독을 실제로 연결한 뒤 이 함수를 구현합니다.
  return "UNKNOWN";
#endif
}

void setLocked(bool locked) {
  commandedLocked = locked;
  lockServo.write(locked ? LOCKED_ANGLE : UNLOCKED_ANGLE);
  ++sequenceNumber;
}

void writeStatus(JsonObject status) {
  status["protocolVersion"] = PROTOCOL_VERSION;
  status["deviceId"] = DEVICE_ID;
  status["firmwareVersion"] = FIRMWARE_VERSION;
  status["sequence"] = sequenceNumber;
  status["uptimeMs"] = millis() - bootMillis;
  status["door"] = doorState();
  status["lock"] = commandedLocked ? "LOCKED" : "UNLOCKED";
  status["sensor"] = sensorState();
#if BASKET_TEST_MODE
  status["weightGrams"] = 0;
#else
  status["weightGrams"] = nullptr;
#endif
  status["loadState"] = loadState();
}

String statusEnvelope() {
  StaticJsonDocument<1024> document;
  document["success"] = true;
  JsonObject data = document.createNestedObject("data");
  JsonObject status = data.createNestedObject("status");
  writeStatus(status);
  String response;
  serializeJson(document, response);
  return response;
}

void sendJson(int statusCode, const String& body) {
  server.send(statusCode, "application/json; charset=utf-8", body);
}

void sendError(int statusCode, const char* code, const char* message, const String& requestId = String()) {
  StaticJsonDocument<512> document;
  document["success"] = false;
  JsonObject error = document.createNestedObject("error");
  error["code"] = code;
  error["message"] = message;
  String response;
  serializeJson(document, response);
  if (requestId.length() > 0) {
    lastRequestId = requestId;
    lastResponse = response;
    lastResponseCode = statusCode;
  }
  sendJson(statusCode, response);
}

bool textField(JsonObject body, const char* field, String& output) {
  if (!body[field].is<const char*>()) return false;
  output = body[field].as<String>();
  output.trim();
  return output.length() > 0 && output.length() <= MAX_ID_LENGTH;
}

bool missionMatches(const String& missionId, const String& stopId) {
  return preparedMissionId == missionId && preparedStopId == stopId &&
      preparedMissionId.length() > 0;
}

bool safeForPreparation() {
  return doorState() == "CLOSED" && commandedLocked && sensorState() == "OK" &&
      loadState() == "OK";
}

bool safeForUnlock() {
  return safeForPreparation() && preparedMissionId.length() > 0;
}

void successfulCommandResponse(const String& requestId) {
  StaticJsonDocument<1024> document;
  document["success"] = true;
  JsonObject data = document.createNestedObject("data");
  data["requestId"] = requestId;
  JsonObject status = data.createNestedObject("status");
  writeStatus(status);
  String response;
  serializeJson(document, response);
  lastRequestId = requestId;
  lastResponse = response;
  lastResponseCode = 200;
  sendJson(200, response);
}

void handleStatus() {
  if (!authorized()) {
    sendError(401, "unauthorized", "바구니 장치 토큰이 올바르지 않습니다.");
    return;
  }
  sendJson(200, statusEnvelope());
}

void handleCommand() {
  if (!authorized()) {
    sendError(401, "unauthorized", "바구니 장치 토큰이 올바르지 않습니다.");
    return;
  }
  if (server.arg("plain").length() > MAX_BODY_BYTES) {
    sendError(413, "request_too_large", "요청 본문이 너무 큽니다.");
    return;
  }

  StaticJsonDocument<1536> body;
  DeserializationError parseError = deserializeJson(body, server.arg("plain"));
  if (parseError) {
    sendError(400, "invalid_json", "올바른 JSON 객체를 보내 주세요.");
    return;
  }
  JsonObject input = body.as<JsonObject>();
  String requestId;
  if (!textField(input, "requestId", requestId)) {
    sendError(400, "invalid_request_id", "requestId가 필요합니다.");
    return;
  }
  if (requestId == lastRequestId && lastResponseCode != 0) {
    // Same requestId means the same side effect must not be executed again.
    sendJson(lastResponseCode, lastResponse);
    return;
  }
  if (input["protocolVersion"] != PROTOCOL_VERSION) {
    sendError(400, "unsupported_protocol", "지원하지 않는 프로토콜 버전입니다.", requestId);
    return;
  }
  String command = input["command"] | "";
  String missionId;
  String stopId;
  if (command != "SAFE_STATE") {
    if (!textField(input, "missionId", missionId) || !textField(input, "stopId", stopId)) {
      sendError(400, "invalid_mission", "missionId와 stopId가 필요합니다.", requestId);
      return;
    }
  }

  if (command == "PREPARE_MISSION") {
    int quantity = input["expectedQuantity"] | 0;
    if (quantity <= 0 || quantity > 100) {
      sendError(400, "invalid_quantity", "expectedQuantity는 1~100이어야 합니다.", requestId);
      return;
    }
    if (!safeForPreparation()) {
      sendError(409, "unsafe_state", "바구니가 준비 가능한 안전 상태가 아닙니다.", requestId);
      return;
    }
    preparedMissionId = missionId;
    preparedStopId = stopId;
    preparedQuantity = quantity;
    ++sequenceNumber;
    successfulCommandResponse(requestId);
    return;
  }

  if (command == "UNLOCK") {
    int quantity = input["expectedQuantity"] | 0;
    if (quantity <= 0 || quantity != preparedQuantity || !missionMatches(missionId, stopId) || !safeForUnlock()) {
      sendError(409, "unsafe_state", "준비된 작업과 안전 상태를 확인할 수 없어 열지 않습니다.", requestId);
      return;
    }
    setLocked(false);
    successfulCommandResponse(requestId);
    return;
  }

  if (command == "LOCK") {
    if (!missionMatches(missionId, stopId)) {
      sendError(409, "mission_mismatch", "준비된 배부 작업과 일치하지 않습니다.", requestId);
      return;
    }
    if (doorState() != "CLOSED") {
      sendError(409, "door_open", "문이 열려 있어 잠글 수 없습니다.", requestId);
      return;
    }
    setLocked(true);
    preparedMissionId = "";
    preparedStopId = "";
    preparedQuantity = 0;
    successfulCommandResponse(requestId);
    return;
  }

  if (command == "SAFE_STATE") {
    if (doorState() != "CLOSED") {
      sendError(409, "door_open", "문이 열려 있어 안전 잠금을 확인할 수 없습니다.", requestId);
      return;
    }
    setLocked(true);
    preparedMissionId = "";
    preparedStopId = "";
    preparedQuantity = 0;
    successfulCommandResponse(requestId);
    return;
  }

  sendError(400, "invalid_command", "허용되지 않은 바구니 명령입니다.", requestId);
}

void setup() {
  Serial.begin(115200);
  bootMillis = millis();
  pinMode(DOOR_SENSOR_PIN, INPUT_PULLUP);

  // Reboot is always a safe locked state.
  lockServo.setPeriodHertz(50);
  lockServo.attach(LOCK_SERVO_PIN, 500, 2400);
  setLocked(true);
  preparedMissionId = "";
  preparedStopId = "";
  preparedQuantity = 0;

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  const unsigned long deadline = millis() + 20'000;
  while (WiFi.status() != WL_CONNECTED && millis() < deadline) {
    delay(250);
    Serial.print('.');
  }
  Serial.println();
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi unavailable; actuator remains locked.");
    return;
  }

  const char* headerKeys[] = {"Authorization"};
  server.collectHeaders(headerKeys, 1);
  server.on("/api/v1/basket/status", HTTP_GET, handleStatus);
  server.on("/api/v1/basket/commands", HTTP_POST, handleCommand);
  server.onNotFound([]() { sendError(404, "not_found", "요청한 바구니 API가 없습니다."); });
  server.begin();
  Serial.print("Basket controller ready at http://");
  Serial.println(WiFi.localIP());
}

void loop() {
  server.handleClient();
}

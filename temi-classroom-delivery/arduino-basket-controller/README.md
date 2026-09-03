# ESP32 Arduino 바구니 컨트롤러

이 폴더는 temi 앱과 같은 Wi-Fi에 연결된 ESP32가 바구니 잠금장치와 센서의
상태를 HTTP/JSON으로 제공하는 최소 펌웨어입니다. `basket-client` Kotlin
모듈과 아래 프로토콜을 공유합니다.

현재 스케치는 하드웨어가 연결되지 않은 상태에서 안전을 우회하지 않습니다.
문 센서 또는 적재 센서가 `UNKNOWN`이면 `PREPARE_MISSION`과 `UNLOCK`을
거부합니다. 실제 센서를 연결하기 전에는 로봇 주행과 연동하지 마세요.

## Arduino IDE 준비

1. Arduino IDE에 Espressif ESP32 보드 패키지를 설치합니다.
2. 라이브러리 매니저에서 `ArduinoJson`과 `ESP32Servo`를 설치합니다.
3. `arduino_secrets.example.h`를 `arduino_secrets.h`로 복사한 뒤 Wi-Fi
   SSID/비밀번호, 장치 ID, 장치 토큰을 장치별로 입력합니다.
   `arduino_secrets.h`는 Git에서 제외되므로 실제 비밀번호와 토큰을 커밋하지
   마세요.
4. `arduino-basket-controller.ino`를 열고 ESP32 보드와 포트를 선택한 뒤
   업로드합니다.

Android APK는 다시 빌드할 필요가 없습니다. 설치 후 우측 상단 **기능 설정**에서
ESP32의 현재 IP 주소, `DEVICE_ID`, 같은 `DEVICE_TOKEN`을 입력하고 Arduino
연동을 켠 뒤 저장합니다. **바구니 상태 확인**이 연결 시험이며, **안전 잠금**은
문 닫힘이 확인된 경우에만 서보 잠금을 요청합니다. 토큰이나 Wi-Fi를 모르는
새 ESP32를 APK만으로 자동 설정할 수는 없으므로 펌웨어 업로드와 같은 네트워크
연결은 먼저 완료해야 합니다.

토큰은 NVIDIA NIM API 키와 전혀 다른 장치용 Bearer 토큰입니다. 스케치의
HTTP 서버는 `Authorization: Bearer <device-token>`이 없거나 다르면 401을
반환합니다. 이 MVP는 TLS를 포함하지 않으므로 학교 내부 WPA2 네트워크에서만
시험하고 포트 포워딩하지 않아야 합니다. 운영 전에는 HTTPS/mTLS 또는 인증된
로컬 게이트웨이를 추가해야 합니다.

## 기본 배선

- 서보 신호: GPIO 18
- 문 닫힘 리드/리미트 센서: GPIO 19, 닫힘일 때 GND로 연결
- ESP32와 서보 전원은 공통 GND를 사용합니다.
- 서보를 ESP32의 3.3V 핀에서 직접 구동하지 말고 별도 전원과 전원 차단 수단을
  사용합니다.
- 실제 잠금 확인에는 서보 각도만 사용하지 말고 잠금 위치 피드백 센서를
  추가해야 합니다.

적재 센서(HX711/로드셀)는 현재 인터페이스만 예약돼 있습니다. 스케치의
`BASKET_TEST_MODE`는 센서가 연결되지 않은 벤치 테스트에서만 사용할 수 있고,
temi 실기 주행에서는 반드시 0으로 두고 실제 센서 판독을 구현해야 합니다.

## API

### 상태 조회

```text
GET /api/v1/basket/status
Authorization: Bearer <device-token>
```

응답 예시:

```json
{
  "success": true,
  "data": {
    "status": {
      "protocolVersion": 1,
      "deviceId": "basket-01",
      "firmwareVersion": "0.1.0",
      "sequence": 42,
      "uptimeMs": 1234567,
      "door": "CLOSED",
      "lock": "LOCKED",
      "sensor": "OK",
      "weightGrams": 1040,
      "loadState": "OK"
    }
  }
}
```

`door`는 `OPEN/CLOSED/UNKNOWN`, `lock`은 `LOCKED/UNLOCKED/UNKNOWN`,
`sensor`와 `loadState`는 `OK/OVERLOAD/UNBALANCED/SENSOR_FAULT/UNKNOWN` 중
하나입니다. 앱은 `UNKNOWN`, 과적, 불균형, 센서 오류를 안전하지 않은 상태로
취급해야 합니다.

### 명령

```text
POST /api/v1/basket/commands
Authorization: Bearer <device-token>
Content-Type: application/json
```

공통 필드:

```json
{
  "protocolVersion": 1,
  "requestId": "uuid",
  "command": "PREPARE_MISSION"
}
```

허용 명령과 추가 필드는 다음과 같습니다.

| 명령 | 추가 필드 | 조건 |
|---|---|---|
| `PREPARE_MISSION` | `missionId`, `stopId`, `expectedQuantity` | 문 닫힘, 잠김, 센서 정상, 적재 정상 |
| `UNLOCK` | `missionId`, `stopId`, `expectedQuantity` | 동일한 준비 작업, 문 닫힘, 잠김, 센서 정상 |
| `LOCK` | `missionId`, `stopId` | 문 닫힘, 동일한 준비 작업 |
| `SAFE_STATE` | `reason` | 문 닫힘 확인 후 준비 작업 해제 및 잠금 |

성공 응답은 요청의 `requestId`를 그대로 반환합니다.

```json
{
  "success": true,
  "data": {
    "requestId": "uuid",
    "status": { "protocolVersion": 1, "deviceId": "basket-01" }
  }
}
```

`requestId`가 같은 명령은 펌웨어가 마지막 응답을 재사용하고 서보를 다시
움직이지 않습니다. Android 클라이언트는 부작용 명령의 HTTP timeout을
자동 재시도하지 않습니다. 응답을 잃은 경우 먼저 status를 조회해야 합니다.

ESP32 재부팅 시 서보 명령 상태는 항상 잠금으로 초기화됩니다. 마지막 명령의
idempotency 기록은 재부팅 후 복구하지 않으므로, 재부팅 전의 `UNLOCK`을
그대로 재생할 수 없습니다.

## PC 시뮬레이터

실제 ESP32 없이 앱 HTTP 클라이언트를 시험하려면 Python 3.10+ 표준 라이브러리만
사용하는 시뮬레이터를 실행합니다.

PowerShell:

```powershell
$env:BASKET_DEVICE_TOKEN = 'local-device-token'
python .\simulator\basket_simulator.py --port 8788 --device-id basket-01
```

`basket-client` 설정의 `baseUrl`을 `http://127.0.0.1:8788`로 지정합니다.
시뮬레이터도 실제 펌웨어처럼 기본 잠금, Bearer 인증, 미리 준비된 작업과
문 닫힘 조건, 중복 `requestId` 처리를 적용합니다.

## 센서 확장 순서

1. 문 센서와 잠금 위치 센서를 먼저 추가합니다.
2. HX711 + 로드셀 1개로 총 무게와 수령 감지를 보정합니다.
3. 무게중심까지 필요하면 바구니 네 모서리에 로드셀 4개를 배치합니다.
4. 센서 단선·드리프트·과적·불균형은 모두 `UNKNOWN`/오류로 보고 출발을
   차단합니다.

LLM/NIM은 설명과 학교 데이터 질의응답만 담당하며, 서보 잠금 해제나 temi
   출발 승인에는 사용하지 않습니다.

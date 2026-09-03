# temi 수업용품 배부 앱

temi V3의 바구니에 수업용품을 싣고, 지도 위 학생 지점을 순서대로 방문해 배부하는 Android 앱입니다. 이 1차 버전은 별도 바구니 센서가 없다는 전제이므로, 각 지점에서 학생이나 교사가 화면의 **수령 확인 · 다음으로** 버튼을 눌러야 다음 지점으로 이동합니다.

버전 0.4.3은 교사가 미리 등록한 물품·교직원 정보를 바탕으로 GPT-5.6 Luna가 먼저 안내하고, 실패하면 NVIDIA NIM의 `deepseek-ai/deepseek-v4-flash-0731`을 사용합니다. 실통신에서 NIM 키와 모델 목록은 유효했지만 추론 요청이 반복해서 timeout되어 우선순위를 바꿨습니다. 두 모델이 모두 실패해도 등록된 정보만으로 답하고, 없는 담당자를 추측하지 않습니다. API 키는 APK에 넣지 않고 별도 Node.js 백엔드에서만 사용합니다.

## 구현된 동작

1. 앱 시작 시 `delivery_route.json`을 읽고 temi 연결 상태와 저장 위치를 확인합니다.
2. 배부 시작 전에 모든 지점과 복귀 위치가 유효한지 검사합니다.
3. 최대 `0.2 m/s`의 저속으로 첫 학생 지점에 이동합니다.
4. 도착 콜백을 받은 뒤 한국어로 수령할 물품과 수량을 안내합니다.
5. 수령 확인 후 다음 지점으로 이동하고, 마지막 지점을 마치면 복귀 위치로 돌아갑니다.
6. 이동 실패 시 자동 재시도하지 않고 `다시 시도`, `건너뛰기`, `지금 복귀` 중 하나를 사람이 선택하게 합니다.
7. 언제든 긴급 정지할 수 있습니다.

앱이 시작되거나 다시 화면에 나타날 때 temi Settings 권한이 있으면 런처의 일반 위치 이동 기본값도 `VERY_SLOW`(약 `0.3 m/s`), 따라가기 기본값은 지원되는 최저 단계인 `SLOW`(약 `0.5 m/s`)로 낮춥니다. 앱이 직접 실행하는 배부·홈베이스·저장 위치 이동에는 이보다 낮은 최대 `0.2 m/s` 제한을 매번 명시합니다.

핵심 로직은 temi SDK와 분리된 `DeliveryStateMachine`에 있습니다. Android/SDK 연결은 `TemiRobotGateway`, 작업 명령 실행은 `DeliveryCoordinator`가 담당합니다.

## AI 물품 설명 및 학교 담당자 찾기

구성은 다음처럼 분리되어 있습니다.

```text
temi 앱의 ai-guide-client
        │ itemId 또는 담당자 질문 전송
        ▼
교사용 ai-guide-backend
        │ 교사가 등록한 물품·공개 교직원 정보만 검색
        ▼
GPT-5.6 Luna
        │ 실패
        ▼
NVIDIA NIM DeepSeek V4 Flash 0731
        │ 실패·타임아웃
        ▼
등록 정보만 사용하는 결정론적 fallback
```

- Android 라이브러리: `ai-guide-client/`
- 독립 백엔드: `ai-guide-backend/`
- 공개 기본 설정: `app/src/main/assets/ai_guide_config.json`
- 로컬 디버그 설정: `app/src/debug/assets/ai_guide_config.local.json` (Git 제외)
- 교사 입력 예시: `ai-guide-backend/data/items.json`
- 교직원 명단: `ai-guide-backend/data/staff-directory.json`
- 배달 물품 연결 키: `delivery_route.json`의 `guideItemId`

현재 예시 경로의 `과학 실험 키트`는 `science-kit`에 연결되어 있습니다. temi가 학생 지점에 도착하면 **AI 사용법 설명 듣기** 버튼이 나타나고, 생성된 설명을 화면에 표시한 뒤 temi TTS로 읽습니다. 설명이 끝나도 로봇은 자동 출발하지 않으며 기존처럼 사람이 **수령 확인 · 다음으로**를 눌러야 합니다.

### 처음 연결하기

1. 교사용 PC에서 `ai-guide-backend/README.md`를 따라 서버를 준비합니다.
2. Luna·음성 인식 키는 `OPENAI_API_KEY` 또는 `LUNA_API_KEY`, NIM 백업 키는 `NVIDIA_NIM_API_KEY`로 `ai-guide-backend/.env`에만 저장합니다.
3. `app/src/main/assets/ai_guide_config.json`을 `app/src/debug/assets/ai_guide_config.local.json`으로 복사하고 `enabled`를 `true`로 바꿉니다.
4. 로컬 설정의 `baseUrl`을 교사용 PC의 실제 LAN 주소로 바꿉니다. 예: `http://192.168.104.50:8787`
5. 백엔드의 필수 `TEMI_CLIENT_TOKEN`과 로컬 설정의 `clientToken`에 같은 긴 임의 값을 넣고 디버그 APK를 다시 빌드합니다. 로컬 설정 파일은 Git에서 제외됩니다. 이 값은 교실망 접근 게이트이며 TLS를 대신하지 않습니다.
6. 같은 Wi-Fi에서 temi가 교사용 PC의 8787 포트에 접근할 수 있는지 확인합니다.

공개 기본 설정은 AI 연결이 꺼져 있습니다. 개발 장비의 Git 제외 로컬 설정에서만 AI 연결을 켜고 교사용 PC의 temi망 주소를 사용합니다. PC와 temi를 같은 Wi-Fi에 연결하고 백엔드를 실행해야 하며, PC의 LAN 주소가 바뀌면 로컬 설정의 `baseUrl`을 새 주소로 갱신한 뒤 APK를 다시 빌드해야 합니다. 디버그 APK만 교실 내부 HTTP 주소를 허용하며, release APK는 HTTPS 서버를 사용해야 합니다. NIM/OpenAI 키, 관리자 토큰, 학생 이름, 로봇 좌표는 APK의 AI 요청에 포함하지 않습니다.

화면 상단의 **학교 담당자 찾기**에는 “진로 상담은 어떤 선생님을 찾아야 하나요?”처럼 업무·과목·이름을 입력합니다. 백엔드는 `visibility: public`인 명단을 먼저 결정론적으로 검색하고 그 결과만 모델에 전달합니다. 명단에 일치 항목이 없으면 모델을 호출하지 않습니다. 외부 시스템은 관리자 Bearer 토큰으로 `PUT /api/v1/admin/directory` 또는 `PUT /api/v1/admin/directory/teachers/{id}`를 호출해 명단을 교체·추가할 수 있습니다. 현재 `staff-directory.json`은 실제 교직원을 임의로 만들지 않기 위해 비어 있으므로, 승인된 공개 업무·과목·위치 데이터를 가져오기 전에는 담당자 없음 안내만 반환합니다. 학생 개인정보나 연락처는 이 명단에 넣지 않습니다.

API 키나 인터넷 연결이 끊겨도 `FALLBACK_ON_PROVIDER_ERROR=true`이면 교사가 입력한 내용만 조합해 읽습니다. 화면에 `교직원 명단 기반 안내` 또는 `선생님 입력 대체 안내`라고 표시하므로 모델 응답으로 오인하지 않습니다.

## 음성 질문과 MVP 기능 설정

학교 담당자 질문은 키보드 외에 다음 세 방식 중 하나를 앱의 **기능 설정**에서 선택할 수 있습니다.

- 한 번 탭해 녹음을 시작하고 다시 탭해 종료
- 버튼을 누르고 있는 동안 녹음
- “Hey temi” 호출 후 temi 내장 ASR로 질문

앞의 두 방식은 Android 마이크에서 최대 20초의 16 kHz 모노 PCM 음성을 메모리에만 녹음합니다. 0.3초의 시작 안정화 뒤 실제 음성을 감지하고, 말이 끝난 뒤 1.5초간 무음이면 자동으로 녹음을 닫아 인증된 백엔드로 보냅니다. `gpt-transcribe`를 먼저 사용하고 실패할 때만 `whisper-1`을 사용합니다. 백엔드는 WAV 형식·길이·크기를 검사하고 원본 음성을 파일로 저장하지 않습니다. 호출어 방식은 temi SDK의 `WakeupWordListener`, `askQuestion`, `AsrListener`를 사용하므로 OpenAI로 원본 음성을 보내지 않습니다.

인식된 문장은 AI 질문보다 먼저 앱 내부의 결정론적 명령 해석기를 통과합니다. 다음 명령은 LLM에 보내지 않고 바로 temi SDK로 실행합니다.

- `홈베이스로 가`, `충전기로 가`, `충전하러 가` → `goTo("home base")`
- `<저장 위치>로 가` (예: `교탁으로 가`) → temi 지도에 실제 저장된 이름을 확인한 뒤 `goTo(location)`
- `나를 따라와` → `beWithMe(SLOW)`
- `멈춰`, `정지해` → `stopMovement()`

주행 명령은 **배부·주행 제어 기능**이 켜져 있고 temi SDK 연결이 준비된 경우에만 실행합니다. 단, 정지 명령은 안전을 위해 해당 설정이 꺼져 있어도 동작합니다. 등록되지 않은 위치는 추측하거나 AI로 넘기지 않습니다. 배부 중 홈베이스·저장 위치·따라가기 명령을 받으면 현재 미션을 먼저 정지·취소한 뒤 새 명령을 보내며, 긴급 정지 상태에서는 화면에서 안전 확인 및 재설정 전까지 음성으로 이동을 재개할 수 없습니다. 배부 및 홈베이스·저장 위치 이동은 최대 `0.2 m/s`로 제한합니다. 따라가기는 temi SDK가 사용자 지정 속도와 `VERY_SLOW`를 지원하지 않으므로 지원되는 최저 단계인 `SLOW`(약 `0.5 m/s`)를 사용합니다.

명령으로 판정되지 않은 문장만 기존 학교 담당자 질문으로 전달됩니다. 따라서 AI 안내를 꺼도 `Hey temi` 모드의 로봇 명령은 계속 사용할 수 있습니다.

**기능 설정**에서는 AI 안내, temi 음성 출력, 음성 명령·질문, 화면 계속 켜기, immersive 화면, 뒤로가기 차단, 배부·주행 제어, Arduino 바구니 연동을 각각 켜고 끌 수 있습니다. Kiosk 앱 선택과 부팅 자동 실행은 안전상 앱 토글이 아니라 temi Settings의 Kiosk 설정이 계속 담당합니다.

## 상시구동과 즉시 종료

Manifest에는 temi Kiosk metadata와 temi 전용 부팅 receiver가 들어 있습니다. 최초 설치 후 한 번만 temi **Settings > Apps > Kiosk**에서 이 앱을 홈 앱으로 선택하고 Kiosk를 켜야 합니다. 이후 부팅·홈 복귀 시 temi Launcher가 앱을 다시 표시합니다. 앱은 화면 켜짐과 immersive 모드를 유지하고 Kiosk 중에는 Back을 소비합니다.

우측 상단의 **앱 즉시 종료** 버튼은 확인창 없이 한 번의 탭으로 동작합니다. 버튼을 누르면 temi 정지와 Kiosk 해제를 각각 최선 시도한 뒤, 성공 여부와 관계없이 Android 작업을 제거하고 앱 프로세스를 즉시 종료합니다. Kiosk 해제 권한이 없거나 temi SDK가 준비되지 않은 상태에서는 프로세스 종료 후 temi Launcher가 앱을 다시 열 수 있으므로, 최초 1회 **Kiosk 설정 시작**에서 Settings 권한을 허용해야 완전히 빠져나갈 수 있습니다. 프로세스가 비정상 재시작되면 SDK가 준비된 최초 1회에 남아 있을 수 있는 이전 이동을 안전 정지합니다. 배터리 방전, OTA, 앱 crash, ADB `force-stop`까지 앱 코드가 절대적인 상시구동을 보장할 수는 없습니다.

## ESP32/Arduino 바구니 준비

- Android 연결 모듈: `basket-client/`
- Arduino IDE 펌웨어·배선·프로토콜: `arduino-basket-controller/`
- APK 실행 후: 우측 상단 **기능 설정**에서 Arduino 주소·장치 ID·장치 토큰 입력
- 기본값 템플릿: `app/src/main/assets/basket_config.json`

기본 설정은 비활성이라 실제 잠금장치를 움직이지 않습니다. APK를 다시 만들지 않고 ESP32 주소·장치별 Bearer 토큰을 입력해 활성화한 뒤 **바구니 상태 확인**으로 장치 ID, 문, 잠금, 센서, 적재 상태를 읽고 **안전 잠금**을 요청할 수 있습니다. 센서가 준비되지 않은 장치의 자동 잠금 해제와 배부 미션 연동은 차단되어 있습니다. NIM/Luna는 바구니 액추에이터나 temi 주행을 제어하지 않으며, 주행 음성 명령은 앱 내부 허용 목록만 사용합니다.

## 가장 안전한 좌표 설정: 저장 위치

실제 운용에서는 원시 좌표보다 temi 지도에 위치를 저장하고 이름으로 이동하는 방식을 권장합니다.

1. temi의 지도/위치 관리 화면에서 로봇을 각 배부 지점에 놓습니다.
2. 기본 설정과 정확히 같은 이름으로 `배부-1`, `배부-2`, `배부-3`, `교탁`을 저장합니다.
3. 앱 화면의 `로봇 정보 새로고침`을 눌러 네 위치가 모두 표시되는지 확인합니다.
4. 사람이 없는 교실에서 빈 바구니로 먼저 시험합니다.

경로, 학생 이름, 물품, 수량은 [delivery_route.json](app/src/main/assets/delivery_route.json)에서 수정합니다.

## 원시 x/y/yaw 좌표 사용

저장 위치 대신 SDK의 `goToPosition()`을 사용하려면 목적지를 아래처럼 바꿉니다. x/y는 현재 지도 좌표, yaw는 라디안이며 `999`는 도착 후 회전하지 않는다는 뜻입니다.

```json
"destination": {
  "type": "coordinate",
  "x": 1.25,
  "y": -0.80,
  "yaw": 1.57,
  "label": "1모둠"
}
```

앱의 `로봇 정보 새로고침`은 현재 x/y/yaw를 화면에 표시합니다. 로봇을 원하는 지점과 방향에 수동으로 놓고 이 값을 기록할 수 있습니다. 좌표는 현재 로드된 지도에 종속되므로 지도를 재생성하거나 바꾸면 다시 측정해야 합니다.

## 빌드

Android Studio에서 이 폴더를 프로젝트로 열거나 PowerShell에서 실행합니다.

```powershell
cd .\temi-classroom-delivery
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat -PasciiBuild test --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
```

생성 APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

사용한 주요 버전은 Android Gradle Plugin 9.2.0, temi SDK 1.138.0, minSdk 23, targetSdk 30입니다. SDK 1.138.0은 temi 138 Launcher(최소 launcher build 19627)용이며, 이 저장소에서 확인한 실기 launcher build 19674와 호환됩니다.

현재 작업공간의 상위 경로에 한글이 있어 Windows Gradle 테스트 워커가 클래스를 못 읽는 문제를 피하도록 테스트 명령에만 `-PasciiBuild`를 사용합니다. 이 옵션의 결과는 `%USERPROFILE%\.gradle\temi-classroom-delivery` 아래에 생성됩니다. 최종 APK는 반드시 이 옵션 없이 두 번째 명령으로 다시 빌드해야 프로젝트의 `app/build/outputs/apk/debug/app-debug.apk`에 생성됩니다.

## temi에 설치

로봇과 PC를 같은 네트워크에 연결하고 ADB가 활성화된 상태에서 다음처럼 설치할 수 있습니다. 주소는 실제 로봇 주소로 바꿉니다.

```powershell
$adb = 'C:\Users\sions\AppData\Local\Android\Sdk\platform-tools\adb.exe'
& $adb connect '192.168.104.254:5555'
& $adb -s '192.168.104.254:5555' install -r '.\app\build\outputs\apk\debug\app-debug.apk'
```

이 기능에 사용하는 `locations`, `goTo`, `goToPosition`, `getPosition`, `stopMovement`, TTS는 별도 temi 런타임 권한이 필요하지 않습니다. 런처의 일반 이동·따라가기 기본 속도를 낮추는 `goToSpeed`, `setFollowSpeed`에는 기존 Kiosk 설정 과정에서 허용하는 temi `SETTINGS` 권한이 필요합니다. 권한이 없어도 앱이 직접 실행하는 이동의 `0.2 m/s` 제한은 적용됩니다. 지도 파일을 읽거나 편집하는 기능을 추가할 때는 `MAP` 권한을 별도로 선언하고 요청해야 합니다.

## 실기 시험 체크리스트

- 바구니와 적재물이 temi의 센서, 화면, 비상 버튼을 가리지 않는지 확인합니다.
- 적재물이 주행 중 떨어지지 않게 고정하고 로봇의 허용 하중을 확인합니다.
- 책상·가방·학생이 없는 상태에서 각 지점과 복귀 경로를 먼저 시험합니다.
- 첫 시험은 기본 제한 속도인 `0.2 m/s`를 유지하고, 사람이 항상 긴급 정지 버튼에 접근할 수 있게 합니다.
- 도착 방향이 부정확하면 저장 위치의 방향을 다시 저장하거나 `highAccuracyArrival`을 확인합니다.
- 장애물로 이동이 중단되어도 자동 재시도하지 않습니다. 주변을 확인한 뒤 사람이 재시도를 선택합니다.

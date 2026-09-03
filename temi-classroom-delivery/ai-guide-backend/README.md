# temi AI 안내 백엔드

교사가 등록한 물품·교직원 디렉터리만 근거로 temi에 한국어 안내를 제공하는 독립 Node.js 서버입니다. OpenAI `gpt-5.6-luna`를 기본 모델로 사용하고, 실패하면 NVIDIA NIM의 OpenAI 호환 Chat Completions, 두 모델이 모두 실패하면 결정론적인 교사 입력 fallback을 사용합니다.

현재 기본 NIM 모델은 `deepseek-ai/deepseek-v4-flash-0731`입니다. 기존 `deepseek-ai/deepseek-v4-flash` 모델은 종료된 모델이므로 사용하지 않습니다.

## 실행

Node.js 22 이상이 필요합니다.

```powershell
cd .\ai-guide-backend
Copy-Item .env.example .env
npm install
npm test
npm start
```

API 키는 이 서버의 `.env` 또는 운영 secret manager에만 저장합니다. APK, `ai_guide_config.json`, JSON 응답, 로그, Git 파일에 NIM/OpenAI 키를 넣지 않습니다. 공유 문서에 노출된 키는 폐기하고 새 키를 발급해야 합니다.

로컬 개발에서는 추적되지 않는 `.env.local`로 `.env` 값을 덮어쓸 수 있습니다. 이 프로젝트가 OneDrive 같은 동기화 폴더 안에 있다면 키 파일은 동기화되지 않는 서버 전용 경로 또는 secret manager로 옮기세요.

주요 환경변수:

```text
NVIDIA_NIM_API_KEY=
NVIDIA_NIM_BASE_URL=https://integrate.api.nvidia.com/v1
NVIDIA_NIM_MODEL=deepseek-ai/deepseek-v4-flash-0731
NVIDIA_NIM_TIMEOUT_MS=8000
LUNA_API_KEY=
LUNA_MODEL=gpt-5.6-luna
LUNA_TIMEOUT_MS=10000
OPENAI_TRANSCRIPTION_MODEL=gpt-transcribe
OPENAI_TRANSCRIPTION_FALLBACK_MODEL=whisper-1
TRANSCRIPTION_TIMEOUT_MS=30000
MAX_TRANSCRIPTION_DURATION_MS=20000
STAFF_DIRECTORY_FILE=./data/staff-directory.json
ADMIN_TOKEN=
TEMI_CLIENT_TOKEN=
FALLBACK_ON_PROVIDER_ERROR=true
```

기존 배포 호환을 위해 `OPENAI_API_KEY`, `OPENAI_MODEL`, `FALLBACK_ON_OPENAI_ERROR`도 alias로 읽습니다. `GET /health`는 키 자체가 아닌 provider/model/configured 상태만 반환합니다.

## 물품 안내 API

- `GET /api/v1/items`
- `GET /api/v1/items/{id}`
- `PUT /api/v1/admin/items/{id}`
- `DELETE /api/v1/admin/items/{id}`
- `POST /api/v1/guides`

관리 API는 `Authorization: Bearer <ADMIN_TOKEN>`, temi API는 필수 `TEMI_CLIENT_TOKEN`을 사용합니다. 토큰이 비어 있으면 서버는 temi 읽기 API를 `503 temi_client_not_configured`로 닫고, 정상 실행 진입도 거부합니다.

```json
{
  "itemId": "science-kit",
  "question": "처음에는 무엇을 해야 해요?"
}
```

## 교직원 디렉터리

초기 데이터는 `data/staff-directory.json`에 넣습니다. 공개 질의에는 `visibility: "public"`인 항목만 사용됩니다. `internal` 항목은 관리자 조회·저장만 가능하고 temi/LLM 응답에 노출되지 않습니다.

교직원 한 명의 형식은 다음과 같습니다.

```json
{
  "id": "kim-minji",
  "name": "김민지",
  "title": "과학 교사",
  "subjects": ["과학"],
  "responsibilities": ["과학실 안전 관리", "실험 준비"],
  "department": "과학부",
  "location": "본관 2층 과학실",
  "aliases": ["민지쌤"],
  "visibility": "public"
}
```

관리자 API:

- `GET /api/v1/admin/directory`: 전체 디렉터리 확인
- `PUT /api/v1/admin/directory`: `{ "schemaVersion": 1, "staff": [...] }` 전체 JSON 교체
- `PUT /api/v1/admin/directory/teachers/{id}`: 한 명 upsert

temi 질의 API:

- `POST /api/v1/school/answers`

```json
{
  "question": "과학실 안전을 담당하는 선생님은 누구예요?"
}
```

응답은 다음 data 계약을 사용합니다.

```json
{
  "answer": "...",
  "source": "nvidia_nim",
  "model": "deepseek-ai/deepseek-v4-flash-0731",
  "warning": null,
  "matches": [
    {
      "id": "kim-minji",
      "name": "김민지",
      "title": "과학 교사",
      "department": "과학부",
      "location": "본관 2층 과학실",
      "responsibilities": ["과학실 안전 관리", "실험 준비"]
    }
  ]
}
```

질문은 서버 내부에서 역할·업무·과목·이름·별칭을 결정론적으로 검색합니다. 학생이 입력한 원문 질문은 외부 모델로 보내지 않고, 검색된 공개 교직원 정보와 고정 안내문만 전달합니다. 일치하는 사람이 없으면 모델을 호출하지 않고 “선생님께 확인해 주세요”라는 fallback을 반환해 추측을 막습니다. 모델이 모두 unavailable이면 검색된 교직원 정보만 조합한 deterministic 답변을 반환합니다.

디렉터리 schema는 허용된 교직원 필드만 받으며, 알 수 없는 필드, API 키·비밀번호·토큰, 이메일·전화번호 형식의 값은 거부합니다. 학생 개인정보를 디렉터리에 입력하지 마세요.

## 음성 인식 API

`POST /api/v1/transcriptions`는 temi client Bearer 인증이 필요하며 `Content-Type: audio/wav`의 16 kHz, 모노, 16-bit PCM만 받습니다. 최대 길이는 기본 20초이고, 요청 크기와 WAV 청크도 서버에서 다시 검사합니다. `gpt-transcribe` 실패 시 `whisper-1`을 한 번만 시도하며 음성 본문이나 인식 문장은 로그·파일에 저장하지 않습니다.

## 응답 출처

`source` 값은 `nvidia_nim`, `luna`, `teacher_fallback` 중 하나입니다. 모델 호출은 실제 provider 대신 local mock server 테스트로 검증하며, 실운영 전에는 새로 발급한 키로 짧은 smoke test를 수행합니다.

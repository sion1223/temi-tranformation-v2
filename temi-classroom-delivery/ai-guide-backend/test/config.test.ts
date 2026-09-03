import assert from "node:assert/strict";
import test from "node:test";
import { loadConfig } from "../src/config.js";

test("config defaults to Luna primary, NVIDIA NIM secondary, and 20 second transcription", () => {
  const config = loadConfig({});

  assert.equal(config.nvidiaBaseUrl, "https://integrate.api.nvidia.com/v1");
  assert.equal(config.nvidiaModel, "deepseek-ai/deepseek-v4-flash-0731");
  assert.equal(config.lunaModel, "gpt-5.6-luna");
  assert.equal(config.transcriptionModel, "gpt-transcribe");
  assert.equal(config.transcriptionFallbackModel, "whisper-1");
  assert.equal(config.maxTranscriptionDurationMs, 20_000);
  assert.equal(config.nvidiaApiKey, "");
  assert.equal(config.lunaApiKey, "");
  assert.match(config.staffDirectoryFile, /staff-directory\.json$/);
});

test("config reads provider keys and preserves old OpenAI aliases", () => {
  const config = loadConfig({
    NVIDIA_NIM_API_KEY: "nim-test-key",
    NVIDIA_NIM_BASE_URL: "https://integrate.api.nvidia.com/v1/",
    NVIDIA_NIM_MODEL: "custom-nim-model",
    NVIDIA_NIM_TIMEOUT_MS: "7000",
    OPENAI_API_KEY: "luna-test-key",
    OPENAI_MODEL: "gpt-5.6-luna-test",
    STAFF_DIRECTORY_FILE: "./data/custom-directory.json",
    FALLBACK_ON_PROVIDER_ERROR: "false",
    OPENAI_TRANSCRIPTION_MODEL: "gpt-4o-transcribe",
    OPENAI_TRANSCRIPTION_FALLBACK_MODEL: "whisper-1",
    TRANSCRIPTION_TIMEOUT_MS: "35000",
    MAX_TRANSCRIPTION_DURATION_MS: "19000",
  });

  assert.equal(config.nvidiaApiKey, "nim-test-key");
  assert.equal(config.nvidiaBaseUrl, "https://integrate.api.nvidia.com/v1");
  assert.equal(config.nvidiaModel, "custom-nim-model");
  assert.equal(config.nvidiaTimeoutMs, 7000);
  assert.equal(config.lunaApiKey, "luna-test-key");
  assert.equal(config.lunaModel, "gpt-5.6-luna-test");
  assert.equal(config.openAiApiKey, "luna-test-key");
  assert.equal(config.openAiModel, "gpt-5.6-luna-test");
  assert.equal(config.fallbackOnOpenAiError, false);
  assert.equal(config.transcriptionModel, "gpt-4o-transcribe");
  assert.equal(config.transcriptionFallbackModel, "whisper-1");
  assert.equal(config.transcriptionTimeoutMs, 35_000);
  assert.equal(config.maxTranscriptionDurationMs, 19_000);
  assert.match(config.staffDirectoryFile, /custom-directory\.json$/);
});

test("config rejects unsafe provider URLs and invalid timeouts", () => {
  assert.throws(
    () => loadConfig({ NVIDIA_NIM_BASE_URL: "file:///secret" }),
    /NVIDIA_NIM_BASE_URL/,
  );
  assert.throws(
    () => loadConfig({ NVIDIA_NIM_BASE_URL: "http://integrate.api.nvidia.com/v1" }),
    /NVIDIA_NIM_BASE_URL/,
  );
  assert.throws(
    () => loadConfig({ NVIDIA_NIM_BASE_URL: "https://attacker.example/v1" }),
    /NVIDIA_NIM_BASE_URL/,
  );
  assert.throws(
    () => loadConfig({ NVIDIA_NIM_TIMEOUT_MS: "999" }),
    /timeout/i,
  );
  assert.throws(
    () => loadConfig({ MAX_TRANSCRIPTION_DURATION_MS: "20001" }),
    /MAX_TRANSCRIPTION_DURATION_MS/,
  );
});

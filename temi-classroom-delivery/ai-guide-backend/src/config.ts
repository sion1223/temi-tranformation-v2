import { resolve } from "node:path";
import { NVIDIA_NIM_DEFAULT_BASE_URL } from "./explanation-generator.js";

export interface AppConfig {
  host: string;
  port: number;
  dataFile: string;
  staffDirectoryFile: string;
  adminToken: string;
  temiClientToken: string;
  nvidiaApiKey: string;
  nvidiaBaseUrl: string;
  nvidiaModel: string;
  nvidiaTimeoutMs: number;
  lunaApiKey: string;
  lunaModel: string;
  lunaTimeoutMs: number;
  transcriptionModel: string;
  transcriptionFallbackModel: string;
  transcriptionTimeoutMs: number;
  maxTranscriptionDurationMs: number;
  fallbackOnProviderError: boolean;
  // Deprecated aliases retained while existing deployments migrate.
  openAiApiKey: string;
  openAiModel: string;
  fallbackOnOpenAiError: boolean;
}

export function loadConfig(environment: NodeJS.ProcessEnv = process.env): AppConfig {
  const port = Number.parseInt(environment.PORT ?? "8787", 10);
  if (!Number.isInteger(port) || port < 1 || port > 65_535) {
    throw new Error("PORT는 1~65535 사이의 정수여야 합니다.");
  }
  const nvidiaBaseUrl = normalizeNvidiaProviderUrl(
    environment.NVIDIA_NIM_BASE_URL?.trim() || NVIDIA_NIM_DEFAULT_BASE_URL,
    "NVIDIA_NIM_BASE_URL",
  );
  const nvidiaTimeoutMs = parseTimeout(
    environment.NVIDIA_NIM_TIMEOUT_MS ?? "8000",
    "NVIDIA_NIM_TIMEOUT_MS",
  );
  const lunaTimeoutMs = parseTimeout(
    environment.LUNA_TIMEOUT_MS ?? "10000",
    "LUNA_TIMEOUT_MS",
  );
  const transcriptionTimeoutMs = parseTimeout(
    environment.TRANSCRIPTION_TIMEOUT_MS ?? "30000",
    "TRANSCRIPTION_TIMEOUT_MS",
  );
  const maxTranscriptionDurationMs = parseTranscriptionDuration(
    environment.MAX_TRANSCRIPTION_DURATION_MS ?? "20000",
  );
  const nvidiaApiKey =
    environment.NVIDIA_NIM_API_KEY?.trim() || environment.NVIDIA_API_KEY?.trim() || "";
  const lunaApiKey =
    environment.LUNA_API_KEY?.trim() || environment.OPENAI_API_KEY?.trim() || "";
  const lunaModel =
    environment.LUNA_MODEL?.trim() || environment.OPENAI_MODEL?.trim() || "gpt-5.6-luna";
  const fallbackOnProviderError = (
    environment.FALLBACK_ON_PROVIDER_ERROR ??
    environment.FALLBACK_ON_OPENAI_ERROR ??
    "true"
  ).toLowerCase() !== "false";
  return {
    host: environment.HOST?.trim() || "0.0.0.0",
    port,
    dataFile: resolve(environment.DATA_FILE?.trim() || "./data/items.json"),
    staffDirectoryFile: resolve(
      environment.STAFF_DIRECTORY_FILE?.trim() || "./data/staff-directory.json",
    ),
    adminToken: environment.ADMIN_TOKEN?.trim() || "",
    temiClientToken: environment.TEMI_CLIENT_TOKEN?.trim() || "",
    nvidiaApiKey,
    nvidiaBaseUrl,
    nvidiaModel: environment.NVIDIA_NIM_MODEL?.trim() || "deepseek-ai/deepseek-v4-flash-0731",
    nvidiaTimeoutMs,
    lunaApiKey,
    lunaModel,
    lunaTimeoutMs,
    transcriptionModel:
      environment.OPENAI_TRANSCRIPTION_MODEL?.trim() || "gpt-transcribe",
    transcriptionFallbackModel:
      environment.OPENAI_TRANSCRIPTION_FALLBACK_MODEL?.trim() || "whisper-1",
    transcriptionTimeoutMs,
    maxTranscriptionDurationMs,
    fallbackOnProviderError,
    openAiApiKey: lunaApiKey,
    openAiModel: lunaModel,
    fallbackOnOpenAiError: fallbackOnProviderError,
  };
}

function normalizeNvidiaProviderUrl(value: string, fieldName: string): string {
  let parsed: URL;
  try {
    parsed = new URL(value);
  } catch (error) {
    throw new Error(`${fieldName}이(가) 올바른 URL이 아닙니다.`, { cause: error });
  }
  const normalizedPath = parsed.pathname.replace(/\/+$/, "") || "/";
  if (
    parsed.protocol !== "https:" ||
    parsed.hostname !== "integrate.api.nvidia.com" ||
    parsed.port !== "" ||
    parsed.username !== "" ||
    parsed.password !== "" ||
    parsed.search !== "" ||
    parsed.hash !== "" ||
    normalizedPath !== "/v1"
  ) {
    throw new Error(
      `${fieldName}은(는) NVIDIA 공식 HTTPS endpoint ` +
        "https://integrate.api.nvidia.com/v1 이어야 합니다.",
    );
  }
  return NVIDIA_NIM_DEFAULT_BASE_URL;
}

function parseTimeout(value: string, fieldName: string): number {
  const timeout = Number.parseInt(value, 10);
  if (!Number.isInteger(timeout) || timeout < 1_000 || timeout > 60_000) {
    throw new Error(`${fieldName} timeout은 1000~60000 사이의 정수여야 합니다.`);
  }
  return timeout;
}

function parseTranscriptionDuration(value: string): number {
  const duration = Number.parseInt(value, 10);
  if (!Number.isInteger(duration) || duration < 1_000 || duration > 20_000) {
    throw new Error("MAX_TRANSCRIPTION_DURATION_MS는 1000~20000 사이의 정수여야 합니다.");
  }
  return duration;
}

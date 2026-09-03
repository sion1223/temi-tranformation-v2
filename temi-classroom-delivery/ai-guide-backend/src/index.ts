import { loadConfig } from "./config.js";
import {
  FallbackExplanationGenerator,
  NvidiaNimExplanationGenerator,
  OpenAiLunaExplanationGenerator,
  UnavailableExplanationGenerator,
} from "./explanation-generator.js";
import { GuideService } from "./guide-service.js";
import { createAppServer } from "./http-server.js";
import { SchoolAnswerService } from "./school-answer-service.js";
import { JsonTeacherDirectoryRepository } from "./staff-directory-repository.js";
import { JsonSupplyItemRepository } from "./supply-item-repository.js";
import {
  FallbackSpeechTranscriber,
  OpenAiSpeechTranscriber,
  UnavailableSpeechTranscriber,
} from "./speech-transcriber.js";

async function main(): Promise<void> {
  const config = loadConfig();
  if (config.temiClientToken.length === 0) {
    throw new Error("TEMI_CLIENT_TOKEN이 필요합니다. 인증 없는 temi API는 시작하지 않습니다.");
  }
  const repository = new JsonSupplyItemRepository(config.dataFile);
  await repository.initialize();
  const directoryRepository = new JsonTeacherDirectoryRepository(config.staffDirectoryFile);
  await directoryRepository.initialize();

  const nvidiaGenerator = config.nvidiaApiKey.length > 0
    ? new NvidiaNimExplanationGenerator(
        config.nvidiaApiKey,
        config.nvidiaModel,
        undefined,
        config.nvidiaBaseUrl,
        config.nvidiaTimeoutMs,
      )
    : new UnavailableExplanationGenerator("NVIDIA_NIM_API_KEY가 설정되지 않았습니다.");
  const lunaGenerator = config.lunaApiKey.length > 0
    ? new OpenAiLunaExplanationGenerator(
        config.lunaApiKey,
        config.lunaModel,
        undefined,
        config.lunaTimeoutMs,
      )
    : new UnavailableExplanationGenerator("OPENAI_API_KEY가 설정되지 않았습니다.");
  const generator = new FallbackExplanationGenerator(
    lunaGenerator,
    nvidiaGenerator,
    {
      primarySource: "luna",
      secondarySource: "nvidia_nim",
      onProviderError: (provider, error) => {
        console.error(JSON.stringify({
          timestamp: new Date().toISOString(),
          level: "error",
          event: "ai_provider_failed",
          provider,
          error: error instanceof Error ? error.message : String(error),
        }));
      },
    },
  );
  const primaryTranscriber = config.lunaApiKey.length > 0
    ? new OpenAiSpeechTranscriber(
        config.lunaApiKey,
        config.transcriptionModel,
        undefined,
        config.transcriptionTimeoutMs,
      )
    : new UnavailableSpeechTranscriber("OPENAI_API_KEY가 설정되지 않았습니다.");
  const fallbackTranscriber = config.lunaApiKey.length > 0
    ? new OpenAiSpeechTranscriber(
        config.lunaApiKey,
        config.transcriptionFallbackModel,
        undefined,
        config.transcriptionTimeoutMs,
      )
    : new UnavailableSpeechTranscriber("OPENAI_API_KEY가 설정되지 않았습니다.");
  const speechTranscriber = new FallbackSpeechTranscriber(
    primaryTranscriber,
    fallbackTranscriber,
    config.transcriptionModel,
    config.transcriptionFallbackModel,
  );
  const guideService = new GuideService(
    repository,
    generator,
    config.fallbackOnOpenAiError,
    (error, itemId) => {
      console.error(JSON.stringify({
        timestamp: new Date().toISOString(),
        level: "error",
        event: "ai_generation_failed",
        itemId,
        error: error instanceof Error ? error.message : String(error),
      }));
    },
  );
  const schoolAnswerService = new SchoolAnswerService(
    directoryRepository,
    generator,
    config.fallbackOnProviderError,
    (error, question) => {
      console.error(JSON.stringify({
        timestamp: new Date().toISOString(),
        level: "error",
        event: "school_answer_generation_failed",
        questionLength: question.length,
        error: error instanceof Error ? error.message : String(error),
      }));
    },
  );
  const server = createAppServer({
    repository,
    guideService,
    adminToken: config.adminToken,
    temiClientToken: config.temiClientToken,
    model: config.lunaModel,
    openAiConfigured: config.lunaApiKey.length > 0,
    nvidiaModel: config.nvidiaModel,
    nvidiaConfigured: config.nvidiaApiKey.length > 0,
    lunaModel: config.lunaModel,
    lunaConfigured: config.lunaApiKey.length > 0,
    directoryRepository,
    schoolAnswerService,
    speechTranscriber,
    transcriptionModel: config.transcriptionModel,
    transcriptionFallbackModel: config.transcriptionFallbackModel,
    maxTranscriptionDurationMs: config.maxTranscriptionDurationMs,
  });

  server.listen(config.port, config.host, () => {
    console.log(JSON.stringify({
      timestamp: new Date().toISOString(),
      level: "info",
      event: "server_started",
      host: config.host,
      port: config.port,
      primaryModel: config.lunaModel,
      fallbackModel: config.nvidiaModel,
      transcriptionModel: config.transcriptionModel,
      transcriptionFallbackModel: config.transcriptionFallbackModel,
      nvidiaConfigured: config.nvidiaApiKey.length > 0,
      lunaConfigured: config.lunaApiKey.length > 0,
      directoryFile: config.staffDirectoryFile,
    }));
  });

  const shutdown = (signal: string): void => {
    console.log(JSON.stringify({
      timestamp: new Date().toISOString(),
      level: "info",
      event: "server_stopping",
      signal,
    }));
    server.close(() => process.exit(0));
  };
  process.on("SIGINT", () => shutdown("SIGINT"));
  process.on("SIGTERM", () => shutdown("SIGTERM"));
}

main().catch((error: unknown) => {
  console.error(JSON.stringify({
    timestamp: new Date().toISOString(),
    level: "error",
    event: "startup_failed",
    error: error instanceof Error ? error.message : String(error),
  }));
  process.exitCode = 1;
});

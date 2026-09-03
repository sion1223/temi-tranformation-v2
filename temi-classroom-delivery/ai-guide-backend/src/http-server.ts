import { createHash, randomUUID, timingSafeEqual } from "node:crypto";
import { createServer, type IncomingMessage, type Server, type ServerResponse } from "node:http";
import { ApiError } from "./errors.js";
import type { GuideService } from "./guide-service.js";
import { SchoolAnswerService } from "./school-answer-service.js";
import {
  parseTeacherDirectoryDocument,
  parseTeacherProfile,
} from "./staff-directory.js";
import type { TeacherDirectoryRepository } from "./staff-directory-repository.js";
import { inspectPcmWav, type SpeechTranscriber } from "./speech-transcriber.js";
import { parseQuestion, parseSupplyItem } from "./supply-item.js";
import type { SupplyItemRepository } from "./supply-item-repository.js";

const MAX_BODY_BYTES = 32 * 1024;

export interface HttpServerDependencies {
  repository: SupplyItemRepository;
  guideService: GuideService;
  adminToken: string;
  temiClientToken: string;
  model: string;
  openAiConfigured: boolean;
  nvidiaModel?: string;
  nvidiaConfigured?: boolean;
  lunaModel?: string;
  lunaConfigured?: boolean;
  directoryRepository?: TeacherDirectoryRepository;
  schoolAnswerService?: SchoolAnswerService;
  speechTranscriber?: SpeechTranscriber;
  transcriptionModel?: string;
  transcriptionFallbackModel?: string;
  maxTranscriptionDurationMs?: number;
}

export function createAppServer(dependencies: HttpServerDependencies): Server {
  return createServer(async (request, response) => {
    const requestId = randomUUID();
    const startedAt = Date.now();
    try {
      await routeRequest(request, response, dependencies, requestId);
      log("info", "request_completed", {
        requestId,
        method: request.method,
        path: pathForLog(request.url),
        statusCode: response.statusCode,
        durationMs: Date.now() - startedAt,
      });
    } catch (error) {
      writeError(response, error, requestId);
      log("error", "request_failed", {
        requestId,
        method: request.method,
        path: pathForLog(request.url),
        statusCode: response.statusCode,
        durationMs: Date.now() - startedAt,
        error: error instanceof Error ? error.message : String(error),
      });
    }
  });
}

async function routeRequest(
  request: IncomingMessage,
  response: ServerResponse,
  dependencies: HttpServerDependencies,
  requestId: string,
): Promise<void> {
  const method = request.method ?? "GET";
  const url = new URL(request.url ?? "/", "http://localhost");
  const path = url.pathname;

  if (method === "GET" && path === "/health") {
    writeJson(response, 200, {
      success: true,
      data: {
        status: "ok",
        model: dependencies.model,
        openAiConfigured: dependencies.openAiConfigured,
        primary: {
          provider: "luna",
          model: dependencies.lunaModel ?? "gpt-5.6-luna",
          configured: dependencies.lunaConfigured ?? dependencies.openAiConfigured,
        },
        fallback: {
          provider: "nvidia_nim",
          model: dependencies.nvidiaModel ?? dependencies.model,
          configured: dependencies.nvidiaConfigured ?? false,
        },
        transcription: {
          configured: dependencies.speechTranscriber !== undefined,
          model: dependencies.transcriptionModel ?? "gpt-transcribe",
          fallbackModel: dependencies.transcriptionFallbackModel ?? "whisper-1",
          maxDurationMs: dependencies.maxTranscriptionDurationMs ?? 20_000,
        },
      },
      requestId,
    });
    return;
  }

  if (method === "GET" && path === "/api/v1/items") {
    requireBearer(request, dependencies.temiClientToken, "temi_client");
    const items = await dependencies.repository.findAll();
    writeJson(response, 200, { success: true, data: { items }, requestId });
    return;
  }

  const itemMatch = path.match(/^\/api\/v1\/items\/([a-z0-9-]+)$/);
  if (method === "GET" && itemMatch?.[1] !== undefined) {
    requireBearer(request, dependencies.temiClientToken, "temi_client");
    const item = await dependencies.repository.findById(itemMatch[1]);
    if (item === null) throw new ApiError(404, "item_not_found", "등록되지 않은 수업용품입니다.");
    writeJson(response, 200, { success: true, data: { item }, requestId });
    return;
  }

  if (method === "POST" && path === "/api/v1/guides") {
    requireBearer(request, dependencies.temiClientToken, "temi_client");
    const body = await readJsonObject(request);
    if (typeof body.itemId !== "string" || body.itemId.trim().length === 0) {
      throw new ApiError(400, "invalid_request", "itemId를 입력해 주세요.");
    }
    const question = parseQuestion(body.question);
    const guide = await dependencies.guideService.explain(
      body.itemId,
      question,
    );
    writeJson(response, 200, { success: true, data: guide, requestId });
    return;
  }

  if (method === "POST" && path === "/api/v1/school/answers") {
    requireBearer(request, dependencies.temiClientToken, "temi_client");
    if (dependencies.schoolAnswerService === undefined) {
      throw new ApiError(503, "directory_not_configured", "교직원 디렉터리가 설정되지 않았습니다.");
    }
    const body = await readJsonObject(request);
    const question = parseQuestion(body.question);
    if (question === undefined) {
      throw new ApiError(400, "invalid_request", "question을 입력해 주세요.");
    }
    const answer = await dependencies.schoolAnswerService.answer(question);
    writeJson(response, 200, { success: true, data: answer, requestId });
    return;
  }

  if (method === "POST" && path === "/api/v1/transcriptions") {
    requireBearer(request, dependencies.temiClientToken, "temi_client");
    if (dependencies.speechTranscriber === undefined) {
      throw new ApiError(503, "transcription_not_configured", "음성 인식 API가 설정되지 않았습니다.");
    }
    const mediaType = (request.headers["content-type"] ?? "").split(";", 1)[0]?.trim().toLowerCase();
    if (mediaType !== "audio/wav" && mediaType !== "audio/x-wav") {
      throw new ApiError(415, "unsupported_audio_type", "16 kHz 모노 PCM WAV만 지원합니다.");
    }
    const maxDurationMs = dependencies.maxTranscriptionDurationMs ?? 20_000;
    const maximumBytes = 44 + Math.ceil(maxDurationMs * 32) + 4_096;
    const audio = await readBody(request, maximumBytes, "음성 데이터가 너무 큽니다.");
    let wavInfo;
    try {
      wavInfo = inspectPcmWav(audio, maxDurationMs);
    } catch (error) {
      throw new ApiError(
        400,
        "invalid_audio",
        error instanceof Error ? error.message : "음성 데이터가 올바르지 않습니다.",
        { cause: error },
      );
    }
    try {
      const transcript = await dependencies.speechTranscriber.transcribe(audio, mediaType);
      writeJson(response, 200, {
        success: true,
        data: { ...transcript, durationMs: wavInfo.durationMs },
        requestId,
      });
    } catch (error) {
      throw new ApiError(502, "transcription_failed", "음성을 글자로 바꾸지 못했습니다.", {
        cause: error,
      });
    }
    return;
  }

  if (path === "/api/v1/admin/directory" && method === "GET") {
    requireAdmin(request, dependencies.adminToken);
    if (dependencies.directoryRepository === undefined) {
      throw new ApiError(503, "directory_not_configured", "교직원 디렉터리가 설정되지 않았습니다.");
    }
    const staff = await dependencies.directoryRepository.findAll();
    writeJson(response, 200, {
      success: true,
      data: { directory: { schemaVersion: 1, staff } },
      requestId,
    });
    return;
  }

  if (path === "/api/v1/admin/directory" && method === "PUT") {
    requireAdmin(request, dependencies.adminToken);
    if (dependencies.directoryRepository === undefined) {
      throw new ApiError(503, "directory_not_configured", "교직원 디렉터리가 설정되지 않았습니다.");
    }
    const document = parseTeacherDirectoryDocument(await readJsonObject(request));
    const staff = await dependencies.directoryRepository.replace(document.staff);
    writeJson(response, 200, {
      success: true,
      data: { directory: { schemaVersion: 1, staff } },
      requestId,
    });
    return;
  }

  const adminTeacherMatch = path.match(/^\/api\/v1\/admin\/directory\/teachers\/([a-z0-9-]+)$/);
  if (adminTeacherMatch?.[1] !== undefined && method === "PUT") {
    requireAdmin(request, dependencies.adminToken);
    if (dependencies.directoryRepository === undefined) {
      throw new ApiError(503, "directory_not_configured", "교직원 디렉터리가 설정되지 않았습니다.");
    }
    const teacher = parseTeacherProfile(
      await readJsonObject(request),
      adminTeacherMatch[1],
    );
    const saved = await dependencies.directoryRepository.upsert(teacher);
    writeJson(response, 200, { success: true, data: { teacher: saved }, requestId });
    return;
  }

  const adminItemMatch = path.match(/^\/api\/v1\/admin\/items\/([a-z0-9-]+)$/);
  if (adminItemMatch?.[1] !== undefined && (method === "PUT" || method === "DELETE")) {
    requireAdmin(request, dependencies.adminToken);
    const itemId = adminItemMatch[1];
    if (method === "PUT") {
      const item = parseSupplyItem(await readJsonObject(request), itemId);
      const saved = await dependencies.repository.upsert(item);
      writeJson(response, 200, { success: true, data: { item: saved }, requestId });
    } else {
      const deleted = await dependencies.repository.delete(itemId);
      if (!deleted) throw new ApiError(404, "item_not_found", "등록되지 않은 수업용품입니다.");
      writeJson(response, 200, { success: true, data: { deleted: true }, requestId });
    }
    return;
  }

  throw new ApiError(404, "not_found", "요청한 API 경로가 없습니다.");
}

async function readJsonObject(request: IncomingMessage): Promise<Record<string, unknown>> {
  const body = await readBody(request, MAX_BODY_BYTES, "요청 본문이 너무 큽니다.");
  try {
    const value = JSON.parse(body.toString("utf8")) as unknown;
    if (value === null || typeof value !== "object" || Array.isArray(value)) {
      throw new Error("not an object");
    }
    return value as Record<string, unknown>;
  } catch (error) {
    throw new ApiError(400, "invalid_json", "올바른 JSON 객체를 보내 주세요.", { cause: error });
  }
}

async function readBody(
  request: IncomingMessage,
  maxBytes: number,
  tooLargeMessage: string,
): Promise<Buffer> {
  const declaredLength = Number.parseInt(request.headers["content-length"] ?? "0", 10);
  if (Number.isFinite(declaredLength) && declaredLength > maxBytes) {
    throw new ApiError(413, "request_too_large", tooLargeMessage);
  }
  const chunks: Buffer[] = [];
  let totalBytes = 0;
  for await (const chunk of request) {
    const buffer = Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk as Uint8Array);
    totalBytes += buffer.length;
    if (totalBytes > maxBytes) {
      throw new ApiError(413, "request_too_large", tooLargeMessage);
    }
    chunks.push(buffer);
  }
  return Buffer.concat(chunks);
}

function requireBearer(request: IncomingMessage, expected: string, kind: string): void {
  if (expected.length === 0) {
    throw new ApiError(
      503,
      `${kind}_not_configured`,
      `${kind} 인증 토큰이 설정되지 않았습니다.`,
    );
  }
  const authorization = request.headers.authorization ?? "";
  const provided = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  if (!safeTokenEquals(provided, expected)) {
    throw new ApiError(401, "unauthorized", `${kind} 인증 토큰이 올바르지 않습니다.`);
  }
}

function requireAdmin(request: IncomingMessage, expected: string): void {
  if (expected.length === 0) {
    throw new ApiError(503, "admin_not_configured", "ADMIN_TOKEN이 설정되지 않았습니다.");
  }
  requireBearer(request, expected, "admin");
}

function safeTokenEquals(left: string, right: string): boolean {
  const leftHash = createHash("sha256").update(left).digest();
  const rightHash = createHash("sha256").update(right).digest();
  return timingSafeEqual(leftHash, rightHash);
}

function writeJson(response: ServerResponse, statusCode: number, body: unknown): void {
  const json = JSON.stringify(body);
  response.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(json),
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
  });
  response.end(json);
}

function writeError(response: ServerResponse, error: unknown, requestId: string): void {
  if (response.headersSent) {
    response.end();
    return;
  }
  const apiError = error instanceof ApiError
    ? error
    : new ApiError(500, "internal_error", "서버 내부 오류가 발생했습니다.", {
        cause: error,
      });
  writeJson(response, apiError.statusCode, {
    success: false,
    error: {
      code: apiError.code,
      message: apiError.message,
    },
    requestId,
  });
}

function log(
  level: "info" | "error",
  event: string,
  fields: Record<string, unknown>,
): void {
  const entry = JSON.stringify({
    timestamp: new Date().toISOString(),
    level,
    event,
    ...fields,
  });
  if (level === "error") console.error(entry);
  else console.log(entry);
}

function pathForLog(requestUrl: string | undefined): string {
  try {
    return new URL(requestUrl ?? "/", "http://localhost").pathname;
  } catch {
    return "/invalid-url";
  }
}

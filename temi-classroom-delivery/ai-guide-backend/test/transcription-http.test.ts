import assert from "node:assert/strict";
import type { AddressInfo } from "node:net";
import test from "node:test";
import type { ExplanationGenerator } from "../src/explanation-generator.js";
import { GuideService } from "../src/guide-service.js";
import { createAppServer } from "../src/http-server.js";
import type { SpeechTranscriber } from "../src/speech-transcriber.js";
import type { SupplyItem } from "../src/supply-item.js";
import type { SupplyItemRepository } from "../src/supply-item-repository.js";

const repository: SupplyItemRepository = {
  async findAll(): Promise<SupplyItem[]> { return []; },
  async findById(): Promise<SupplyItem | null> { return null; },
  async upsert(item): Promise<SupplyItem> { return item; },
  async delete(): Promise<boolean> { return false; },
};
const generator: ExplanationGenerator = {
  async generate() { return { text: "unused", model: "unused" }; },
};

test("authenticated temi client can transcribe WAV and invalid uploads fail closed", async (context) => {
  let receivedBytes = 0;
  const speechTranscriber: SpeechTranscriber = {
    async transcribe(audio) {
      receivedBytes = audio.byteLength;
      return { text: "진로 상담 선생님을 찾아 주세요.", model: "gpt-transcribe" };
    },
  };
  const server = createAppServer({
    repository,
    guideService: new GuideService(repository, generator, false),
    adminToken: "teacher-secret",
    temiClientToken: "robot-token",
    model: "gpt-5.6-luna",
    openAiConfigured: true,
    speechTranscriber,
    transcriptionModel: "gpt-transcribe",
    transcriptionFallbackModel: "whisper-1",
    maxTranscriptionDurationMs: 20_000,
  });
  await new Promise<void>((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  context.after(() => new Promise<void>((resolve, reject) => {
    server.close((error) => error === undefined ? resolve() : reject(error));
  }));
  const baseUrl = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;
  const audio = pcmWav(250);

  const unauthorized = await fetch(`${baseUrl}/api/v1/transcriptions`, {
    method: "POST",
    headers: { "Content-Type": "audio/wav" },
    body: Uint8Array.from(audio).buffer,
  });
  assert.equal(unauthorized.status, 401);

  const wrongType = await fetch(`${baseUrl}/api/v1/transcriptions`, {
    method: "POST",
    headers: {
      Authorization: "Bearer robot-token",
      "Content-Type": "application/octet-stream",
    },
    body: Uint8Array.from(audio).buffer,
  });
  assert.equal(wrongType.status, 415);

  const malformed = await fetch(`${baseUrl}/api/v1/transcriptions`, {
    method: "POST",
    headers: {
      Authorization: "Bearer robot-token",
      "Content-Type": "audio/wav",
    },
    body: Uint8Array.from(Buffer.from("not a wav")).buffer,
  });
  assert.equal(malformed.status, 400);

  const response = await fetch(`${baseUrl}/api/v1/transcriptions`, {
    method: "POST",
    headers: {
      Authorization: "Bearer robot-token",
      "Content-Type": "audio/wav",
    },
    body: Uint8Array.from(audio).buffer,
  });
  assert.equal(response.status, 200);
  const body = await response.json() as {
    data: { text: string; model: string; durationMs: number };
  };
  assert.equal(body.data.text, "진로 상담 선생님을 찾아 주세요.");
  assert.equal(body.data.model, "gpt-transcribe");
  assert.equal(body.data.durationMs, 250);
  assert.equal(receivedBytes, audio.byteLength);
});

function pcmWav(durationMs: number): Buffer {
  const sampleRate = 16_000;
  const dataSize = sampleRate * 2 * durationMs / 1_000;
  const buffer = Buffer.alloc(44 + dataSize);
  buffer.write("RIFF", 0, "ascii");
  buffer.writeUInt32LE(36 + dataSize, 4);
  buffer.write("WAVEfmt ", 8, "ascii");
  buffer.writeUInt32LE(16, 16);
  buffer.writeUInt16LE(1, 20);
  buffer.writeUInt16LE(1, 22);
  buffer.writeUInt32LE(sampleRate, 24);
  buffer.writeUInt32LE(sampleRate * 2, 28);
  buffer.writeUInt16LE(2, 32);
  buffer.writeUInt16LE(16, 34);
  buffer.write("data", 36, "ascii");
  buffer.writeUInt32LE(dataSize, 40);
  return buffer;
}

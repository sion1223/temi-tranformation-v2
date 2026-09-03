import assert from "node:assert/strict";
import { createServer } from "node:http";
import type { AddressInfo } from "node:net";
import test from "node:test";
import OpenAI from "openai";
import {
  FallbackSpeechTranscriber,
  OpenAiSpeechTranscriber,
  inspectPcmWav,
  type SpeechTranscriber,
} from "../src/speech-transcriber.js";

test("PCM WAV inspection accepts 20 seconds and rejects anything longer", () => {
  assert.equal(inspectPcmWav(pcmWav(20_000), 20_000).durationMs, 20_000);
  assert.throws(() => inspectPcmWav(pcmWav(20_001), 20_000), /20초/);
  assert.throws(() => inspectPcmWav(Buffer.from("not audio"), 20_000), /WAV/);
});

test("OpenAI speech transcriber sends bounded Korean WAV without storing it", async (context) => {
  let requestPath = "";
  let authorization = "";
  let requestBody = "";
  const server = createServer(async (request, response) => {
    requestPath = request.url ?? "";
    authorization = request.headers.authorization ?? "";
    const chunks: Buffer[] = [];
    for await (const chunk of request) chunks.push(Buffer.from(chunk as Uint8Array));
    requestBody = Buffer.concat(chunks).toString("latin1");
    response.writeHead(200, { "Content-Type": "application/json" });
    response.end(JSON.stringify({ text: "진로 상담 선생님을 찾아 주세요." }));
  });
  await new Promise<void>((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  context.after(() => new Promise<void>((resolve, reject) => {
    server.close((error) => error === undefined ? resolve() : reject(error));
  }));
  const client = new OpenAI({
    apiKey: "test-only-key",
    baseURL: `http://127.0.0.1:${(server.address() as AddressInfo).port}/v1`,
    maxRetries: 0,
  });

  const result = await new OpenAiSpeechTranscriber(
    "test-only-key",
    "gpt-transcribe",
    client,
  ).transcribe(pcmWav(200), "audio/wav");

  assert.equal(requestPath, "/v1/audio/transcriptions");
  assert.equal(authorization, "Bearer test-only-key");
  assert.match(requestBody, /name="model"\r\n\r\ngpt-transcribe/);
  assert.match(requestBody, /name="language"\r\n\r\nko/);
  assert.equal(result.text, "진로 상담 선생님을 찾아 주세요.");
  assert.equal(result.model, "gpt-transcribe");
});

test("speech fallback uses whisper-1 only when gpt-transcribe fails", async () => {
  const calls: string[] = [];
  const primary: SpeechTranscriber = {
    async transcribe() {
      calls.push("gpt-transcribe");
      throw new Error("primary failed");
    },
  };
  const fallback: SpeechTranscriber = {
    async transcribe() {
      calls.push("whisper-1");
      return { text: "과학실 담당자", model: "whisper-1" };
    },
  };

  const result = await new FallbackSpeechTranscriber(primary, fallback).transcribe(
    pcmWav(100),
    "audio/wav",
  );

  assert.deepEqual(calls, ["gpt-transcribe", "whisper-1"]);
  assert.equal(result.model, "whisper-1");
  assert.match(result.warning ?? "", /gpt-transcribe.*whisper-1/);
});

function pcmWav(durationMs: number): Buffer {
  const sampleRate = 16_000;
  const dataSize = Math.ceil(sampleRate * 2 * durationMs / 1_000);
  const buffer = Buffer.alloc(44 + dataSize);
  buffer.write("RIFF", 0, "ascii");
  buffer.writeUInt32LE(36 + dataSize, 4);
  buffer.write("WAVE", 8, "ascii");
  buffer.write("fmt ", 12, "ascii");
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

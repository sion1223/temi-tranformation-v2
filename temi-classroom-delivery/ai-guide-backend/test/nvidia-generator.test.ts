import assert from "node:assert/strict";
import { createServer } from "node:http";
import test from "node:test";
import OpenAI from "openai";
import { NvidiaNimExplanationGenerator } from "../src/explanation-generator.js";
import type { TeacherProfile } from "../src/staff-directory.js";

test("NVIDIA NIM generator uses chat completions with grounded teacher data", async (context) => {
  let requestPath = "";
  let authorization = "";
  let requestBody: Record<string, unknown> = {};
  const server = createServer(async (request, response) => {
    requestPath = request.url ?? "";
    authorization = request.headers.authorization ?? "";
    const chunks: Buffer[] = [];
    for await (const chunk of request) chunks.push(Buffer.from(chunk as Uint8Array));
    requestBody = JSON.parse(Buffer.concat(chunks).toString("utf8")) as Record<string, unknown>;
    const body = JSON.stringify({
      id: "chatcmpl_test",
      object: "chat.completion",
      created: 1_787_500_000,
      model: "deepseek-ai/deepseek-v4-flash-0731",
      choices: [{
        index: 0,
        message: { role: "assistant", content: "과학실 담당 선생님은 실험 안전을 관리합니다." },
        finish_reason: "stop",
      }],
    });
    response.writeHead(200, { "Content-Type": "application/json" });
    response.end(body);
  });
  await new Promise<void>((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  context.after(async () => {
    await new Promise<void>((resolve, reject) => {
      server.close((error) => error === undefined ? resolve() : reject(error));
    });
  });

  const client = new OpenAI({
    apiKey: "test-only-key",
    baseURL: `http://127.0.0.1:${(server.address() as { port: number }).port}/v1`,
    maxRetries: 0,
  });
  const generator = new NvidiaNimExplanationGenerator(
    "test-only-key",
    "deepseek-ai/deepseek-v4-flash-0731",
    client,
  );
  const teacher: TeacherProfile = {
    id: "kim-minji",
    name: "김민지",
    title: "과학 교사",
    subjects: ["과학"],
    responsibilities: ["과학실 안전 관리"],
    department: "과학부",
    location: "본관 2층 과학실",
    aliases: ["민지쌤"],
    visibility: "public",
  };

  const generated = await generator.generate({
    question: "과학실 안전을 담당하는 선생님은 누구예요?",
    directoryMatches: [teacher],
  });

  assert.equal(requestPath, "/v1/chat/completions");
  assert.equal(authorization, "Bearer test-only-key");
  assert.equal(requestBody.model, "deepseek-ai/deepseek-v4-flash-0731");
  assert.equal(requestBody.stream, false);
  const messages = requestBody.messages as Array<{ role: string; content: string }>;
  assert.equal(messages[0]?.role, "system");
  assert.match(messages[0]?.content ?? "", /교직원 디렉터리/);
  assert.match(messages[1]?.content ?? "", /김민지/);
  assert.match(messages[1]?.content ?? "", /과학실 안전 관리/);
  assert.equal(generated.source, "nvidia_nim");
  assert.equal(generated.model, "deepseek-ai/deepseek-v4-flash-0731");
  assert.equal(generated.text, "과학실 담당 선생님은 실험 안전을 관리합니다.");
});

test("NVIDIA NIM generator rejects empty responses for fallback", async (context) => {
  const server = createServer((_request, response) => {
    response.writeHead(200, { "Content-Type": "application/json" });
    response.end(JSON.stringify({ choices: [{ message: { content: "" } }] }));
  });
  await new Promise<void>((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  context.after(async () => {
    await new Promise<void>((resolve, reject) => {
      server.close((error) => error === undefined ? resolve() : reject(error));
    });
  });

  const client = new OpenAI({
    apiKey: "test-only-key",
    baseURL: `http://127.0.0.1:${(server.address() as { port: number }).port}/v1`,
    maxRetries: 0,
  });
  const generator = new NvidiaNimExplanationGenerator(
    "test-only-key",
    "deepseek-ai/deepseek-v4-flash-0731",
    client,
  );

  await assert.rejects(
    generator.generate({ question: "담당자를 찾아 주세요.", directoryMatches: [] }),
    (error: unknown) => error instanceof Error && error.message.includes("NVIDIA NIM"),
  );
});

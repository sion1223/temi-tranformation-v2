import assert from "node:assert/strict";
import { createServer } from "node:http";
import type { AddressInfo } from "node:net";
import test from "node:test";
import OpenAI from "openai";
import { OpenAiExplanationGenerator } from "../src/explanation-generator.js";

test("OpenAI generator uses Responses API with GPT-5.6 Luna and no response storage", async (context) => {
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
      id: "resp_test",
      object: "response",
      created_at: 1_787_500_000,
      status: "completed",
      model: "gpt-5.6-luna",
      output: [
        {
          id: "msg_test",
          type: "message",
          status: "completed",
          role: "assistant",
          content: [
            {
              type: "output_text",
              text: "과학 실험 키트 사용법입니다.",
              annotations: [],
              logprobs: [],
            },
          ],
        },
      ],
      error: null,
      incomplete_details: null,
      metadata: {},
      usage: { input_tokens: 10, output_tokens: 8, total_tokens: 18 },
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
  const port = (server.address() as AddressInfo).port;
  const client = new OpenAI({
    apiKey: "test-only-key",
    baseURL: `http://127.0.0.1:${port}/v1`,
    maxRetries: 0,
  });
  const generator = new OpenAiExplanationGenerator(
    "test-only-key",
    "gpt-5.6-luna",
    client,
  );

  const generated = await generator.generate({
    item: {
      id: "science-kit",
      name: "과학 실험 키트",
      shortDescription: "관찰 실험 도구입니다.",
      usageSteps: ["선생님의 순서대로 사용합니다."],
      safetyNotes: ["재료를 먹지 않습니다."],
    },
  });

  assert.equal(requestPath, "/v1/responses");
  assert.equal(authorization, "Bearer test-only-key");
  assert.equal(requestBody.model, "gpt-5.6-luna");
  assert.equal(requestBody.store, false);
  assert.equal((requestBody.reasoning as { effort: string }).effort, "low");
  assert.match(String(requestBody.instructions), /교사가 제공한 JSON 정보만/);
  assert.equal(generated.text, "과학 실험 키트 사용법입니다.");
  assert.equal(generated.model, "gpt-5.6-luna");
});

import assert from "node:assert/strict";
import test from "node:test";
import {
  FallbackExplanationGenerator,
  type ExplanationGenerator,
  type GeneratedExplanation,
} from "../src/explanation-generator.js";

const input = { question: "과학실 담당자를 찾아 주세요.", directoryMatches: [] };

test("provider fallback tries Luna first and NVIDIA NIM second", async () => {
  const calls: string[] = [];
  const primary: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      calls.push("luna");
      throw new Error("Luna unavailable");
    },
  };
  const secondary: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      calls.push("nvidia_nim");
      return {
        text: "등록된 정보입니다.",
        model: "deepseek-ai/deepseek-v4-flash-0731",
        source: "nvidia_nim",
      };
    },
  };

  const result = await new FallbackExplanationGenerator(primary, secondary, {
    primarySource: "luna",
    secondarySource: "nvidia_nim",
  }).generate(input);

  assert.deepEqual(calls, ["luna", "nvidia_nim"]);
  assert.equal(result.source, "nvidia_nim");
  assert.match(result.warning ?? "", /Luna.*NVIDIA NIM/);
});

test("provider fallback does not call NVIDIA NIM after a Luna success", async () => {
  let secondaryCalled = false;
  const primary: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      return { text: "Luna 답변", model: "gpt-5.6-luna", source: "luna" };
    },
  };
  const secondary: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      secondaryCalled = true;
      return {
        text: "잘못된 fallback",
        model: "deepseek-ai/deepseek-v4-flash-0731",
        source: "nvidia_nim",
      };
    },
  };

  const result = await new FallbackExplanationGenerator(primary, secondary, {
    primarySource: "luna",
    secondarySource: "nvidia_nim",
  }).generate(input);

  assert.equal(secondaryCalled, false);
  assert.equal(result.source, "luna");
});

test("provider fallback reports both failures without exposing provider credentials", async () => {
  const failedProviders: string[] = [];
  const primary: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      throw new Error("NIM request failed with test-only-key");
    },
  };
  const secondary: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      throw new Error("Luna request failed with test-only-key");
    },
  };

  await assert.rejects(
    new FallbackExplanationGenerator(
      primary,
      secondary,
      {
        primarySource: "luna",
        secondarySource: "nvidia_nim",
        onProviderError: (provider) => failedProviders.push(provider),
      },
    ).generate(input),
    (error: unknown) => error instanceof Error &&
      error.message.includes("Luna와 NVIDIA NIM") &&
      !error.message.includes("test-only-key"),
  );
  assert.deepEqual(failedProviders, ["luna", "nvidia_nim"]);
});

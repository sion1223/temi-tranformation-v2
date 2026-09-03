import assert from "node:assert/strict";
import test from "node:test";
import type {
  ExplanationGenerator,
  ExplanationInput,
  GeneratedExplanation,
} from "../src/explanation-generator.js";
import { GuideService } from "../src/guide-service.js";
import type { SupplyItem } from "../src/supply-item.js";
import type { SupplyItemRepository } from "../src/supply-item-repository.js";

const scienceKit: SupplyItem = {
  id: "science-kit",
  name: "과학 실험 키트",
  shortDescription: "관찰 실험에 사용하는 도구 묶음입니다.",
  usageSteps: ["내용물을 확인합니다.", "선생님 순서대로 사용합니다."],
  safetyNotes: ["재료를 먹지 않습니다."],
  audience: "중학생",
};

class MemoryRepository implements SupplyItemRepository {
  private readonly items = new Map([[scienceKit.id, scienceKit]]);

  async findAll(): Promise<SupplyItem[]> {
    return [...this.items.values()];
  }

  async findById(id: string): Promise<SupplyItem | null> {
    return this.items.get(id) ?? null;
  }

  async upsert(item: SupplyItem): Promise<SupplyItem> {
    this.items.set(item.id, item);
    return item;
  }

  async delete(id: string): Promise<boolean> {
    return this.items.delete(id);
  }
}

test("teacher data is passed to the OpenAI generator", async () => {
  let received: ExplanationInput | undefined;
  const generator: ExplanationGenerator = {
    async generate(input): Promise<GeneratedExplanation> {
      received = input;
      return { text: "키트를 안전하게 사용하는 방법입니다.", model: "gpt-5.6-luna" };
    },
  };
  const service = new GuideService(new MemoryRepository(), generator, true);

  const result = await service.explain("science-kit", "이게 뭐예요?");

  assert.ok(received);
  assert.equal(received.item?.id, "science-kit");
  assert.equal(received.question, "이게 뭐예요?");
  assert.equal(result.source, "luna");
  assert.equal(result.model, "gpt-5.6-luna");
});

test("teacher-authored fallback is returned when OpenAI is unavailable", async () => {
  const generator: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      throw new Error("network unavailable");
    },
  };
  const service = new GuideService(new MemoryRepository(), generator, true);

  const result = await service.explain("science-kit");

  assert.equal(result.source, "teacher_fallback");
  assert.equal(result.model, null);
  assert.match(result.explanation, /내용물을 확인합니다/);
  assert.match(result.explanation, /재료를 먹지 않습니다/);
  assert.ok(result.warning);
});

test("unknown items are rejected before an OpenAI request", async () => {
  let called = false;
  const generator: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      called = true;
      return { text: "unexpected", model: "gpt-5.6-luna" };
    },
  };
  const service = new GuideService(new MemoryRepository(), generator, false);

  await assert.rejects(
    service.explain("missing"),
    (error: unknown) => error instanceof Error && error.message.includes("등록되지 않은"),
  );
  assert.equal(called, false);
});

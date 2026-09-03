import assert from "node:assert/strict";
import type { AddressInfo } from "node:net";
import test from "node:test";
import type { ExplanationGenerator } from "../src/explanation-generator.js";
import { GuideService } from "../src/guide-service.js";
import { createAppServer } from "../src/http-server.js";
import type { SupplyItem } from "../src/supply-item.js";
import type { SupplyItemRepository } from "../src/supply-item-repository.js";

class MemoryRepository implements SupplyItemRepository {
  private readonly items = new Map<string, SupplyItem>();

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

test("admin can register an item and the temi client can request its guide", async (context) => {
  const repository = new MemoryRepository();
  const generator: ExplanationGenerator = {
    async generate({ item }) {
      return { text: `${item?.name ?? "물품"} 설명`, model: "gpt-5.6-luna" };
    },
  };
  const guideService = new GuideService(repository, generator, false);
  const server = createAppServer({
    repository,
    guideService,
    adminToken: "teacher-secret",
    temiClientToken: "robot-token",
    model: "gpt-5.6-luna",
    openAiConfigured: true,
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
  const baseUrl = `http://127.0.0.1:${port}`;

  const unauthorized = await fetch(`${baseUrl}/api/v1/admin/items/microscope`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(validMicroscope()),
  });
  assert.equal(unauthorized.status, 401);

  const saved = await fetch(`${baseUrl}/api/v1/admin/items/microscope`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer teacher-secret",
    },
    body: JSON.stringify(validMicroscope()),
  });
  assert.equal(saved.status, 200);

  const guideResponse = await fetch(`${baseUrl}/api/v1/guides`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer robot-token",
    },
    body: JSON.stringify({ itemId: "microscope" }),
  });
  assert.equal(guideResponse.status, 200);
  const body = await guideResponse.json() as {
    success: boolean;
    data: { explanation: string; source: string; model: string };
  };
  assert.equal(body.success, true);
  assert.equal(body.data.explanation, "현미경 설명");
  assert.equal(body.data.source, "luna");
  assert.equal(body.data.model, "gpt-5.6-luna");

  const health = await fetch(`${baseUrl}/health`);
  assert.equal(health.status, 200);
  const healthBody = await health.json() as {
    data: {
      model: string;
      openAiConfigured: boolean;
      primary: { provider: string };
      fallback: { provider: string };
      transcription: { model: string; maxDurationMs: number };
    };
  };
  assert.equal(healthBody.data.model, "gpt-5.6-luna");
  assert.equal(healthBody.data.openAiConfigured, true);
  assert.equal(healthBody.data.primary.provider, "luna");
  assert.equal(healthBody.data.fallback.provider, "nvidia_nim");
  assert.equal(healthBody.data.transcription.model, "gpt-transcribe");
  assert.equal(healthBody.data.transcription.maxDurationMs, 20_000);
});

test("temi APIs fail closed when the client token is not configured", async (context) => {
  const repository = new MemoryRepository();
  const generator: ExplanationGenerator = {
    async generate() {
      return { text: "unused", model: "unused" };
    },
  };
  const server = createAppServer({
    repository,
    guideService: new GuideService(repository, generator, false),
    adminToken: "teacher-secret",
    temiClientToken: "",
    model: "gpt-5.6-luna",
    openAiConfigured: true,
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
  const response = await fetch(`http://127.0.0.1:${port}/api/v1/items`);

  assert.equal(response.status, 503);
  const body = await response.json() as { error: { code: string } };
  assert.equal(body.error.code, "temi_client_not_configured");
});

function validMicroscope(): Omit<SupplyItem, "id"> {
  return {
    name: "현미경",
    shortDescription: "작은 물체를 확대해 관찰하는 도구입니다.",
    usageSteps: ["가장 낮은 배율로 시작합니다."],
    safetyNotes: ["렌즈를 손으로 만지지 않습니다."],
  };
}

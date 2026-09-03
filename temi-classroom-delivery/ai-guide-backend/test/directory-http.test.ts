import assert from "node:assert/strict";
import type { AddressInfo } from "node:net";
import test from "node:test";
import { GuideService } from "../src/guide-service.js";
import type { ExplanationGenerator, GeneratedExplanation } from "../src/explanation-generator.js";
import { createAppServer } from "../src/http-server.js";
import { SchoolAnswerService } from "../src/school-answer-service.js";
import type { SupplyItem } from "../src/supply-item.js";
import type { SupplyItemRepository } from "../src/supply-item-repository.js";
import type { TeacherDirectoryRepository, TeacherProfile } from "../src/staff-directory-repository.js";

const teacher: TeacherProfile = {
  id: "kim-minji",
  name: "김민지",
  title: "과학 교사",
  subjects: ["과학"],
  responsibilities: ["과학실 안전 관리", "실험 준비"],
  department: "과학부",
  location: "본관 2층 과학실",
  aliases: ["민지쌤"],
  visibility: "public",
};

class MemoryItems implements SupplyItemRepository {
  async findAll(): Promise<SupplyItem[]> { return []; }
  async findById(): Promise<SupplyItem | null> { return null; }
  async upsert(item: SupplyItem): Promise<SupplyItem> { return item; }
  async delete(): Promise<boolean> { return false; }
}

class MemoryDirectory implements TeacherDirectoryRepository {
  private teachers: TeacherProfile[];

  constructor(initial: TeacherProfile[] = []) {
    this.teachers = [...initial];
  }

  async findAll(): Promise<TeacherProfile[]> { return this.teachers.map(cloneTeacher); }

  async findById(id: string): Promise<TeacherProfile | null> {
    const found = this.teachers.find((entry) => entry.id === id);
    return found === undefined ? null : cloneTeacher(found);
  }

  async search(query: string, limit = 5): Promise<TeacherProfile[]> {
    const normalized = query.toLocaleLowerCase("ko-KR");
    return this.teachers
      .filter((entry) => entry.visibility === "public" &&
        `${entry.name} ${entry.title} ${entry.subjects.join(" ")} ${entry.responsibilities.join(" ")}`
          .toLocaleLowerCase("ko-KR")
          .includes(normalized.includes("과학") ? "과학" : "never-match"))
      .slice(0, limit)
      .map(cloneTeacher);
  }

  async upsert(entry: TeacherProfile): Promise<TeacherProfile> {
    this.teachers = [...this.teachers.filter((current) => current.id !== entry.id), cloneTeacher(entry)];
    return cloneTeacher(entry);
  }

  async replace(entries: TeacherProfile[]): Promise<TeacherProfile[]> {
    this.teachers = entries.map(cloneTeacher);
    return this.findAll();
  }
}

test("directory APIs enforce admin/temi auth and return the fixed school answer contract", async (context) => {
  const directory = new MemoryDirectory();
  const generator: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      return {
        text: "김민지 선생님은 과학실 안전 관리와 실험 준비를 담당합니다.",
        source: "nvidia_nim",
        model: "deepseek-ai/deepseek-v4-flash-0731",
      };
    },
  };
  const guideService = new GuideService(new MemoryItems(), generator, true);
  const schoolAnswerService = new SchoolAnswerService(directory, generator, true);
  const server = createAppServer({
    repository: new MemoryItems(),
    guideService,
    adminToken: "teacher-secret",
    temiClientToken: "robot-token",
    model: "deepseek-ai/deepseek-v4-flash-0731",
    openAiConfigured: true,
    nvidiaModel: "deepseek-ai/deepseek-v4-flash-0731",
    nvidiaConfigured: true,
    lunaModel: "gpt-5.6-luna",
    lunaConfigured: true,
    directoryRepository: directory,
    schoolAnswerService,
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
  const baseUrl = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;
  const directoryDocument = { schemaVersion: 1, staff: [teacher] };

  const unauthorized = await fetch(`${baseUrl}/api/v1/admin/directory`, { method: "GET" });
  assert.equal(unauthorized.status, 401);

  const imported = await fetch(`${baseUrl}/api/v1/admin/directory`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer teacher-secret",
    },
    body: JSON.stringify(directoryDocument),
  });
  assert.equal(imported.status, 200);

  const answerUnauthorized = await fetch(`${baseUrl}/api/v1/school/answers`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ question: "과학실 안전 담당자는 누구예요?" }),
  });
  assert.equal(answerUnauthorized.status, 401);

  const answerResponse = await fetch(`${baseUrl}/api/v1/school/answers`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer robot-token",
    },
    body: JSON.stringify({ question: "과학실 안전 담당자는 누구예요?" }),
  });
  assert.equal(answerResponse.status, 200);
  const answerBody = await answerResponse.json() as {
    data: {
      answer: string;
      source: string;
      model: string;
      warning: string | null;
      matches: Array<Record<string, unknown>>;
    };
  };
  assert.equal(answerBody.data.source, "nvidia_nim");
  assert.equal(answerBody.data.model, "deepseek-ai/deepseek-v4-flash-0731");
  assert.equal(answerBody.data.matches[0]?.name, "김민지");
  assert.deepEqual(Object.keys(answerBody.data.matches[0] ?? {}).sort(), [
    "department",
    "id",
    "location",
    "name",
    "responsibilities",
    "title",
  ]);

  const upserted = await fetch(`${baseUrl}/api/v1/admin/directory/teachers/lee-junho`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer teacher-secret",
    },
    body: JSON.stringify({
      name: "이준호",
      title: "상담 교사",
      subjects: ["상담"],
      responsibilities: ["학생 상담"],
      department: "학생생활부",
      location: "본관 1층 상담실",
      aliases: ["준호 선생님"],
      visibility: "public",
    }),
  });
  assert.equal(upserted.status, 200);
  const listed = await fetch(`${baseUrl}/api/v1/admin/directory`, {
    headers: { Authorization: "Bearer teacher-secret" },
  });
  assert.equal(listed.status, 200);
  const listedBody = await listed.json() as { data: { directory: { staff: TeacherProfile[] } } };
  assert.deepEqual(listedBody.data.directory.staff.map((entry) => entry.id).sort(), [
    "kim-minji",
    "lee-junho",
  ]);
});

test("directory import rejects student or secret fields before persistence", async (context) => {
  const directory = new MemoryDirectory();
  const generator: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      return { text: "unused", source: "nvidia_nim", model: "nim" };
    },
  };
  const server = createAppServer({
    repository: new MemoryItems(),
    guideService: new GuideService(new MemoryItems(), generator, true),
    adminToken: "teacher-secret",
    temiClientToken: "robot-token",
    model: "nim",
    openAiConfigured: false,
    directoryRepository: directory,
    schoolAnswerService: new SchoolAnswerService(directory, generator, true),
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
  const baseUrl = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;
  const response = await fetch(`${baseUrl}/api/v1/admin/directory`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer teacher-secret",
    },
    body: JSON.stringify({ schemaVersion: 1, staff: [{ ...teacher, studentName: "학생" }] }),
  });
  assert.equal(response.status, 400);
  assert.deepEqual(await directory.findAll(), []);
});

function cloneTeacher(entry: TeacherProfile): TeacherProfile {
  return {
    ...entry,
    subjects: [...entry.subjects],
    responsibilities: [...entry.responsibilities],
    aliases: [...entry.aliases],
  };
}

import assert from "node:assert/strict";
import test from "node:test";
import type {
  ExplanationGenerator,
  GeneratedExplanation,
} from "../src/explanation-generator.js";
import { SchoolAnswerService } from "../src/school-answer-service.js";
import type {
  TeacherDirectoryRepository,
  TeacherProfile,
} from "../src/staff-directory-repository.js";

const scienceTeacher: TeacherProfile = {
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

class MemoryDirectory implements TeacherDirectoryRepository {
  constructor(private readonly teachers: TeacherProfile[]) {}

  async findAll(): Promise<TeacherProfile[]> {
    return [...this.teachers];
  }

  async findById(id: string): Promise<TeacherProfile | null> {
    return this.teachers.find((teacher) => teacher.id === id) ?? null;
  }

  async upsert(teacher: TeacherProfile): Promise<TeacherProfile> {
    this.teachers.push(teacher);
    return teacher;
  }

  async replace(teachers: TeacherProfile[]): Promise<TeacherProfile[]> {
    this.teachers.splice(0, this.teachers.length, ...teachers);
    return [...teachers];
  }

  async search(query: string): Promise<TeacherProfile[]> {
    return this.teachers.filter((teacher) =>
      teacher.visibility === "public" &&
      `${teacher.name} ${teacher.title} ${teacher.subjects.join(" ")} ${teacher.responsibilities.join(" ")} ${teacher.department}`
        .includes("과학"),
    ).filter(() => query.includes("과학"));
  }
}

test("school answer searches public staff and gives grounded matches to the generator", async () => {
  let receivedQuestion = "";
  let receivedMatches: TeacherProfile[] = [];
  const generator: ExplanationGenerator = {
    async generate(input): Promise<GeneratedExplanation> {
      receivedQuestion = input.question ?? "";
      receivedMatches = [...(input.directoryMatches ?? [])];
      return {
        text: "김민지 선생님은 과학실 안전 관리와 실험 준비를 담당합니다.",
        model: "deepseek-ai/deepseek-v4-flash-0731",
        source: "nvidia_nim",
      };
    },
  };
  const service = new SchoolAnswerService(
    new MemoryDirectory([scienceTeacher]),
    generator,
    true,
  );

  const result = await service.answer("과학실 안전을 담당하는 선생님은 누구예요?");

  assert.equal(receivedQuestion, "검색된 공개 교직원 정보만 사용해 담당자를 안내해 주세요.");
  assert.notEqual(receivedQuestion, "과학실 안전을 담당하는 선생님은 누구예요?");
  assert.deepEqual(receivedMatches.map((teacher) => teacher.id), ["kim-minji"]);
  assert.equal(result.source, "nvidia_nim");
  assert.equal(result.model, "deepseek-ai/deepseek-v4-flash-0731");
  assert.equal(result.matches[0]?.name, "김민지");
  assert.equal(result.matches[0]?.responsibilities[0], "과학실 안전 관리");
});

test("school answer never asks an LLM to guess when directory search has no match", async () => {
  let called = false;
  const generator: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      called = true;
      return { text: "추측", model: "bad", source: "nvidia_nim" };
    },
  };
  const service = new SchoolAnswerService(
    new MemoryDirectory([scienceTeacher]),
    generator,
    true,
  );

  const result = await service.answer("등록되지 않은 업무 담당자를 찾아 주세요.");

  assert.equal(called, false);
  assert.equal(result.source, "teacher_fallback");
  assert.equal(result.model, null);
  assert.deepEqual(result.matches, []);
  assert.match(result.answer, /찾지 못했습니다/);
});

test("school answer uses deterministic staff facts if NIM and Luna both fail", async () => {
  const generator: ExplanationGenerator = {
    async generate(): Promise<GeneratedExplanation> {
      throw new Error("both providers unavailable");
    },
  };
  const service = new SchoolAnswerService(
    new MemoryDirectory([scienceTeacher]),
    generator,
    true,
  );

  const result = await service.answer("과학실 안전을 담당하는 선생님은 누구예요?");

  assert.equal(result.source, "teacher_fallback");
  assert.equal(result.model, null);
  assert.match(result.answer, /김민지/);
  assert.match(result.answer, /과학실 안전 관리/);
  assert.match(result.warning ?? "", /Luna와 NVIDIA NIM/);
});

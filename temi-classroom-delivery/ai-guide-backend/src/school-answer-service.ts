import { ApiError } from "./errors.js";
import type {
  ExplanationGenerator,
  GeneratedExplanationSource,
} from "./explanation-generator.js";
import {
  toPublicTeacherMatch,
  type PublicTeacherMatch,
} from "./staff-directory.js";
import type {
  TeacherDirectoryRepository,
  TeacherProfile,
} from "./staff-directory-repository.js";

export interface SchoolAnswer {
  answer: string;
  source: GeneratedExplanationSource | "teacher_fallback";
  model: string | null;
  warning: string | null;
  matches: PublicTeacherMatch[];
}

const GROUNDED_DIRECTORY_PROMPT =
  "검색된 공개 교직원 정보만 사용해 담당자를 안내해 주세요.";

export class SchoolAnswerService {
  constructor(
    private readonly directory: TeacherDirectoryRepository,
    private readonly generator: ExplanationGenerator,
    private readonly fallbackOnGeneratorError: boolean,
    private readonly onGeneratorError: (error: unknown, question: string) => void = () => undefined,
  ) {}

  async answer(question: string): Promise<SchoolAnswer> {
    const normalizedQuestion = question.trim();
    if (normalizedQuestion.length === 0) {
      throw new ApiError(400, "invalid_request", "question을 입력해 주세요.");
    }

    const teachers = (await this.directory.search(normalizedQuestion, 5))
      .filter((teacher) => teacher.visibility === "public");
    const matches = teachers.map(toPublicTeacherMatch);
    if (teachers.length === 0) {
      return {
        answer: "등록된 공개 교직원 디렉터리에서 질문과 관련된 담당자를 찾지 못했습니다. 선생님께 확인해 주세요.",
        source: "teacher_fallback",
        model: null,
        warning: "등록된 디렉터리 정보와 일치하는 담당자가 없습니다.",
        matches: [],
      };
    }

    try {
      const generated = await this.generator.generate({
        // The raw kiosk question is used only for local directory search. Do not
        // send arbitrary student-entered text or prompt injection to providers.
        question: GROUNDED_DIRECTORY_PROMPT,
        directoryMatches: teachers,
      });
      return {
        answer: generated.text,
        source: generated.source ?? "luna",
        model: generated.model,
        warning: generated.warning ?? null,
        matches,
      };
    } catch (error) {
      this.onGeneratorError(error, normalizedQuestion);
      if (!this.fallbackOnGeneratorError) {
        throw new ApiError(
          503,
          "explanation_unavailable",
          "AI 교직원 안내를 지금 생성할 수 없습니다. 잠시 후 다시 시도해 주세요.",
          { cause: error },
        );
      }
      return {
        answer: buildTeacherDirectoryFallback(teachers),
        source: "teacher_fallback",
        model: null,
        warning: "Luna와 NVIDIA NIM에 연결하지 못해 등록된 교직원 정보만 안내합니다.",
        matches,
      };
    }
  }
}

function buildTeacherDirectoryFallback(teachers: readonly TeacherProfile[]): string {
  const facts = teachers.map((teacher) => {
    const responsibilities = teacher.responsibilities.join(", ");
    return `${teacher.name} 선생님(${teacher.title}, ${teacher.department})은(는) ` +
      `${responsibilities}을(를) 담당하고, ${teacher.location}에 있습니다.`;
  }).join(" ");
  return `${facts} 등록된 정보만 안내했으며, 구체적인 담당 여부는 선생님께 확인해 주세요.`;
}

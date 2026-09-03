import { ApiError } from "./errors.js";

export type TeacherVisibility = "public" | "internal";

export interface TeacherProfile {
  id: string;
  name: string;
  title: string;
  subjects: string[];
  responsibilities: string[];
  department: string;
  location: string;
  aliases: string[];
  visibility: TeacherVisibility;
}

export interface TeacherDirectoryDocument {
  schemaVersion: 1;
  staff: TeacherProfile[];
}

export interface PublicTeacherMatch {
  id: string;
  name: string;
  title: string;
  department: string;
  location: string;
  responsibilities: string[];
}

const TEACHER_ID_PATTERN = /^[a-z0-9][a-z0-9-]{0,63}$/;
const TEACHER_FIELDS = new Set([
  "id",
  "name",
  "title",
  "subjects",
  "responsibilities",
  "department",
  "location",
  "aliases",
  "visibility",
]);
const DIRECTORY_FIELDS = new Set(["schemaVersion", "staff"]);
const SECRET_PATTERN = /(?:nvapi-|sk-[a-z0-9]|AIza[a-z0-9_-]+|gh[pousr]_[a-z0-9]+|xox[baprs]-)/i;
const EMAIL_PATTERN = /\b[^\s@]+@[^\s@]+\.[^\s@]+\b/;
const PHONE_PATTERN = /(?:01[016789]|02|0[3-9]\d)[-\s]?\d{3,4}[-\s]?\d{4}/;
const SEARCH_STOP_WORDS = new Set([
  "어떤",
  "사람",
  "사람을",
  "누구",
  "누구예요",
  "선생님",
  "교사",
  "담당",
  "담당자",
  "담당자를",
  "찾아",
  "찾아줘",
  "찾아주세요",
  "찾아야",
  "알려",
  "알려줘",
  "알려주세요",
  "하는",
  "해야",
  "할",
  "무엇",
  "무엇을",
  "입니다",
  "인가요",
  "주세요",
]);

export function parseTeacherProfile(value: unknown, expectedId?: string): TeacherProfile {
  const input = asObject(value, "교직원");
  rejectUnknownFields(input, TEACHER_FIELDS, "교직원");

  const id = requiredString(expectedId ?? input.id, "id", 64).toLowerCase();
  if (!TEACHER_ID_PATTERN.test(id)) {
    throw new ApiError(
      400,
      "invalid_teacher_id",
      "교직원 id는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.",
    );
  }
  if (expectedId !== undefined && input.id !== undefined) {
    const bodyId = requiredString(input.id, "id", 64).toLowerCase();
    if (bodyId !== id) {
      throw new ApiError(400, "teacher_id_mismatch", "경로의 교직원 id와 본문의 id가 다릅니다.");
    }
  }

  const name = requiredString(input.name, "name", 100);
  const title = requiredString(input.title, "title", 100);
  const subjects = stringArray(input.subjects, "subjects", 12);
  const responsibilities = stringArray(input.responsibilities, "responsibilities", 20);
  const department = requiredString(input.department, "department", 100);
  const location = requiredString(input.location, "location", 160);
  const aliases = optionalStringArray(input.aliases, "aliases", 12);
  const visibility = requiredVisibility(input.visibility);

  for (const [fieldName, values] of Object.entries({
    name,
    title,
    subjects,
    responsibilities,
    department,
    location,
    aliases,
  })) {
    rejectSensitiveValues(values, fieldName);
  }

  return {
    id,
    name,
    title,
    subjects,
    responsibilities,
    department,
    location,
    aliases,
    visibility,
  };
}

export function parseTeacherDirectoryDocument(value: unknown): TeacherDirectoryDocument {
  const input = asObject(value, "교직원 디렉터리");
  rejectUnknownFields(input, DIRECTORY_FIELDS, "교직원 디렉터리");
  if (input.schemaVersion !== 1) {
    throw new ApiError(400, "invalid_directory", "지원하지 않는 교직원 디렉터리 스키마입니다.");
  }
  if (!Array.isArray(input.staff) || input.staff.length > 1_000) {
    throw new ApiError(
      400,
      "invalid_directory",
      "staff는 최대 1000명의 교직원을 담은 배열이어야 합니다.",
    );
  }
  const staff = input.staff.map((teacher) => parseTeacherProfile(teacher));
  const ids = new Set<string>();
  for (const teacher of staff) {
    if (ids.has(teacher.id)) {
      throw new ApiError(400, "duplicate_teacher_id", `중복된 교직원 id입니다: ${teacher.id}`);
    }
    ids.add(teacher.id);
  }
  return { schemaVersion: 1, staff };
}

export function toPublicTeacherMatch(teacher: TeacherProfile): PublicTeacherMatch {
  return {
    id: teacher.id,
    name: teacher.name,
    title: teacher.title,
    department: teacher.department,
    location: teacher.location,
    responsibilities: [...teacher.responsibilities],
  };
}

export function searchTeachers(
  teachers: readonly TeacherProfile[],
  query: string,
  limit = 5,
): TeacherProfile[] {
  const normalizedQuery = normalizeSearchText(query);
  if (normalizedQuery.length === 0) return [];
  const tokens = normalizedQuery
    .split(" ")
    .filter((token) => token.length > 1 && !SEARCH_STOP_WORDS.has(token));
  if (tokens.length === 0) return [];

  return teachers
    .filter((teacher) => teacher.visibility === "public")
    .map((teacher) => ({ teacher, score: scoreTeacher(teacher, normalizedQuery, tokens) }))
    .filter((entry) => entry.score > 0)
    .sort((left, right) =>
      right.score - left.score ||
      left.teacher.name.localeCompare(right.teacher.name, "ko") ||
      left.teacher.id.localeCompare(right.teacher.id),
    )
    .slice(0, Math.max(0, limit))
    .map((entry) => entry.teacher);
}

function scoreTeacher(
  teacher: TeacherProfile,
  query: string,
  tokens: readonly string[],
): number {
  const fields: Array<{ value: string; weight: number }> = [
    { value: teacher.responsibilities.join(" "), weight: 6 },
    { value: teacher.subjects.join(" "), weight: 5 },
    { value: teacher.title, weight: 5 },
    { value: teacher.department, weight: 4 },
    { value: teacher.aliases.join(" "), weight: 3 },
    { value: teacher.name, weight: 3 },
    { value: teacher.location, weight: 2 },
  ];
  let score = 0;
  for (const field of fields) {
    const normalizedField = normalizeSearchText(field.value);
    if (normalizedField.length === 0) continue;
    if (query.includes(normalizedField)) score += field.weight * 2;
    for (const token of tokens) {
      if (normalizedField.includes(token)) score += field.weight;
    }
  }
  return score;
}

function normalizeSearchText(value: string): string {
  return value
    .normalize("NFKC")
    .toLocaleLowerCase("ko-KR")
    .replace(/[^\p{L}\p{N}]+/gu, " ")
    .trim()
    .replace(/\s+/g, " ");
}

function asObject(value: unknown, fieldName: string): Record<string, unknown> {
  if (value === null || typeof value !== "object" || Array.isArray(value)) {
    throw new ApiError(400, "invalid_request", `${fieldName}은(는) JSON 객체여야 합니다.`);
  }
  return value as Record<string, unknown>;
}

function rejectUnknownFields(
  input: Record<string, unknown>,
  allowed: ReadonlySet<string>,
  fieldName: string,
): void {
  for (const key of Object.keys(input)) {
    if (!allowed.has(key)) {
      throw new ApiError(
        400,
        "invalid_staff_field",
        `${fieldName}에 허용되지 않은 필드가 있습니다: ${key}`,
      );
    }
  }
}

function requiredString(value: unknown, fieldName: string, maxLength: number): string {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new ApiError(400, "invalid_request", `${fieldName}을(를) 입력해 주세요.`);
  }
  const normalized = value.trim();
  if (normalized.length > maxLength) {
    throw new ApiError(400, "invalid_request", `${fieldName}은(는) ${maxLength}자 이하여야 합니다.`);
  }
  return normalized;
}

function stringArray(value: unknown, fieldName: string, maxItems: number): string[] {
  if (!Array.isArray(value) || value.length === 0 || value.length > maxItems) {
    throw new ApiError(
      400,
      "invalid_request",
      `${fieldName}은(는) 1~${maxItems}개의 문자열 배열이어야 합니다.`,
    );
  }
  return value.map((entry, index) => requiredString(entry, `${fieldName}[${index}]`, 300));
}

function optionalStringArray(value: unknown, fieldName: string, maxItems: number): string[] {
  if (value === undefined || value === null) return [];
  if (!Array.isArray(value) || value.length > maxItems) {
    throw new ApiError(
      400,
      "invalid_request",
      `${fieldName}은(는) 최대 ${maxItems}개의 문자열 배열이어야 합니다.`,
    );
  }
  return value.map((entry, index) => requiredString(entry, `${fieldName}[${index}]`, 100));
}

function requiredVisibility(value: unknown): TeacherVisibility {
  if (value !== "public" && value !== "internal") {
    throw new ApiError(400, "invalid_visibility", "visibility는 public 또는 internal이어야 합니다.");
  }
  return value;
}

function rejectSensitiveValues(value: unknown, fieldName: string): void {
  const values = Array.isArray(value) ? value : [value];
  for (const entry of values) {
    if (typeof entry !== "string") continue;
    if (SECRET_PATTERN.test(entry) || EMAIL_PATTERN.test(entry) || PHONE_PATTERN.test(entry)) {
      throw new ApiError(
        400,
        "sensitive_data_not_allowed",
        `${fieldName}에 민감한 개인정보(API 키, 비밀번호 또는 연락처)를 입력할 수 없습니다.`,
      );
    }
  }
}

import { ApiError } from "./errors.js";

export interface SupplyItem {
  id: string;
  name: string;
  shortDescription: string;
  usageSteps: string[];
  safetyNotes: string[];
  audience?: string;
  teacherNotes?: string;
}

export interface SupplyCatalogDocument {
  schemaVersion: 1;
  items: SupplyItem[];
}

const ITEM_ID_PATTERN = /^[a-z0-9][a-z0-9-]{0,63}$/;

function asObject(value: unknown, fieldName: string): Record<string, unknown> {
  if (value === null || typeof value !== "object" || Array.isArray(value)) {
    throw new ApiError(400, "invalid_request", `${fieldName}은(는) JSON 객체여야 합니다.`);
  }
  return value as Record<string, unknown>;
}

function requiredString(
  value: unknown,
  fieldName: string,
  maxLength: number,
): string {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new ApiError(400, "invalid_request", `${fieldName}을(를) 입력해 주세요.`);
  }
  const normalized = value.trim();
  if (normalized.length > maxLength) {
    throw new ApiError(
      400,
      "invalid_request",
      `${fieldName}은(는) ${maxLength}자 이하여야 합니다.`,
    );
  }
  return normalized;
}

function optionalString(
  value: unknown,
  fieldName: string,
  maxLength: number,
): string | undefined {
  if (value === undefined || value === null || value === "") return undefined;
  return requiredString(value, fieldName, maxLength);
}

function stringArray(
  value: unknown,
  fieldName: string,
  maxItems: number,
): string[] {
  if (!Array.isArray(value) || value.length === 0 || value.length > maxItems) {
    throw new ApiError(
      400,
      "invalid_request",
      `${fieldName}은(는) 1~${maxItems}개의 문자열 배열이어야 합니다.`,
    );
  }
  return value.map((entry, index) =>
    requiredString(entry, `${fieldName}[${index}]`, 300),
  );
}

export function parseSupplyItem(value: unknown, expectedId?: string): SupplyItem {
  const input = asObject(value, "물품");
  const id = requiredString(expectedId ?? input.id, "id", 64).toLowerCase();
  if (!ITEM_ID_PATTERN.test(id)) {
    throw new ApiError(
      400,
      "invalid_item_id",
      "id는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.",
    );
  }

  const audience = optionalString(input.audience, "audience", 80);
  const teacherNotes = optionalString(input.teacherNotes, "teacherNotes", 1000);
  return {
    id,
    name: requiredString(input.name, "name", 100),
    shortDescription: requiredString(input.shortDescription, "shortDescription", 1000),
    usageSteps: stringArray(input.usageSteps, "usageSteps", 12),
    safetyNotes: stringArray(input.safetyNotes, "safetyNotes", 12),
    ...(audience === undefined ? {} : { audience }),
    ...(teacherNotes === undefined ? {} : { teacherNotes }),
  };
}

export function parseCatalogDocument(value: unknown): SupplyCatalogDocument {
  const input = asObject(value, "물품 데이터");
  if (input.schemaVersion !== 1) {
    throw new ApiError(500, "invalid_catalog", "지원하지 않는 물품 데이터 스키마입니다.");
  }
  if (!Array.isArray(input.items)) {
    throw new ApiError(500, "invalid_catalog", "items 배열이 필요합니다.");
  }
  const items = input.items.map((item) => parseSupplyItem(item));
  const ids = new Set<string>();
  for (const item of items) {
    if (ids.has(item.id)) {
      throw new ApiError(500, "invalid_catalog", `중복된 물품 id입니다: ${item.id}`);
    }
    ids.add(item.id);
  }
  return { schemaVersion: 1, items };
}

export function parseQuestion(value: unknown): string | undefined {
  return optionalString(value, "question", 300);
}

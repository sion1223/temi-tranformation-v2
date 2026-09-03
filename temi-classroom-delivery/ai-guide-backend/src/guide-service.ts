import { ApiError } from "./errors.js";
import type {
  ExplanationGenerator,
  GeneratedExplanationSource,
} from "./explanation-generator.js";
import type { SupplyItem } from "./supply-item.js";
import type { SupplyItemRepository } from "./supply-item-repository.js";

export interface SupplyGuide {
  itemId: string;
  itemName: string;
  explanation: string;
  source: GeneratedExplanationSource | "teacher_fallback";
  model: string | null;
  warning: string | null;
}

export class GuideService {
  constructor(
    private readonly repository: SupplyItemRepository,
    private readonly generator: ExplanationGenerator,
    private readonly fallbackOnGeneratorError: boolean,
    private readonly onGeneratorError: (error: unknown, itemId: string) => void = () => undefined,
  ) {}

  async explain(itemId: string, question?: string): Promise<SupplyGuide> {
    const normalizedId = itemId.trim().toLowerCase();
    const item = await this.repository.findById(normalizedId);
    if (item === null) {
      throw new ApiError(404, "item_not_found", "등록되지 않은 수업용품입니다.");
    }

    try {
      const generated = await this.generator.generate({
        item,
        ...(question === undefined ? {} : { question }),
      });
      return {
        itemId: item.id,
        itemName: item.name,
        explanation: generated.text,
        source: generated.source ?? "luna",
        model: generated.model,
        warning: generated.warning ?? null,
      };
    } catch (error) {
      this.onGeneratorError(error, item.id);
      if (!this.fallbackOnGeneratorError) {
        throw new ApiError(
          503,
          "explanation_unavailable",
          "AI 설명을 지금 생성할 수 없습니다. 잠시 후 다시 시도해 주세요.",
          { cause: error },
        );
      }
      return {
        itemId: item.id,
        itemName: item.name,
        explanation: buildTeacherFallback(item),
        source: "teacher_fallback",
        model: null,
        warning: "Luna와 NVIDIA NIM에 연결하지 못해 선생님이 입력한 내용만 그대로 안내합니다.",
      };
    }
  }
}

function buildTeacherFallback(item: SupplyItem): string {
  const steps = item.usageSteps.map((step, index) => `${index + 1}단계, ${step}`).join(" ");
  const safety = item.safetyNotes.join(" 또한, ");
  return `이 물품은 ${item.name}입니다. ${item.shortDescription} 사용 순서는 다음과 같습니다. ` +
    `${steps} 안전을 위해 ${safety} 선생님의 직접 지시를 가장 먼저 따라 주세요.`;
}

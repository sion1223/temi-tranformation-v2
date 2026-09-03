import { mkdir, readFile, rename, writeFile } from "node:fs/promises";
import { dirname } from "node:path";
import { ApiError } from "./errors.js";
import {
  parseCatalogDocument,
  type SupplyCatalogDocument,
  type SupplyItem,
} from "./supply-item.js";

export interface SupplyItemRepository {
  findAll(): Promise<SupplyItem[]>;
  findById(id: string): Promise<SupplyItem | null>;
  upsert(item: SupplyItem): Promise<SupplyItem>;
  delete(id: string): Promise<boolean>;
}

export class JsonSupplyItemRepository implements SupplyItemRepository {
  private items = new Map<string, SupplyItem>();
  private writeQueue: Promise<void> = Promise.resolve();

  constructor(private readonly filePath: string) {}

  async initialize(): Promise<void> {
    const raw = await readFile(this.filePath, "utf8");
    const catalog = parseCatalogDocument(JSON.parse(raw) as unknown);
    this.items = new Map(catalog.items.map((item) => [item.id, item]));
  }

  async findAll(): Promise<SupplyItem[]> {
    return [...this.items.values()]
      .sort((left, right) => left.name.localeCompare(right.name, "ko"))
      .map(cloneItem);
  }

  async findById(id: string): Promise<SupplyItem | null> {
    const item = this.items.get(id.toLowerCase());
    return item === undefined ? null : cloneItem(item);
  }

  upsert(item: SupplyItem): Promise<SupplyItem> {
    return this.enqueue(async () => {
      this.items.set(item.id, cloneItem(item));
      await this.persist();
      return cloneItem(item);
    });
  }

  delete(id: string): Promise<boolean> {
    return this.enqueue(async () => {
      const deleted = this.items.delete(id.toLowerCase());
      if (deleted) await this.persist();
      return deleted;
    });
  }

  private enqueue<T>(operation: () => Promise<T>): Promise<T> {
    const result = this.writeQueue.then(operation, operation);
    this.writeQueue = result.then(
      () => undefined,
      () => undefined,
    );
    return result;
  }

  private async persist(): Promise<void> {
    const document: SupplyCatalogDocument = {
      schemaVersion: 1,
      items: [...this.items.values()],
    };
    await mkdir(dirname(this.filePath), { recursive: true });
    const temporaryPath = `${this.filePath}.${process.pid}.tmp`;
    await writeFile(temporaryPath, `${JSON.stringify(document, null, 2)}\n`, {
      encoding: "utf8",
      flag: "w",
    });
    try {
      await rename(temporaryPath, this.filePath);
    } catch (error) {
      throw new ApiError(500, "catalog_write_failed", "물품 정보를 저장하지 못했습니다.", {
        cause: error,
      });
    }
  }
}

function cloneItem(item: SupplyItem): SupplyItem {
  return {
    ...item,
    usageSteps: [...item.usageSteps],
    safetyNotes: [...item.safetyNotes],
  };
}

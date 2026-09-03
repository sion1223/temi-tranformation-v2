import { mkdir, readFile, rename, writeFile } from "node:fs/promises";
import { dirname } from "node:path";
import {
  parseTeacherDirectoryDocument,
  parseTeacherProfile,
  searchTeachers,
  type TeacherDirectoryDocument,
  type TeacherProfile,
} from "./staff-directory.js";

export type { TeacherProfile } from "./staff-directory.js";

export interface TeacherDirectoryRepository {
  findAll(): Promise<TeacherProfile[]>;
  findById(id: string): Promise<TeacherProfile | null>;
  search(query: string, limit?: number): Promise<TeacherProfile[]>;
  upsert(teacher: TeacherProfile): Promise<TeacherProfile>;
  replace(teachers: TeacherProfile[]): Promise<TeacherProfile[]>;
}

export class JsonTeacherDirectoryRepository implements TeacherDirectoryRepository {
  private teachers = new Map<string, TeacherProfile>();
  private writeQueue: Promise<void> = Promise.resolve();

  constructor(private readonly filePath: string) {}

  async initialize(): Promise<void> {
    const raw = await readFile(this.filePath, "utf8");
    const document = parseTeacherDirectoryDocument(JSON.parse(raw) as unknown);
    this.teachers = new Map(document.staff.map((teacher) => [teacher.id, cloneTeacher(teacher)]));
  }

  async findAll(): Promise<TeacherProfile[]> {
    return [...this.teachers.values()]
      .sort((left, right) => left.name.localeCompare(right.name, "ko") || left.id.localeCompare(right.id))
      .map(cloneTeacher);
  }

  async findById(id: string): Promise<TeacherProfile | null> {
    const teacher = this.teachers.get(id.trim().toLowerCase());
    return teacher === undefined ? null : cloneTeacher(teacher);
  }

  async search(query: string, limit = 5): Promise<TeacherProfile[]> {
    return searchTeachers([...this.teachers.values()], query, limit).map(cloneTeacher);
  }

  upsert(teacher: TeacherProfile): Promise<TeacherProfile> {
    const validated = parseTeacherProfile(teacher);
    return this.enqueue(async () => {
      this.teachers.set(validated.id, cloneTeacher(validated));
      await this.persist();
      return cloneTeacher(validated);
    });
  }

  replace(teachers: TeacherProfile[]): Promise<TeacherProfile[]> {
    const document: TeacherDirectoryDocument = parseTeacherDirectoryDocument({
      schemaVersion: 1,
      staff: teachers,
    });
    return this.enqueue(async () => {
      this.teachers = new Map(document.staff.map((teacher) => [teacher.id, cloneTeacher(teacher)]));
      await this.persist();
      return document.staff.map(cloneTeacher);
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
    const document: TeacherDirectoryDocument = {
      schemaVersion: 1,
      staff: [...this.teachers.values()].map(cloneTeacher),
    };
    await mkdir(dirname(this.filePath), { recursive: true });
    const temporaryPath = `${this.filePath}.${process.pid}.tmp`;
    await writeFile(temporaryPath, `${JSON.stringify(document, null, 2)}\n`, {
      encoding: "utf8",
      flag: "w",
    });
    await rename(temporaryPath, this.filePath);
  }
}

function cloneTeacher(teacher: TeacherProfile): TeacherProfile {
  return {
    ...teacher,
    subjects: [...teacher.subjects],
    responsibilities: [...teacher.responsibilities],
    aliases: [...teacher.aliases],
  };
}

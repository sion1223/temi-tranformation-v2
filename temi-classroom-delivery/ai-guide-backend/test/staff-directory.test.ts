import assert from "node:assert/strict";
import test from "node:test";
import {
  parseTeacherDirectoryDocument,
  parseTeacherProfile,
  searchTeachers,
  type TeacherProfile,
} from "../src/staff-directory.js";

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

const counselor: TeacherProfile = {
  id: "lee-junho",
  name: "이준호",
  title: "상담 교사",
  subjects: ["상담"],
  responsibilities: ["학생 상담"],
  department: "학생생활부",
  location: "본관 1층 상담실",
  aliases: ["준호 선생님"],
  visibility: "public",
};

test("teacher directory validates the required fields and visibility", () => {
  const parsed = parseTeacherProfile(scienceTeacher);
  assert.deepEqual(parsed, scienceTeacher);
  assert.equal(parseTeacherProfile({ ...scienceTeacher, visibility: "internal" }).visibility, "internal");

  assert.throws(
    () => parseTeacherProfile({ ...scienceTeacher, visibility: "students-only" }),
    /visibility/,
  );
  assert.throws(
    () => parseTeacherProfile({ ...scienceTeacher, apiKey: `nvapi-${"do-not-store"}` }),
    /허용되지 않은|secret|민감/,
  );
  assert.throws(
    () => parseTeacherProfile({ ...scienceTeacher, studentName: "학생" }),
    /허용되지 않은|학생|민감/,
  );
});

test("teacher directory rejects secrets and personal contact data", () => {
  assert.throws(
    () => parseTeacherProfile({ ...scienceTeacher, location: "010-1234-5678" }),
    /민감|개인정보|허용되지 않은/,
  );
  assert.throws(
    () => parseTeacherProfile({ ...scienceTeacher, department: "sk-test-secret" }),
    /민감|secret|허용되지 않은/,
  );
});

test("directory import validates duplicate IDs and search is deterministic", () => {
  const document = parseTeacherDirectoryDocument({
    schemaVersion: 1,
    staff: [scienceTeacher, counselor, { ...scienceTeacher, id: "hidden-science", visibility: "internal" }],
  });
  assert.equal(document.staff.length, 3);

  const matches = searchTeachers(document.staff, "과학실 안전 담당 선생님");
  assert.deepEqual(matches.map((teacher) => teacher.id), ["kim-minji"]);
  assert.deepEqual(
    searchTeachers(document.staff, "없는 역할").map((teacher) => teacher.id),
    [],
  );
  assert.throws(
    () => parseTeacherDirectoryDocument({
      schemaVersion: 1,
      staff: [scienceTeacher, scienceTeacher],
    }),
    /중복/,
  );
});

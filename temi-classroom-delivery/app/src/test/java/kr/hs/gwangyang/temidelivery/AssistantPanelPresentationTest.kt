package kr.hs.gwangyang.temidelivery

import kr.hs.gwangyang.temidelivery.aiguide.SchoolAnswer
import kr.hs.gwangyang.temidelivery.aiguide.SupplyGuideSource
import kr.hs.gwangyang.temidelivery.aiguide.TeacherMatch
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantPanelPresentationTest {
    @Test
    fun `NIM school answer shows provider and grounded teacher facts`() {
        val rendered = formatSchoolAnswer(
            SchoolAnswer(
                answer = "진로 상담은 김진로 선생님을 찾아가세요.",
                source = SupplyGuideSource.NVIDIA_NIM,
                model = "deepseek-ai/deepseek-v4-flash-0731",
                warning = null,
                matches = listOf(
                    TeacherMatch(
                        id = "career-teacher",
                        name = "김진로",
                        title = "진로 교사",
                        department = "진로상담부",
                        location = "진로상담실",
                        responsibilities = listOf("진로 상담", "진학 상담"),
                    ),
                ),
            ),
        )

        assertTrue(rendered.contains("NVIDIA NIM"))
        assertTrue(rendered.contains("김진로 · 진로 교사"))
        assertTrue(rendered.contains("진로상담실"))
        assertTrue(rendered.contains("진로 상담"))
        assertFalse(rendered.contains("null"))
    }

    @Test
    fun `teacher fallback is clearly labelled and keeps warning`() {
        val rendered = formatSchoolAnswer(
            SchoolAnswer(
                answer = "등록된 담당자를 찾지 못했습니다.",
                source = SupplyGuideSource.TEACHER_FALLBACK,
                model = null,
                warning = "선생님께 확인해 주세요.",
                matches = emptyList(),
            ),
        )

        assertTrue(rendered.contains("교직원 명단 기반 안내"))
        assertTrue(rendered.contains("선생님께 확인해 주세요."))
        assertFalse(rendered.contains("확인된 담당자"))
    }
}

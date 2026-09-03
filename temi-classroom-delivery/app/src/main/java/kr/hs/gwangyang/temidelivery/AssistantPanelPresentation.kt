package kr.hs.gwangyang.temidelivery

import kr.hs.gwangyang.temidelivery.aiguide.SchoolAnswer
import kr.hs.gwangyang.temidelivery.aiguide.SupplyGuideSource

internal fun formatSchoolAnswer(answer: SchoolAnswer): String = buildString {
    val sourceLabel = when (answer.source) {
        SupplyGuideSource.NVIDIA_NIM ->
            "NVIDIA NIM · ${answer.model ?: "DeepSeek V4 Flash 0731"}"
        SupplyGuideSource.LUNA -> "Luna · ${answer.model ?: "GPT-5.6 Luna"}"
        SupplyGuideSource.TEACHER_FALLBACK -> "교직원 명단 기반 안내"
    }
    appendLine(sourceLabel)
    append(answer.answer)

    if (answer.matches.isNotEmpty()) {
        append("\n\n확인된 담당자")
        answer.matches.forEach { teacher ->
            append("\n• ${teacher.name} · ${teacher.title}")
            teacher.department?.takeIf(String::isNotBlank)?.let { append(" · $it") }
            teacher.location?.takeIf(String::isNotBlank)?.let { append(" · $it") }
            if (teacher.responsibilities.isNotEmpty()) {
                append("\n  담당: ${teacher.responsibilities.joinToString(", ")}")
            }
        }
    }

    answer.warning?.takeIf(String::isNotBlank)?.let { append("\n\n주의: $it") }
}

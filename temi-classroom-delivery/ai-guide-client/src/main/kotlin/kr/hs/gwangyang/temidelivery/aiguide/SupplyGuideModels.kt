package kr.hs.gwangyang.temidelivery.aiguide

data class SupplyGuide(
    val itemId: String,
    val itemName: String,
    val explanation: String,
    val source: SupplyGuideSource,
    val model: String?,
    val warning: String?,
)

enum class SupplyGuideSource {
    NVIDIA_NIM,
    LUNA,
    TEACHER_FALLBACK,
}

data class TeacherMatch(
    val id: String,
    val name: String,
    val title: String,
    val department: String?,
    val location: String?,
    val responsibilities: List<String>,
)

data class SchoolAnswer(
    val answer: String,
    val source: SupplyGuideSource,
    val model: String?,
    val warning: String?,
    val matches: List<TeacherMatch>,
)

data class SpeechTranscript(
    val text: String,
    val model: String,
    val durationMs: Int,
    val warning: String?,
)

data class SupplyGuideClientConfig(
    val enabled: Boolean,
    val baseUrl: String,
    val clientToken: String,
)

interface SupplyGuideGateway {
    suspend fun explain(itemId: String, question: String? = null): Result<SupplyGuide>
}

interface SchoolAnswerGateway {
    suspend fun answer(question: String): Result<SchoolAnswer>
}

interface SpeechTranscriptionGateway {
    suspend fun transcribeWav(wavBytes: ByteArray): Result<SpeechTranscript>
}

class SupplyGuideServiceException(
    val statusCode: Int,
    val errorCode: String,
    message: String,
) : Exception(message)

data class SupplyGuideClientBundle(
    val gateway: SupplyGuideGateway,
    val schoolAnswerGateway: SchoolAnswerGateway,
    val speechTranscriptionGateway: SpeechTranscriptionGateway,
    val enabled: Boolean,
    val statusMessage: String,
)

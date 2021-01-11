package blue.starry.saya.models

import kotlinx.serialization.Serializable

@Serializable
data class Storage(
    val directory: String,
    val size: Long,
    val used: Long,
    val recorded: Long,
    val available: Long
)

@Serializable
data class RecordedProgram(
    val program: Program,
    val path: String,
    val priority: Int,
    val tuner: Tuner?,
    val rule: Rule?,
    val isConflict: Boolean
)

@Serializable
data class RecordingProgram(
    val program: Program
)

@Serializable
data class ReservedProgram(
    val program: Program
)

@Serializable
data class Rule(
    val id: Long,
    val isEnabled: Boolean,
    val name: String,
    val description: String
) {
    enum class Day {
        Sun, Mon, Tue, Wed, Thu, Fri, Sat
    }
}

@Serializable
data class RecordedFile(
    val path: String,
    val size: Long
)

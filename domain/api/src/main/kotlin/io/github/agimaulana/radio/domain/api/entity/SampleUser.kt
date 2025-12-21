package io.github.agimaulana.radio.domain.api.entity

data class SampleUser(
    val id: String,
    val name: String,
    val email: String,
    val isActive: Boolean,
    val education: List<Education>
) {
    data class Education(
        val degree: Degree,
        val institution: String,
        val field: String,
        val year: Int
    ) {

        enum class Degree {
            UNKNOWN, BACHELOR, MASTER, DOCTORATE;
        }
    }
}

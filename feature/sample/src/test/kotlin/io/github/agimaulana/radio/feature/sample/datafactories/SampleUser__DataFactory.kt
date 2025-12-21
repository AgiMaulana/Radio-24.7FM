package io.github.agimaulana.radio.feature.sample.datafactories

import io.github.agimaulana.radio.domain.api.entity.SampleUser

fun newSampleUser(
    withId: String = "",
    withName: String = "",
    withEmail: String = "",
    withIsActive: Boolean = false,
    withEducation: List<SampleUser.Education> = emptyList()
) = SampleUser(
    id = withId,
    name = withName,
    email = withEmail,
    isActive = withIsActive,
    education = withEducation
)

fun newSampleUserEducation(
    withDegree: SampleUser.Education.Degree = SampleUser.Education.Degree.UNKNOWN,
    withInstitution: String = "",
    withField: String = "",
    withYear: Int = 0
) = SampleUser.Education(
    degree = withDegree,
    institution = withInstitution,
    field = withField,
    year = withYear
)
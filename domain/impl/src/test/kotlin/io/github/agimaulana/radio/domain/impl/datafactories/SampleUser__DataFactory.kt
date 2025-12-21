package io.github.agimaulana.radio.domain.impl.datafactories

import io.github.agimaulana.radio.domain.api.entity.SampleUser

fun newSampleUser(
    withId: String = "",
    withName: String = "",
    withEmail: String = "",
    withIsActive: Boolean = false,
    withEducation: List<SampleUser.Education> = emptyList()
): SampleUser = SampleUser(
    id = withId,
    name = withName,
    email = withEmail,
    isActive = withIsActive,
    education = withEducation
)

fun newSampleUserEducation(
    withDegree: SampleUser.Education.Degree = SampleUser.Education.Degree.UNKNOWN,
    withInstitution: String = "",
    withYear: Int = 0,
    withField: String = ""
): SampleUser.Education = SampleUser.Education(
    degree = withDegree,
    institution = withInstitution,
    year = withYear,
    field = withField,
)

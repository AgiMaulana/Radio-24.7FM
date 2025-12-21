package io.github.agimaulana.radio.feature.sample.datafactories

import io.github.agimaulana.radio.feature.sample.SampleViewModel

fun newUiStateMember(
    withName: String = "",
    withEmail: String = ""
) = SampleViewModel.UiState.Member(
    name = withName,
    email = withEmail
)
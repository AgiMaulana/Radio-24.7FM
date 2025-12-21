package io.github.agimaulana.radio.feature.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.agimaulana.radio.domain.api.entity.SampleUser
import io.github.agimaulana.radio.domain.api.usecase.GetSampleUsersUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
    private val getSampleUsersUseCase: GetSampleUsersUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun init() {
        viewModelScope.launch {
            val members = getSampleUsersUseCase.execute().map { it.toMember() }.toPersistentList()
            _uiState.update { it.copy(members = members) }
        }
    }

    private fun SampleUser.toMember() = UiState.Member(name, email)

    data class UiState(
        val members: ImmutableList<Member> = persistentListOf()
    ) {
        data class Member(
            val name: String,
            val email: String,
        )
    }
}

package io.github.agimaulana.radio.feature.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.TopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.agimaulana.radio.feature.sample.SampleViewModel.UiState.Member
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SampleRoute(
    viewModel: SampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    SampleScreen(uiState)
}

@Composable
private fun SampleScreen(
    uiState: SampleViewModel.UiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.safeDrawingPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sample Screen",
                        color = Color.White,
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(uiState.members) { index, member ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(member.name)
                    Text(member.email)
                }

                if (index < uiState.members.lastIndex) {
                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun SampleScreenPreview() {
    SampleScreen(
        uiState = SampleViewModel.UiState(
            members = persistentListOf(
                Member("User 1", "user1@androidacademy.ac.id"),
                Member("User 2", "user2@androidacademy.ac.id"),
                Member("User 3", "user3@androidacademy.ac.id"),
            )
        )
    )
}

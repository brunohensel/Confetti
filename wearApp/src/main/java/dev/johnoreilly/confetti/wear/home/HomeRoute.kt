package dev.johnoreilly.confetti.wear.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.utils.QueryResult

@Composable
fun HomeRoute(
    component: HomeComponent,
    columnState: ScalingLazyColumnState = rememberColumnState(),
) {
    val uiState = component.uiState.collectAsStateWithLifecycle().value
    val bookmarksUiState by component.bookmarksUiState.collectAsStateWithLifecycle()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is QueryResult.Loading && bookmarksUiState !is QueryResult.Loading
        }
    }

    ScreenScaffold(scrollState = columnState) {
        HomeScreen(
            uiState = uiState,
            bookmarksUiState = bookmarksUiState,
            sessionSelected = {
                component.onSessionClicked(it)
            },
            daySelected = {
                component.onDayClicked(it)
            },
            onSettingsClick = {
                component.onSettingsClicked()
            },
            onBookmarksClick = {
                component.onBookmarksClick()
            },
            columnState = columnState,
        )
    }
}

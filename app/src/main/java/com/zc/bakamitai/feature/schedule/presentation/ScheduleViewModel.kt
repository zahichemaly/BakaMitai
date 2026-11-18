package com.zc.bakamitai.feature.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.domain.usecase.GetTodayDateUseCase
import com.zc.bakamitai.common.ui.component.error.mapper.toErrorUIModel
import com.zc.bakamitai.feature.schedule.domain.model.WeeklySchedule
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyScheduleUseCase
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.mapper.toDayChipUIModels
import com.zc.bakamitai.feature.schedule.presentation.mapper.toUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScheduleViewModel(
    private val getWeeklySchedule: GetWeeklyScheduleUseCase,
    private val getTodayDate: GetTodayDateUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    private val _effect = Channel<ScheduleEffect>(Channel.BUFFERED)
    val effect: Flow<ScheduleEffect> = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    /** The last successful load, re-reduced when the day or the filter changes. Never exposed. */
    private var schedule: WeeklySchedule? = null

    init {
        onIntent(ScheduleIntent.Load)
    }

    fun onIntent(intent: ScheduleIntent) {
        when (intent) {
            ScheduleIntent.Load,
            ScheduleIntent.Retry,
            -> load(forceRefresh = false)

            ScheduleIntent.Refresh -> load(forceRefresh = true)

            ScheduleIntent.ScreenStarted -> reloadOnNewDay()

            is ScheduleIntent.DaySelected -> selectDay(intent.day)

            is ScheduleIntent.FilterSelected -> selectFilter(intent.filter)

            ScheduleIntent.ResetFilterClicked -> selectFilter(StatusFilter.ALL)

            is ScheduleIntent.ShowClicked -> sendEffect(ScheduleEffect.OpenDetails(intent.slug))

            ScheduleIntent.SettingsClicked -> sendEffect(ScheduleEffect.OpenSettings)
        }
    }

    /** Ignored while a load is running. With content shown, it refreshes in place; otherwise it loads. */
    private fun load(forceRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        _state.update { it.copy(isLoading = !it.hasContent, isRefreshing = it.hasContent, error = null) }
        loadJob = viewModelScope.launch {
            getWeeklySchedule(forceRefresh = forceRefresh)
                .onSuccess { loaded ->
                    val previous = schedule
                    schedule = loaded
                    _state.update { state ->
                        // A new day (or the first load) selects today; otherwise the selection stays.
                        val isNewDay = previous == null || previous.today != loaded.today
                        val selectedDay = state.selectedDay.takeUnless { isNewDay } ?: loaded.today.dayOfWeek
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                            days = loaded.toDayChipUIModels(),
                            selectedDay = selectedDay,
                        ).withDay(loaded)
                    }
                }
                .onFailure { error ->
                    val errorModel = error.toErrorUIModel()
                    val hadContent = _state.value.hasContent
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = if (state.hasContent) null else errorModel,
                        )
                    }
                    if (hadContent) _effect.send(ScheduleEffect.ShowError(errorModel))
                }
        }
    }

    private fun reloadOnNewDay() {
        val lastToday = schedule?.today ?: return
        if (loadJob?.isActive != true && getTodayDate() != lastToday) load(forceRefresh = false)
    }

    private fun selectDay(day: DayOfWeek) {
        val loaded = schedule ?: return
        _state.update { it.copy(selectedDay = day).withDay(loaded) }
    }

    private fun selectFilter(filter: StatusFilter) {
        val loaded = schedule
        _state.update { state ->
            val filtered = state.copy(statusFilter = filter)
            if (loaded != null) filtered.withDay(loaded) else filtered
        }
    }

    /** Reduces the selected day of [schedule] with the current filter; today when the selection isn't found. */
    private fun ScheduleState.withDay(schedule: WeeklySchedule): ScheduleState {
        val selected = schedule.days.firstOrNull { it.dayOfWeek == selectedDay }
            ?: schedule.days.firstOrNull { it.date == schedule.today }
        return copy(
            selectedDay = selected?.dayOfWeek ?: selectedDay,
            day = selected?.toUIModel(schedule.utcOffset, statusFilter),
        )
    }

    private fun sendEffect(effect: ScheduleEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressListScreenViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchTrackedProgressesFlow(
  fetchData: () -> Flow<List<TrackedProgress>>
): Flow<List<TrackedProgressListScreenViewModel.ProgressModel>> {
  return fetchData().map { list ->
    list.map {
      TrackedProgressListScreenViewModel.ProgressModel(progress = it, isSelected = false)
    }
  }
}
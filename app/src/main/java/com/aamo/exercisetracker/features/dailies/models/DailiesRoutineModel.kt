package com.aamo.exercisetracker.features.dailies.models

import com.aamo.exercisetracker.database.entities.Routine

data class DailiesRoutineModel(val routine: Routine, val progress: Progress) {
  data class Progress(val finishedCount: Int, val totalCount: Int)
}
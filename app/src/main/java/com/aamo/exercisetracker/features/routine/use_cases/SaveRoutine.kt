package com.aamo.exercisetracker.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.RoutineFormViewModel
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.general.ifElse

suspend fun saveRoutine(
  model: RoutineWithSchedule,
  saveData: suspend (RoutineWithSchedule) -> Boolean,
): Boolean {
  return saveData(model)
}

fun RoutineFormViewModel.Model.toDao(routineId: Long): RoutineWithSchedule {
  return RoutineWithSchedule(
    routine = Routine(id = routineId, name = this.routineName),
    schedule = ifElse(condition = this.selectedDays.isNotEmpty(), ifTrue = {
      RoutineSchedule(
        routineId = routineId,
        sunday = this.selectedDays.contains(Day.SUNDAY),
        monday = this.selectedDays.contains(Day.MONDAY),
        tuesday = this.selectedDays.contains(Day.TUESDAY),
        wednesday = this.selectedDays.contains(Day.WEDNESDAY),
        thursday = this.selectedDays.contains(Day.THURSDAY),
        friday = this.selectedDays.contains(Day.FRIDAY),
        saturday = this.selectedDays.contains(Day.SATURDAY),
      )
    }, ifFalse = { null })
  )
}
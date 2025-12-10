package com.aamo.exercisetracker.test_utility.mockers

import com.aamo.exercisetracker.database.entities.RoutineSchedule

class RoutineScheduleMocker {
  var schedule = RoutineSchedule(routineId = 0L)
    private set

  fun modify(schedule: (RoutineSchedule) -> RoutineSchedule): RoutineScheduleMocker {
    this.schedule = schedule(this.schedule)
    return this
  }

  fun setDay(number: Int): RoutineScheduleMocker {
    when (number) {
      1 -> modify { it.copy(sunday = true) }
      2 -> modify { it.copy(monday = true) }
      3 -> modify { it.copy(tuesday = true) }
      4 -> modify { it.copy(wednesday = true) }
      5 -> modify { it.copy(thursday = true) }
      6 -> modify { it.copy(friday = true) }
      7 -> modify { it.copy(saturday = true) }
    }
    return this
  }

  fun mock(): RoutineSchedule {
    return schedule
  }
}
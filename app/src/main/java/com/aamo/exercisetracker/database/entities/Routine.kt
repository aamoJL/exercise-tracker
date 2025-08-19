@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.aamo.exercisetracker.utility.extensions.date.Day

@Entity(tableName = "routine")
data class Routine(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "name") val name: String,
)

@Entity(
  tableName = "routine_schedule", foreignKeys = [ForeignKey(
    entity = Routine::class,
    parentColumns = ["id"],
    childColumns = ["routine_id"],
    onDelete = ForeignKey.CASCADE
  )], indices = [Index(value = ["routine_id"], unique = true)]
)
data class RoutineSchedule(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "routine_id") val routineId: Long,
  @ColumnInfo(name = "sunday") val sunday: Boolean = false,
  @ColumnInfo(name = "monday") val monday: Boolean = false,
  @ColumnInfo(name = "tuesday") val tuesday: Boolean = false,
  @ColumnInfo(name = "wednesday") val wednesday: Boolean = false,
  @ColumnInfo(name = "thursday") val thursday: Boolean = false,
  @ColumnInfo(name = "friday") val friday: Boolean = false,
  @ColumnInfo(name = "saturday") val saturday: Boolean = false,
) {
  fun isDaySelected(dayNumber: Int): Boolean {
    return when (dayNumber) {
      1 -> sunday
      2 -> monday
      3 -> tuesday
      4 -> wednesday
      5 -> thursday
      6 -> friday
      7 -> saturday
      else -> false
    }
  }

  fun asListOfDays(): List<Day> {
    return Day.entries.filter { this.isDaySelected(it.getDayNumber()) }
  }
}

data class RoutineWithSchedule(
  @Embedded val routine: Routine,
  @Relation(entity = RoutineSchedule::class, parentColumn = "id", entityColumn = "routine_id")
  val schedule: RoutineSchedule?
)

data class RoutineWithExerciseProgresses(
  @Embedded val routine: Routine,
  @Relation(entity = Exercise::class, parentColumn = "id", entityColumn = "routine_id")
  val exerciseProgresses: List<ExerciseWithProgress>
)

data class RoutineWithScheduleAndExerciseProgresses(
  @Embedded val routine: Routine,
  @Relation(entity = RoutineSchedule::class, parentColumn = "id", entityColumn = "routine_id")
  val schedule: RoutineSchedule?,
  @Relation(entity = Exercise::class, parentColumn = "id", entityColumn = "routine_id")
  val exerciseProgresses: List<ExerciseWithProgress>
)
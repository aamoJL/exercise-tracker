package com.aamo.exercisetracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import kotlin.time.Duration

@Entity(tableName = "routines")
data class Routine(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "rest_duration") val restDuration: Duration,
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
  @ColumnInfo(name = "sunday") val sunday: Boolean,
  @ColumnInfo(name = "monday") val monday: Boolean,
  @ColumnInfo(name = "tuesday") val tuesday: Boolean,
  @ColumnInfo(name = "wednesday") val wednesday: Boolean,
  @ColumnInfo(name = "thursday") val thursday: Boolean,
  @ColumnInfo(name = "friday") val friday: Boolean,
  @ColumnInfo(name = "saturday") val saturday: Boolean,
)

data class RoutineWithSchedule(
  @Embedded val routine: Routine,
  @Relation(parentColumn = "id", entityColumn = "routine_id") val schedule: RoutineSchedule?
)

@Dao
interface RoutineDao {
  @Query("SELECT * FROM routine_schedule WHERE routine_id = :routineId")
  suspend fun getRoutineScheduleByRoutineId(routineId: Long): RoutineSchedule?

  /**
   * Returns id on insertion, -1 on update
   */
  @Upsert
  suspend fun upsert(routine: Routine): Long

  /**
   * Returns id on insertion, -1 on update
   */
  @Upsert
  suspend fun upsert(schedule: RoutineSchedule): Long

  @Transaction
  suspend fun upsert(routineWithSchedule: RoutineWithSchedule) {
    val (routine, schedule) = routineWithSchedule

    upsert(routine).let {
      val routineId = upsertReturnToId(returnValue = it, oldId = routine.id)

      getRoutineScheduleByRoutineId(routineId)?.let { existingSchedule ->
        if (schedule == null) {
          delete(existingSchedule)
          return // Delete schedule
        }

        upsert(schedule.copy(id = existingSchedule.id, routineId = existingSchedule.routineId))
        return // Update existing schedule
      }

      if (schedule != null) {
        upsert(schedule.copy(id = 0L, routineId = routineId)) // Insert new schedule
      }
    }
  }

  @Delete
  suspend fun delete(routine: Routine)

  @Delete
  suspend fun delete(routineSchedule: RoutineSchedule)

  /**
   * Returns the new id from upsert if the value is not -1, otherwise returns old id
   */
  private fun upsertReturnToId(returnValue: Long, oldId: Long): Long {
    return if (returnValue == -1L) oldId else returnValue
  }
}
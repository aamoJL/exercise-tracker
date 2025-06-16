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
  tableName = "routine_schedules", foreignKeys = [ForeignKey(
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
)

data class RoutineWithSchedule(
  @Embedded val routine: Routine,
  @Relation(parentColumn = "id", entityColumn = "routine_id") val schedule: RoutineSchedule?
)

@Dao
interface RoutineDao {
  @Query("SELECT * FROM routines WHERE id = :routineId")
  suspend fun getRoutine(routineId: Long): Routine?

  @Query("SELECT * FROM routine_schedules WHERE id = :scheduleId")
  suspend fun getSchedule(scheduleId: Long): RoutineSchedule?

  @Query("SELECT * FROM routine_schedules WHERE routine_id = :routineId")
  suspend fun getScheduleByRoutineId(routineId: Long): RoutineSchedule?

  @Query("SELECT * FROM routine_schedules")
  suspend fun getSchedules(): List<RoutineSchedule>

  @Query("SELECT * FROM routines WHERE id = :routineId")
  suspend fun getRoutineWithSchedule(routineId: Long): RoutineWithSchedule

  /**
   * @return id on insertion, -1 on update
   */
  @Upsert
  suspend fun upsert(routine: Routine): Long

  /**
   * @return id on insertion, -1 on update
   */
  @Upsert
  suspend fun upsert(schedule: RoutineSchedule): Long

  /**
   * @return routine ID and schedule ID as a pair
   */
  @Transaction
  suspend fun upsert(routineWithSchedule: RoutineWithSchedule): Pair<Long, Long?> {
    val (routine, schedule) = routineWithSchedule

    // Upsert routine
    val routineId = upsertReturnValueOrOldId(returnValue = upsert(routine), oldId = routine.id)
    var scheduleId = schedule?.id

    getScheduleByRoutineId(routineId)?.let { existingSchedule ->
      // Schedule exists
      scheduleId = if (schedule != null) existingSchedule.id else null

      if (schedule == null) {
        // Delete old schedule if current is null
        delete(existingSchedule)
      }
    }

    if (schedule != null && scheduleId != null) {
      // Upsert schedule
      scheduleId = upsertReturnValueOrOldId(
        returnValue = upsert(schedule.copy(id = scheduleId, routineId = routineId)),
        oldId = routine.id
      )
    }

    return Pair(routineId, scheduleId)
  }

  @Delete
  suspend fun delete(routine: Routine)

  @Delete
  suspend fun delete(routineSchedule: RoutineSchedule)

  /**
   * @return the new id from upsert if the value is not -1, otherwise returns old id
   */
  private fun upsertReturnValueOrOldId(returnValue: Long, oldId: Long): Long {
    return if (returnValue == -1L) oldId else returnValue
  }
}
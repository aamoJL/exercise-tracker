@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
  // region GET
  @Query("SELECT * FROM routines WHERE id = :routineId")
  suspend fun getRoutine(routineId: Long): Routine?

  @Query("SELECT * FROM routine_schedules WHERE routine_id = :routineId")
  suspend fun getScheduleByRoutineId(routineId: Long): RoutineSchedule?

  @Query("SELECT * FROM exercises WHERE id = :exerciseId")
  suspend fun getExercise(exerciseId: Long): Exercise?

  @Query("SELECT * FROM exercise_progress WHERE exercise_id = :exerciseId")
  suspend fun getExerciseProgressByExerciseId(exerciseId: Long): ExerciseProgress?

  @Transaction
  @Query("SELECT * FROM routines WHERE id = :routineId")
  suspend fun getRoutineWithSchedule(routineId: Long): RoutineWithSchedule?

  @Transaction
  @Query("SELECT * FROM routines")
  fun getRoutinesWithScheduleFlow(): Flow<List<RoutineWithSchedule>>

  @Transaction
  @Query("SELECT * FROM routines")
  fun getRoutinesWithScheduleAndProgressesFlow(): Flow<List<RoutineWithScheduleAndExerciseProgresses>>

  @Transaction
  @Query("SELECT * FROM exercises WHERE id = :exerciseId")
  suspend fun getExerciseWithSets(exerciseId: Long): ExerciseWithSets?

  @Transaction
  @Query("SELECT * FROM routines WHERE id = :routineId")
  fun getRoutineWithProgressesFlow(routineId: Long): Flow<RoutineWithExerciseProgresses?>

  @Transaction
  @Query("SELECT * FROM exercises WHERE id = :exerciseId")
  suspend fun getExerciseWithProgressAndSets(exerciseId: Long): ExerciseWithProgressAndSets?
  // endregion

  // region UPSERT
  @Upsert
  suspend fun upsert(routine: Routine): Long

  @Upsert
  suspend fun upsert(schedule: RoutineSchedule): Long

  @Upsert
  suspend fun upsert(exercise: Exercise): Long

  @Upsert
  suspend fun upsert(vararg exerciseSet: ExerciseSet)

  @Upsert
  suspend fun upsert(exerciseProgress: ExerciseProgress): Long

  /**
   * @return exerciseId
   */
  @Transaction
  suspend fun upsert(exerciseWithSets: ExerciseWithSets): Long {
    val (exercise, sets) = exerciseWithSets

    // Upsert exercise
    val exerciseId = upsert(exercise).letIf({ it < 0L }) { exercise.id }

    getExerciseWithSets(exerciseId)?.let { existingEws ->
      // Delete removed sets
      delete(existingEws.sets.filter { !sets.contains(it) })
    }

    // Upsert sets
    upsert(*sets.map { it.copy(exerciseId = exerciseId) }.toTypedArray())

    return exerciseId
  }

  /**
   * @return routine ID and schedule ID as a pair
   */
  @Transaction
  suspend fun upsert(routineWithSchedule: RoutineWithSchedule): Pair<Long, Long?> {
    val (routine, schedule) = routineWithSchedule

    // Upsert routine
    val routineId = upsert(routine).letIf({ it < 0L }) { routine.id }

    //val routineId = upsertReturnValueOrOldId(returnValue = upsert(routine), oldId = routine.id)
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
      scheduleId = upsert(
        schedule.copy(id = scheduleId, routineId = routineId)
      ).letIf({ it < 0L }) { schedule.id }
    }

    return Pair(routineId, scheduleId)
  }

  @Transaction
  suspend fun upsertAndGet(routineWithSchedule: RoutineWithSchedule): RoutineWithSchedule? {
    return getRoutineWithSchedule(upsert(routineWithSchedule).first)
  }
  // endregion

  // region DELETE
  @Delete
  suspend fun delete(routine: Routine): Int

  @Delete
  suspend fun delete(routineSchedule: RoutineSchedule)

  @Delete
  suspend fun delete(exercise: Exercise): Int

  @Delete
  suspend fun delete(exerciseSets: List<ExerciseSet>)
  // endregion
}
@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RoutineDao {
  // region GET
  @Query("SELECT * FROM routine WHERE id = :routineId")
  suspend fun getRoutine(routineId: Long): Routine?

  @Query("SELECT * FROM routine_schedule WHERE routine_id = :routineId")
  suspend fun getScheduleByRoutineId(routineId: Long): RoutineSchedule?

  @Query("SELECT * FROM exercise WHERE id = :exerciseId")
  suspend fun getExercise(exerciseId: Long): Exercise?

  @Query("SELECT * FROM exercise_set WHERE exercise_id = :exerciseId")
  suspend fun getExerciseSets(exerciseId: Long): List<ExerciseSet>

  @Transaction
  @Query("SELECT * FROM exercise WHERE id = :exerciseId")
  suspend fun getExerciseWithSets(exerciseId: Long): ExerciseWithSets?

  @Query("SELECT * FROM exercise_progress WHERE exercise_id = :exerciseId")
  suspend fun getExerciseProgressByExerciseId(exerciseId: Long): ExerciseProgress?

  @Transaction
  @Query("SELECT * FROM routine WHERE id = :routineId")
  suspend fun getRoutineWithSchedule(routineId: Long): RoutineWithSchedule?

  @Transaction
  @Query("SELECT * FROM routine")
  fun getRoutinesWithScheduleFlow(): Flow<List<RoutineWithSchedule>>

  @Transaction
  @Query("SELECT * FROM routine WHERE id = :routineId")
  fun getRoutineWithProgressesFlow(routineId: Long): Flow<RoutineWithExerciseProgresses?>

  @Transaction
  @Query(
    """
    SELECT routine.*, progress.finished_date
    FROM routine 
    JOIN exercise ON exercise.routine_id = routine.id
    LEFT OUTER JOIN exercise_progress AS progress ON progress.exercise_id = exercise.id
  """
  )
  fun getRoutineScheduleWithProgressFlow(): Flow<Map<RoutineWithSchedule, List<@MapColumn(
    columnName = "finished_date", tableName = "progress"
  ) Date?>>>

  @Transaction
  @Query("SELECT * FROM exercise WHERE id = :exerciseId")
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
  suspend fun upsert(exercise: Exercise, sets: List<ExerciseSet>): Long {
    // Upsert exercise
    val exerciseId = upsert(exercise).letIf({ it == -1L }) { exercise.id }

    // Delete old sets
    delete(*getExerciseSets(exerciseId).toTypedArray())

    // Upsert new sets
    upsert(*sets.map { it.copy(exerciseId = exerciseId) }.toTypedArray())

    return exerciseId
  }

  /**
   * @return routine ID and schedule ID as a pair
   */
  @Transaction
  suspend fun upsert(routine: Routine, schedule: RoutineSchedule): Pair<Long, Long> {
    val routine = upsert(routine).let { id -> routine.letIf(id != -1L) { routine.copy(id = id) } }
    val schedule = upsert(schedule.copy(routineId = routine.id)).let { id ->
      schedule.letIf(id != -1L) { schedule.copy(id = id) }
    }

    return Pair(routine.id, schedule.id)
  }
  // endregion

  // region DELETE
  @Delete
  suspend fun delete(vararg routine: Routine): Int

  @Delete
  suspend fun delete(routineSchedule: RoutineSchedule)

  @Delete
  suspend fun delete(exercise: Exercise): Int

  @Delete
  suspend fun delete(vararg exerciseSet: ExerciseSet)
  // endregion
}
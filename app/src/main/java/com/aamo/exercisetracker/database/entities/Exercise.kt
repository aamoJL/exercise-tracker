package com.aamo.exercisetracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// region Entities
@Entity(
  tableName = "exercises", foreignKeys = [ForeignKey(
    entity = Routine::class,
    parentColumns = ["id"],
    childColumns = ["routine_id"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class Exercise(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "routine_id") val routineId: Long = 0,
  @ColumnInfo(name = "name") val name: String = String.EMPTY,
  @ColumnInfo(name = "rest_duration") val restDuration: Duration = 0.minutes,
)

@Entity(
  tableName = "exercise_sets", foreignKeys = [ForeignKey(
    entity = Exercise::class,
    parentColumns = ["id"],
    childColumns = ["exercise_id"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class ExerciseSet(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "exercise_id") val exerciseId: Long = 0,
  @ColumnInfo(name = "value") val value: Int = 0,
  @ColumnInfo(name = "unit") val unit: String = "reps",
  @ColumnInfo(name = "value_type", defaultValue = "0")
  val valueType: ValueType = ValueType.REPETITION,
) {
  enum class ValueType(val id: Int) {
    REPETITION(id = 0),
    COUNTDOWN(id = 1)
  }
}

@Entity(
  tableName = "exercise_progress", foreignKeys = [ForeignKey(
    entity = Exercise::class,
    parentColumns = ["id"],
    childColumns = ["exercise_id"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class ExerciseProgress(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "exercise_id") val exerciseId: Long = 0,
  @ColumnInfo(name = "finished_date") val finishedDate: Date,
)
// endregion

data class ExerciseWithSets(
  @Embedded val exercise: Exercise,
  @Relation(parentColumn = "id", entityColumn = "exercise_id") val sets: List<ExerciseSet>
)

data class ExerciseWithProgress(
  @Embedded val exercise: Exercise,
  @Relation(parentColumn = "id", entityColumn = "exercise_id") val progress: ExerciseProgress?,
)

data class ExerciseWithProgressAndSets(
  @Embedded val exercise: Exercise,
  @Relation(parentColumn = "id", entityColumn = "exercise_id") val sets: List<ExerciseSet>,
  @Relation(parentColumn = "id", entityColumn = "exercise_id") val progress: ExerciseProgress?,
)
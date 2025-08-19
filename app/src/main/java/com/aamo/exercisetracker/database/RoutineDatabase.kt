package com.aamo.exercisetracker.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.aamo.exercisetracker.database.converters.DateConverter
import com.aamo.exercisetracker.database.converters.DurationConverter
import com.aamo.exercisetracker.database.converters.ExerciseSetValueTypeConverter
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineDao
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgressValue

@Database(
  version = RoutineDatabase.Properties.VERSION,
  entities = [Routine::class, RoutineSchedule::class, Exercise::class, ExerciseSet::class, ExerciseProgress::class, TrackedProgress::class, TrackedProgressValue::class],
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
    AutoMigration(from = 2, to = 3),
    AutoMigration(from = 3, to = 4),
    AutoMigration(
      from = 4, to = 5, spec = RoutineDatabase.VersionFourToFiveAutoMigrationSpec::class
    ),
  ],
)
@TypeConverters(
  DurationConverter::class, DateConverter::class, ExerciseSetValueTypeConverter::class
)
abstract class RoutineDatabase : RoomDatabase() {
  object Properties {
    const val VERSION = 5
  }

  abstract fun routineDao(): RoutineDao
  abstract fun trackedProgressDao(): TrackedProgressDao

  companion object {
    @Suppress("HardCodedStringLiteral") private const val DATABASE_NAME = "routine_database"

    @Volatile private var Instance: RoutineDatabase? = null

    fun getDatabase(applicationContext: Context): RoutineDatabase {
      return Instance ?: synchronized(this) {
        Room.databaseBuilder(
          context = applicationContext, klass = RoutineDatabase::class.java, name = DATABASE_NAME
        ).build().also { Instance = it }
      }
    }
  }

  @Suppress("HardCodedStringLiteral")
  @RenameTable.Entries(
    RenameTable(fromTableName = "exercises", toTableName = "exercise"),
    RenameTable(fromTableName = "exercise_sets", toTableName = "exercise_set"),
    RenameTable(fromTableName = "routines", toTableName = "routine"),
    RenameTable(fromTableName = "routine_schedules", toTableName = "routine_schedule"),
    RenameTable(fromTableName = "tracked_progress_values", toTableName = "tracked_progress_value"),
  )
  class VersionFourToFiveAutoMigrationSpec : AutoMigrationSpec
}
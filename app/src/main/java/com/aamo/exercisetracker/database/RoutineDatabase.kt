package com.aamo.exercisetracker.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aamo.exercisetracker.BuildConfig
import com.aamo.exercisetracker.database.converters.DateConverter
import com.aamo.exercisetracker.database.converters.DurationConverter
import com.aamo.exercisetracker.database.converters.ExerciseSetValueTypeConverter
import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.database.migrations.VersionFourToFiveAutoMigrationSpec

@Database(
  version = RoutineDatabase.Properties.VERSION,
  entities = [Routine::class, RoutineSchedule::class, Exercise::class, ExerciseSet::class, ExerciseProgress::class, TrackedProgress::class, TrackedProgressValue::class],
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
    AutoMigration(from = 2, to = 3),
    AutoMigration(from = 3, to = 4),
    AutoMigration(from = 4, to = 5, spec = VersionFourToFiveAutoMigrationSpec::class),
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
        val builder =
          Room.databaseBuilder(applicationContext, RoutineDatabase::class.java, DATABASE_NAME)

        // Allow main thread queries on debug build so unit tests can clear the database tables after execution
        if (BuildConfig.DEBUG) {
          builder.allowMainThreadQueries()
        }

        builder.build()
      }.also { Instance = it }
    }
  }
}
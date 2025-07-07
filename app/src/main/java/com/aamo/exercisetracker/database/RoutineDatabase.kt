package com.aamo.exercisetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aamo.exercisetracker.database.converters.DateConverter
import com.aamo.exercisetracker.database.converters.DurationConverter
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineDao
import com.aamo.exercisetracker.database.entities.RoutineSchedule

@Database(
  version = RoutineDatabase.Properties.VERSION,
  entities = [Routine::class, RoutineSchedule::class, Exercise::class, ExerciseSet::class, ExerciseProgress::class],
  autoMigrations = [],
)
@TypeConverters(DurationConverter::class, DateConverter::class)
abstract class RoutineDatabase : RoomDatabase() {
  object Properties {
    const val VERSION = 1
  }

  abstract fun routineDao(): RoutineDao

  companion object {
    private const val DATABASE_NAME = "routine_database"

    @Volatile private var Instance: RoutineDatabase? = null

    fun getDatabase(applicationContext: Context): RoutineDatabase {
      return Instance ?: synchronized(this) {
        Room.databaseBuilder(
          context = applicationContext, klass = RoutineDatabase::class.java, name = DATABASE_NAME
        ).build().also { Instance = it }
      }
    }
  }
}
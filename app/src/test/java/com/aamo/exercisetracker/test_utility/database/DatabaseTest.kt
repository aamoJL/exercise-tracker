package com.aamo.exercisetracker.test_utility.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import org.junit.After
import org.junit.Before
import java.io.IOException

abstract class DatabaseTest {
  protected lateinit var database: RoutineDatabase
  protected lateinit var routineDao: RoutineDao
  protected lateinit var trackedProgressDao: TrackedProgressDao

  @Before
  open fun setup() {
    database = Room.inMemoryDatabaseBuilder(
      context = ApplicationProvider.getApplicationContext(), klass = RoutineDatabase::class.java
    ).build()
    routineDao = database.routineDao()
    trackedProgressDao = database.trackedProgressDao()
  }

  @After
  @Throws(IOException::class)
  open fun cleanup() {
    database.close()
  }
}
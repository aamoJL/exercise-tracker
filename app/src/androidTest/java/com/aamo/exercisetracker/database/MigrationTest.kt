@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
  private object Constants {
    const val TEST_DB = "migration-test"
  }

  @get:Rule val helper: MigrationTestHelper = MigrationTestHelper(
    instrumentation = InstrumentationRegistry.getInstrumentation(),
    databaseClass = RoutineDatabase::class.java
  )

  @Test
  @Throws(IOException::class)
  fun migrateAll() {
    // Create earliest version of the database.
    helper.createDatabase(Constants.TEST_DB, 1).apply {
      close()
    }

    for (version in 1..RoutineDatabase.Properties.VERSION) {
      helper.runMigrationsAndValidate(Constants.TEST_DB, version, true)
    }
  }
}
package com.aamo.exercisetracker.database.migrations

import androidx.room.RenameTable
import androidx.room.migration.AutoMigrationSpec

@Suppress("HardCodedStringLiteral")
@RenameTable.Entries(
  RenameTable(fromTableName = "exercises", toTableName = "exercise"),
  RenameTable(fromTableName = "exercise_sets", toTableName = "exercise_set"),
  RenameTable(fromTableName = "routines", toTableName = "routine"),
  RenameTable(fromTableName = "routine_schedules", toTableName = "routine_schedule"),
  RenameTable(fromTableName = "tracked_progress_values", toTableName = "tracked_progress_value"),
)
class VersionFourToFiveAutoMigrationSpec : AutoMigrationSpec
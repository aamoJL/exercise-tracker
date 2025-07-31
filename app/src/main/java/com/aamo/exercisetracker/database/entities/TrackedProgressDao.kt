@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database.entities

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedProgressDao {
  @Query("SELECT * FROM tracked_progress")
  fun getProgressesFlow(): Flow<List<TrackedProgress>>
}
@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedProgressDao {
  @Query("SELECT * FROM tracked_progress WHERE id = :id")
  suspend fun getTrackedProgress(id: Long): TrackedProgress?

  @Query("SELECT * FROM tracked_progress")
  fun getProgressesFlow(): Flow<List<TrackedProgress>>

  @Upsert
  suspend fun upsert(trackedProgress: TrackedProgress): Long

  @Delete
  suspend fun delete(trackedProgress: TrackedProgress): Int
}
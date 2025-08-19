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

  @Query("SELECT * FROM tracked_progress_values WHERE tracked_progress_id = :progressId")
  fun getProgressValuesFlow(progressId: Long): Flow<List<TrackedProgressValue>>

  @Query("SELECT * FROM tracked_progress_values WHERE id = :valueId")
  suspend fun getProgressValueById(valueId: Long): TrackedProgressValue?

  @Query("SELECT * FROM tracked_progress LEFT JOIN tracked_progress_values ON tracked_progress_id = tracked_progress.id")
  fun getProgressesWithValuesFlow(): Flow<Map<TrackedProgress, List<TrackedProgressValue>>>

  @Upsert
  suspend fun upsert(trackedProgress: TrackedProgress): Long

  @Upsert
  suspend fun upsert(trackedProgressValue: TrackedProgressValue): Long

  @Delete
  suspend fun delete(trackedProgress: TrackedProgress): Int

  @Query("DELETE FROM tracked_progress_values WHERE id = :trackedProgressId")
  suspend fun deleteValueById(trackedProgressId: Long)
}
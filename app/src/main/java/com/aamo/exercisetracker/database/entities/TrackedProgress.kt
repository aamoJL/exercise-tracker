@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import java.util.Date

// region Entities
@Entity(tableName = "tracked_progress")
data class TrackedProgress(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "name") val name: String = String.EMPTY,
  @ColumnInfo(name = "interval_weeks") val intervalWeeks: Int = 1,
  @ColumnInfo(name = "unit") val unit: String = String.EMPTY,
  @ColumnInfo(name = "has_stopwatch") val hasStopWatch: Boolean = false,
  @ColumnInfo(name = "timer_time") val timerTime: Long? = null
)

@Entity(
  tableName = "tracked_progress_value", foreignKeys = [ForeignKey(
    entity = TrackedProgress::class,
    parentColumns = ["id"],
    childColumns = ["tracked_progress_id"],
    onDelete = ForeignKey.CASCADE
  )], indices = [Index(value = ["tracked_progress_id"], unique = false)]
)
data class TrackedProgressValue(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "tracked_progress_id") val progressId: Long,
  @ColumnInfo(name = "value") val value: Int = 0,
  @ColumnInfo(name = "added_date") val addedDate: Date,
)
// endregion
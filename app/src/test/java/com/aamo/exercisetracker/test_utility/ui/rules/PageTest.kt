package com.aamo.exercisetracker.test_utility.ui.rules

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.MainActivity
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.utility.tags.UITag
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.text.SimpleDateFormat

open class PageTest {
  lateinit var routineDao: RoutineDao
  lateinit var trackedProgressDao: TrackedProgressDao

  @get:Rule val rule = createAndroidComposeRule<MainActivity>()

  @Before
  open fun setup() {
    routineDao = RoutineDatabase.getDatabase(rule.activity).routineDao()
    trackedProgressDao = RoutineDatabase.getDatabase(rule.activity).trackedProgressDao()
  }

  @After
  open fun cleanup() {
    RoutineDatabase.getDatabase(rule.activity.applicationContext).clearAllTables()
  }

  fun getString(@StringRes id: Int): String {
    return rule.activity.getString(id)
  }

  fun getString(@StringRes id: Int, param: String): String {
    return rule.activity.getString(id, param)
  }

  fun getString(@StringRes id: Int, param: Int): String {
    return rule.activity.getString(id, param)
  }

  suspend fun waitForLoading() {
    val progressElement = rule.onNodeWithTag(UITag.PROGRESS_INDICATOR.name)

    while (progressElement.isDisplayed()) yield()
  }

  suspend fun toRoutineListScreen(routine: Routine): Routine {
    rule.onNodeWithText(getString(R.string.label_routines)).performClick()
    waitForLoading()

    val insert = routineDao.upsert(routine).let { routine.copy(id = it) }

    rule.onNodeWithText(insert.name).waitForDisplayed()
    return insert
  }

  suspend fun toRoutineFormScreen(model: RoutineWithSchedule? = null): RoutineWithSchedule {
    rule.onNodeWithText(getString(R.string.label_routines)).performClick()
    waitForLoading()

    if (model != null) {
      val insert = routineDao.upsert(model.routine, model.schedule).let { (routineId, scheduleId) ->
        model.copy(
          routine = model.routine.copy(id = routineId),
          schedule = model.schedule?.copy(id = scheduleId ?: 0L, routineId = routineId)
        )
      }

      rule.onNodeWithText(insert.routine.name).waitForDisplayed().performClick()
      waitForLoading()
      rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).performClick()
      return insert
    }
    else {
      rule.onNodeWithContentDescription(getString(R.string.cd_add_routine)).performClick()
      return RoutineWithSchedule(routine = Routine(), schedule = RoutineSchedule(routineId = 0L))
    }
  }

  suspend fun toTrackedProgressListScreen(progress: TrackedProgress): TrackedProgress {
    rule.onNodeWithText(getString(R.string.label_progress)).performClick()
    waitForLoading()

    val insert = trackedProgressDao.upsert(progress).let { progress.copy(id = it) }

    rule.onNodeWithText(insert.name).waitForDisplayed()
    return insert
  }

  suspend fun toTrackedProgressFormScreen(model: TrackedProgress? = null): TrackedProgress {
    rule.onNodeWithText(getString(R.string.label_progress)).performClick()
    waitForLoading()

    if (model != null) {
      val insert = trackedProgressDao.upsert(model).let { id -> model.copy(id = id) }

      rule.onNodeWithText(insert.name).waitForDisplayed().performClick()
      waitForLoading()
      rule.onNodeWithContentDescription(getString(R.string.cd_edit_tracked_progress)).performClick()
      return insert
    }
    else {
      rule.onNodeWithContentDescription(getString(R.string.cd_add_tracked_progress)).performClick()
      return TrackedProgress()
    }
  }

  suspend fun toProgressTrackingScreen(progress: TrackedProgress): TrackedProgress {
    rule.onNodeWithText(getString(R.string.label_progress)).performClick()
    waitForLoading()

    val insert = trackedProgressDao.upsert(progress).let { progress.copy(id = it) }

    rule.onNodeWithText(insert.name).waitForDisplayed().performClick()
    return insert
  }

  suspend fun toTrackedProgressRecordListScreen(
    progress: TrackedProgress, record: TrackedProgressValue
  ): TrackedProgressWithValues {
    val progressInsert = toProgressTrackingScreen(progress)
    val recordInsert = record.copy(progressId = progressInsert.id).let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }
    waitForLoading()

    rule.onNodeWithContentDescription(getString(R.string.cd_show_records)).assertIsEnabled()
      .performClick()
    waitForLoading()

    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(recordInsert.addedDate))
      .waitForDisplayed()

    return TrackedProgressWithValues(
      trackedProgress = progressInsert, values = listOf(recordInsert)
    )
  }

  suspend fun toRoutineScreen(routine: Routine): Routine {
    val routineInsert = toRoutineListScreen(routine)
    waitForLoading()

    rule.onNodeWithText(routineInsert.name).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).waitForDisplayed()

    return routineInsert
  }
}
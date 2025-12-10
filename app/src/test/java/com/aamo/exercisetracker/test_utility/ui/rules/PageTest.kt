package com.aamo.exercisetracker.test_utility.ui.rules

import androidx.annotation.StringRes
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.aamo.exercisetracker.MainActivity
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.utility.tags.UITag
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Before
import org.junit.Rule

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

  suspend fun waitForLoading() {
    val progressElement = rule.onNodeWithTag(UITag.PROGRESS_INDICATOR.name)

    while (progressElement.isDisplayed()) yield()
  }
}
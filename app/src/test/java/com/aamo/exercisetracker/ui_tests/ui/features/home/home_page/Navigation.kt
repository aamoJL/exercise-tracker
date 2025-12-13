package com.aamo.exercisetracker.ui_tests.ui.features.home.home_page

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    waitForLoading()
  }

  @Test
  fun `on dailiesScreen`() = runTest {
    rule.onNodeWithText(getString(Day.today().nameResourceKey)).assertExists()
  }

  @Test
  fun `back always returns to dailiesScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.label_routines)).performClick()
    waitForLoading()
    rule.onNodeWithText(getString(R.string.label_progress)).performClick()
    waitForLoading()

    rule.activity.onBackPressedDispatcher.onBackPressed()
    waitForLoading()

    rule.onNodeWithText(getString(Day.today().nameResourceKey)).assertExists()
  }

  @Test
  fun `to routineListScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.label_routines)).performClick()
    waitForLoading()

    val routine = Routine(name = "123").also {
      routineDao.upsert(it)
    }

    rule.onNodeWithText(routine.name).waitForDisplayed().assertExists()
  }

  @Test
  fun `to trackedProgressListScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.label_progress)).performClick()
    waitForLoading()

    val progress = TrackedProgress(name = "123").also {
      trackedProgressDao.upsert(it)
    }

    rule.onNodeWithText(progress.name).waitForDisplayed().assertExists()
  }

  @Test
  fun `to dailiesScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.label_progress)).performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.label_dailies)).performClick()
    waitForLoading()

    `on dailiesScreen`()
  }
}
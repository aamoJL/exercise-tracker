package com.aamo.exercisetracker.test_utility.ui.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

abstract class UnconfinedTest {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  open fun setup() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  open fun after() {
    Dispatchers.resetMain()
  }
}
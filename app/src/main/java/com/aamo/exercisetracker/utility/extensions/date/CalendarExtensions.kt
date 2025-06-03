package com.aamo.exercisetracker.utility.extensions.date

import java.util.Calendar

fun Calendar.getLocalDayOfWeek(): Int {
  return (this.get(Calendar.DAY_OF_WEEK) - this.firstDayOfWeek).mod(7) + 1
}

/**
 * Returns list of days ordered by the user's locale
 */
fun Calendar.getLocalListOfDays(): List<String> {
  // List of days in the order of the Calendar class
  val listOfDays =
    listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
  // First day of the week of the user's locale
  val firstDayOfWeekIndex = this.firstDayOfWeek - 1

  if (firstDayOfWeekIndex == 0) {
    return listOfDays
  }

  val afterLocalFirstDay = listOfDays.slice(0..firstDayOfWeekIndex - 1)
  val beforeLocalFistDay = listOfDays.slice(firstDayOfWeekIndex..listOfDays.count() - 1)

  return beforeLocalFistDay.plus(afterLocalFirstDay)
}
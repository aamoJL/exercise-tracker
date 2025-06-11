package com.aamo.exercisetracker.utility.extensions.date

import androidx.annotation.StringRes
import com.aamo.exercisetracker.R
import java.util.Calendar

enum class Day(@StringRes val nameResourceKey: Int) {
  SUNDAY(R.string.sunday),
  MONDAY(R.string.monday),
  TUESDAY(R.string.tuesday),
  WEDNESDAY(R.string.wednesday),
  THURSDAY(R.string.thursday),
  FRIDAY(R.string.friday),
  SATURDAY(R.string.saturday);

  /**
   * Returns the number of the Day object
   */
  fun getDayNumber(): Int {
    return Day.entries.indexOf(this) + 1
  }

  companion object {
    /**
     * Returns the Day class object of the given day number,
     * Sunday being the day number 1
     */
    fun getByDayNumber(dayNumber: Int): Day {
      return Day.entries[dayNumber - 1]
    }
  }
}

/**
 * Returns list of days ordered by the user's locale
 */
fun Calendar.getLocalDayOrder(): List<Day> {
  // List of days in the order of the Calendar class
  val listOfDays = Day.entries
  // First day of the week of the user's locale
  val firstDayOfWeekIndex = this.firstDayOfWeek - 1

  if (firstDayOfWeekIndex == 0) {
    return listOfDays
  }

  val afterLocalFirstDay = listOfDays.slice(0..firstDayOfWeekIndex - 1)
  val beforeLocalFistDay = listOfDays.slice(firstDayOfWeekIndex..listOfDays.count() - 1)

  return beforeLocalFistDay.plus(afterLocalFirstDay)
}

fun Calendar.today(): Int {
  return this.get(Calendar.DAY_OF_WEEK)
}
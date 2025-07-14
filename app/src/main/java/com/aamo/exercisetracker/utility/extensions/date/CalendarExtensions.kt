package com.aamo.exercisetracker.utility.extensions.date

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.aamo.exercisetracker.R
import java.util.Calendar

@Keep
enum class Day(@param:StringRes val nameResourceKey: Int) {
  SUNDAY(R.string.day_sunday),
  MONDAY(R.string.day_monday),
  TUESDAY(R.string.day_tuesday),
  WEDNESDAY(R.string.day_wednesday),
  THURSDAY(R.string.day_thursday),
  FRIDAY(R.string.day_friday),
  SATURDAY(R.string.day_saturday);

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

    fun today(): Day {
      return entries.elementAt(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
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
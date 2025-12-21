package com.aamo.exercisetracker.features.routine.form.models

import com.aamo.exercisetracker.utility.extensions.date.Day

data class RoutineFormFields(val name: String, val days: List<Day>)
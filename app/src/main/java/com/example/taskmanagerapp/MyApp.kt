package com.example.taskmanagerapp

import android.app.Application
import android.os.Build
import com.example.taskmanagerapp.model.database.AppDatabase
import com.example.taskmanagerapp.model.database.TaskRepository
import com.google.android.material.color.DynamicColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyApp : Application() {
	lateinit var repository: TaskRepository
		private set

	override fun onCreate() {
		super.onCreate()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			DynamicColors.applyToActivitiesIfAvailable(this)
		}

		val database = AppDatabase.getDatabase(this)
		repository = TaskRepository(database.taskDao())
	}
}

object AdditionalFunction {

	fun getFormattedTime(time: Long): String {
		val dateFormat = "dd/MM/yyyy"
		val instant = Instant.ofEpochMilli(time)
		val zonedDateTime = instant.atZone(ZoneId.systemDefault())
		return DateTimeFormatter.ofPattern(dateFormat).format(zonedDateTime)
	}

}

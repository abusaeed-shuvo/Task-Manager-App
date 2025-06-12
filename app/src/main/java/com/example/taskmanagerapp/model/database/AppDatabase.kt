package com.example.taskmanagerapp.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.taskmanagerapp.model.data.Task

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
	abstract fun taskDao(): TaskDao
}
package com.example.taskmanagerapp.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskmanagerapp.model.data.Task

@Dao
interface TaskDao {
	@Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
	fun addTask(task: Task)

	@Update
	fun updateTask(task: Task)

	@Delete
	fun deleteTask(task: Task)

	@Query("SELECT * FROM task")
	fun getAllTask(): List<Task>

}
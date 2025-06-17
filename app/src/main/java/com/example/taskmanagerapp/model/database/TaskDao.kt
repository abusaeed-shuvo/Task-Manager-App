package com.example.taskmanagerapp.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskmanagerapp.model.data.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
	@Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
	suspend fun addTask(task: Task)

	@Update
	suspend fun updateTask(task: Task)

	@Delete
	suspend fun deleteTask(task: Task)

	@Query("SELECT * FROM task")
	fun getAllTask(): Flow<List<Task>>


}
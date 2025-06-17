package com.example.taskmanagerapp.model.database

import com.example.taskmanagerapp.model.data.Task

class TaskRepository(private val taskDao: TaskDao) {
	val allTasks = taskDao.getAllTask()

	suspend fun insert(task: Task) {
		taskDao.addTask(task)
	}

	suspend fun update(task: Task) {
		taskDao.updateTask(task)
	}

	suspend fun delete(task: Task) {
		taskDao.deleteTask(task)
	}
}
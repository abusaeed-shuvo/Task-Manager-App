package com.example.taskmanagerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagerapp.model.database.TaskRepository

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST") return TaskViewModel(repository) as T
		}
		throw IllegalArgumentException("UNKNOWN VIEWMODEL CLASS")
	}
}
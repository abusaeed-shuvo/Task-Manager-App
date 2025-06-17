package com.example.taskmanagerapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerapp.model.data.Task
import com.example.taskmanagerapp.model.database.TaskRepository
import com.example.taskmanagerapp.model.enums.TaskSortTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
	private val _selectedTask = MutableLiveData<Task?>()
	private val _sortType = MutableStateFlow(TaskSortTypes.DUE_DATE)

	val allTasks = repository.allTasks
	val selectedTask = _selectedTask

	val sortedTask = combine(allTasks, _sortType) { allTasks, sort ->

		when (sort) {
			TaskSortTypes.TITLE             -> allTasks.sortedBy { it.title }
			TaskSortTypes.TITLE_REVERSED    -> allTasks.sortedByDescending { it.title }
			TaskSortTypes.DUE_DATE          -> allTasks.sortedByDescending { it.dueDate }
			TaskSortTypes.DUE_DATE_REVERSED -> allTasks.sortedBy { it.dueDate }
		}
	}.asLiveData()


	fun insert(task: Task) = viewModelScope.launch { repository.insert(task) }
	fun update(task: Task) = viewModelScope.launch { repository.update(task) }
	fun delete(task: Task) = viewModelScope.launch { repository.delete(task) }


	fun setSortType(type: TaskSortTypes) {
		_sortType.value = type
	}

	fun selectTask(task: Task) {
		_selectedTask.value = task
	}

	fun clearSelection() {
		_selectedTask.value = null
	}


}
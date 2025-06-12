package com.example.taskmanagerapp.view.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.ActivityMainBinding
import com.example.taskmanagerapp.databinding.DialogTaskBinding
import com.example.taskmanagerapp.model.data.Task
import com.example.taskmanagerapp.model.database.AppDatabase
import com.example.taskmanagerapp.model.database.TaskDao
import com.example.taskmanagerapp.model.enums.TaskSortTypes
import com.example.taskmanagerapp.view.adapter.TaskListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), TaskListAdapter.ClickHandler {
	private lateinit var binding: ActivityMainBinding
	private lateinit var adapter: TaskListAdapter
	private lateinit var taskDao: TaskDao

	private var isSelectionMode = false
	private var currentSortType = TaskSortTypes.DUE_DATE


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val db = Room.databaseBuilder(
			applicationContext, AppDatabase::class.java, "Task-DB"
		).allowMainThreadQueries().build()

		taskDao = db.taskDao()


		setupRecyclerviewWithAdapter()

		updateTitleBar()
		setupSortMenu(taskDao.getAllTask())

		binding.inputSearch.editText?.doAfterTextChanged { editText ->
			val allTasks = taskDao.getAllTask()
			val input = editText.toString()
			val filteredTasks = if (input.isNotEmpty()) {
				allTasks.filter {
					it.title.contains(input, ignoreCase = true) || it.description.contains(
						input,
						ignoreCase = true
					)
				}
			} else allTasks

			adapter.updateList(getSortedTasks(filteredTasks, currentSortType))

			checkEmptyState(filteredTasks)
		}


		binding.apply {
			btnAddOrDeleteTask.setOnClickListener {
				if (!isSelectionMode) {
					val addTaskIntent = Intent(this@MainActivity, AddTaskActivity::class.java)
					startActivity(addTaskIntent)
				} else {
					val selectedTasks = adapter.getSelectedTasks()
					lifecycleScope.launch {
						for (task in selectedTasks) {
							taskDao.deleteTask(task)
						}
					}

					adapter.clearSelection()
					val updatedList = getSortedTasks(taskDao.getAllTask(), currentSortType)
					adapter.updateList(updatedList)
					checkEmptyState(updatedList)
					updateTitleBar()
				}
			}
		}

		onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				if (isSelectionMode) {
					adapter.clearSelection()
					isSelectionMode = false
					updateTitleBar()
				} else {
					MaterialAlertDialogBuilder(binding.root.context).setTitle("Exit:")
						.setMessage("Do you want to exit?").setPositiveButton("Yes") { _, _ ->
							finishAffinity()
						}.setNeutralButton("No", null).show()
				}
			}
		})
	}

	private fun setupSortMenu(allTasks: List<Task>) {
		val sortOptions = TaskSortTypes.entries.map { it.value }
		val dropdownView = binding.dropdownSortMenu

		val dropdownAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sortOptions)
		dropdownView.setAdapter(dropdownAdapter)

		val defaultSort = TaskSortTypes.DUE_DATE
		val sortedList = getSortedTasks(allTasks, defaultSort)
		dropdownView.setText(defaultSort.value, false)
		adapter.updateList(sortedList)
		checkEmptyState(sortedList)

		dropdownView.setOnItemClickListener { _, _, position, _ ->
			val selectedSort = TaskSortTypes.entries[position]
			currentSortType = selectedSort
			val sortedList = getSortedTasks(allTasks, currentSortType)
			adapter.updateList(sortedList)
			checkEmptyState(sortedList)
		}

	}

	private fun getSortedTasks(allTasks: List<Task>, sortType: TaskSortTypes): List<Task> {

		return when (sortType) {
			TaskSortTypes.TITLE             -> allTasks.sortedBy { it.title }
			TaskSortTypes.TITLE_REVERSED    -> allTasks.sortedByDescending { it.title }
			TaskSortTypes.DUE_DATE          -> allTasks.sortedByDescending { it.dueDate }
			TaskSortTypes.DUE_DATE_REVERSED -> allTasks.sortedBy { it.dueDate }
		}
	}

	private fun checkEmptyState(taskList: List<Task>) {
		binding.tvNoVisibleData.visibility = if (taskList.isEmpty()) View.VISIBLE else View.GONE
		binding.rcvTaskList.visibility = if (taskList.isEmpty()) View.GONE else View.VISIBLE
	}

	private fun setupRecyclerviewWithAdapter() {
		val taskList = taskDao.getAllTask()
		adapter = TaskListAdapter(taskList, this)
		binding.rcvTaskList.adapter = adapter
		binding.rcvTaskList.layoutManager = LinearLayoutManager(this)

		checkEmptyState(taskList)
	}

	private fun showTaskDialog(task: Task, taskDao: TaskDao) {
		val bindingDialog = DialogTaskBinding.inflate(layoutInflater)
		val dialog = MaterialAlertDialogBuilder(this).setView(bindingDialog.root).create()

		bindingDialog.apply {
			dialogTaskTitle.text = task.title
			dialogTaskDescription.text = task.description
			dialogTaskDueDate.text = getFormattedTime(task.dueDate)
			dialogTaskStatus.text = if (task.isCompleted) "Completed" else "Incomplete"
			ivStatusIcon.setImageDrawable(
				ContextCompat.getDrawable(
					this@MainActivity,
					if (task.isCompleted) R.drawable.ic_check else R.drawable.ic_timer
				)
			)

			btnDialogEdit.setOnClickListener {
				Intent(this@MainActivity, AddTaskActivity::class.java).putExtra("task", task).also {
					startActivity(it)
				}
				dialog.dismiss()
			}
			btnDialogChangeStatus.setOnClickListener {
				lifecycleScope.launch {
					task.isCompleted = !task.isCompleted
					taskDao.updateTask(task)
					updateTitleBar()
				}
				val updatedList = getSortedTasks(taskDao.getAllTask(), currentSortType)
				adapter.updateList(updatedList)
				checkEmptyState(updatedList)
				dialogTaskStatus.text = if (task.isCompleted) "Completed" else "Incomplete"
				ivStatusIcon.setImageDrawable(
					ContextCompat.getDrawable(
						this@MainActivity,
						if (task.isCompleted) R.drawable.ic_check else R.drawable.ic_timer
					)
				)
			}

		}

		dialog.show()

	}

	private fun getFormattedTime(time: Long): String {
		val dateFormat = "MMM, dd, yyyy"
		val instant = Instant.ofEpochMilli(time)
		val zonedDateTime = instant.atZone(ZoneId.systemDefault())
		return DateTimeFormatter.ofPattern(dateFormat).format(zonedDateTime)
	}

	override fun onItemClick(task: Task) {

		if (isSelectionMode) {
			adapter.toggleSelection(task)
			updateTitleBar()
		} else {
			showTaskDialog(task, taskDao)
		}

	}

	override fun onLongClick(task: Task) {

		if (!isSelectionMode) {
			isSelectionMode = true
		}
		adapter.toggleSelection(task)
		updateTitleBar()
	}

	private fun updateTitleBar() {
		val selectedCount = adapter.getSelectedTasks().size

		if (isSelectionMode && selectedCount == 0) {
			isSelectionMode = false
		}


		binding.topAppBar.text = if (isSelectionMode && selectedCount > 0) {
			"$selectedCount selected"
		} else {
			"All Tasks"
		}

		binding.btnAddOrDeleteTask.apply {
			text = if (isSelectionMode) "Delete Task" else "Add Task"
			icon = ContextCompat.getDrawable(
				binding.root.context,
				if (isSelectionMode) R.drawable.ic_delete else R.drawable.ic_add
			)
		}

	}

}
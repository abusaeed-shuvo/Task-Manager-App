package com.example.taskmanagerapp.view.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.taskmanagerapp.databinding.ActivityAddTaskBinding
import com.example.taskmanagerapp.model.data.Task
import com.example.taskmanagerapp.model.database.AppDatabase
import com.example.taskmanagerapp.model.database.TaskDao
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddTaskActivity : AppCompatActivity() {
	private lateinit var binding: ActivityAddTaskBinding
	private lateinit var initTask: Task
	private lateinit var currentTips: List<String>

	private var isEditingMode = false
	private var dateTime: Long = System.currentTimeMillis()
	private var currentTip = ""


	private val tipsAdd = listOf(
		"Keep titles under 20 characters.",
		"Use a clear description of the task.",
		"Choose a realistic due date.",
		"You can edit this later if needed."
	)

	private val tipsEdit = listOf(
		"Make only necessary changes.",
		"Avoid overwriting useful info.",
		"Set a new due date if needed."
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAddTaskBinding.inflate(layoutInflater)
		setContentView(binding.root)


		val db = Room.databaseBuilder(
			applicationContext, AppDatabase::class.java, "Task-DB"
		).allowMainThreadQueries().build()

		val taskDao = db.taskDao()

		if (intent.hasExtra("task")) {
			val task = intent.getParcelableExtra<Task>("task")
			task?.let {
				initTask = it
				dateTime = it.dueDate
				binding.apply {
					inputTaskTitle.editText?.setText(it.title)
					inputTaskDescription.editText?.setText(it.description)
					btnPickDueDate.text = getFormattedTime(it.dueDate)
					btnSaveOrUpdateTask.text = "Update Task"
				}
				binding.topAppBar.text = "Edit Task"
			}
			isEditingMode = true
		} else {
			binding.btnSaveOrUpdateTask.text = "Save Task"
			binding.topAppBar.text = "Add Task"
		}

		currentTips = if (isEditingMode) tipsEdit else tipsAdd
		currentTip = currentTips.random()
		binding.tvTip.text = currentTip

		binding.apply {
			//Date Picker Dialog logic
			btnPickDueDate.setOnClickListener {
				val datePicker = MaterialDatePicker.Builder.datePicker().setSelection(dateTime)
					.setTitleText("Select Due Date").build()

				datePicker.addOnPositiveButtonClickListener { selectedDateInLong ->
					dateTime = selectedDateInLong
					btnPickDueDate.text = getFormattedTime(selectedDateInLong)
				}
				datePicker.show(supportFragmentManager, "DATE_PICKER")
			}


			//Save or Update button logic
			btnSaveOrUpdateTask.setOnClickListener {
				val titleStr = inputTaskTitle.editText?.text.toString().trim()
				val descriptionStr = inputTaskDescription.editText?.text.toString().trim()

				if (titleStr.isEmpty() || descriptionStr.isEmpty()) {
					inputTaskTitle.error =
						if (titleStr.isEmpty()) "No blank Entry Allowed" else null
					inputTaskDescription.error =
						if (descriptionStr.isEmpty()) "No blank empty allowed" else null
					return@setOnClickListener
				}

				if (titleStr.length > 20 || descriptionStr.length > 300) {
					inputTaskTitle.error =
						if (titleStr.length > 20) "Title can't be longer than 20 character!" else null
					inputTaskDescription.error =
						if (descriptionStr.length > 300) "Description can't be longer than 300 character!" else null
					return@setOnClickListener
				}

				saveOrUpdateTask(taskDao, titleStr, descriptionStr)
			}

			//Input layout Logic:
			inputTaskTitle.editText?.doOnTextChanged { input, _, _, _ ->
				inputTaskTitle.error = if (input.isNullOrEmpty()) "No blank entry allowed" else null
			}
			inputTaskDescription.editText?.doOnTextChanged { input, _, _, _ ->
				inputTaskDescription.error =
					if (input.isNullOrEmpty()) "No blank entry allowed" else null
			}


			//back button handler
			topAppBarIcon.setOnClickListener {
				goBack()

			}

			btnNextTip.setOnClickListener {
				val newTip = currentTips.filterNot { it == currentTip }.random()
				currentTip = newTip
				tvTip.text = newTip
			}
			btnHideTip.setOnClickListener {
				cardTip.visibility = View.GONE
			}
		}

		//when back pressed
		onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				goBack()
			}

		})
	}

	private fun saveOrUpdateTask(taskDao: TaskDao, titleStr: String, descriptionStr: String) {
		lifecycleScope.launch {
			if (isEditingMode) {
				val task = Task(
					initTask.id, titleStr, descriptionStr, dateTime, false
				)
				taskDao.updateTask(task)
			} else {
				val task = Task(0, titleStr, descriptionStr, dateTime, false)
				taskDao.addTask(task)
			}
		}
		val intentThis = Intent(this@AddTaskActivity, MainActivity::class.java)
		startActivity(intentThis)
	}

	private fun getFormattedTime(time: Long): String {
		val dateFormat = "dd/MM/yyyy"
		val instant = Instant.ofEpochMilli(time)
		val zonedDateTime = instant.atZone(ZoneId.systemDefault())
		return DateTimeFormatter.ofPattern(dateFormat).format(zonedDateTime)
	}

	private fun goBack() {
		MaterialAlertDialogBuilder(this@AddTaskActivity).setTitle("Do you want to exit?")
			.setMessage("No changes will be saved!").setPositiveButton("yes") { dialog, _ ->
				finish()
			}.setNeutralButton("No", null).show()
	}

}
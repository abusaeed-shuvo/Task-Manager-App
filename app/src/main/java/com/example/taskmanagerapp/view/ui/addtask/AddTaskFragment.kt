package com.example.taskmanagerapp.view.ui.addtask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.taskmanagerapp.AdditionalFunction.getFormattedTime
import com.example.taskmanagerapp.MyApp
import com.example.taskmanagerapp.databinding.FragmentAddTaskBinding
import com.example.taskmanagerapp.model.data.Task
import com.example.taskmanagerapp.viewmodel.TaskViewModel
import com.example.taskmanagerapp.viewmodel.TaskViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch


class AddTaskFragment : Fragment() {
	private var _binding: FragmentAddTaskBinding? = null
	private val binding get() = _binding!!
	private var isEditingMode = false
	private var dateTime = System.currentTimeMillis()


	private lateinit var currentTips: List<String>
	private lateinit var currentTip: String


	private val viewModel: TaskViewModel by activityViewModels {
		TaskViewModelFactory((requireActivity().application as MyApp).repository)
	}


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentAddTaskBinding.inflate(inflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				setupUI()
				setupListeners()

			}
		}


	}


	private fun setupUI() {
		val selectedTask = viewModel.selectedTask.value
		val activity = requireActivity() as AppCompatActivity

		if (selectedTask != null) {
			binding.inputTaskTitle.editText?.setText(selectedTask.title)
			binding.inputTaskDescription.editText?.setText(selectedTask.description)
			binding.btnPickDueDate.text = getFormattedTime(selectedTask.dueDate)
			binding.btnSaveOrUpdateTask.text = "Update Task"
			activity.title = "Edit Task"
			isEditingMode = true
		} else {
			binding.btnSaveOrUpdateTask.text = "Save Task"
			activity.title = "Add Task"
			isEditingMode = false
		}

	}

	private fun setupListeners() = binding.apply {
		val selectedTask = viewModel.selectedTask.value


		btnPickDueDate.setOnClickListener {
			val datePicker =
				MaterialDatePicker.Builder.datePicker().setSelection(dateTime)
					.setTitleText("Select Due Date").build()

			datePicker.addOnPositiveButtonClickListener { selectedDate ->
				dateTime = selectedDate
				btnPickDueDate.text = getFormattedTime(selectedDate)
			}
			datePicker.show(parentFragmentManager, "DATE_PICKER")
		}

		btnSaveOrUpdateTask.setOnClickListener {
			val titleStr = inputTaskTitle.editText?.text.toString().trim()
			val descStr = inputTaskDescription.editText?.text.toString().trim()

			if (titleStr.isEmpty() || descStr.isEmpty()) {
				inputTaskTitle.error =
					if (titleStr.isEmpty()) "Title cannot be empty!" else null
				inputTaskDescription.error =
					if (descStr.isEmpty()) "Description cannot be empty!" else null
				return@setOnClickListener
			}

			if (titleStr.length > 20 || descStr.length > 300) {
				inputTaskTitle.error =
					if (titleStr.length > 20) "Title cannot be longer than 20 character" else null
				inputTaskDescription.error =
					if (descStr.length > 300) "Description cannot be longer than 300" else null
				return@setOnClickListener
			}

			val task = if (isEditingMode && selectedTask != null) {

				selectedTask.copy(
					title = titleStr, description = descStr, dueDate = dateTime
				)
			} else {
				Task(0, titleStr, descStr, dateTime, false)
			}
			saveOrUpdateTask(task)

		}

		inputTaskTitle.editText?.doOnTextChanged { input, _, _, _ ->
			inputTaskTitle.error =
				if (input.isNullOrEmpty()) "No blank entry allowed" else null
			inputTaskTitle.error =
				if (input.toString().length > 20) "Title cannot be longer than 20 character" else null

		}
		inputTaskDescription.editText?.doOnTextChanged { input, _, _, _ ->
			inputTaskDescription.error =
				if (input.isNullOrEmpty()) "No blank entry allowed" else null
			inputTaskDescription.error =
				if (input.toString().length > 300) "Description cannot be longer than 300" else null
		}

	}


	private fun saveOrUpdateTask(task: Task) {
		if (isEditingMode) {
			viewModel.update(task)
			Toast.makeText(requireContext(), "Task updated!", Toast.LENGTH_SHORT).show()

		} else {
			viewModel.insert(task)
			Toast.makeText(requireContext(), "Task Saved!", Toast.LENGTH_SHORT).show()
		}

		findNavController().popBackStack()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
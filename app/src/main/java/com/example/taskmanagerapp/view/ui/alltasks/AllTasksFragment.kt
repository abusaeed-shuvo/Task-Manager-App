package com.example.taskmanagerapp.view.ui.alltasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagerapp.AdditionalFunction.getFormattedTime
import com.example.taskmanagerapp.MyApp
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.DialogTaskBinding
import com.example.taskmanagerapp.databinding.FragmentAllTasksBinding
import com.example.taskmanagerapp.model.data.Task
import com.example.taskmanagerapp.model.enums.TaskSortTypes
import com.example.taskmanagerapp.view.adapter.TaskListAdapter
import com.example.taskmanagerapp.viewmodel.TaskViewModel
import com.example.taskmanagerapp.viewmodel.TaskViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class AllTasksFragment : Fragment() {
	private var _binding: FragmentAllTasksBinding? = null
	private val binding get() = _binding!!

	private lateinit var adapter: TaskListAdapter
	private var isSelectionMode = false

	private val viewModel: TaskViewModel by activityViewModels {
		TaskViewModelFactory((requireActivity().application as MyApp).repository)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentAllTasksBinding.inflate(inflater, container, false)
		// Inflate the layout for this fragment
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupAdapter()
		setupObservers()
		setupUI()



		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					if (isSelectionMode) {
						isSelectionMode = false
						adapter.clearSelection()
						updateToolbarTitle()
					} else {
						isEnabled = false
						requireActivity().onBackPressedDispatcher.onBackPressed()
					}
				}
			}

		)
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(
			if (isSelectionMode) R.menu.task_selection_menu else R.menu.all_task_menu,
			menu
		)

		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.action_add_task -> {
				viewModel.clearSelection()
				findNavController().navigate(R.id.action_allTasksFragment_to_addTaskFragment)

				true
			}

			R.id.action_delete -> {
				deleteSelectedTasks()
				true
			}

			R.id.action_clear_selection -> {

				adapter.clearSelection()
				isSelectionMode = false
				updateToolbarTitle()
				true
			}

			else -> super.onOptionsItemSelected(item)

		}

	}

	private fun setupAdapter() {
		adapter = TaskListAdapter(onItemClick = { task ->
			if (isSelectionMode) {
				adapter.toggleSelection(task)
				updateToolbarTitle()
			} else {
				viewModel.selectTask(task)
				showTaskDialog(task, viewModel)
			}
		}, onLongClick = { task ->
			if (!isSelectionMode) isSelectionMode = true
			adapter.toggleSelection(task)
			updateToolbarTitle()
		})

		binding.rcvTaskList.adapter = adapter
		binding.rcvTaskList.layoutManager = LinearLayoutManager(requireContext())
	}

	private fun setupObservers() {
		viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
			if (loading) {
				binding.loadingIndicator.visibility = View.VISIBLE
				binding.rcvTaskList.visibility = View.GONE
			} else {
				binding.loadingIndicator.visibility = View.GONE
				binding.rcvTaskList.visibility = View.VISIBLE
			}
		}
		viewModel.sortedTask.observe(viewLifecycleOwner) { tasks ->
			adapter.submitList(tasks)
			checkEmptyState(tasks)
			updateToolbarTitle()
		}


	}

	fun View.fadeIn() {
		animate().alpha(1f).setDuration(200).withStartAction {
			visibility = View.VISIBLE
		}
	}

	fun View.fadeOut() {
		animate().alpha(1f).setDuration(200).withEndAction {
			visibility = View.GONE
		}
	}

	fun onSearchQueryChanged(query: String) {
		viewModel.setSearchQuery(query)
	}

	fun onSortTypeChanged(sortType: TaskSortTypes) {
		viewModel.setSortType(sortType)
	}

	private fun deleteSelectedTasks() {
		val selected = adapter.getSelectedTasks()
		val msg = "Delete ${selected.size} tasks?"
		MaterialAlertDialogBuilder(requireContext()).setMessage(msg)
			.setPositiveButton("Confirm") { _, _ ->
				selected.forEach { viewModel.delete(it) }
				adapter.clearSelection()
				isSelectionMode = false
				updateToolbarTitle()
			}.setNegativeButton("Dismiss") { dialog, _ ->
				adapter.clearSelection()
				updateToolbarTitle()
				dialog.dismiss()
			}.show()
	}

	private fun setupUI() {


		binding.btnAddOrDeleteTask.setOnClickListener {
			viewModel.clearSelection()
			findNavController().navigate(R.id.action_allTasksFragment_to_addTaskFragment)

		}

		binding.btnCancelSelection.setOnClickListener {
			adapter.clearSelection()
			isSelectionMode = false
			updateToolbarTitle()
		}

		binding.btnDeleteTask.setOnClickListener {
			deleteSelectedTasks()

		}

	}


	private fun updateToolbarTitle() {
		val selectedCount = adapter.getSelectedTasks().size
		val activity = requireActivity() as AppCompatActivity

		activity.invalidateMenu()
		if (isSelectionMode && selectedCount > 0) {
			activity.supportActionBar?.title = "$selectedCount Selected"
			binding.btnAddOrDeleteTask.visibility = View.GONE
			binding.btnTaskContainer.visibility = View.VISIBLE

		} else {
			isSelectionMode = false
			activity.supportActionBar?.title = "All Tasks"
			binding.btnAddOrDeleteTask.visibility = View.VISIBLE
			binding.btnTaskContainer.visibility = View.GONE
		}


	}

	private fun checkEmptyState(tasks: List<Task>) {
		binding.tvNoVisibleData.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
		binding.rcvTaskList.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
	}

	private fun showTaskDialog(task: Task, viewModel: TaskViewModel) {
		val bindingDialog = DialogTaskBinding.inflate(layoutInflater)
		val dialog =
			MaterialAlertDialogBuilder(requireContext()).setView(bindingDialog.root).create()

		bindingDialog.apply {
			dialogTaskTitle.text = task.title
			dialogTaskDescription.text = task.description
			dialogTaskDueDate.text = getFormattedTime(task.dueDate)
			dialogTaskStatus.text = if (task.isCompleted) "Completed" else "Incomplete"
			ivStatusIcon.setImageDrawable(
				ContextCompat.getDrawable(
					requireActivity(),
					if (task.isCompleted) R.drawable.ic_check else R.drawable.ic_timer
				)
			)

			btnDialogEdit.setOnClickListener {
				findNavController().navigate(R.id.action_allTasksFragment_to_addTaskFragment)
				dialog.dismiss()
			}
			btnDialogChangeStatus.setOnClickListener {
				lifecycleScope.launch {
					task.isCompleted = !task.isCompleted
					viewModel.update(task)
					updateToolbarTitle()
				}

				val updatedList = viewModel.sortedTask
				adapter.submitList(updatedList.value)
				checkEmptyState(updatedList.value)
				dialogTaskStatus.text = if (task.isCompleted) "Completed" else "Incomplete"
				ivStatusIcon.setImageDrawable(
					ContextCompat.getDrawable(
						requireActivity(),
						if (task.isCompleted) R.drawable.ic_check else R.drawable.ic_timer
					)
				)
			}

		}

		dialog.show()

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
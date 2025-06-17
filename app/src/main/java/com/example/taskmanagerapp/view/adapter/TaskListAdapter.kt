package com.example.taskmanagerapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagerapp.AdditionalFunction.getFormattedTime
import com.example.taskmanagerapp.databinding.ItemListTaskBinding
import com.example.taskmanagerapp.model.data.Task

class TaskListAdapter(

	val onItemClick: (task: Task) -> Unit,
	val onLongClick: (task: Task) -> Unit
) : ListAdapter<Task, TaskListAdapter.TaskListViewHolder>(TaskDiffCallback()) {


	private val selectedTaskIds = mutableSetOf<Int>()

	fun getSelectedTasks() = currentList.filter { selectedTaskIds.contains(it.id) }

	fun clearSelection() {
		selectedTaskIds.clear()
		notifyDataSetChanged()
	}

	fun toggleSelection(task: Task) {
		if (selectedTaskIds.contains(task.id)) {
			selectedTaskIds.remove(task.id)
		} else {
			selectedTaskIds.add(task.id)
		}
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): TaskListViewHolder {
		return TaskListViewHolder(
			ItemListTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	}

	override fun onBindViewHolder(
		holder: TaskListViewHolder,
		position: Int
	) {
		val task = getItem(position)
		holder.bind(task)
	}


	inner class TaskListViewHolder(private val binding: ItemListTaskBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(task: Task) = with(binding) {
			tvTaskTitle.text = task.title
			tvTaskDescription.text = task.description
			tvTaskDueDate.text = "Due: ${getFormattedTime(task.dueDate)}"
			tvTaskStatus.text = if (task.isCompleted) "Completed" else "Incomplete"

			root.isChecked = selectedTaskIds.contains(task.id)

			root.setOnClickListener {
				onItemClick(task)
			}
			root.setOnLongClickListener {
				onLongClick(task)
				true
			}

		}
	}

	class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
		override fun areItemsTheSame(
			oldItem: Task,
			newItem: Task
		): Boolean = oldItem.id == newItem.id

		override fun areContentsTheSame(
			oldItem: Task,
			newItem: Task
		): Boolean = oldItem == newItem

	}


}
package com.example.taskmanagerapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagerapp.databinding.ItemListTaskBinding
import com.example.taskmanagerapp.model.data.Task
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskListAdapter(

	var taskList: List<Task>, val clickHandler: ClickHandler

) : RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>() {
	interface ClickHandler {
		fun onItemClick(task: Task)
		fun onLongClick(task: Task)
	}

	private val selectedTaskIds = mutableSetOf<Int>()

	fun getSelectedTasks() = taskList.filter { selectedTaskIds.contains(it.id) }

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
		parent: ViewGroup, viewType: Int
	): TaskListViewHolder {
		return TaskListViewHolder(
			ItemListTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)
	}

	override fun onBindViewHolder(
		holder: TaskListViewHolder, position: Int
	) {
		val task = taskList[position]
		holder.bind(task)
	}

	override fun getItemCount(): Int = taskList.size


	fun updateList(newList: List<Task>) {
		taskList = newList
		notifyDataSetChanged()
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
				clickHandler.onItemClick(task)
			}
			root.setOnLongClickListener {
				clickHandler.onLongClick(task)
				true
			}

		}
	}

	private fun getFormattedTime(time: Long): String {
		val dateFormat = "dd/MM/yyyy"
		val instant = Instant.ofEpochMilli(time)
		val zonedDateTime = instant.atZone(ZoneId.systemDefault())
		return DateTimeFormatter.ofPattern(dateFormat).format(zonedDateTime)
	}
}
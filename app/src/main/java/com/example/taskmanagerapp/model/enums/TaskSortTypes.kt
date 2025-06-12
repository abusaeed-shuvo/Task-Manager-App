package com.example.taskmanagerapp.model.enums

enum class TaskSortTypes(val value: String) {
	TITLE("Title (A-Z)"),
	TITLE_REVERSED("Title (Z-A)"),
	DUE_DATE("Due Date (Newest)"),
	DUE_DATE_REVERSED("Due Date (Oldest)")
}
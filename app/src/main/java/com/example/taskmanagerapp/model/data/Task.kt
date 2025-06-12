package com.example.taskmanagerapp.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Task(
	@PrimaryKey(autoGenerate = true) val id: Int,
	@ColumnInfo(name = "title") var title: String,
	@ColumnInfo(name = "description") var description: String,
	@ColumnInfo(name = "due_date") var dueDate: Long,
	@ColumnInfo(name = "completed") var isCompleted: Boolean
) : Parcelable

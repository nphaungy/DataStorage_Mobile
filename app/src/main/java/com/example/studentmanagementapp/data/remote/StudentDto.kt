package com.example.studentmanagementapp.data.remote

import com.example.studentmanagementapp.data.local.StudentEntity
import com.example.studentmanagementapp.data.local.SyncStatus
import com.google.gson.annotations.SerializedName

data class StudentDto(
    val id: Int,
    @SerializedName("student_code") val studentCode: String,
    val name: String,
    val major: String
)

data class StudentRequest(
    @SerializedName("student_code") val studentCode: String,
    val name: String,
    val major: String
)

fun StudentDto.toEntity() = StudentEntity(
    serverId = id,
    studentCode = studentCode,
    name = name,
    major = major,
    syncStatus = SyncStatus.SYNCED
)
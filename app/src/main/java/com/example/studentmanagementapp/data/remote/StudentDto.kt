package com.example.studentmanagementapp.data.remote

import com.example.studentmanagementapp.model.Student
import com.google.gson.annotations.SerializedName

data class StudentDto(
    val id: Int,
    @SerializedName("student_code") val studentCode: String,
    val name: String,
    val major: String,
)
data class StudentCreateRequest(
    @SerializedName("student_code") val studentCode: String,
    val name: String,
    val major: String,
)
fun StudentDto.toEntity() = Student(
    id = id,
    studentCode = studentCode,
    name = name,
    major = major
)
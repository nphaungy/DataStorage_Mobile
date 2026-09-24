package com.example.studentmanagementapp.data.remote

import com.example.studentmanagementapp.model.Student
import retrofit2.http.GET

interface StudentApi {

    @GET("student.json")
    suspend fun getStudents(): List<Student>
}

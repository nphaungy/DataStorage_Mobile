package com.example.studentmanagementapp.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StudentApi {
    @GET("students")
    suspend fun  getStudents(): List<StudentDto>
    @POST("students")
    suspend fun createStudent(@Body student: StudentCreateRequest): StudentDto
    @PUT("students/{id}")
    suspend fun updateStudent(
        @Path("id") id: Int,
        @Body student: StudentCreateRequest
    ): StudentDto
    @DELETE("students/{id}")
    suspend fun deleteStudent(@Path("id")id : Int)
}

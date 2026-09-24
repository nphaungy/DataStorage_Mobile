package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.model.Student
import kotlin.coroutines.cancellation.CancellationException

data class StudentLoadResult(
    val students: List<Student>,
    val fromNetwork: Boolean
)

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi
) {

    suspend fun getStudents(): StudentLoadResult {
        return try {
            val remoteStudents = api.getStudents()
            dao.insertAll(remoteStudents)
            StudentLoadResult(dao.getAllStudents(), fromNetwork = true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            StudentLoadResult(dao.getAllStudents(), fromNetwork = false)
        }
    }

    suspend fun getLocalStudents(): List<Student> = dao.getAllStudents()

    suspend fun addStudent(student: Student) = dao.insertStudent(student)

    suspend fun updateStudent(student: Student) = dao.updateStudent(student)

    suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)
}

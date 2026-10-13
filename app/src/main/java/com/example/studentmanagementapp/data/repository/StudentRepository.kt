package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.StudentCreateRequest
import com.example.studentmanagementapp.data.remote.toEntity
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
            val remote = api.getStudents().map {it.toEntity()}
            dao.deleteAll()
            dao.insertAll(remote)
            StudentLoadResult(dao.getAllStudents(), fromNetwork = true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            StudentLoadResult(dao.getAllStudents(), fromNetwork = false)
        }
    }

    suspend fun getLocalStudents(): List<Student> = dao.getAllStudents()

    suspend fun addStudent(code: String, name: String, major: String) {
        api.createStudent(StudentCreateRequest(code, name, major))
    }

    suspend fun updateStudent(student: Student) {
        api.updateStudent(
            student.id,
            StudentCreateRequest(student.studentCode, student.name, student.major)
        )
    }

    suspend fun deleteStudent(student: Student) {
        api.deleteStudent(student.id)
    }
}

package com.example.studentmanagementapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.studentmanagementapp.data.local.AppDatabase
import com.example.studentmanagementapp.data.remote.RetrofitClient
import com.example.studentmanagementapp.data.repository.StudentRepository
import com.example.studentmanagementapp.model.Student
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class StudentViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    var students by mutableStateOf(listOf<Student>())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isOnline by mutableStateOf(false)          // Exercise 4
        private set
    var searchQuery by mutableStateOf("")          // Exercise 3
        private set
    var selectedStudent by mutableStateOf<Student?>(null)  // Bonus
        private set

    // Exercise 3: คำนวณจาก state สองตัว ทุกครั้งที่ UI อ่าน
    val filteredStudents: List<Student>
        get() {
            val q = searchQuery.trim()
            if (q.isEmpty()) return students
            return students.filter {
                it.studentCode.contains(q, ignoreCase = true) ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.major.contains(q, ignoreCase = true)
            }
        }

    fun loadStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.getStudents()
                students = result.students
                isOnline = result.fromNetwork
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = "ไม่สามารถโหลดข้อมูลได้"
            } finally {
                isLoading = false
            }
        }
    }

    // Exercise 1
    fun addStudent(code: String, name: String, major: String) {
        viewModelScope.launch {
            try {
                repository.addStudent(
                    Student(studentCode = code.trim(), name = name.trim(), major = major.trim())
                )
                students = repository.getLocalStudents()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = "ไม่สามารถบันทึกข้อมูลได้"
            }
        }
    }

    // Exercise 2
    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                students = repository.getLocalStudents()
                if (selectedStudent?.id == student.id) selectedStudent = null
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = "ไม่สามารถลบข้อมูลได้"
            }
        }
    }

    // Bonus: Edit
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.updateStudent(student)
                students = repository.getLocalStudents()
                selectedStudent = student
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = "ไม่สามารถแก้ไขข้อมูลได้"
            }
        }
    }

    fun onSearchQueryChange(query: String) { searchQuery = query }

    fun selectStudent(student: Student?) { selectedStudent = student }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val db = AppDatabase.getDatabase(app)
                StudentViewModel(StudentRepository(db.studentDao(), RetrofitClient.api))
            }
        }
    }
}

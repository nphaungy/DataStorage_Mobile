package com.example.studentmanagementapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studentmanagementapp.model.Student
import com.example.studentmanagementapp.viewmodel.StudentViewModel

@Composable
fun StudentScreen(
    modifier: Modifier = Modifier,
    viewModel: StudentViewModel = viewModel(factory = StudentViewModel.Factory)
) {
    // ยังไม่เคยกดโหลด = ยังไม่รู้สถานะออนไลน์
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Student Management",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Exercise 4: สถานะออนไลน์ / ออฟไลน์
        if (hasLoaded && !viewModel.isLoading) {
            item { OnlineStatus(isOnline = viewModel.isOnline) }
        }

        item {
            Button(
                onClick = {
                    hasLoaded = true
                    viewModel.loadStudents()
                },
                enabled = !viewModel.isLoading
            ) {
                Text("Load Students")
            }
        }

        viewModel.errorMessage?.let { message ->
            item {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }

        // Exercise 1: เพิ่มนักศึกษา
        item {
            AddStudentForm(onAdd = viewModel::addStudent)
        }

        // Exercise 3: ค้นหา
        item {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("ค้นหา (รหัส / ชื่อ / สาขา)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (viewModel.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            val list = viewModel.filteredStudents
            if (list.isEmpty() && viewModel.searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "ไม่พบนักศึกษาที่ตรงกับ \"${viewModel.searchQuery}\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(list, key = { it.id }) { student ->
                StudentRow(
                    student = student,
                    onClick = { viewModel.selectStudent(student) },
                    onDelete = { viewModel.deleteStudent(student) }
                )
            }
        }

        item { Box(modifier = Modifier.padding(bottom = 16.dp)) }
    }

    // Bonus: แก้ไขนักศึกษาที่เลือก
    viewModel.selectedStudent?.let { selected ->
        EditStudentDialog(
            student = selected,
            onSave = viewModel::updateStudent,
            onDismiss = { viewModel.selectStudent(null) }
        )
    }
}

@Composable
private fun OnlineStatus(isOnline: Boolean) {
    val color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    Text(
        text = if (isOnline) "● ออนไลน์: ข้อมูลล่าสุดจาก GitHub"
        else "● ออฟไลน์: แสดงข้อมูลที่บันทึกไว้ในเครื่อง",
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun AddStudentForm(onAdd: (code: String, name: String, major: String) -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var major by rememberSaveable { mutableStateOf("") }
    val canAdd = code.isNotBlank() && name.isNotBlank() && major.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("เพิ่มนักศึกษา", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("รหัสนักศึกษา") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("ชื่อ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = major,
            onValueChange = { major = it },
            label = { Text("สาขา") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onAdd(code, name, major)
                code = ""
                name = ""
                major = ""
            },
            enabled = canAdd
        ) {
            Text("เพิ่มนักศึกษา")
        }
    }
}

@Composable
private fun StudentRow(
    student: Student,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.studentCode} - ${student.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = student.major, style = MaterialTheme.typography.bodyMedium)
            }
            // Exercise 2: ลบ
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("ลบ")
            }
        }
    }
}

@Composable
private fun EditStudentDialog(
    student: Student,
    onSave: (Student) -> Unit,
    onDismiss: () -> Unit
) {
    // ผูก state กับ id: เลือกคนใหม่ = ค่าเริ่มต้นใหม่
    var code by remember(student.id) { mutableStateOf(student.studentCode) }
    var name by remember(student.id) { mutableStateOf(student.name) }
    var major by remember(student.id) { mutableStateOf(student.major) }

    val edited = student.copy(studentCode = code.trim(), name = name.trim(), major = major.trim())
    val canSave = code.isNotBlank() && name.isNotBlank() && major.isNotBlank() && edited != student

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ข้อมูลนักศึกษา") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("รหัสนักศึกษา") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อ") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = major,
                    onValueChange = { major = it },
                    label = { Text("สาขา") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(edited) }, enabled = canSave) {
                Text("บันทึก")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ปิด")
            }
        }
    )
}

package com.example.studentmanagementapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(

    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,

    val serverId: Int? = null,     // null = ยังไม่ขึ้น Server

    val studentCode: String,
    val name: String,
    val major: String,

    val syncStatus: String = SyncStatus.PENDING,

    val updatedAt: Long = System.currentTimeMillis()
)

object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
    const val FAILED = "FAILED"     // Server ปฏิเสธ
}
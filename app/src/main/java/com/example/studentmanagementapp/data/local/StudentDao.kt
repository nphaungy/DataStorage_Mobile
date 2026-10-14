package com.example.studentmanagementapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    // ตามใบงาน
    @Query("SELECT * FROM students ORDER BY name")
    fun observeStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE syncStatus = 'PENDING'")
    suspend fun getPendingStudents(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    // เพิ่มเอง
    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Int): StudentEntity?

    @Query("SELECT * FROM students WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Int): StudentEntity?

    // ลบแถว SYNCED ที่ Server ไม่มีแล้ว
    @Query("DELETE FROM students WHERE syncStatus = 'SYNCED' AND serverId NOT IN (:serverIds)")
    suspend fun deleteSyncedNotIn(serverIds: List<Int>)
}
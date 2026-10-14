package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.local.StudentEntity
import com.example.studentmanagementapp.data.local.SyncStatus
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.StudentRequest
import com.example.studentmanagementapp.data.remote.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

enum class SyncOutcome { OK, OFFLINE, UNAUTHORIZED, SERVER_ERROR }

data class PushResult(val outcome: SyncOutcome, val synced: Int, val failed: Int)

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi
) {

    // หน้าจออ่านจาก Room เท่านั้น
    fun observeStudents(): Flow<List<StudentEntity>> {
        return dao.observeStudents()
    }

    // บันทึกลง Room ก่อน ยังไม่ส่ง Server
    suspend fun addStudent(student: StudentEntity) {
        dao.insertStudent(student.copy(syncStatus = SyncStatus.PENDING))
    }

    // ส่ง PENDING ขึ้น Server
    suspend fun syncPending(): PushResult {
        var synced = 0
        var failed = 0

        for (student in dao.getPendingStudents()) {
            try {
                val response = api.createStudent(
                    StudentRequest(
                        studentCode = student.studentCode,
                        name = student.name,
                        major = student.major
                    )
                )
                dao.updateStudent(
                    student.copy(
                        serverId = response.id,
                        syncStatus = SyncStatus.SYNCED,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                synced++
            } catch (e: IOException) {
                // ออฟไลน์: หยุด ที่เหลือยังเป็น PENDING
                return PushResult(SyncOutcome.OFFLINE, synced, failed)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // token หมดอายุ: หยุดทั้งรอบ
                    return PushResult(SyncOutcome.UNAUTHORIZED, synced, failed)
                }
                // Server ปฏิเสธรายการนี้: FAILED แล้วไปต่อ
                dao.updateStudent(
                    student.copy(
                        syncStatus = SyncStatus.FAILED,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                failed++
            }
        }
        return PushResult(SyncOutcome.OK, synced, failed)
    }

    // ดึงจาก Server มา merge (ไม่ทับ PENDING/FAILED)
    suspend fun refreshFromServer(): SyncOutcome = callServer {
        val remote = api.getStudents()
        val now = System.currentTimeMillis()

        for (dto in remote) {
            val local = dao.getByServerId(dto.id)
            if (local == null) {
                dao.insertStudent(dto.toEntity())
            } else {
                dao.updateStudent(
                    local.copy(
                        studentCode = dto.studentCode,
                        name = dto.name,
                        major = dto.major,
                        syncStatus = SyncStatus.SYNCED,
                        updatedAt = now
                    )
                )
            }
        }
        dao.deleteSyncedNotIn(remote.map { it.id })
    }

    // ลบ: ถ้าขึ้น Server แล้วต้องออนไลน์
    suspend fun deleteStudent(student: StudentEntity): SyncOutcome {
        val current = dao.getByLocalId(student.localId) ?: return SyncOutcome.OK
        val serverId = current.serverId

        if (serverId == null) {            // ยังไม่ขึ้น Server: ลบในเครื่องเลย
            dao.deleteStudent(current)
            return SyncOutcome.OK
        }
        return callServer {
            api.deleteStudent(serverId)
            dao.deleteStudent(current)
        }
    }

    suspend fun updateStudent(student: StudentEntity): SyncOutcome {
        // อ่านแถวล่าสุด กัน object เก่าจาก UI ทับ serverId/สถานะ
        val current = dao.getByLocalId(student.localId) ?: return SyncOutcome.OK
        val edited = current.copy(
            studentCode = student.studentCode,
            name = student.name,
            major = student.major,
            updatedAt = System.currentTimeMillis()
        )
        val serverId = current.serverId

        if (serverId == null) {            // ยังไม่ขึ้น Server: แก้ในเครื่องแล้วรอ sync
            dao.updateStudent(edited.copy(syncStatus = SyncStatus.PENDING))
            return SyncOutcome.OK
        }
        return callServer {
            api.updateStudent(
                serverId,
                StudentRequest(edited.studentCode, edited.name, edited.major)
            )
            dao.updateStudent(edited.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    private suspend fun callServer(block: suspend () -> Unit): SyncOutcome {
        return try {
            block()
            SyncOutcome.OK
        } catch (e: IOException) {
            SyncOutcome.OFFLINE
        } catch (e: HttpException) {
            if (e.code() == 401) SyncOutcome.UNAUTHORIZED else SyncOutcome.SERVER_ERROR
        }
    }
}
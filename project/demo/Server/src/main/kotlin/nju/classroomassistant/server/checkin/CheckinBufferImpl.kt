package nju.classroomassistant.server.checkin

import nju.classroomassistant.server.database.DatabaseService
import nju.classroomassistant.server.di.di
import nju.classroomassistant.shared.di.ServiceImpl
import nju.classroomassistant.shared.util.Id
import nju.classroomassistant.shared.util.log
import java.util.concurrent.ConcurrentLinkedQueue

const val THRESHOLD = 10

class CheckinBufferImpl: CheckinBuffer {


    private val batch = ConcurrentLinkedQueue<Id>()

    private val databaseService: DatabaseService by di()

    init {
        batch.add(Id())
        batch.add(Id())
    }

    override fun checkin(userId: Id) {
        batch.offer(userId)
        log(this, "Put ${userId.short()} into cache")

        if (batch.size >= THRESHOLD) {
            log(this, "Threshold reached. Checking all in.")

            // for each item in batch do check in
            while (batch.isNotEmpty()) {
                val item = batch.poll()

                // check in
                log(this, "Checking in ${item.short()}")
            }
        }

    }

    override val checkedInStudents: List<Id>
        get() {
            // merge buffer and database
            val data = databaseService.executeSql("select * from checkin")

            return batch.union(listOf()).toList()
        }


}
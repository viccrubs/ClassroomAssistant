package nju.classroomassistant.server.database

import nju.classroomassistant.server.database.cluster.DatabaseCluster
import nju.classroomassistant.server.di.di
import nju.classroomassistant.shared.di.ServiceImpl

@ServiceImpl
class DatabaseServiceImpl: DatabaseService {

    private val cluster: DatabaseCluster by di()

//    init {
//        securityService.start()
//    }

    override fun executeSql(sql: String): String {
        // execute sql on all databases on cluster
        return cluster.executeSqlOnAll(sql)
    }

}
package nju.classroomassistant.server.discussion

import nju.classroomassistant.server.database.DatabaseService
import nju.classroomassistant.server.di.di
import nju.classroomassistant.server.network.Export
import nju.classroomassistant.server.permission.PermissionService
import nju.classroomassistant.shared.discussion.DiscussionService
import nju.classroomassistant.shared.discussion.vo.DiscussionVo
import nju.classroomassistant.shared.discussion.vo.PostVo
import nju.classroomassistant.shared.model.user.UserRole
import nju.classroomassistant.shared.util.Id
import java.rmi.server.UnicastRemoteObject

@Export
class DiscussionServiceImpl: UnicastRemoteObject(), DiscussionService {

    private val permissionService: PermissionService by di()
    private val databaseService: DatabaseService by di()


    override fun post(senderId: Id, content: String) {
        val post = PostVo(senderId, content)
        // get the discussion and add it

    }

    override fun startDiscussion(title: String): Id {
        permissionService.checkRole(UserRole.TEACHER)

        val vo = DiscussionVo(Id(), title, arrayListOf())
        databaseService.executeSql("insert into $vo")
        return vo.id
    }

    override fun getCurrentDiscussion(): DiscussionVo {
        return DiscussionVo(Id(), "讨论1", arrayListOf(
            PostVo(Id.ZERO, "讨论参与1"),
            PostVo(Id.ZERO, "讨论参与1")
        ))
    }


}
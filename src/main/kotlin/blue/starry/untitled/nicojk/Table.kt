package blue.starry.untitled.nicojk

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Chat: IntIdTable("chat") {
    val thread = integer("thread")
    val jkId = integer("jk_id")
    val no = integer("no")
    val vpos = integer("vpos").nullable()
    val date = integer("date")
    val mail = text("mail")
    val name = text("name")
    val userId = text("user_id")
    val anonymity = integer("anonymity").nullable()
    val deleted = integer("deleted").nullable()
    val dateUsec = integer("date_usec").nullable()
    val premium = integer("premium").nullable()
    val xmlText = text("xml_text")
}

object JkName: Table("jk_name") {
    val jkId = integer("jk_id")
    val name = text("name")

    override val primaryKey = PrimaryKey(name)
}

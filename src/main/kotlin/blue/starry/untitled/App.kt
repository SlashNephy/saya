package blue.starry.untitled

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.charset.Charset
import java.sql.Connection

val db = Database.connect("jdbc:sqlite:D:\\Downloaded\\jk211 BSイレブン.sqlite3", "org.sqlite.JDBC")

fun main() {
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED

    transaction(db) {
        Chat.selectAll().limit(50).forEach {
            val s: ByteArray = it[Chat.xmlText].toByteArray(Charset.forName("SJIS"))
            val t = java.lang.String(s, "SJIS")
            println(t)
        }
    }
}

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

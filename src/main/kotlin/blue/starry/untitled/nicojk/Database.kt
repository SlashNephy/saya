package blue.starry.untitled.nicojk

import org.jetbrains.exposed.sql.Database
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

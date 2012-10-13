package model
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConversions._
import org.joda.time.DateTime

case class User(
  id: String,
  firstname: String,
  lastname: String,
  email: String,
  groupId: String)

case class Trace(
  id: String,
  category: String,
  clientDate: DateTime,
  date: DateTime,
  info: String,
  data: Map[String, String],
  user: User)

object Trace extends HBaseObject[Trace]("trace") {
  /**
   * Return a user by it's email.
   */
  def findLast(id: String): Option[Trace] = {
    val get = new Get(Bytes.toBytes(id))
    val result = table.get(get)
    getFromResult(result)
  }

  /**
   * Build a trace from a result
   */
  private def getFromResult(result: Result): Option[Trace] = {
    if (result.isEmpty()) {
      return None
    }
    val id = getStr(result, "info", "id")
    val category = getStr(result, "info", "category")
    val clientDate = getDate(result, "info", "clientDate")
    val date = getDate(result, "info", "date")
    val info = getStr(result, "info", "info")

    val data = getColumnFamilyAsMap(result, "data")

    val userId = getStr(result, "user", "userId")
    val firstname = getStr(result, "user", "firstname")
    val lastname = getStr(result, "user", "lastname")
    val email = getStr(result, "user", "email")
    val groupId = getStr(result, "user", "groupId")
    val user = User(userId, firstname, lastname, email, groupId)
    Option(Trace(id, category, clientDate, date, info, data, user))
  }
}

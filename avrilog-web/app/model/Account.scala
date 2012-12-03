package model
import scala.collection.JavaConversions._
import model.security.Permission
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.Put

case class Account(
  email: String,
  firstname: String,
  lastname: String,
  password: String,
  applications: List[Application],
  permissions: Set[Permission],
  salt: String) {

}

object Account extends HBaseObject[Account]("account") {
  val tablePermissionName = "permission"
  val roleTable = new HTable(config, tablePermissionName);

  /**
   * Return a user by it's email.
   */
  def getByEmail(email: String): Option[Account] = {
    val get = new Get(Bytes.toBytes(email))
    val result = table.get(get)
    getFromResult(result)
  }

  /**
   * Return the roles for the given user.
   */
  def getRole(email: String): Set[Permission] = {
    val get = new Get(Bytes.toBytes(email));
    val result = roleTable.get(get);
    val roles = Set[Permission](Permission.USER)
    if (result.isEmpty()) {
      return roles
    }
    val keyValues = result.list().toList;
    keyValues.foreach { keyValue =>
      roles + Permission.valueOf(Bytes.toString(keyValue.getValue()))
    }
    roles
  }

  def authenticate(email: String, password: String): Option[Account] = {
    getByEmail(email) //.filter { account => BCrypt.checkpw(password, account.password) }
  }

  /**
   * Build a user from a result
   */
  private def getFromResult(result: Result): Option[Account] = {
    if (result.isEmpty()) {
      return None
    }
    val email = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("email"))
    val firstname = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("firstname"))
    val lastname = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("lastname"))
    val password = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("password"))
    val salt = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("salt"))
    val permissions = getRole(Bytes.toString(email))
    val apps = Application.findAll()
    Option(Account(Bytes.toString(email), Bytes.toString(firstname), Bytes.toString(lastname), Bytes.toString(password), apps, permissions, Bytes.toString(salt)))
  }
}

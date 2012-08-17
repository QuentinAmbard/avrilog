package com.avricot.model
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConversions._

case class User(
  email: String,
  firstname: String,
  lastname: String,
  password: String,
  salt: String) {

}

object User {
  val config = HBaseConfiguration.create()
  val table = new HTable(config, "user");
  val roleTable = new HTable(config, "role");

  /**
   * Return a user by it's email.
   */
  def getByEmail(email: String): Option[User] = {
    val get = new Get(Bytes.toBytes(email))
    val result = table.get(get)
    getFromResult(result)
  }

  /**
   * Return the roles for the given user.
   */
  def getRole(email: String): Set[String] = {
    val get = new Get(Bytes.toBytes(email));
    val result = roleTable.get(get);
    val keyValues = result.list().toList;
    val roles = Set[String]()
    keyValues.foreach { keyValue =>
      roles + Bytes.toString(keyValue.getValue())
    }
    roles
  }
  /**
   * Build a user from a result
   */
  private def getFromResult(result: Result): Option[User] = {
    if (result.isEmpty()) {
      return None
    }
    val email = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("email"))
    val firstname = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("firstname"))
    val lastname = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("lastname"))
    val password = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("password"))
    val salt = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("salt"))
    Option(User(Bytes.toString(email), Bytes.toString(firstname), Bytes.toString(lastname), Bytes.toString(password), Bytes.toString(salt)))
  }
}

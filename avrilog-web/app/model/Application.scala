package model
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Scan

case class Application(
  id: String,
  name: String,
  secret: String) {

}

object Application extends HBaseObject[Application]("application") {

  /**
   * Return an application by it's id (which is sha1(name)).
   */
  def findById(id: String): Option[Application] = {
    val get = new Get(Bytes.toBytes(id))
    val result = table.get(get)
    getFromResult(result)
  }

  def save(app: Application) = {
    val newApp = Option(app.id) match {
      case None => app.copy(id = sha1(app.name + System.currentTimeMillis))
      case _ => app
    }

    val put = new Put(Bytes.toBytes(newApp.id))
    put.add(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(app.name))
    put.add(Bytes.toBytes("info"), Bytes.toBytes("secret"), Bytes.toBytes(app.secret))
    table.put(put)
  }

  def findAll(): List[Application] = {
    val rs = table.getScanner(new Scan)
    scan(rs, getFromResult(_))
  }

  /**
   * Build an application from a result
   */
  private def getFromResult(result: Result): Option[Application] = {
    if (result.isEmpty()) {
      return None
    }
    val id = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("id"))
    val name = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("name"))
    val secret = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("secret"))
    Option(Application(Bytes.toString(id), Bytes.toString(name), Bytes.toString(secret)))
  }
}

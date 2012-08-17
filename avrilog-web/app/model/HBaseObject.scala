package model
import org.apache.hadoop.hbase.HBaseConfiguration
import java.security.MessageDigest
import org.apache.hadoop.hbase.client.ResultScanner
import org.apache.hadoop.hbase.client.Result

trait HBaseObject[A] {
  val config = HBaseConfiguration.create()

  def sha1(string: String) = {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(string.getBytes)
    md.digest().map(i => "%02x".format(i)).mkString
  }

  /**
   * Scan a table, execute the given fun to retrive the A and retur a list of the values.
   */
  def scan(rs: ResultScanner, fun: (Result) => Option[A]): List[A] = rs.next() match {
    case null => Nil
    case r =>
      fun(r).get :: scan(rs, fun)
  }

}
package com.avricot.avrilog.model

import org.apache.hadoop.hbase.HBaseConfiguration
import java.security.MessageDigest
import org.apache.hadoop.hbase.client.ResultScanner
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.apache.hadoop.hbase.client.HTable
import scala.Array.canBuildFrom
import org.apache.hadoop.hbase.client.Put

/**
 * Default hbase trait.
 */
trait BaseHBaseObject {
  def getHBaseId(): Array[Byte]
}

/**
 * Default hbase object, with hbase helpers for model companions.
 */
class HBaseObject[A](name: String) {
  val tableName = name
  val config = HBaseConfiguration.create()
  val table = new HTable(config, tableName);

  val isoFormatter = ISODateTimeFormat.dateTime();

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

  /**
   * Return a String from the given column.
   */
  def getStr(rs: Result, columFamily: String, column: String): String = {
    Bytes.toString(getByte(rs, columFamily, column))
  }

  /**
   * Return a [Byte] from the given column.
   */
  def getByte(rs: Result, columFamily: String, column: String): Array[Byte] = {
    rs.getValue(Bytes.toBytes("info"), Bytes.toBytes("id"))
  }

  /**
   * Return a date from the given column (supposed to be iso formatted)
   */
  def getDate(rs: Result, columFamily: String, column: String): DateTime = {
    isoFormatter.parseDateTime(getStr(rs, columFamily, column))
  }

  /**
   * Return a column family as a map.
   */
  def getColumnFamilyAsMap(rs: Result, columnFamily: String): Map[String, String] = {
    val data = scala.collection.mutable.Map[String, String]();
    for (kv <- rs.raw()) {
      val splitKV = kv.split()
      if (Bytes.toString(splitKV.getFamily()) == columnFamily) {
        val qualifier = Bytes.toString(splitKV.getQualifier())
        val value = Bytes.toString(splitKV.getValue())
        data(qualifier) = value
      }
    }
    data.toMap
  }

  def add(put: Put, family: String, qualifier: String, value: String) = {
    put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value))
  }

  /**
   * Save the given object to the db.
   * Recursivly explore imbricated object.
   */
  def save(obj: BaseHBaseObject) = {
    val put = new Put(obj.getHBaseId)
    def findType(family: Array[Byte], name: String, value: Any): Unit = {
      val bytes = value match {
        case v if v == null => null
        case v: Int => Bytes.toBytes(v.asInstanceOf[Int])
        case v: Long => Bytes.toBytes(v.asInstanceOf[Long])
        case v: Float => Bytes.toBytes(v.asInstanceOf[Float])
        case v: String => Bytes.toBytes(v.asInstanceOf[String])
        case v: Boolean => Bytes.toBytes(v.asInstanceOf[Boolean])
        case v: DateTime => Bytes.toBytes(isoFormatter.print(v.asInstanceOf[DateTime]))
        case v: Array[Byte] => v.asInstanceOf[Array[Byte]]
        case v: scala.collection.mutable.HashMap[_, _] => {
          println("ok i'm a map")
          for ((k, v) <- v.asInstanceOf[scala.collection.mutable.HashMap[_, _]]) {
            println(name + " " + k.toString + ", " + v)
            findType(Bytes.toBytes(Bytes.toString(family) + "." + name), k.toString, v)
          }
          null
        }
        case v: Any => exploreObj(Bytes.toBytes(Bytes.toString(family) + "." + name), v); null
        case _ => null
      }
      if (bytes != null) {
        println("put.add(" + Bytes.toString(family) + ", " + name + ", " + bytes + "))")
        put.add(family, Bytes.toBytes(name), bytes)
      }
    }

    def exploreObj(family: Array[Byte], obj: Any) = {
      println("exploring(" + Bytes.toString(family) + ", " + obj.getClass)
      for (field <- obj.getClass.getDeclaredFields) {
        field.setAccessible(true)
        val t = field.getType()
        val value = field.get(obj)
        findType(family, field.getName(), value)
      }
    }
    exploreObj(Bytes.toBytes("info"), obj)
    table.put(put)
  }
}
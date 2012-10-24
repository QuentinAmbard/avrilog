import play.api._
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HConstants
import play.api.Play.current
import org.slf4j.LoggerFactory
import org.apache.hadoop.hbase.HTableDescriptor
import model.Account
import model.Application
import model.Trace
import org.apache.hadoop.hbase.HColumnDescriptor
import model.HBaseMetaData
import model.security.Permission
import com.typesafe.config.ConfigFactory

object Global extends GlobalSettings {
  def logger = LoggerFactory.getLogger(Global.getClass())

  override def onStart(app: play.api.Application) {
    logger.info("init hbase configuration")
    val configuration = HBaseConfiguration.create();
    //TODO scala ConfigFactory ?
    configuration.setStrings(HConstants.ZOOKEEPER_QUORUM, Play.configuration.getString(HConstants.ZOOKEEPER_QUORUM) getOrElse HConstants.DEFAULT_HOST)
    configuration.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, Play.configuration.getInt(HConstants.ZOOKEEPER_CLIENT_PORT) getOrElse HConstants.DEFAULT_ZOOKEPER_CLIENT_PORT)
    val hbaseAdmin = new HBaseAdmin(configuration)

    logger.info("check avrilog hbase tables")
    initTable(hbaseAdmin, HBaseMetaData.Application.tableName, Array(HBaseMetaData.Application.cfInfo))
    initTable(hbaseAdmin, HBaseMetaData.Permission.tableName, Array(HBaseMetaData.Permission.cfInfo))
    initTable(hbaseAdmin, HBaseMetaData.Account.tableName, Array(HBaseMetaData.Account.cfInfo), () =>
      Account.insert(Account("admin@admin.com", "admin", "admin", "admin", List[Application](), Set[Permission](Permission.ADMIN), "admin")))
    //Application.findAll()
    //initTable(hbaseAdmin, Trace.tableName, Array("info", "user"))
  }

  private def initTable(hbaseAdmin: HBaseAdmin, tableName: String, columnFamilies: Array[String]) {
    initTable(hbaseAdmin, tableName, columnFamilies, () => Unit)
  }

  private def initTable(hbaseAdmin: HBaseAdmin, tableName: String, columnFamilies: Array[String], fun: () => Unit) {
    if (hbaseAdmin.tableExists(tableName)) {
      logger.info("table {} already exists", tableName)
    } else {
      logger.info("table {} doesn't exist, try to create it", tableName)
      val newHBaseTable = new HTableDescriptor(tableName);
      columnFamilies.foreach { columnFamily =>
        newHBaseTable.addFamily(new HColumnDescriptor(columnFamily));
      }
      hbaseAdmin.createTable(newHBaseTable)
      fun()
    }
  }
}
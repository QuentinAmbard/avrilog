package com.avricot.avrilog.model

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.HBaseAdmin
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.HColumnDescriptor

object HBaseModel {
  def logger = LoggerFactory.getLogger(HBaseModel.getClass())
  val config = ConfigFactory.load()

  /**
   * Return a hbase admin object base on the conf properties.
   */
  def getHBaseAdmin() = {
    val configuration = HBaseConfiguration.create();
    configuration.setStrings(HConstants.ZOOKEEPER_QUORUM, config.getString(HConstants.ZOOKEEPER_QUORUM))
    configuration.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, config.getInt(HConstants.ZOOKEEPER_CLIENT_PORT))
    new HBaseAdmin(configuration)
  }

  /**
   * Create the common schema if not existing
   */
  def checkTable() {
    val hbaseAdmin = getHBaseAdmin
    initTable(hbaseAdmin, HBaseCommonMetaData.Trace.tableName, Array(HBaseCommonMetaData.Trace.cfInfo, HBaseCommonMetaData.Trace.cfUser, HBaseCommonMetaData.Trace.cfData))
  }

  /**
   * Init the given table.
   */
  def initTable(hbaseAdmin: HBaseAdmin, tableName: String, columnFamilies: Array[String]) {
    initTable(hbaseAdmin, tableName, columnFamilies, () => Unit)
  }

  /**
   * Init the given table, and execute the fun if the table doesn't exist.
   */
  def initTable(hbaseAdmin: HBaseAdmin, tableName: String, columnFamilies: Array[String], fun: () => Unit) {
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
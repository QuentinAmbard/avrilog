package com.avricot.avrilog.model

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.HBaseAdmin

object HBaseCommonMetaData {
  object Trace {
    val tableName = "trace"
    val cfInfo = "info"
    val cfUser = "info.user"
    val cfData = "info.data"
  }
}
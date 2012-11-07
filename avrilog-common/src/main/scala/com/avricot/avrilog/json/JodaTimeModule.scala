package com.avricot.avrilog.json

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.ModuleVersion
import org.joda.time.DateTime

object JodaTimeModule extends SimpleModule {
  addSerializer(classOf[DateTime], DateTimeSerializer)
}
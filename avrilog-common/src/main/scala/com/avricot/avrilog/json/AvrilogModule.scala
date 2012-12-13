package com.avricot.avrilog.json

import com.fasterxml.jackson.databind.module.SimpleModule
import org.joda.time.DateTime

object AvrilogModule extends SimpleModule {
  addSerializer(classOf[DateTime], DateTimeSerializer)
  addDeserializer(classOf[DateTime], DateTimeDeserializer)
}


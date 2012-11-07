package com.avricot.avrilog.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonMapper {
  val mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(DefaultScalaModule)
    .registerModule(JodaTimeModule)
}

trait JsonObj {
  def toJson() = {
    JsonMapper.mapper.writeValueAsString(this)
  }
}
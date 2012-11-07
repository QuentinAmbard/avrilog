package com.avricot.avrilog.json
import org.joda.time.DateTime
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.JsonNode
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
//  val isoFormatter = ISODateTimeFormat.dateTime();
//  isoFormatter.print(datetime)
//  isoFormatter.parseDateTime(long)

object DateTimeSerializer extends StdSerializer[DateTime](classOf[DateTime]) {
  val isoFormatter = ISODateTimeFormat.dateTime();
  def serialize(value: DateTime, gen: JsonGenerator, arg2: SerializerProvider) = {
    gen.writeString(isoFormatter.print(value))
  }

  override def getSchema(provider: SerializerProvider, typeHint: java.lang.reflect.Type): JsonNode = {
    createSchemaNode("string", true);
  }
}


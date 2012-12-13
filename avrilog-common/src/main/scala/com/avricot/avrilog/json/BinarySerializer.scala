package com.avricot.avrilog.json
import org.joda.time.DateTime
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.JsonNode
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer

object ByteArrayDeserializer extends StdDeserializer[Binary](classOf[Binary]) {
  val isoFormatter = ISODateTimeFormat.dateTime();

  override def deserialize(jp: JsonParser, ctxt: DeserializationContext) = {
    Binary(jp.getBinaryValue())
  }
}

object ByteArraySerializer extends StdSerializer[Binary](classOf[Binary]) {
  val isoFormatter = ISODateTimeFormat.dateTime();

  override def serialize(value: Binary, gen: JsonGenerator, arg2: SerializerProvider) = {
    if (value.bytes != null) {
      gen.writeBinary(value.bytes)
    }
  }
}


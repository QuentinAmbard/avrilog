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

object MapSerializer extends StdSerializer[Map[_, _]](classOf[Map[_, _]]) {
  override def serialize(value: Map[_, _], gen: JsonGenerator, arg2: SerializerProvider) = {
    gen.writeString("")
  }

  override def getSchema(provider: SerializerProvider, typeHint: java.lang.reflect.Type): JsonNode = {
    createSchemaNode("string", true);
  }
}

object MapDeserializer extends StdDeserializer[Map[_, _]](classOf[Map[_, _]]) {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext) = {
    //isoFormatter.parseDateTime(jp.getValueAsString())
    null
  }
}


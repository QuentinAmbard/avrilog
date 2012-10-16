package com.avricot.avrilog.serialize.template

import org.msgpack.packer.Packer
import org.msgpack.unpacker.Unpacker
import org.msgpack.MessageTypeException
import scala.collection.mutable.Map
import org.msgpack.template.AbstractTemplate
import org.msgpack.template.Template
import org.msgpack.`type`.Value
//>: String
class MutableMapTemplateTest[K >: String, V >: String](keyTemplate: Template[K], valueTemplate: Template[V]) extends AbstractTemplate[Map[K, V]] {
  def write(pk: Packer, target: Map[K, V], required: Boolean) = {
    if (!(target.isInstanceOf[Map[K, V]])) {
      if (target == null) {
        if (required) {
          throw new MessageTypeException("Attempted to write null");
        }
        pk.writeNil();
        None
      }
      throw new MessageTypeException("Target is not a Map but " + target.getClass());
    }
    pk.writeMapBegin(target.size);
    for ((key, value) <- target) {
      keyTemplate.write(pk, key);
      valueTemplate.write(pk, value);
    }
    pk.writeMapEnd();
  }

  def read(u: Unpacker, to: Map[K, V], required: Boolean): Map[K, V] = {
    if (!required && u.trySkipNil()) {
      return null;
    }
    val n = u.readMapBegin();
    var map: Map[K, V] = null
    if (to != null) {
      map = to;
      map.clear();
    } else {
      map = Map[K, V]();
    }
    for (val i <- 0 until n) {
      val key = keyTemplate.read(u, null.asInstanceOf[K]);
      val value = valueTemplate.read(u, null.asInstanceOf[V]);
      map.put(key, value);
    }
    u.readMapEnd();
    return map;
  }
}

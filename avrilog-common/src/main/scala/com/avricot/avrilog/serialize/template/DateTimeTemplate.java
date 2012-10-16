package com.avricot.avrilog.serialize.template;

import java.io.IOException;

import org.joda.time.DateTime;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

public class DateTimeTemplate extends AbstractTemplate<DateTime> {
    private DateTimeTemplate() {
    }

    @Override
    public void write(final Packer pk, final DateTime target, final boolean required) throws IOException {
        if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write(target.getMillis());
    }

    @Override
    public DateTime read(final Unpacker u, final DateTime to, final boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        long temp = u.readLong();
        return new DateTime(temp);
    }

    static public DateTimeTemplate getInstance() {
        return instance;
    }

    static final DateTimeTemplate instance = new DateTimeTemplate();
}
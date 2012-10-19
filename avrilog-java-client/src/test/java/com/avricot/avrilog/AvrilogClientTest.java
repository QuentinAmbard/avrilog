package com.avricot.avrilog;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.msgpack.MessagePack;

public class AvrilogClientTest {
    @Test
    public void testSerialization() throws IOException {
        Trace t = new Trace().setClientDate(45646231L).setCategory("test").setUser(new User().setId("userid").setFirstname("firstname"));
        byte[] id = t.getId();
        byte[] b = AvrilogClient.compressTrace(t);
        MessagePack msgpack = new MessagePack();
        Trace t2 = msgpack.read(b, Trace.class);
        Assert.assertEquals(45646231L, t2.getClientDate());
        Assert.assertEquals("test", t2.getCategory());
        Assert.assertEquals("userid", t2.getUser().getId());
        Assert.assertEquals("firstname", t2.getUser().getFirstname());
        Assert.assertEquals(IdGenerator.explodeId(id), IdGenerator.explodeId(t2.getId()));
    }
}

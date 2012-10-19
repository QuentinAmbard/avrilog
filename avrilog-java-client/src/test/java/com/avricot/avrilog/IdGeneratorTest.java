package com.avricot.avrilog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class IdGeneratorTest {

    @Test
    public void testExplode() {
        byte[] b = ByteBuffer.allocate(17).put((byte) 14).putInt(54421).putInt(2123).putLong(54324456L).array();
        Assert.assertEquals("14 54421 2123 54324456", IdGenerator.explodeId(b));
        byte[] b2 = ByteBuffer.allocate(17).put((byte) 0).putInt(0).putInt(0).putLong(0L).array();
        Assert.assertEquals("0 0 0 0", IdGenerator.explodeId(b2));
    }

    @Test
    public void testGen() throws InterruptedException {
        List<byte[]> ids = new ArrayList<byte[]>();
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        Thread.sleep((long) (Math.random() * 10));
        ids.add(IdGenerator.generateId());
        ids.add(IdGenerator.generateId());
        ids.add(IdGenerator.generateId());
        ids.add(IdGenerator.generateId());
        for (int i = 0; i < ids.size(); i++) {
            System.out.println(IdGenerator.explodeId(ids.get(i)));
        }
        for (int i = 1; i < ids.size(); i++) {
            String id = IdGenerator.explodeId(ids.get(i - 1));
            System.out.println(id);
            String id1 = IdGenerator.explodeId(ids.get(i));
            Assert.assertTrue(id.substring(1, id.length()).compareTo(id1.substring(1, id1.length())) > 0);
        }
    }

}

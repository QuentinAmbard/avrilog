package com.avricot.avrilog;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to generate a unique id.
 * <p>
 * Consists of 17 bytes, divided as follows: <blockquote>
 * 
 * <pre>
 * <table border="1">
 * <tr><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td>
 *     <td>7</td><td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td><td>15</td><td>16</td></tr>
 * <tr><td colspan="1">regionId</td><td colspan="4">time</td><td colspan="2">machine</td>
 *     <td colspan="2">pid</td><td colspan="8">dec</td></tr>
 * </table>
 * </pre>
 * 
 * </blockquote>
 * 
 */
public class IdGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerator.class);
    private static byte[] machineUuid;
    private final static int REGION_BYTE_SIZE = 1;
    private final static int COUNTER_BYTE_SIZE = 8;
    private final static int MACHINE_UUID_BYTE_SIZE = 4;
    private final static int REVERSE_TIMESTAMP_BYTE_SIZE = 4;
    private final static int ID_BYTE_SIZE = REGION_BYTE_SIZE + COUNTER_BYTE_SIZE + MACHINE_UUID_BYTE_SIZE + REVERSE_TIMESTAMP_BYTE_SIZE;
    private static int regionNumber = 3;
    private static AtomicLong counter = new AtomicLong(Long.MAX_VALUE - Math.abs(new java.util.Random().nextInt()));

    public static byte[] generateId() {
        int reverseTimestamp = (int) ((Long.MAX_VALUE - System.currentTimeMillis()) / 1000);
        long count = counter.getAndDecrement();
        LOGGER.debug("count {} reverseTimestamp {} ", count, reverseTimestamp);
        byte regionId = (byte) new Random().nextInt(regionNumber);
        return ByteBuffer.allocate(ID_BYTE_SIZE).put(regionId).putInt(reverseTimestamp).put(machineUuid).putLong(count).array();
    }

    public static String idToB64(final byte[] id) {
        return StringUtils.newStringUtf8(Base64.encodeBase64(id));
    }

    public static String explodeId(final byte[] id) {
        ByteBuffer bb = ByteBuffer.wrap(id);
        return bb.get() + " " + bb.getInt() + " " + bb.getInt() + " " + bb.getLong();
    }

    static {
        try {
            int machinePiece;
            {
                StringBuilder sb = new StringBuilder();
                // 2 bytes machine piece.
                try {
                    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        NetworkInterface ni = e.nextElement();
                        byte[] mac = ni.getHardwareAddress();
                        if (mac != null) {
                            for (int i = 0; i < mac.length; i++) {
                                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                            }
                        } else {
                            sb.append(ni.toString());
                        }
                    }
                    machinePiece = sb.toString().hashCode() << 16;
                } catch (Exception e) {
                    LOGGER.warn("sometimes happens with IBM JVM, use random", e);
                    sb.append(new Random().nextInt());
                    machinePiece = sb.toString().hashCode() << 16;
                }
                LOGGER.debug("machine id : {}", sb.toString());
            }

            // 2 bytes process piece.
            // must represent not only the JVM but the class loader.
            // Since static var belong to class loader there could be collisions
            // otherwise
            int processPiece;
            {
                int processId = new java.util.Random().nextInt();
                try {
                    processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
                } catch (Throwable t) {
                }

                ClassLoader loader = IdGenerator.class.getClassLoader();
                int loaderId = loader != null ? System.identityHashCode(loader) : 0;

                StringBuilder sb = new StringBuilder();
                sb.append(Integer.toHexString(processId));
                sb.append(Integer.toHexString(loaderId));
                processPiece = sb.toString().hashCode() & 0xFFFF;
                LOGGER.debug("process piece: {}", Integer.toHexString(processPiece));
            }

            int uuid = machinePiece | processPiece;
            LOGGER.debug("client unique id : {}", uuid);
            machineUuid = ByteBuffer.allocate(4).putInt(uuid).array();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

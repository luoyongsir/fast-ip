package com.fast.ip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ipip.net 免费ip数据读取
 *
 * @author Luo Yong
 * @date 2017-03-12
 */
public final class IPOld {

    private static final Logger LOG = Logger.getLogger(IPOld.class.getName());
    private static byte[] data;
    private static long indexSize;

    static {
        load("dat/17monipdb.datx");
        indexSize = bytesToLong(data[0], data[1], data[2], data[3]);
    }

    private IPOld() {
    }

    /**
     * 根据ip地址获取国省市区县拼接的字符串
     */
    public static String find(final String ip) {
        return find(intToLong(CommUtil.ipToInt(ip)));
    }

    /**
     * 根据ip地址对应的int值获取国省市区县拼接的字符串
     */
    public static String find(final int address) {
        return find(intToLong(address));
    }

    /**
     * 根据ip地址获取国省市区县字符串数组
     */
    public static String[] findArr(final String ip) {
        return findArr(intToLong(CommUtil.ipToInt(ip)));
    }

    /**
     * 根据ip地址对应的int值获取国省市区县字符串数组
     */
    public static String[] findArr(final int address) {
        return findArr(intToLong(address));
    }

    /**
     * 返回国省市区县拼接的字符串
     */
    private static String find(final long val) {
        String[] arr = findArr(val);
        StringBuilder bud = new StringBuilder(arr[0]);
        if (arr.length > 1) {
            // 去除重复
            for (int i = 1; i < arr.length; i++) {
                if (!arr[i - 1].equals(arr[i])) {
                    bud.append(arr[i]);
                }
            }
        }
        return bud.toString();
    }

    /**
     * 返回国省市区县字符串数组
     */
    private static String[] findArr(long val) {
        int start = 262148;
        int low = 0;
        int mid = 0;
        int high = (int) (((indexSize - 262144 - 262148) / 9) - 1);
        int pos = 0;
        while (low <= high) {
            mid = (low + high) / 2;
            pos = mid * 9;
            long s = 0;
            if (mid > 0) {
                int pos1 = (mid - 1) * 9;
                s = bytesToLong(data[start + pos1], data[start + pos1 + 1], data[start + pos1 + 2], data[start
                    + pos1 + 3]);
            }
            long end = bytesToLong(data[start + pos], data[start + pos + 1], data[start + pos + 2], data[start
                + pos + 3]);
            if (val > end) {
                low = mid + 1;
            } else if (val < s) {
                high = mid - 1;
            } else {
                byte b = 0;
                long off = bytesToLong(b, data[start + pos + 6], data[start + pos + 5], data[start + pos + 4]);
                long len = bytesToLong(b, b, data[start + pos + 7], data[start + pos + 8]);
                int offset = (int) (off - 262144 + indexSize);
                byte[] loc = Arrays.copyOfRange(data, offset, (int) (offset + len));
                return new String(loc, StandardCharsets.UTF_8).split("\t", -1);
            }
        }
        return new String[]{""};
    }

    /**
     * 加载ip数据库
     */
    private static void load(final String fileName) {
        try (InputStream is = CommUtil.getDefaultClassLoader().getResourceAsStream(fileName);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buff = new byte[1024];
            int rc;
            while ((rc = is.read(buff, 0, 1024)) > 0) {
                outputStream.write(buff, 0, rc);
            }
            data = outputStream.toByteArray();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "IP 数据库加载出错：", e);
        }
    }


    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return intToLong(CommUtil.bytesToInt(a, b, c, d));
    }

    private static long intToLong(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
}

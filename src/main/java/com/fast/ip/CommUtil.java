package com.fast.ip;

/**
 * @author Luo Yong
 * @date 2017-03-12
 */
public final class CommUtil {

    /**
     * copy from org.springframework.util.ClassUtils
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Exception ex) {
            throw new RuntimeException(" Thread.currentThread() 获取 ClassLoader 出错：", ex);
        }
        if (cl == null) {
            cl = CommUtil.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Exception ex) {
                    throw new RuntimeException(" ClassLoader.getSystemClassLoader() 获取 ClassLoader 出错：", ex);
                }
            }
        }
        return cl;
    }

    /**
     * ip字符串转成long
     *
     * @param ip 字符串ip
     * @return ip切割后的byte数组，转成long
     */
    public static int ipToInt(final String ip) {
        if (ip == null) {
            return 0;
        }
        byte[] b = new byte[4];
        int bIndex = 0;
        // ip地址长度
        int len = ip.length();
        // 临时存储数字
        int num = 0;
        for (int i = 0; i < len; i++) {
            char c = ip.charAt(i);
            if (c == '.') {
                if (num < 0 || num > 255) {
                    throw new RuntimeException("ip数字错误！");
                }
                b[bIndex] = (byte) num;
                bIndex++;
                num = 0;
            } else {
                // char 转成数字
                int tmp = c - '0';
                if (tmp > -1 && tmp < 10) {
                    num = (num * 10) + tmp;
                } else {
                    throw new RuntimeException("ip包含非法字符！");
                }
            }
        }
        // ip必须包含3个"." 并且ip的最后位必须大于等于0小于256
        if (bIndex == 3 && num > -1 && num < 256) {
            b[bIndex] = (byte) num;
        } else {
            throw new RuntimeException("ip地址格式错误！");
        }
        return bytesToInt(b[0], b[1], b[2], b[3]);
    }

    public static int bytesToInt(byte a, byte b, byte c, byte d) {
        return ((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff);
    }

    private CommUtil() {
    }
}

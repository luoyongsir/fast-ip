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
     * ip字符串转成int
     *
     * @param ip 字符串ip
     * @return ip切割后的byte数组，转成int
     */
    public static int ipToInt(final String ip) {
        int result = 0;
        if (ip == null) {
            return result;
        }

        // ip地址长度
        int len = ip.length();
        // 临时存储数字
        int num = 0;
        // 点号间256进制
        int offset = 24;
        for (int i = 0; i < len; i++) {
            char c = ip.charAt(i);
            if (c == '.') {
                if (num < 0 || num > 255) {
                    throw new RuntimeException("ip数字错误！");
                }
                result += (num << offset);
                num = 0;
                offset -= 8;
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
        if (offset == 0 && num > -1 && num < 256) {
            result += num;
        } else {
            throw new RuntimeException("ip地址格式错误！");
        }

        return result;
    }

    private CommUtil() {
    }
}

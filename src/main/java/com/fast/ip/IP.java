package com.fast.ip;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * ipip.net 免费ip数据读取
 *
 * @author Luo Yong
 * @date 2017-03-12
 */
public final class IP {

    private static final Logger LOG = Logger.getLogger(IP.class.getName());
    private static int[] ipArr;
    private static int[][] valArr;
    private static String[] countryArr;
    private static String[] regionArr;
    private static String[] cityArr;

    static {
        load("dat/ipData.txt");
    }

    /**
     * 根据ip地址获取国省市区县拼接的字符串
     */
    public static String find(final String ip) {
        return find(CommUtil.ipToInt(ip));
    }

    /**
     * 根据ip地址对应的int值获取国省市区县拼接的字符串
     */
    public static String find(final int address) {
        String[] arr = findArr(address);
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
     * 根据ip地址获取国省市区县字符串数组
     */
    public static String[] findArr(final String ip) {
        return findArr(CommUtil.ipToInt(ip));
    }

    /**
     * 根据ip地址对应的int值获取国省市区县字符串数组
     */
    public static String[] findArr(final int address) {
        int ind = Arrays.binarySearch(ipArr, address);
        if (ind < 0) {
            ind *= -1;
            ind -= 2;
        }

        int[] indArr = valArr[ind];
        if (indArr.length == 3) {
            return new String[]{countryArr[indArr[0]], regionArr[indArr[1]], cityArr[indArr[2]]};
        }
        return new String[]{""};
    }

    /**
     * 加载ip数据库
     */
    private static void load(final String fileName) {
        try (InputStream is = CommUtil.getDefaultClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String str;
            int i = 0;
            while ((str = reader.readLine()) != null) {
                initData(i, str);
                i++;
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "IP 数据库加载出错：", e);
        }
    }

    private static void initData(int i, String s) {
        if (s == null) {
            return;
        }
        String str = unGzip(s);
        String[] arr;
        switch (i) {
            case 0:
                arr = str.split(",", -1);
                ipArr = new int[arr.length];
                for (int j = 0; j < arr.length; j++) {
                    ipArr[j] = Integer.parseInt(arr[j]);
                }
                break;
            case 1:
                arr = str.split(",", -1);
                valArr = new int[arr.length][];
                for (int j = 0; j < arr.length; j++) {
                    String[] temp = arr[j].split(";");
                    valArr[j] = new int[temp.length];
                    for (int k = 0; k < temp.length; k++) {
                        valArr[j][k] = Integer.parseInt(temp[k]);
                    }
                }
                break;
            case 2:
                countryArr = str.split(",", -1);
                break;
            case 3:
                regionArr = str.split(",", -1);
                break;
            case 4:
                cityArr = str.split(",", -1);
                break;
            default:
                break;
        }
    }

    /**
     * 解压GZip
     *
     * @return
     */
    private static String unGzip(final String input) {
        byte[] inputBytes = Base64.getDecoder().decode(input);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(inputBytes);
             GZIPInputStream gzip = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int num = -1;
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, num);
            }
            byte[] bytes = bos.toByteArray();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("ZIP 解压出错：", e);
        }
    }

    private IP() {
    }
}

package com.fast.ip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ipip.net 免费ip数据读取
 *
 * @author Luo Yong
 * @date 2017-03-12
 */
public final class IPNew {

    private static final Logger LOG = Logger.getLogger(IPNew.class.getName());
    private static int[] ipArr;
    private static int[][] valArr;
    private static String[] countryArr;
    private static String[] regionArr;
    private static String[] cityArr;

    static {
        load("dat/ipData.txt");
    }

    private IPNew() {
    }

    /**
     * 根据ip地址获取国省市区县拼接的字符串
     */
    public static String find(final String ip) {
        return find(IP.ipToInt(ip));
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
        return findArr(IP.ipToInt(ip));
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
        try (InputStream is = IP.getDefaultClassLoader().getResourceAsStream(fileName);
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

    private static void initData(int i, String str) {
        if (str == null) {
            return;
        }
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
}

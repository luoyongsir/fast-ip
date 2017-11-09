package com.fast.ip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ipip.net 免费数据读取
 *
 * @author Luo Yong
 * @date 2017-03-12
 */
public final class IP {

	private static final Logger LOG = Logger.getLogger(IP.class.getName());

	private static byte[] ipData;
	private static int textOffset;
	private static int[] index;
	private static int[] indexData1;
	private static int[] indexData2;
	private static byte[] indexData3;
	static {
		load("dat/17monipdb.dat");
	}

	private IP() {
	}

	/**
	 * 根据ip地址获取国省市区县拼接的字符串
	 */
	public static String find(final String ip) {
		return find(ipToByteArray(ip));
	}

	/**
	 * 根据ip地址对应的int值获取国省市区县拼接的字符串
	 */
	public static String find(final int address) {
		return find(intToByteArray(address));
	}

	/**
	 * 根据ip地址获取国省市区县字符串数组
	 */
	public static String[] findArr(final String ip) {
		return findArr(ipToByteArray(ip));
	}

	/**
	 * 根据ip地址对应的int值获取国省市区县字符串数组
	 */
	public static String[] findArr(final int address) {
		return findArr(intToByteArray(address));
	}

	/**
	 * 返回国省市区县拼接的字符串
	 */
	private static String find(final byte[] ipBin) {
		String[] arr = findArr(ipBin);
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
	private static String[] findArr(final byte[] ipBin) {
		int end = indexData1.length - 1;
		int a = 0xff & ((int) ipBin[0]);
		if (a != 0xff) {
			end = index[a + 1];
		}
		long ip = (long) byteArrayToInt(ipBin, 0, true) & 0xffffffffL;
		int idx = findIndexOffset(ip, index[a], end);
		int off = indexData2[idx];
		String str = new String(ipData, textOffset - 1024 + off, 0xff & (int) indexData3[idx], StandardCharsets.UTF_8);
		return str.split("\t", -1);
	}

	/**
	 * 加载ip数据库
	 */
	private static void load(final String fileName) {
		try (InputStream is = getDefaultClassLoader().getResourceAsStream(fileName);
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buff = new byte[1024];
			int rc;
			while ((rc = is.read(buff, 0, 1024)) > 0) {
				baos.write(buff, 0, rc);
			}
			byte[] data = baos.toByteArray();
			ipData = data;
			textOffset = byteArrayToInt(data, 0, true);
			index = new int[256];
			for (int i = 0; i < 256; i++) {
				index[i] = byteArrayToInt(data, 4 + i * 4, false);
			}
			int idx = (textOffset - 4 - 1024 - 1024) / 8;
			indexData1 = new int[idx];
			indexData2 = new int[idx];
			indexData3 = new byte[idx];
			for (int i = 0, off; i < idx; i++) {
				off = 4 + 1024 + i * 8;
				indexData1[i] = byteArrayToInt(ipData, off, true);
				indexData2[i] = ((int) ipData[off + 6] & 0xff) << 16 | ((int) ipData[off + 5] & 0xff) << 8
						| ((int) ipData[off + 4] & 0xff);
				indexData3[i] = ipData[off + 7];
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "IP 数据库加载出错：", e);
		}
	}

	/**
	 * copy from org.springframework.util.ClassUtils
	 */
	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Exception ex) {
			throw new RuntimeException(" Thread.currentThread() 获取 ClassLoader 出错：", ex);
		}
		if (cl == null) {
			cl = IP.class.getClassLoader();
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
	 * byte数组中取int数值
	 */
	private static int byteArrayToInt(final byte[] arr, final int offset, final boolean highBefore) {
		if (highBefore) {
			// 高位在前，低位在后
			return ((arr[offset] & 0xFF) << 24) | ((arr[offset + 1] & 0xFF) << 16) | ((arr[offset + 2] & 0xFF) << 8)
					| (arr[offset + 3] & 0xFF);
		} else {
			// 低位在前，高位在后
			return ((arr[offset + 3] & 0xFF) << 24) | ((arr[offset + 2] & 0xFF) << 16)
					| ((arr[offset + 1] & 0xFF) << 8) | (arr[offset] & 0xFF);
		}
	}

	/**
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序
	 */
	private static byte[] intToByteArray(final int num) {
		byte[] arr = new byte[4];
		arr[0] = (byte) ((num >> 24) & 0xFF);
		arr[1] = (byte) ((num >> 16) & 0xFF);
		arr[2] = (byte) ((num >> 8) & 0xFF);
		arr[3] = (byte) (num & 0xFF);
		return arr;
	}

	/**
	 * ip字符串转成byte数组
	 * @param ip 字符串ip
	 * @return ip切割后的byte数组
	 */
	private static byte[] ipToByteArray(final String ip) {
		if (ip == null) {
			return null;
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
		return b;
	}

	private static int findIndexOffset(long ip, int start, int end) {
		int mid;
		while (start < end) {
			mid = (start + end) / 2;
			long l = 0xffffffffL & ((long) indexData1[mid]);
			if (ip > l) {
				start = mid + 1;
			} else {
				end = mid;
			}
		}
		long l = ((long) indexData1[end]) & 0xffffffffL;
		if (l >= ip) {
			return end;
		}
		return start;
	}

}

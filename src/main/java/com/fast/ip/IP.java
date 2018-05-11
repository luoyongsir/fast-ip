package com.fast.ip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ipip.net 免费数据读取
 * @author Luo Yong
 * @date 2017-03-12
 */
public final class IP {

	private static final Logger LOG = Logger.getLogger(IP.class.getName());
	private static byte[] data;
	private static long indexSize;
	static {
		load("dat/17monipdb.datx");
		indexSize = bytesToLong(data[0], data[1], data[2], data[3]);
	}

	private IP() {
	}

	/**
	 * 根据ip地址获取国省市区县拼接的字符串
	 */
	public static String find(final String ip) {
		return find(ipToLong(ip));
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
		return findArr(ipToLong(ip));
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
		int high = new Long((indexSize - 262144 - 262148) / 9).intValue() - 1;
		int pos = 0;
		while (low <= high) {
			mid = new Double((low + high) / 2).intValue();
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
				int offset = new Long(off - 262144 + indexSize).intValue();
				byte[] loc = Arrays.copyOfRange(data, offset, offset + new Long(len).intValue());
				return new String(loc, StandardCharsets.UTF_8).split("\t", -1);
			}
		}
		return null;
	}

	/**
	 * 加载ip数据库
	 */
	private static void load(final String fileName) {
		try (InputStream is = getDefaultClassLoader().getResourceAsStream(fileName);
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
	 * ip字符串转成long
	 * @param ip 字符串ip
	 * @return ip切割后的byte数组，转成long
	 */
	private static long ipToLong(final String ip) {
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
		return bytesToLong(b[0], b[1], b[2], b[3]);
	}

	private static long bytesToLong(byte a, byte b, byte c, byte d) {
		return intToLong((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
	}

	private static long intToLong(int i) {
		long l = i & 0x7fffffffL;
		if (i < 0) {
			l |= 0x080000000L;
		}
		return l;
	}

}

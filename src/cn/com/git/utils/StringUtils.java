package cn.com.git.utils;

/**
 * 字符串工具类
 * 
 * @author vision
 *
 */
public class StringUtils {

	/**
	 * 按指定字符生成一个指定长度的字符串
	 * @param count 长度
	 * @param c 指定字符
	 * @return 字符串
	 */
	public static String generateString(int count, char c) {
		if (count < 0) {
			throw new IllegalArgumentException("count must be greater than or equal 0.");
		}
		char[] chs = new char[count];
		for (int i = 0; i < count; i++) {
			chs[i] = c;
		}
		return new String(chs);
	}
	
	/**
	 * 将字符串里除了回车换行符之外的字符替换为指定字符
	 * @param str 要处理的字符串
	 * @param c 指定字符
	 * @return 处理结果
	 */
	public static String replaceNotCRLF(String str, String c) {
		if (c == null) {
			c = "";
		}
		return str.replaceAll("[^\r\n]", c);
	}
}

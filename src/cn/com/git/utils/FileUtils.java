package cn.com.git.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件操作工具类
 * 
 * @author vision-ken
 *
 */
public class FileUtils {

	/**
	 * 读取文件内容到string
	 * @param file 文本文件
	 * @param charset 编码
	 * @return 文件内容
	 * @throws IOException
	 */
	public static String loadFileToString(File file, String charset) throws IOException {
		InputStream is = null;
		String ret = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			long contentLength = file.length();
			ByteArrayOutputStream outstream = new ByteArrayOutputStream(contentLength > 0 ? (int) contentLength : 1024);
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) > 0) {
				outstream.write(buffer, 0, len);
			}
			outstream.close();
			ret = outstream.toString(charset);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return ret;
	}

}

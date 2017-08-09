package cn.com.git.tools;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cn.com.git.tools.HtmlTagValidator.ValidateError;

/**
 * html标签检查器测试
 * 
 * @author vision
 *
 */
public class HtmlTagValidatorTest {

	/**
	 * 检查单个文件，文件为正确内容
	 * 
	 * Test method for {@link cn.com.git.tools.HtmlTagValidator#exec()}.
	 * @throws IOException 
	 */
	@Test
	public void testExec1() throws IOException {
		System.out.println("----testExec1-----");
		HtmlTagValidator validator = new HtmlTagValidator(new String[] { "htm", "html", "jsp" }, true);
		String path = HtmlTagValidatorTest.class.getResource("test1.jsp").getPath();
		List<ValidateError> result1 = validator.exec(path, "UTF-8");
		for (ValidateError error : result1) {
			System.out.println(error.getMessage());
		}
		Assert.assertTrue("校验正确的test1.jsp", result1.isEmpty());
	}
	
	/**
	 * 检查单个文件，文件有错误内容
	 * 
	 * Test method for {@link cn.com.git.tools.HtmlTagValidator#exec()}.
	 * @throws IOException 
	 */
	@Test
	public void testExec2() throws IOException {
		System.out.println("----testExec2-----");
		HtmlTagValidator validator = new HtmlTagValidator(new String[] { "htm", "html", "jsp" });
		String path = HtmlTagValidatorTest.class.getResource("test2.jsp").getPath();
		List<ValidateError> result2 = validator.exec(path, "UTF-8");
		Assert.assertTrue("校验有错误的test2.jsp", !result2.isEmpty());
		for (ValidateError error : result2) {
			System.out.println(error.getMessage());
		}
	}
	
	/**
	 * 检查指定的目录：dltools/bin/cn/com/git/tools/
	 * 
	 * Test method for {@link cn.com.git.tools.HtmlTagValidator#exec()}.
	 * @throws IOException 
	 */
	@Test
	public void testExec3() throws IOException {
		System.out.println("----testExec3-----");
		HtmlTagValidator validator = new HtmlTagValidator(new String[] { "htm", "html", "jsp" });
		// 检查目录：dltools/bin/cn/com/git/tools/
		String path = HtmlTagValidatorTest.class.getResource("").getPath();
		List<ValidateError> result3 = validator.exec(path, "UTF-8");
		Assert.assertTrue("校验有错误的目录", !result3.isEmpty());
		for (ValidateError error : result3) {
			System.out.println(error.getMessage());
		}
	}

}

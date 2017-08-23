package cn.com.git.tools;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cn.com.git.tools.HtmlTagValidator.ValidateMessage;

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
		HtmlTagValidator validator = new HtmlTagValidator(new String[] { "htm", "html", "jsp" });
		String path = HtmlTagValidatorTest.class.getResource("test1.jsp").getPath();
		List<ValidateMessage> result = validator.exec(path);
		printMessage(result);
		Assert.assertTrue("校验正确的test1.jsp", result.isEmpty());
	}

	/**
	 * 打印错误信息
	 * @param list 校验结果
	 */
	private void printMessage(List<ValidateMessage> list) {
		for (ValidateMessage msg : list) {
			if (MessageLevel.ERROR.equals(msg.getLevel())) {
				System.err.println(msg.getMessage());
			}
			if (MessageLevel.WARNING.equals(msg.getLevel())) {
				System.out.println(msg.getMessage());
			}
		}
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
		List<ValidateMessage> result = validator.exec(path, "UTF-8");
		printMessage(result);
		Assert.assertTrue("校验有错误的test2.jsp", !result.isEmpty());
	}
	
	/**
	 * 检查指定的目录
	 * 
	 * Test method for {@link cn.com.git.tools.HtmlTagValidator#exec()}.
	 * @throws IOException 
	 */
	@Test
	public void testExec3() throws IOException {
		System.out.println("----testExec3-----");
		HtmlTagValidator validator = new HtmlTagValidator(new String[] { "htm", "html", "jsp" });
		// 检查指定的目录：dltools/bin/cn/com/git/tools/
		String path = HtmlTagValidatorTest.class.getResource("").getPath();
		List<ValidateMessage> result = validator.exec(path);
		printMessage(result);
		Assert.assertTrue("校验有错误的目录", !result.isEmpty());
	}

}

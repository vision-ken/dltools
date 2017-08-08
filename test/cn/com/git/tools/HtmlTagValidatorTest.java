package cn.com.git.tools;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cn.com.git.tools.HtmlTagValidator.ValidateError;

/**
 * html标签检查器测试
 * 
 * @author vision-ken
 *
 */
public class HtmlTagValidatorTest {

	/**
	 * Test method for {@link cn.com.git.tools.HtmlTagValidator#exec()}.
	 * @throws IOException 
	 */
	@Test
	public void testExec() throws IOException {
		HtmlTagValidator validator = new HtmlTagValidator();
		String path = HtmlTagValidatorTest.class.getResource("test1.jsp").getPath();
		List<ValidateError> result1 = validator.exec(path, "UTF-8");
		Assert.assertTrue("校验正确的test1.jsp", result1.isEmpty());
		
		path = HtmlTagValidatorTest.class.getResource("test2.jsp").getPath();
		List<ValidateError> result2 = validator.exec(path, "UTF-8");
		Assert.assertTrue("校验有错误的test2.jsp", !result2.isEmpty());
	}

}

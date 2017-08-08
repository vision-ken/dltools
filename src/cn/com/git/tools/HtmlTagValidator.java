package cn.com.git.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.git.utils.FileUtils;

/**
 * <p>html标签检查器</p>
 * 
 * <p>检查html标签是否闭合、结束标签的位置是否正确</p>
 * 
 * @author vision-ken
 */
public class HtmlTagValidator {

	/**
	 * 非自闭合标签必须有开始标签和结束标签，而自闭合标签没有闭合标签。
	 * 在w3c不同的规范中，对标签的闭合检查也是不一样的。
	 * XHTML最为严格，必须在自闭合标签中添加"/"。在HTML4.01中，不推荐在自闭合标签中添加“/”，而HTML5最宽松，添不添加都符合规范。
	 * HTML中所有的自闭合标签如下:
	 */
	public static List<String> ENCLOSING_TAGS = Arrays.asList("area", "base", "br", "col", "command", "embed", "hr", "img",
			"input", "keygen", "link", "meta", "param", "source", "track", "wbr");

	public List<ValidateError> exec(String path, String charset) throws IOException {
		/**
		 * 核心算法：
		 * 1、先把 html 文本中的标签提取出来，然后再去掉属性之类的，转换成一个 “标签列表”，比如：
		 * <div class="main><ul><li>test</li> 就转换成了：["div","ul","li""/li"]
		 * 2、接着读取上面这个列表，把开始标签压栈，遇到以 / 开头的结束标签，检查是否和栈顶的标签相匹配，匹配就弹出，走完标签列表后，还留在栈里的标签就是需要闭合的标签了。
		 */
		// 获取标签列表
		String html = FileUtils.loadFileToString(new File(path), charset);
		List<Tag> tagList = this.getTags(html);
		// 校验合法性
		return this.validate(tagList);
	}
	
	/**
	 * 校验合法性
	 * @param tagList html标签列表
	 * @return 是否校验通过
	 */
	protected List<ValidateError> validate(List<Tag> tagList) {
		Stack<Tag> stack = new Stack<Tag>();
		List<ValidateError> result = new ArrayList<ValidateError>();
		for (Tag tag : tagList) {
			if (stack.isEmpty()) {
				stack.push(tag);
			} else {
				if (tag.name.startsWith("/")) {
					Tag previous = stack.peek();
					if (previous.name.equals(tag.name.substring(1))) {
						stack.pop();
					} else {
						System.out.println("Error:" + previous.toString());
						stack.push(tag);
						result.add(new ValidateError(previous, tag));
					}
				} else {
					stack.push(tag);
				}
			}
		}
		return result;
	}

	/**
	 * 获取标签列表
	 * @param html html内容
	 * @return 标签列表
	 */
	protected List<Tag> getTags(String html) {
		String ss = "<[^>]+>";
		String tagHtml = null;
		Pattern pa = Pattern.compile(ss);
		Matcher ma = null;
		ma = pa.matcher(html);
		List<Tag> list = new ArrayList<Tag>();
		while (ma.find()) {
			tagHtml = ma.group();
			if (tagHtml != null) {
				if (tagHtml.startsWith(">")) {
					tagHtml = tagHtml.substring(1);
				}
				if (tagHtml.endsWith("<")) {
					tagHtml = tagHtml.substring(0, tagHtml.length() - 1);
				}
				// 忽略jsp标签和script标签
				if (tagHtml.equals("") || tagHtml.startsWith("<%") || tagHtml.startsWith("<script")
						|| tagHtml.endsWith("</script>")) {
					continue;
				}
				int position = ma.start();
				// System.out.println(temp + " start:" + position);
				Tag tag = new Tag(tagHtml, getPos(html, position));
				list.add(tag);
			}
		}
		return list;
	}

	/**
	 * 根据位置获取行、列
	 * 
	 * @param content
	 *            html的全部内容
	 * @param position
	 *            标签的起始位置
	 * @return 标签位置信息
	 */
	protected Position getPos(String content, int position) {
		int c = 1;
		int line = 1, col = 0;
		while (c <= position) {
			if (content.charAt(c) == '\n') {
				++line;
				col = 0;
			} else {
				++col;
			}
			c++;
		}
		return new Position(position, line, col);
	}

	/**
	 * 校验错误信息
	 */
	public class ValidateError {
		
		private Tag beginTag; 	// 开始标签
		private Tag endTag;		// 结束标签
		
		public ValidateError(Tag beginTag, Tag endTag) {
			this.beginTag = beginTag;
			this.endTag = endTag;
		}
		
		public Tag getBeginTag() {
			return beginTag;
		}
		public void setBeginTag(Tag beginTag) {
			this.beginTag = beginTag;
		}
		public Tag getEndTag() {
			return endTag;
		}
		public void setEndTag(Tag endTag) {
			this.endTag = endTag;
		}
		
	}
	
	/**
	 * 标签
	 */
	public class Tag {

		private String html; 		// 前标签或后标签的html，如<div class="page-content">
		private String name; 		// 标签的name，如div、input
		private Position position; 	// 标签在html的起始位置

		public Tag(String html, Position position) {
			this.html = html;
			this.name = this.getName(html);
			this.position = position;
		}

		private String getName(String html) {
			if (html.startsWith("</")) {
				return html.substring(1, html.length() - 1);
			} else {
				int index = html.indexOf(' ');
				if (index > 0) {
					return html.substring(1, index);
				} else {
					return html.substring(1, html.length() - 1);
				}
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Position getPosition() {
			return position;
		}

		public void setPosition(Position position) {
			this.position = position;
		}

		public String toString() {
			return "tag: " + name + ", line: " + this.position.line + ", col: " + this.position.col + ", position: "
					+ this.position.position;
		}

		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}

	}

	/**
	 * 位置
	 */
	public class Position {

		private int line; 		// 所在行
		private int col; 		// 所在列
		private int position; 	// 标签在html的起始位置

		public Position(int position, int line, int col) {
			this.line = line;
			this.col = col;
			this.position = position;
		}

		public int getLine() {
			return line;
		}

		public void setLine(int line) {
			this.line = line;
		}

		public int getCol() {
			return col;
		}

		public void setCol(int col) {
			this.col = col;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}
	}
}

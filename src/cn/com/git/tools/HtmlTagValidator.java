package cn.com.git.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.git.utils.FileUtils;
import cn.com.git.utils.StringUtils;

/**
 * <p>html标签检查器</p>
 * 
 * <p>检查html标签是否闭合、结束标签的位置是否正确</p>
 * 
 * @author vision
 */
public class HtmlTagValidator {

	private boolean isDebug = false;
	
	/**
	 * 非自闭合标签必须有开始标签和结束标签，而自闭合标签没有闭合标签。
	 * 在w3c不同的规范中，对标签的闭合检查也是不一样的。
	 * XHTML最为严格，必须在自闭合标签中添加"/"。在HTML4.01中，不推荐在自闭合标签中添加“/”，而HTML5最宽松，添不添加都符合规范。
	 * HTML中所有的自闭合标签如下:"
	 * area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"
	 * 
	 * 其它自闭合的标签：DOCTYPE，见https://segmentfault.com/a/1190000004525812
	 */
	public static List<String> ENCLOSING_TAGS = Arrays.asList("area", "base", "br", "col", "command", "embed", "hr", "img",
			"input", "keygen", "link", "meta", "param", "source", "track", "wbr", "!DOCTYPE");

	// 要校验的文件类型，既扩展名
	private String[] fileTypes;
	
	public HtmlTagValidator() {
		fileTypes = new String[] { "htm", "html", "jsp" };
	}

	/**
	 * @param fileTypes 要检查文件类型
	 */
	public HtmlTagValidator(String[] fileTypes) {
		if (fileTypes != null && fileTypes.length > 0) {
			this.fileTypes = fileTypes;
		}
	}
	
	/**
	 * @param fileTypes 要检查文件类型
	 * @param isDebug 是否为debug模式，可选，默认为非debug模式
	 */
	public HtmlTagValidator(String[] fileTypes, boolean... isDebugs) {
		this(fileTypes);
		this.isDebug = isDebugs.length == 1 ? isDebugs[0] : false;
	}
	
	/**
	 * @param isDebug 是否为debug模式
	 */
	public HtmlTagValidator(boolean isDebug) {
		this();
		this.isDebug = isDebug;
	}
	
	/**
	 * 检查html的标签
	 * @param path 文件或目录的绝对路径
	 * @param charset 文件的编码，可选，默认为"UTF-8"
	 * @return 检查结果
	 * @throws IOException
	 */
	public List<ValidateMessage> exec(String path, String... charsets) throws IOException {
		String charset = charsets.length == 1 ? charsets[0] : "UTF-8";
		return this.recursive(new File(path), charset);
	}
	
	/**
	 * 递归处理
	 * 
	 * @param srcFile 待检测的文件或目录
	 * @param charset 文件的字符集
	 * @throws IOException
	 */
	protected List<ValidateMessage> recursive(File srcFile, String charset) throws IOException {
		if (srcFile.isFile()) { // 文件
			// 检查文件类型
			for (String fileType : fileTypes) {
				if (getFileExtension(srcFile.getName()).equalsIgnoreCase(fileType)) {
					return this.process(srcFile, charset);
				}
			}
			return Collections.emptyList();
		} else { // 文件夹
			List<ValidateMessage> result = new ArrayList<ValidateMessage>();
			for (File file : srcFile.listFiles()) {
				List<ValidateMessage> val = recursive(file, charset);
				result.addAll(val);
			}
			return result;
		}
	}
	
	/**
	 * 获取文件扩展名
	 * @param fileName
	 * @return
	 */
	private static String getFileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}
	
	/**
	 * 核心算法：
	 * 1、先把 html 文本中的标签提取出来，然后再去掉属性之类的，转换成一个 “标签列表”，比如：
	 * <div class="main><ul><li>test</li> 就转换成了：["div","ul","li""/li"]
	 * 2、接着读取上面这个列表，把开始标签压栈，遇到以 / 开头的结束标签，检查是否和栈顶的标签相匹配，匹配就弹出，走完标签列表后，还留在栈里的标签就是需要闭合的标签了。
	 * 
	 * @param file 要检查的文件
	 * @param charset 文件的字符集
	 * @return 检查结果
	 * @throws IOException
	 */
	public List<ValidateMessage> process(File file, String charset) throws IOException {
		String filePath = file.getAbsolutePath();
		if (this.isDebug) {
			System.out.println("检查" + filePath);
		}
		// 获取标签列表
		String html = FileUtils.loadFileToString(file, charset);;
		html = this.preProcess(html);
		// 检查未闭合的注释
		List<ValidateMessage> result = validateComment(filePath, html);
		html = html.replaceAll("<!--", "   ");
		html = html.replaceAll("-->", "   ");
		// 获取标签列表
		List<Tag> tagList = this.getTags(html);
		// 校验合法性
		result.addAll(this.validate(filePath, tagList, html));
		this.sortError(result);
		if (result.isEmpty()) {
			System.out.println(filePath + " ok!");
		}
		return result;
	}
	
	/**
	 * 对处理结果进行排序
	 * @param errorList 错误列表
	 * @return 排序结果
	 */
	private void sortError(List<ValidateMessage> errorList) {
		Collections.sort(errorList, new Comparator<ValidateMessage>() {
		      @Override
		      public int compare(ValidateMessage o1, ValidateMessage o2) {
		    	  return Integer.valueOf(o1.getCurrentTag().getPosition().getLine()).compareTo(Integer.valueOf(o2.getCurrentTag().getPosition().getLine()));
		      }
		    });
	}

	/**
	 * 前置处理
	 * @param html
	 */
	protected String preProcess(String html) {
		// 将注释等内容替换为等长的空格
//		String ss = "<!--(.|\\s)*?-->";	// 匹配html多行注释, "<!--.*?-->" 匹配单行注释
//		ss = "<!--(.|[\r\n])*?-->";
//		ss = "<!--.*?-->";
		String ss = "<!--[\\w\\W\r\\n]*?-->";
		String commentHtml = null;
		Pattern pa = Pattern.compile(ss);
		Matcher ma = null;
		ma = pa.matcher(html);
		while (ma.find()) {
			commentHtml = ma.group();
			if (isDebug) {
				System.out.println(commentHtml + "\r\n####");
			}
			html = html.replace(commentHtml, StringUtils.replaceNotCRLF(commentHtml, " "));
		}
//		System.out.println(html);
		
		// 处理jsp代码
		//Matcher ma2 = Pattern.compile("<%[^>]+%>").matcher(html);
		Matcher ma2 = Pattern.compile("<%[\\w\\W\r\\n]*?%>").matcher(html);
		while (ma2.find()) {
			String jsp = ma2.group();
			if (isDebug) {
				System.out.println(jsp + "\r\n####");
			}
			html = html.replace(jsp, StringUtils.replaceNotCRLF(jsp, " "));
		}
		// 处理jsp注释
		Matcher ma3 = Pattern.compile("<%--[\\w\\W\r\\n]*?--%>").matcher(html);
		while (ma3.find()) {
			String jsp = ma3.group();
			if (isDebug) {
				System.out.println(jsp + "\r\n****");
			}
			html = html.replace(jsp, StringUtils.replaceNotCRLF(jsp, " "));
		}
		
		return html;
	}
	
	/**
	 * 校验合法性
	 * @param tagList html标签列表
	 * @return 是否校验通过
	 */
	protected List<ValidateMessage> validate(String filePath, List<Tag> tagList, String html) {
		Stack<Tag> stack = new Stack<Tag>();
		List<ValidateMessage> result = new ArrayList<ValidateMessage>();
		boolean hasFoundMatchError = false; // 是否已发现过标签匹配错误
		for (Tag tag : tagList) {
			if (stack.isEmpty()) {
				if (this.isEnclosingTag(tag)) {
					if (tag.getHtml().endsWith("/>")) {
						result.add(new ValidateMessage(filePath + " 自闭合标签不需要/>: " + tag.toString(), tag, MessageLevel.WARNING));
					}
					continue;
				}
				stack.push(tag);
			} else {
				if (isDebug) {
					System.out.println("checking " + tag.html);
				}
				if (tag.getName().startsWith("/")) {
					if (this.isEnclosingEndTag(tag)) {
						result.add(new ValidateMessage(filePath + " 自闭合标签不需要结束标签: " + tag.toString(), tag, MessageLevel.ERROR));
						continue;
					}
					Tag previous = stack.peek();
					if (previous.getName().equals(tag.getName().substring(1))) {
						stack.pop();
					} else {
						if (hasFoundMatchError) {
							continue;
						} else {
							hasFoundMatchError = true;
							result.add(new ValidateMessage(filePath + " 标签匹配错误: " + previous.toString() + " 和 " + tag.toString(), previous, tag, MessageLevel.ERROR));
						}
					}
				} else if (this.isEnclosingTag(tag)) {
					if (tag.getHtml().endsWith("/>")) {
						result.add(new ValidateMessage(filePath + " 自闭合标签不需要/>: " + tag.toString(), tag, MessageLevel.WARNING));
					}
					continue;
				} else if (tag.getHtml().endsWith("/>")) {
					continue;
				} else {
					stack.push(tag);
				}
			}
		}
		if (result.isEmpty() && !stack.isEmpty()) { // 当前面没有出错时，才检查stack
			Tag tag = stack.pop();
			result.add(new ValidateMessage(filePath + " 缺少结束标签: " + tag.toString(), tag, MessageLevel.ERROR));
		}
		return result;
	}

	/**
	 * 校验注释
	 * @param filePath 文件路径
	 * @param html html代码
	 * @return 检验结果
	 */
	protected List<ValidateMessage> validateComment(String filePath, String html) {
		List<ValidateMessage> result = new ArrayList<ValidateMessage>();
		String ss = "<!--";
		Matcher ma = Pattern.compile(ss).matcher(html);
		while (ma.find()) {
			Position pos = getPos(html, ma.start());
			int position = ma.start();
			String tagHtml = ma.group();
			Tag tag = new Tag(tagHtml, getPos(html, position));
			result.add(new ValidateMessage(filePath + " 注释没有结束标签: " + ss + ", line " + pos.line + ", col " + pos.col + ", pos" + ma.start(), tag, MessageLevel.ERROR));
		}
		ss = "-->";
		ma = Pattern.compile(ss).matcher(html);
		while (ma.find()) {
			Position pos = getPos(html, ma.start());
			int position = ma.start();
			String tagHtml = ma.group();
			Tag tag = new Tag(tagHtml, getPos(html, position));
			result.add(new ValidateMessage(filePath + " 注释没有开始标签: " + ss + ", line " + pos.line + ", col " + pos.col + ", pos" + ma.start(), tag, MessageLevel.ERROR));
		}
		return result;
	}

	/**
	 * 判断结束标签是否为自闭合标签
	 * @param endTag 结束标签
	 * @return 是否为自闭合标签
	 */
	private boolean isEnclosingEndTag(Tag endTag) {
		for (String enclosingTag : ENCLOSING_TAGS) {
			if (enclosingTag.equalsIgnoreCase(endTag.getName().substring(1))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断标签是否为自闭合标签
	 * @param endTag 结束标签
	 * @return 是否为自闭合标签
	 */
	protected boolean isEnclosingTag(Tag tag) {
		for (String enclosingTag : ENCLOSING_TAGS) {
			if (enclosingTag.equalsIgnoreCase(tag.getName())) {
				return true;
			}
		}
		return false;
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
				// 忽略script标签
				if (tagHtml.equals("") || tagHtml.startsWith("<script")
						|| tagHtml.endsWith("</script>")) {
					continue;
				}
				if (isDebug) {
					System.out.println("getTags " + tagHtml);
				}
				int position = ma.start();
				Tag tag = new Tag(tagHtml, getPos(html, position));
				
				// 忽略自闭合标签
//				boolean isEnclosingTag = false;
//				for (String enclosingTag : ENCLOSING_TAGS) {
//					if (enclosingTag.equalsIgnoreCase(tag.getName())) {
//						isEnclosingTag = true;
//						break;
//					}
//				}
//				if (isEnclosingTag) {
//					continue;
//				}
				
				list.add(tag);
			}
		}
		return list;
	}

	/**
	 * 根据位置获取行、列
	 * 
	 * @param content html的全部内容
	 * @param position 标签的起始位置
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
	public class ValidateMessage {
		
		private MessageLevel level;	// 信息级别
		private Tag currentTag; 	// 当前标签
		private Tag compareTag;		// 对比的标签
		private String message; 	// 提示信息
		
		public ValidateMessage(String message, MessageLevel level) {
			if (MessageLevel.HINT.equals(level)) {
				this.message = "hint  " + message; 
			} else if (MessageLevel.WARNING.equals(level)) {
				this.message = "warn  " + message; 
			} else if (MessageLevel.ERROR.equals(level)) {
				this.message = "error " + message; 
			} else {
				this.message = message;
			}
			this.level = level;
		}
		
		public ValidateMessage(String message, Tag currentTag, MessageLevel level) {
			this(message, level);
			this.currentTag = currentTag;
		}
		
		public ValidateMessage(String message, Tag currentTag, Tag compareTag, MessageLevel level) {
			this(message, currentTag, level);
			this.compareTag = compareTag;
		}
		
		public Tag getCurrentTag() {
			return currentTag;
		}

		public Tag getCompareTag() {
			return compareTag;
		}

		public String getMessage() {
			return message;
		}

		public MessageLevel getLevel() {
			return level;
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
			this.name = this.genName(html);
			this.position = position;
		}

		private String genName(String html) {
			if (html.startsWith("</")) {
				return html.substring(1, html.length() - 1);
			} else {
				String html2 = html.replaceAll("\\s", " "); // 替换空白符号为空格
				int index = html2.indexOf(' ');
				if (index > 0) {
					return html2.substring(1, index);
				} else {
					return html2.substring(1, html2.length() - 1);
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
			return "<" + name + ">, line " + this.position.line + ", col " + this.position.col;
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

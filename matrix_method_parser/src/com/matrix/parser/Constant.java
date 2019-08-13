package com.matrix.parser;

public final class Constant {
	public static final String TITLE = "Matrix方法耗时解析";
	public static final String FILE_METHOD_MAPPING = "methodMapping.txt文件";
	public static final String LABEL_ISSUE_CONTENT = "问题内容(json格式)";
	public static final String LABEL_START_PARSER = "开始解析";
	public static final String LABEL_SELECT_FILE = "选择文件";
	public static final String LABEL_PARSER_RESULT = "解析结果";
	public static final String test_matrix_method_mapping_error = "请选择methodMapping.txt文件";
	public static final String test_matrix_issue_hint = "请输入问题内容";
	public static final String test_matrix_parser_fail = "解析失败";
	public static final String test_matrix_method_not_found = "没有找到对应的方法声明：可能methodMapping.txt和安装的APK不一致";
	public static final String test_matrix_stack_not_found = "没有找到对应的stack：json数据格式可能不对";
	public static final String test_matrix_result = "进程：\r\n%1$s\r\n\r\n具体的耗时场景：\r\n%2$s\r\n\r\n耗时：\r\n%3$sms\r\n\r\n\r\n耗时方法：\r\n%4$s\r\n\r\n\r\n方法调用的栈信息：\r\n%5$s\r\n\r\n\r\n详细信息：\r\n%6$s\r\n";
	public static final String test_matrix_stack_line = "[%1$s 执行次数:%2$s 总耗时:%3$sms]\r\n\r\n";
	public static final String[] matrix_method_scene = new String[] { "NORMAL: 普通慢函数场景", "ENTER: Activity进入场景",
			"ANR: anr超时场景", "FULL: 满buffer场景", "STARTUP: 启动耗时场景" };

}

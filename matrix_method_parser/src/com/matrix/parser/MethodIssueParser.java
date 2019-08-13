package com.matrix.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import com.google.gson.Gson;
import com.matrix.parser.model.Issue;
import com.matrix.parser.util.Util;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;

/**
 * 方法耗时问题解析
 */
public class MethodIssueParser {

	private static final int MOTHOD_ID_INVILID = -1;

	private File methodMappingFile;
	private final HashMap<Integer, String> methodMap = new HashMap<>();
	private Listener listener;
	private Gson gson;

	public void clear() {
		methodMappingFile = null;
		listener = null;
		methodMap.clear();
		gson = null;
	}

	public MethodIssueParser(Listener listener) {
		this.listener = listener;
	}

	public File getMethodMappingFile() {
		return methodMappingFile;
	}

	/**
	 * 设置methodMapping.txt文件
	 * 
	 * @param methodMappingFile
	 */
	public void setMethodMappingFile(File methodMappingFile) {
		this.methodMappingFile = methodMappingFile;
	}

	public void start(String issueJson) {

		// 判断methodMapping.txt是否存在
		if (!Util.isExists(methodMappingFile)) {
			if (listener != null) {
				listener.onError(Constant.test_matrix_method_mapping_error);
			}
			return;
		}

		if (Util.isEmpty(issueJson)) {
			if (listener != null) {
				listener.onError(Constant.test_matrix_issue_hint);
			}
			return;
		}

		Observable.create((ObservableOnSubscribe<ParserResult>) emitter -> {
			ParserResult result = getParserResult(issueJson);
			if (result == null) {
				emitter.onError(new Throwable("result is null"));
			} else {
				emitter.onNext(result);
				emitter.onComplete();
			}

		}).subscribeOn(Schedulers.newThread()).subscribe(result -> {
			if (listener != null && result != null) {
				listener.onResult(result);
			}
		}, throwable -> {
			if (throwable != null) {
				throwable.printStackTrace();
			}
			if (listener != null) {
				listener.onError(String.format(Constant.test_matrix_parser_fail));
			}

		});

	}

	private ParserResult getParserResult(String issueJson) {
		Issue issue = getIssue(issueJson);
		if (issue == null) {
			System.out.println("MethodIssueParser.getParserResult() issue is null");
			return null;
		}
		try {
			// 初始化方法映射map
			initMethodMap();

			// 开始解析相关的堆栈
			HashMap<String, String> content = issue.getContent();

			// stack
			String stackKeyName = "stack";
			String stack = content.get(stackKeyName);
			stack = parserStack(stack);
			if (Util.isEmpty(stack)) {
				stack = Constant.test_matrix_stack_not_found;
			}
			content.put(stackKeyName, stack);

			// stackKey
			stackKeyName = "stackKey";
			String stackKey = content.get(stackKeyName);
			String methodId = (Util.isEmpty(stackKey) || !stackKey.contains("|")) ? ""
					: stackKey.substring(0, stackKey.indexOf('|'));
			stackKey = methodMap.get(Util.getInt(methodId, MOTHOD_ID_INVILID));
			if (Util.isEmpty(stackKey)) {
				stackKey = String.format(Constant.test_matrix_method_not_found);
			}
			content.put(stackKeyName, stackKey);

			//
			issue.setContent(content);

			//
			ParserResult result = new ParserResult();
			result.processName = content.get("process");
			result.scene = getSceneStr(content.get("detail"));
			result.costTime = content.get("cost");
			result.costStackKey = stackKey;
			result.stackDetail = stack;
			result.result = getJsonStr(issue);
			return result;
		} catch (Throwable e) {
			System.out.println("MethodIssueParser.getParserResult() fail:" + e.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * 获取耗时的场景描述
	 *
	 * @param name
	 * @return
	 */
	private String getSceneStr(String name) {
		if (Util.isEmpty(name)) {
			return "scene name is null";
		}
		try {
			return Scene.valueOf(name).getDesc();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "scene name is unknown";
	}

	private String parserStack(String stack) {
		if (Util.isEmpty(stack)) {
			return stack;
		}
		// 解析stack
		// stack每行的格式：stack层级，方法id，方法执行次数，方法执行总耗时
		String[] items = stack.split("\n");
		if (Util.isEmpty(items)) {
			return stack;
		}
		StringBuilder builder = new StringBuilder();
		String lineFormat = Constant.test_matrix_stack_line;
		for (String item : items) {
			if (Util.isEmpty(item)) {
				continue;
			}
			String[] temp = item.split(",");
			if (Util.isEmpty(temp) || temp.length < 4) {
				continue;
			}
			String method = methodMap.get(Util.getInt(temp[1], MOTHOD_ID_INVILID));
			if (Util.isEmpty(method)) {
				method = String.format(Constant.test_matrix_method_not_found);
			}
			builder.append(String.format(Locale.getDefault(), lineFormat, method, temp[2], temp[3]));
		}
		return builder.toString();

	}

	/**
	 * 初始化方法映射map
	 */
	private void initMethodMap() {
		if (methodMap != null && methodMap.size() > 0) {
			// 已经初始化
			return;
		}
		if (methodMappingFile == null) {
			return;
		}
		FileInputStream fis = null;
		BufferedReader bufferReader = null;
		try {

			if (!methodMappingFile.exists()) {
				return;
			}
			fis = new FileInputStream(methodMappingFile);
			bufferReader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			String line;
			while ((line = bufferReader.readLine()) != null) {
				// methodMapping.txt每行的格式：方法id，方法accessType，类名，方法名，方法描述；
				String[] items = line.split(",");
				if (Util.isEmpty(items)) {
					continue;
				}
				// 方法id->方法声明
				int key = Util.getInt(items[0], MOTHOD_ID_INVILID);
				if (key == MOTHOD_ID_INVILID) {
					System.out.println("MethodIssueParser initMethodMap methodId:" + items[0] + " can't parser to int");
					continue;
				}
				methodMap.put(key, items[2]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Util.closeQuietly(fis);
			Util.closeQuietly(bufferReader);
		}

	}

	/**
	 * 获取issue
	 *
	 * @param issueJson
	 * @return
	 */
	@Nullable
	private Issue getIssue(@NonNull String issueJson) {
		if (Util.isEmpty(issueJson)) {
			return null;
		}
		synchronized (this) {
			if (gson == null) {
				gson = Util.getGson();
			}
		}
		try {
			return gson.fromJson(issueJson, Issue.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取优美的json格式
	 * 
	 * @param json
	 * @return
	 */
	public String getPrettyStr(String json) {
		if (Util.isEmpty(json)) {
			return null;
		}
		synchronized (this) {
			if (gson == null) {
				gson = Util.getGson();
			}
		}
		try {
			return gson.toJson(gson.fromJson(json, Issue.class));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取issue的json字符串
	 *
	 * @param issue
	 * @return
	 */
	private String getJsonStr(@NonNull Issue issue) {
		if (issue == null) {
			return null;
		}
		synchronized (this) {
			if (gson == null) {
				gson = Util.getGson();
			}
		}
		try {
			return gson.toJson(issue);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取methodMapping的文件路径
	 *
	 * @return
	 */
	public String getMethodMappingFilePath() {
		return methodMappingFile != null ? methodMappingFile.getAbsolutePath() : null;
	}

	/**
	 * 耗时的场景
	 */
	public enum Scene {
		NORMAL, // 普通慢函数场景
		ENTER, // Activity进入场景
		ANR, // anr超时场景
		FULL, // 满buffer场景
		STARTUP;// 启动耗时场景

		public String getDesc() {
			return Util.getItemAt(Constant.matrix_method_scene, ordinal());
		}
	}

	/**
	 * 解析结果
	 */
	public static class ParserResult {
		/**
		 * 进程名称
		 */
		public String processName;
		/**
		 * 具体的耗时场景
		 */
		public String scene;
		/**
		 * 耗时（单位 ms）
		 */
		public String costTime;
		/**
		 * 耗时的方法信息
		 */
		public String costStackKey;
		/**
		 * 方法调用的栈信息
		 */
		public String stackDetail;

		/**
		 * 最详细的信息
		 */
		public String result;
	}

	public interface Listener {
		/**
		 * 错误
		 *
		 * @param msg
		 */
		void onError(CharSequence msg);

		/**
		 * 结果
		 *
		 * @param result
		 */
		void onResult(ParserResult result);
	}

}

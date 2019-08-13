package com.matrix.parser.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.annotations.Nullable;

public final class Util {
	/**
	 * 是否文件存在
	 *
	 * @param file
	 * @return
	 */
	public static boolean isExists(File file) {
		return file != null && file.exists();
	}

	/**
	 * 关闭IO,不抛出异常
	 *
	 * @param closeable 可以为null
	 */
	public static void closeQuietly(@Nullable Closeable closeable) {
		if (closeable == null)
			return;
		try {
			closeable.close();
		} catch (IOException ignored) {

		}
	}

	/**
	 * 数组中指定index的item
	 *
	 * @param list
	 * @param index
	 * @param <T>
	 * @return
	 */
	@Nullable
	public static <T> T getItemAt(@Nullable T[] list, int index) {
		if (list != null && index >= 0 && list.length > index) {
			return list[index];
		}
		return null;
	}

	/**
	 * 数组是否为空
	 *
	 * @param list
	 * @param <T>
	 * @return
	 */
	public static <T> boolean isEmpty(@Nullable T[] list) {
		return list == null || list.length <= 0;
	}

	/***
	 * string to int
	 *
	 * @param numberStr 数字字符串
	 * @return
	 */
	public static int getInt(String numberStr, int defaultValue) {
		if (isEmpty(numberStr)) {
			return defaultValue;
		}
		try {
			return Integer.valueOf(numberStr);
		} catch (Exception e) {

		}
		return defaultValue;
	}

	/**
	 * 是否为空或空串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * 获取Gson实例
	 *
	 * @return
	 */
	public static Gson getGson() {
		return new GsonBuilder().setPrettyPrinting().setExclusionStrategies(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				// 对plugin字段不做序列化和反序列化
				return f != null && "plugin".equals(f.getName());
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		}).create();
	}
}

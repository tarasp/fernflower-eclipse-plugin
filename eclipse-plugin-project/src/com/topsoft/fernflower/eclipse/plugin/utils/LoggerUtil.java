package com.topsoft.fernflower.eclipse.plugin.utils;

import com.topsoft.fernflower.eclipse.plugin.Activator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class LoggerUtil {
	private static ILog LOGGER = Activator.getDefault().getLog();

	public static void logError(String message, Throwable e) {
		LOGGER.log(new Status(4, "com.topsoft.fernflower.eclipse.plugin", message, e));
	}

	public static void logError(Throwable e) {
		String message = StringUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
		LOGGER.log(new Status(4, "com.topsoft.fernflower.eclipse.plugin", message, e));
	}

	public static void logError(String message) {
		LOGGER.log(new Status(4, "com.topsoft.fernflower.eclipse.plugin", message));
	}

	public static void logWarning(String message) {
		LOGGER.log(new Status(2, "com.topsoft.fernflower.eclipse.plugin", message));
	}

	public static void logInfo(String message) {
		LOGGER.log(new Status(1, "com.topsoft.fernflower.eclipse.plugin", message));
	}

	public static void logDebug(String message) {
		LOGGER.log(new Status(0, "com.topsoft.fernflower.eclipse.plugin", message));
	}
}

package com.topsoft.fernflower.eclipse.plugin.runner;

import com.topsoft.fernflower.eclipse.plugin.utils.LoggerUtil;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.console.MessageConsoleStream;

public class DecompilerOutputReader implements Runnable {
	private BufferedReader reader;
	private MessageConsoleStream consoleStream;
	private String command;

	public DecompilerOutputReader(List<String> command, InputStream inputStream, MessageConsoleStream consoleStream) {
		this.command = StringUtils.join(command, ' ');
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
		this.consoleStream = consoleStream;
	}

	public void run() {
		try {
			String line = reader.readLine();
			consoleStream.println(command);
			while (line != null) {
				consoleStream.println(line);
				line = reader.readLine();
			}
			consoleStream.println("Done");
			LoggerUtil.logDebug("Decompiler execution is finished..");
			close(reader);
			close(consoleStream);
		} catch (IOException e) {
			LoggerUtil.logError("Error reading decompiler output.", e);
		}
	}

	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (IOException e) {
			LoggerUtil.logError("Error closing " + stream.getClass().getSimpleName(), e);
		}
	}
}

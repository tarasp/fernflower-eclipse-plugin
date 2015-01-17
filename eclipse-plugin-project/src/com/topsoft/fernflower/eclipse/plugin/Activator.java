package com.topsoft.fernflower.eclipse.plugin;

import org.eclipse.ui.console.ConsolePlugin;
import org.osgi.framework.BundleContext;

public class Activator extends ConsolePlugin {
	public static final String PLUGIN_ID = "com.topsoft.fernflower.eclipse.plugin";
	private static Activator plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
}

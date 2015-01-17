package com.topsoft.fernflower.eclipse.plugin.handler;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.topsoft.fernflower.eclipse.plugin.dialogs.DecompileDialog;
import com.topsoft.fernflower.eclipse.plugin.utils.LoggerUtil;

public class DecompilerPluginHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		Shell activeShell = HandlerUtil.getActiveShell(event);
		DecompileDialog dialog = new DecompileDialog(activeShell, getFiles(selection));
		dialog.open();
		return null;
	}

	private List<String> getFiles(ISelection selection) {
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		List<String> result = new LinkedList<String>();

		Iterator<Object> selectionIterator = structuredSelection.iterator();
		while (selectionIterator.hasNext()) {
			Object selectionElement = selectionIterator.next();
			if ((selectionElement instanceof IResource)) {
				LoggerUtil.logDebug("IResource selected..");
				result.add(((IResource) selectionElement).getLocation().toString());
			} else if ((selectionElement instanceof IPackageFragmentRoot)) {
				LoggerUtil.logDebug("IPackageFragmentRoot selected..");
				IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) selectionElement;
				String fileName = packageFragmentRoot.getPath().toOSString();
				addFileNameIfValid(result, fileName);
			}
		}
		LoggerUtil.logDebug(result.size() + " files were selected");
		return result;
	}

	private void addFileNameIfValid(List<String> files, String fileName) {
		LoggerUtil.logDebug("Checking file name: " + fileName);
		if (StringUtils.isBlank(fileName)) {
			LoggerUtil.logDebug("Empty file name");
			return;
		}
		if ((!fileName.endsWith(".jar")) && (!fileName.endsWith(".class"))) {
			LoggerUtil.logDebug(".jar or .class file must be specified..");
			return;
		}
		File file = new File(fileName);
		if ((!file.exists()) || (file.isDirectory()) || (!file.canRead())) {
			LoggerUtil.logDebug("file doesn't exist, is a directory or can't be read..");
			return;
		}
		files.add(file.getAbsolutePath());
	}
}

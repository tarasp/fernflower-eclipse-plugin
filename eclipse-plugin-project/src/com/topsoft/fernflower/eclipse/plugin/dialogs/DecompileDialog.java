package com.topsoft.fernflower.eclipse.plugin.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.prefs.BackingStoreException;

import com.topsoft.fernflower.eclipse.plugin.Activator;
import com.topsoft.fernflower.eclipse.plugin.runner.DecompilerOutputReader;
import com.topsoft.fernflower.eclipse.plugin.settings.FernflowerSettings;
import com.topsoft.fernflower.eclipse.plugin.settings.FernflowerSettings.CmdOption;
import com.topsoft.fernflower.eclipse.plugin.utils.LoggerUtil;

public class DecompileDialog extends Dialog {
	private org.eclipse.swt.widgets.List fileList;
	private org.eclipse.swt.widgets.List dependenciesList;
	private java.util.List<String> selectedFileNames = new LinkedList<String>();
	private Text targetDirectory;
	private Text pathToDecompilerJar;
	private Button decompileButton;
	private FernflowerSettings settings = new FernflowerSettings();

	public DecompileDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(48);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public DecompileDialog(Shell shell, java.util.List<String> fileNames) {
		super(shell);
		selectedFileNames.addAll(fileNames);
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		TabFolder tabFolder = new TabFolder(container, 0);
		GridData gd_tabFolder = new GridData(4, 16777216, false, false, 2, 1);
		gd_tabFolder.heightHint = 303;
		gd_tabFolder.widthHint = 538;
		tabFolder.setLayoutData(gd_tabFolder);

		TabItem tbtmDecompile_1 = new TabItem(tabFolder, 0);
		tbtmDecompile_1.setText("Decompile");

		Composite composite = new Composite(tabFolder, 0);
		tbtmDecompile_1.setControl(composite);
		composite.setLayout(new GridLayout(2, false));

		Label lblFilesToDecompile = new Label(composite, 0);
		lblFilesToDecompile.setFont(SWTResourceManager.getFont("Tahoma", 8, SWT.NORMAL));
		lblFilesToDecompile.setLayoutData(new GridData(16384, 16777216, true, false, 1, 1));
		lblFilesToDecompile.setText("Files To Decompile");
		new Label(composite, 0);

		fileList = new org.eclipse.swt.widgets.List(composite, 2818);
		GridData gd_fileList = new GridData(16384, 128, true, true, 1, 2);
		gd_fileList.heightHint = 107;
		gd_fileList.widthHint = 501;
		fileList.setLayoutData(gd_fileList);
		addFilesToTheList(fileList, selectedFileNames);

		Button addResourceButton = new Button(composite, 0);
		addResourceButton.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(getShell(), 2);
				fd.setText("Select files to decompile");
				fd.setFilterPath(getWorkspaceDirectory());
				fd.setFilterExtensions(new String[] { "*.class;*.jar" });
				String firstSelectionAbsoluteName = fd.open();
				String[] allFileNames = fd.getFileNames();
				addFilesToTheList(fileList, getAllAbsolutePaths(firstSelectionAbsoluteName, allFileNames));
			}
		});
		addResourceButton.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/add_obj.gif"));

		Button removeResourceButton = new Button(composite, 0);
		removeResourceButton.setLayoutData(new GridData(16384, 128, false, false, 1, 1));
		removeResourceButton.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				for (int index : fileList.getSelectionIndices()) {
					fileList.remove(index);
				}
			}
		});
		removeResourceButton.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/delete_obj.gif"));

		Label lblDependencies = new Label(composite, 0);
		lblDependencies.setLayoutData(new GridData(16384, 16777216, true, false, 1, 1));
		lblDependencies.setText("Dependencies");
		new Label(composite, 0);

		dependenciesList = new org.eclipse.swt.widgets.List(composite, 2818);
		GridData gd_dependenciesList = new GridData(16384, 128, true, true, 1, 2);
		gd_dependenciesList.heightHint = 57;
		gd_dependenciesList.widthHint = 501;
		dependenciesList.setLayoutData(gd_dependenciesList);

		Button addDependencyButton = new Button(composite, 0);
		addDependencyButton.setLayoutData(new GridData(16384, 128, false, false, 1, 1));
		addDependencyButton.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/add_obj.gif"));
		addDependencyButton.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(getShell(), 2);
				fd.setText("Select dependencies");
				fd.setFilterPath(getWorkspaceDirectory());
				fd.setFilterExtensions(new String[] { "*.jar" });
				String firstSelectionAbsoluteName = fd.open();
				String[] allFileNames = fd.getFileNames();
				addFilesToTheList(dependenciesList, getAllAbsolutePaths(firstSelectionAbsoluteName, allFileNames));
			}
		});
		Button removeDependencyButton = new Button(composite, 0);
		removeDependencyButton.setLayoutData(new GridData(16384, 128, false, false, 1, 1));
		removeDependencyButton.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/delete_obj.gif"));
		removeDependencyButton.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				for (int index : dependenciesList.getSelectionIndices()) {
					dependenciesList.remove(index);
				}
			}
		});
		Label lblTargetDirectory = new Label(composite, 0);
		lblTargetDirectory.setLayoutData(new GridData(16384, 16777216, true, false, 1, 1));
		lblTargetDirectory.setText("Target Directory");
		new Label(composite, 0);

		targetDirectory = new Text(composite, 2048);
		GridData gd_targetDirectory = new GridData(16384, 128, true, true, 1, 1);
		gd_targetDirectory.heightHint = 16;
		gd_targetDirectory.widthHint = 501;
		targetDirectory.setLayoutData(gd_targetDirectory);

		Button selectTargetDirectory = new Button(composite, 0);
		selectTargetDirectory.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog directiryDialog = new DirectoryDialog(getShell());
				directiryDialog.setText("Select directory to decompile files to");
				directiryDialog.setFilterPath(getWorkspaceDirectory());
				String targetDirectoryPath = directiryDialog.open();
				if (StringUtils.isNotEmpty(targetDirectoryPath)) {
					targetDirectory.setText(targetDirectoryPath);
				}
			}
		});
		selectTargetDirectory.setImage(SWTResourceManager.getImage(DecompileDialog.class,
				"/icons/fileFolderType_filter.gif"));

		final TabItem tbtmSettings = new TabItem(tabFolder, 0);
		tbtmSettings.setText("Settings");
		if (StringUtils.isEmpty(getDecompilerPathSetting())) {
			tbtmSettings.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/hprio_tsk.gif"));
		}
		Composite composite_1 = new Composite(tabFolder, 0);
		tbtmSettings.setControl(composite_1);
		composite_1.setLayout(new GridLayout(1, false));

		Group lblPathToFernflower = new Group(composite_1, 0);
		lblPathToFernflower.setLayout(new GridLayout(2, false));
		lblPathToFernflower.setText("Path to Fernflower jar");

		pathToDecompilerJar = new Text(lblPathToFernflower, 2048);
		GridData gd_pathToDecompilerJar = new GridData(16384, 16777216, false, false, 1, 1);
		gd_pathToDecompilerJar.widthHint = 468;
		pathToDecompilerJar.setLayoutData(gd_pathToDecompilerJar);
		pathToDecompilerJar.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
			}
		});
		pathToDecompilerJar.setText(getDecompilerPathSetting());

		Button setFernflowerJar = new Button(lblPathToFernflower, 0);
		setFernflowerJar.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/add_obj.gif"));
		setFernflowerJar.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(getShell(), 4);
				fd.setText("Specify Fernflower decompiler jar");
				fd.setFilterPath(getWorkspaceDirectory());
				fd.setFilterExtensions(new String[] { "fernflower.jar" });
				String decompilerJarAbsoluteName = fd.open();
				pathToDecompilerJar.setText(decompilerJarAbsoluteName);
				validateSettings(tbtmSettings, decompilerJarAbsoluteName);
			}
		});
		pathToDecompilerJar.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				validateSettings(tbtmSettings, pathToDecompilerJar.getText());
			}
		});
		Group grpFernflowerCommandLine = new Group(composite_1, 0);
		grpFernflowerCommandLine.setLayoutData(new GridData(16384, 128, false, false, 1, 1));
		grpFernflowerCommandLine.setLayout(new GridLayout(2, false));
		grpFernflowerCommandLine.setText("Fernflower Command Line Options");

		for (final CmdOption cmdOption : settings.getAllOptions()) {
			final Button button = new Button(grpFernflowerCommandLine, SWT.CHECK);
			button.setText(cmdOption.name().toLowerCase() + " - " + cmdOption.getOptionDescription());
			button.setToolTipText(cmdOption.getAdditionalDescription());
			button.setSelection(cmdOption.getCurrentValue());
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					cmdOption.setCurrentValue(button.getSelection());
				}
			});
		}

		return container;
	}

	private void validateSettings(TabItem tbtmSettings, String decompilerJarAbsoluteName) {
		if (isPathToDecompilerValid(decompilerJarAbsoluteName)) {
			tbtmSettings.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/hprio_tsk.gif"));
		} else {
			tbtmSettings.setImage(SWTResourceManager.getImage(DecompileDialog.class, "/icons/tasks_tsk.gif"));
		}
	}

	protected void createButtonsForButtonBar(Composite parent) {
		decompileButton = createButton(parent, 8, "Decompile", true);
		decompileButton.addListener(13, new Listener() {
			public void handleEvent(Event event) {
				if (isValidArguments()) {
					decompile(getArguments());
				}
			}
		});
		createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
	}

	protected Point getInitialSize() {
		return new Point(567, 416);
	}

	private void addFilesToTheList(org.eclipse.swt.widgets.List fileList, java.util.List<String> fileNames) {
		for (String fileName : fileNames) {
			File file = new File(fileName);
			if (fileList.indexOf(file.getAbsolutePath()) < 0) {
				fileList.add(file.getAbsolutePath());
			}
		}
	}

	private java.util.List<String> getAllAbsolutePaths(String firstSelectionAbsoluteName, String[] allFileNames) {
		java.util.List<String> result = new LinkedList<String>();
		if (firstSelectionAbsoluteName != null) {
			String directoryName = new File(firstSelectionAbsoluteName).getParent();
			for (String fileName : allFileNames) {
				String absoluteFileName = new File(directoryName, fileName).getAbsolutePath();
				result.add(absoluteFileName);
			}
		}
		return result;
	}

	private String getDecompilerPathSetting() {
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode("com.topsoft.fernflower.eclipse.plugin");
		return prefs.get("fernflower.decompiler.path", "");
	}

	private void setDecompilerPathSetting(String pathToDecompiler) {
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode("com.topsoft.fernflower.eclipse.plugin");
		prefs.put("fernflower.decompiler.path", pathToDecompiler);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			LoggerUtil.logError("Error saving plugin settings", e);
		}
	}

	private boolean isValidArguments() {
		return (isPathToDecompilerValid(pathToDecompilerJar.getText()))
				&& (wereFilesToDecompileSpecified(fileList.getItems()))
				&& (wasTargetDirectorySpecified(targetDirectory.getText()));
	}

	private java.util.List<String> getArguments() {
		java.util.List<String> arguments = new LinkedList<String>();
		arguments.add("java");
		arguments.add("-jar");
		arguments.add(pathToDecompilerJar.getText());
		arguments.add(settings.getOptionsLine());
		arguments.addAll(Arrays.asList(fileList.getItems()));
		for (String dependencyResource : dependenciesList.getItems()) {
			arguments.add("-e=" + dependencyResource);
		}
		arguments.add(targetDirectory.getText());

		LoggerUtil.logDebug("Command: " + StringUtils.join(arguments, ' '));
		return arguments;
	}

	private void decompile(java.util.List<String> command) {
		MessageConsole console = getConsole("Fernflower Decompiler");

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File(getProjectOrWorkspaceDirectory()));

		try {
			Process decompilerProcess = processBuilder.start();
			DecompilerOutputReader outputReader = new DecompilerOutputReader(command,
					decompilerProcess.getInputStream(), console.newMessageStream());
			Thread thread = new Thread(outputReader, "LogStreamReader");
			thread.start();
		} catch (IOException e) {
			String errorPrefix = "Error executing decompiler";
			LoggerUtil.logError(errorPrefix, e);
			String errorMessage = errorPrefix + ": " + e.getMessage();
			MessageDialog.openWarning(getShell(), errorPrefix, errorMessage
					+ ". Please check fernflower-eclipse-plugin logs for error details.");
		}
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		try {
			IConsoleView view = (IConsoleView) page.showView("org.eclipse.ui.console.ConsoleView");
			view.display(console);
			LoggerUtil.logDebug("Activeted console");
		} catch (PartInitException e) {
			LoggerUtil.logError("Failed to show console view", e);
		}
		close();
	}

	private MessageConsole getConsole(String name) {
		Activator plugin = Activator.getDefault();
		IConsoleManager consoleManager = plugin.getConsoleManager();
		IConsole[] existing = consoleManager.getConsoles();
		LoggerUtil.logDebug("Consoles returned from ConsoleManager: " + existing.length);
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				LoggerUtil.logDebug("Found console with name: " + name);
				MessageConsole messageConsole = (MessageConsole) existing[i];
				messageConsole.clearConsole();
				return messageConsole;
			}
		}
		LoggerUtil.logDebug("No console with name " + name + " was found, creating such..");
		MessageConsole decompilerConsole = new MessageConsole(name, null);
		consoleManager.addConsoles(new IConsole[] { decompilerConsole });
		return decompilerConsole;
	}

	private boolean isPathToDecompilerValid(String pathToDecompiler) {
		boolean validationResult = false;
		File decompilerJar = new File(pathToDecompiler);
		if (!decompilerJar.exists()) {
			LoggerUtil.logWarning("Decompiler jar file doesn't exists: " + pathToDecompilerJar.getText());
			MessageDialog.openWarning(getShell(), "Path to decompiler..",
					"Specified file doesn't exist! Please specify valid path to Fernflower decompiler jar file.");
		} else if (!decompilerJar.isFile()) {
			MessageDialog.openWarning(getShell(), "Path to decompiler..",
					"Specified path does not represent a file! Please specify path to Fernflower decompiler jar file!");
		} else if (!decompilerJar.canRead()) {
			MessageDialog.openWarning(getShell(), "Path to decompiler..",
					"Specified file could not be read! Please specify path to Fernflower decompiler jar file!");
		} else {
			setDecompilerPathSetting(pathToDecompiler);
			validationResult = true;
		}
		return validationResult;
	}

	private boolean wereFilesToDecompileSpecified(String[] filesToDecompile) {
		boolean validationResult = true;
		if ((filesToDecompile == null) || (filesToDecompile.length == 0)) {
			String message = "Files to decompile were not selected. Please select files to decompile first.";
			LoggerUtil.logWarning(message);
			MessageDialog.openWarning(getShell(), "Files to Decompile..", message);
			validationResult = false;
		}
		return validationResult;
	}

	private boolean wasTargetDirectorySpecified(String targetDirectory) {
		boolean validationResult = true;
		if ((targetDirectory == null) || (targetDirectory.isEmpty())) {
			String message = "Target Directory was not specified. Please specify directory where files will be decompiled to.";
			LoggerUtil.logWarning(message);
			MessageDialog.openWarning(getShell(), "Target Directory..", message);
			validationResult = false;
		}
		return validationResult;
	}

	private String getProjectOrWorkspaceDirectory() {
		return getCurrentProjectDirectory() != null ? getCurrentProjectDirectory() : getWorkspaceDirectory();
	}

	private String getWorkspaceDirectory() {
		String workspaceDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		LoggerUtil.logDebug("Workspace directory resolved to " + workspaceDirectory);
		return workspaceDirectory;
	}

	private String getCurrentProjectDirectory() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ISelectionService selectionService = workbench.getActiveWorkbenchWindow().getSelectionService();

		ISelection selection = selectionService.getSelection();

		IProject project = null;
		if ((selection instanceof IStructuredSelection)) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if ((element instanceof IResource)) {
				project = ((IResource) element).getProject();
			} else if ((element instanceof IPackageFragmentRoot)) {
				IJavaProject jProject = ((IPackageFragmentRoot) element).getJavaProject();
				project = jProject.getProject();
			} else if ((element instanceof IJavaElement)) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
		}
		if (project != null) {
			LoggerUtil.logDebug("Selected project directory: " + project.getLocation().toFile().getAbsolutePath());
			return project.getLocation().toFile().getAbsolutePath();
		}
		LoggerUtil.logDebug("Selected project directory was not identified");
		return null;
	}
}

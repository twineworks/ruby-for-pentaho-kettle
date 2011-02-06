package org.typeexit.kettle.plugin.steps.ruby;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.typeexit.kettle.plugin.steps.ruby.meta.OutputFieldMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RoleStepMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyVariableMeta;

public class RubyStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = RubyStepMeta.class; // for i18n purposes

	private RubyStepSyntaxHighlighter syntaxHighlighter;
	private RubyStepParseErrorHelper parseErrorHelper;
	private RubyStepMeta input;

	// output field name

	private CTabFolder wScriptsFolder;

	private ModifyListener lsMod;

	private TableView wFields;

	private Button wClearInputFields;

	private Label wlEditingPosition;
	private Composite wTopLeft;

	private GUIResource guiResource = GUIResource.getInstance();

	private Label wlSyntaxCheck;

	int margin = Const.MARGIN;

	private CTabFolder wLeftFolder;

	private SashForm wTop;

	private ToolItem itemSettings;

	private ToolBar wScriptToolBar;

	private Label wSeparator;

	private CTabFolder wBottomFolder;

	private String[] prevStepNames;
	private String[] nextStepNames;

	private TableView wScopeVariables;
	private TableView wInfoSteps;
	private TableView wTargetSteps;

	private Image scriptImage;
	private Image rubyImage;
	private Image checkImage;

	final private String[] NO_YES = new String[2];

	private Menu scriptMenu;

	private Image addImage;

	private Image renameImage;

	private MenuItem renameItem;

	public RubyStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (RubyStepMeta) in;
		NO_YES[0] = BaseMessages.getString(PKG, "System.Combo.No");
		NO_YES[1] = BaseMessages.getString(PKG, "System.Combo.Yes");
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		syntaxHighlighter = new RubyStepSyntaxHighlighter();
		parseErrorHelper = new RubyStepParseErrorHelper();

		props.setLook(shell);
		setShellImage(shell, input);

		lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "RubyStep.Shell.Title"));

		int middle = props.getMiddlePct();

		// load images
		if (!guiResource.getImageMap().containsKey("TypeExitRubyStep:empty16x16")) {
			guiResource.getImageMap().put("TypeExitRubyStep:empty16x16", new Image(display, 16, 16));
		}

		String pluginBaseDir = PluginRegistry.getInstance().findPluginWithId(StepPluginType.class, "TypeExitRubyStep").getPluginDirectory().toString();

		try {
			scriptImage = guiResource.getImage(pluginBaseDir + ("/images/libScript.png".replaceAll("/", Const.FILE_SEPARATOR)));
			checkImage = guiResource.getImage(pluginBaseDir + ("/images/check.png".replaceAll("/", Const.FILE_SEPARATOR)));
			rubyImage = guiResource.getImage(pluginBaseDir + ("/images/ruby_16.png".replaceAll("/", Const.FILE_SEPARATOR)));
			addImage = guiResource.getImage(pluginBaseDir + ("/images/addSmall.png".replaceAll("/", Const.FILE_SEPARATOR)));
			renameImage = guiResource.getImage(pluginBaseDir + ("/images/edit.png".replaceAll("/", Const.FILE_SEPARATOR)));
		} catch (Exception e) {
			Image empty = guiResource.getImage("TypeExitRubyStep:empty16x16");
			scriptImage = empty;
			checkImage = empty;
			rubyImage = empty;
			addImage = empty;
			renameImage = empty;
		}

		// start construction

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		SashForm wSash = new SashForm(shell, SWT.VERTICAL);

		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Upper part of form  
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/

		// top part 
		wTop = new SashForm(wSash, SWT.HORIZONTAL);
		props.setLook(wTop);

		FormLayout topLayout = new FormLayout();
		topLayout.marginWidth = Const.FORM_MARGIN;
		topLayout.marginHeight = Const.FORM_MARGIN;
		wTop.setLayout(topLayout);

		addSettingsArea();
		addScriptArea();

		FormData fdTop = new FormData();
		fdTop.left = new FormAttachment(0, 0);
		fdTop.top = new FormAttachment(0, 0);
		fdTop.right = new FormAttachment(100, 0);
		fdTop.bottom = new FormAttachment(100, 0);
		wTop.setLayoutData(fdTop);

		wTop.SASH_WIDTH = margin;
		wTop.setWeights(new int[] { 32, 68 });

		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Bottom part of form 
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/

		Composite wBottom = new Composite(wSash, SWT.NONE);
		props.setLook(wBottom);

		FormLayout bottomLayout = new FormLayout();
		bottomLayout.marginWidth = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);

		// separator
		wSeparator = new Label(wBottom, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fdSeparator = new FormData();
		fdSeparator.left = new FormAttachment(0, 0);
		fdSeparator.right = new FormAttachment(100, 0);
		fdSeparator.top = new FormAttachment(0, -margin);
		wSeparator.setLayoutData(fdSeparator);

		// bottom tab folder
		wBottomFolder = new CTabFolder(wBottom, SWT.BORDER | SWT.RESIZE);
		wBottomFolder.setSimple(false);
		wBottomFolder.setUnselectedImageVisible(true);
		wBottomFolder.setUnselectedCloseVisible(false);
		wBottomFolder.setMaximizeVisible(false);
		wBottomFolder.setMinimizeVisible(false);
		props.setLook(wBottomFolder);

		styleTabFolder(wBottomFolder);

		addOutputFieldsTab();
		addScopeVariablesTab();
		addExecutionModelTab();

		// set selected item in tab
		wBottomFolder.setSelection(0);

		// layout tab folder below the label 
		FormData fdBottomFolder = new FormData();
		fdBottomFolder.left = new FormAttachment(0, 0);
		fdBottomFolder.top = new FormAttachment(wSeparator, margin);
		fdBottomFolder.right = new FormAttachment(100, 0);
		fdBottomFolder.bottom = new FormAttachment(100, 0);
		wBottomFolder.setLayoutData(fdBottomFolder);

		FormData fdBottom = new FormData();
		fdBottom.left = new FormAttachment(0, 0);
		fdBottom.top = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom = new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);

		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Wrapping up form 
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/

		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		fdSash.top = new FormAttachment(wStepname, margin);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(100, -50);
		wSash.setLayoutData(fdSash);

		wSash.SASH_WIDTH = margin;
		wSash.setWeights(new int[] { 75, 25 });

		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wSash);

		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();

		// make sure there's at least one default script
		if (wScriptsFolder.getItemCount() == 0) {
			addScriptTab(RubyScriptMeta.DEFAULT_SCRIPT);
		}

		wScriptsFolder.setSelection(wScriptsFolder.getItem(0));

		highlightSyntax();

		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	private void addScopeVariablesTab() {

		CTabItem outFieldsItem = new CTabItem(wBottomFolder, SWT.NONE);
		outFieldsItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.ScopeVariables.Label"));

		Composite wPanel = new Composite(wBottomFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		outFieldsItem.setControl(wPanel);

		final int nrRows = input.getRubyVariables().size();
		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.ScopeVariable"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.ScopeVariableValue"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};
		colinf[1].setUsingVariables(true);

		wScopeVariables = new TableView(transMeta, wPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, nrRows, lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wScopeVariables.setLayoutData(fdFields);

	}

	private void addOutputFieldsTab() {

		CTabItem outFieldsItem = new CTabItem(wBottomFolder, SWT.NONE);
		outFieldsItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Fields.Label"));

		Composite wPanel = new Composite(wBottomFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		outFieldsItem.setControl(wPanel);

		wClearInputFields = new Button(wPanel, SWT.CHECK);
		wClearInputFields.setText(BaseMessages.getString(PKG, "RubyStepDialog.ClearFields.Label")); //$NON-NLS-1$
		props.setLook(wClearInputFields);
		FormData fdClearResultFields = new FormData();
		fdClearResultFields.right = new FormAttachment(100, 0);
		fdClearResultFields.bottom = new FormAttachment(100, 0);
		wClearInputFields.setLayoutData(fdClearResultFields);

		final int fieldsRows = input.getOutputFields().size();

		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.Fieldname"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.UpdateExisting"), ColumnInfo.COLUMN_TYPE_CCOMBO, NO_YES) //$NON-NLS-1$
		};

		wFields = new TableView(transMeta, wPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldsRows, lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wClearInputFields, -margin);
		wFields.setLayoutData(fdFields);

	}

	private void addInfoStepsTab() {

		CTabItem infoStepsItem = new CTabItem(wLeftFolder, SWT.NONE);
		infoStepsItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.InfoSteps.Label"));

		Composite wPanel = new Composite(wLeftFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		infoStepsItem.setControl(wPanel);

		final int nrRows = input.getInfoSteps().size();

		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.InfoStepTag"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.InfoStepName"), ColumnInfo.COLUMN_TYPE_CCOMBO, prevStepNames), //$NON-NLS-1$
		};

		wInfoSteps = new TableView(transMeta, wPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, nrRows, lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wInfoSteps.setLayoutData(fdFields);

	}

	private void addTargetStepsTab() {

		CTabItem targetStepsItem = new CTabItem(wLeftFolder, SWT.NONE);
		targetStepsItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.TargetSteps.Label"));

		Composite wPanel = new Composite(wLeftFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		targetStepsItem.setControl(wPanel);

		final int nrRows = input.getTargetSteps().size();

		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.TargetStepTag"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.TargetStepName"), ColumnInfo.COLUMN_TYPE_CCOMBO, nextStepNames), //$NON-NLS-1$
		};

		wTargetSteps = new TableView(transMeta, wPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, nrRows, lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wTargetSteps.setLayoutData(fdFields);

	}

	private void addFieldSummaryTab() {

		CTabItem fieldSummaryItem = new CTabItem(wLeftFolder, SWT.NONE);
		fieldSummaryItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.FieldSummary.Label"));

		Composite wPanel = new Composite(wLeftFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		fieldSummaryItem.setControl(wPanel);

	}

	private void addExecutionModelTab() {

		CTabItem executionModelItem = new CTabItem(wBottomFolder, SWT.NONE);
		executionModelItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.ExecutionModel.Label"));

		Composite wPanel = new Composite(wBottomFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		executionModelItem.setControl(wPanel);

	}

	private void addScriptArea() {

		// top right composite
		Composite wTopRight = new Composite(wTop, SWT.NONE);
		FormLayout topRightLayout = new FormLayout();
		topRightLayout.marginWidth = 0;
		topRightLayout.marginHeight = 0;
		wTopRight.setLayout(topRightLayout);

		// script tab folder
		wScriptsFolder = new CTabFolder(wTopRight, SWT.BORDER | SWT.RESIZE);
		wScriptsFolder.setSimple(false);
		wScriptsFolder.setUnselectedImageVisible(true);
		wScriptsFolder.setUnselectedCloseVisible(true);

		props.setLook(wScriptsFolder);
		styleTabFolder(wScriptsFolder);

		// confirms closing script tabs, and will never close the last one
		wScriptsFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				CTabItem cItem = (CTabItem) event.item;
				event.doit = false;
				if (cItem != null && wScriptsFolder.getItemCount() > 1) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
					messageBox.setText(BaseMessages.getString(PKG, "RubyStepDialog.DeleteItem.Label"));
					messageBox.setMessage(BaseMessages.getString(PKG, "RubyStepDialog.ConfirmDeleteItem.Label", cItem.getText()));
					switch (messageBox.open()) {
					case SWT.YES:
							event.doit = true;
							input.setChanged();
							break;
						}
					}
				}
		});

		addScriptFolderDnD();

		addScriptPopupMenu();

		// toolbar below the script window
		wScriptToolBar = new ToolBar(wTopRight, SWT.FLAT | SWT.RIGHT);

		itemSettings = new ToolItem(wScriptToolBar, SWT.NONE);
		itemSettings.setImage(scriptImage);
		itemSettings.setText(BaseMessages.getString(PKG, "RubyStepDialog.AdvancedSettings.Label")); //$NON-NLS-1$
		itemSettings.setEnabled(false);

		itemSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			// logic to display the advanced settings area
			public void widgetSelected(SelectionEvent e) {
				wTop.SASH_WIDTH = margin;
				wTop.setWeights(new int[] { 32, 68 });
				itemSettings.setEnabled(false);
			}
		});

		ToolItem item = new ToolItem(wScriptToolBar, SWT.NONE);
		item.setImage(checkImage);
		item.setText(BaseMessages.getString(PKG, "RubyStepDialog.CheckSyntax.Label")); //$NON-NLS-1$

		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkForParseErrors();
			}
		});

		wScriptToolBar.pack();

		// layout toolbar
		FormData fdBar = new FormData();
		fdBar.left = new FormAttachment(0, 0);
		fdBar.bottom = new FormAttachment(100, 0);
		wScriptToolBar.setLayoutData(fdBar);

		// layout scripts folder
		FormData fdFolder = new FormData();
		fdFolder.left = new FormAttachment(0, 0);
		fdFolder.right = new FormAttachment(100, 0);
		fdFolder.top = new FormAttachment(0, 0);
		fdFolder.bottom = new FormAttachment(wScriptToolBar, 0);
		wScriptsFolder.setLayoutData(fdFolder);

		// editing position label
		wlEditingPosition = new Label(wTopRight, SWT.RIGHT);
		wlEditingPosition.setText("0 : 0 ");

		FormData fdPos = new FormData();
		fdPos.left = new FormAttachment(100, -60);
		fdPos.right = new FormAttachment(100, -margin);
		fdPos.bottom = new FormAttachment(100, -margin + 1);
		wlEditingPosition.setLayoutData(fdPos);

		// syntax check result label
		wlSyntaxCheck = new Label(wTopRight, SWT.LEFT);
		wlSyntaxCheck.setText("");
		FormData fdSyntaxCheck = new FormData();
		fdSyntaxCheck.left = new FormAttachment(wScriptToolBar, 0);
		fdSyntaxCheck.bottom = new FormAttachment(100, -margin + 1);
		fdSyntaxCheck.right = new FormAttachment(wlEditingPosition, -margin);
		wlSyntaxCheck.setLayoutData(fdSyntaxCheck);

	}

	private void addScriptFolderDnD() {
		Listener listener = new Listener() {

			boolean drag = false;
			boolean exitDrag = false;
			CTabItem dragItem;
			Display display = shell.getDisplay();
			CTabFolder folder = wScriptsFolder;

			public void handleEvent(Event e) {
				Point p = new Point(e.x, e.y);
				if (e.type == SWT.DragDetect) {
					p = folder.toControl(display.getCursorLocation()); //see bug 43251
				}
				switch (e.type) {
				case SWT.DragDetect: {
					CTabItem item = folder.getItem(p);
					if (item == null)
						return;
					drag = true;
					exitDrag = false;
					dragItem = item;
					break;
				}
				case SWT.MouseEnter:
					if (exitDrag) {
						exitDrag = false;
						drag = e.button != 0;
					}
					break;
				case SWT.MouseExit:
					if (drag) {
						folder.setInsertMark(null, false);
						exitDrag = true;
						drag = false;
					}
					break;
				case SWT.MouseUp: {
					if (!drag)
						return;
					folder.setInsertMark(null, false);
					CTabItem item = folder.getItem(new Point(p.x, 1));
					if (item != null) {
						Rectangle rect = item.getBounds();
						boolean after = p.x > rect.x + rect.width / 2;
						int index = folder.indexOf(item);
						index = after ? index + 1 : index - 1;
						index = Math.max(0, index);
						CTabItem newItem = new CTabItem(folder, dragItem.getStyle(), index);
						newItem.setText(dragItem.getText());
						newItem.setImage(dragItem.getImage());
						Control c = dragItem.getControl();
						dragItem.setControl(null);
						newItem.setControl(c);
						dragItem.dispose();
						folder.setSelection(newItem);
					}
					drag = false;
					exitDrag = false;
					dragItem = null;
					break;
				}
				case SWT.MouseMove: {
					if (!drag)
						return;
					CTabItem item = folder.getItem(new Point(p.x, 2));
					if (item == null) {
						folder.setInsertMark(null, false);
						return;
					}
					Rectangle rect = item.getBounds();
					boolean after = p.x > rect.x + rect.width / 2;
					folder.setInsertMark(item, after);
					break;
				}
				}
			}
		};
		wScriptsFolder.addListener(SWT.DragDetect, listener);
		wScriptsFolder.addListener(SWT.MouseUp, listener);
		wScriptsFolder.addListener(SWT.MouseMove, listener);
		wScriptsFolder.addListener(SWT.MouseExit, listener);
		wScriptsFolder.addListener(SWT.MouseEnter, listener);

	}

	// Read data and place it in the dialog
	public void getData() {

		wStepname.selectAll();

		// load the different scripts
		for (RubyScriptMeta rubyScriptMeta : input.getScripts()) {
			addScriptTab(rubyScriptMeta);
		}

		// load output fields
		int rowNum = 0;
		for (OutputFieldMeta outField : input.getOutputFields()) {
			TableItem row = wFields.table.getItem(rowNum++);
			row.setText(1, outField.getName());
			row.setText(2, ValueMeta.getTypeDesc(outField.getType()));
			row.setText(3, outField.isUpdate() ? NO_YES[1] : NO_YES[0]);
		}
		wFields.optWidth(true);
		wFields.setRowNums();

		// load clear input fields flag
		wClearInputFields.setSelection(input.isClearInputFields());

		// load ruby vars
		int varNum = 0;
		for (RubyVariableMeta var : input.getRubyVariables()) {
			TableItem row = wScopeVariables.table.getItem(varNum++);
			row.setText(1, var.getName());
			row.setText(2, var.getValue());
		}
		wScopeVariables.optWidth(true);
		wScopeVariables.setRowNums();

		// load info steps
		int infoNum = 0;
		for (RoleStepMeta step : input.getInfoSteps()) {
			TableItem row = wInfoSteps.table.getItem(infoNum++);
			row.setText(1, step.getRoleName());
			row.setText(2, step.getStepName());
		}
		wInfoSteps.optWidth(true);
		wInfoSteps.setRowNums();

		// load target steps
		int targetNum = 0;
		for (RoleStepMeta step : input.getTargetSteps()) {
			TableItem row = wTargetSteps.table.getItem(targetNum++);
			row.setText(1, step.getRoleName());
			row.setText(2, step.getStepName());
		}
		wTargetSteps.optWidth(true);
		wTargetSteps.setRowNums();

	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	// let the plugin know about the entered data
	private void ok() {
		stepname = wStepname.getText(); // return value

		// generate scripts
		List<RubyScriptMeta> scripts = input.getScripts();

		scripts.clear();
		scripts.addAll(collectScripts());

		// generate output fields
		List<OutputFieldMeta> outFields = input.getOutputFields();
		outFields.clear();

		int fieldCount = wFields.nrNonEmpty();
		for (int i = 0; i < fieldCount; i++) {
			TableItem t = wFields.getNonEmpty(i);
			outFields.add(new OutputFieldMeta(t.getText(1), ValueMeta.getType(t.getText(2)), NO_YES[1].equalsIgnoreCase(t.getText(3))));
		}

		// save clear input fields flag
		input.setClearInputFields(wClearInputFields.getSelection());

		// generate ruby vars
		List<RubyVariableMeta> rubyVars = input.getRubyVariables();
		rubyVars.clear();

		int varCount = wScopeVariables.nrNonEmpty();
		for (int i = 0; i < varCount; i++) {
			TableItem t = wScopeVariables.getNonEmpty(i);
			rubyVars.add(new RubyVariableMeta(t.getText(1), t.getText(2)));
		}

		// generate info steps
		List<RoleStepMeta> infoSteps = input.getInfoSteps();
		infoSteps.clear();

		int infoCount = wInfoSteps.nrNonEmpty();
		for (int i = 0; i < infoCount; i++) {
			TableItem t = wInfoSteps.getNonEmpty(i);
			infoSteps.add(new RoleStepMeta(t.getText(2), t.getText(1)));
		}

		// generate target steps
		List<RoleStepMeta> targetSteps = input.getTargetSteps();
		targetSteps.clear();

		int targetCount = wTargetSteps.nrNonEmpty();
		for (int i = 0; i < targetCount; i++) {
			TableItem t = wTargetSteps.getNonEmpty(i);
			targetSteps.add(new RoleStepMeta(t.getText(2), t.getText(1)));
		}

		// make sure the input finds its info and target step metas alright
		input.searchInfoAndTargetSteps(transMeta.getSteps());
		dispose();
	}

	private List<RubyScriptMeta> collectScripts() {

		List<RubyScriptMeta> retval = new ArrayList<RubyScriptMeta>(wScriptsFolder.getItemCount());
		CTabItem[] items = wScriptsFolder.getItems();
		for (int i = 0; i < items.length; i++) {

			CTabItem item = items[i];
			StyledTextComp wText = (StyledTextComp) item.getControl();
			retval.add(new RubyScriptMeta(item.getText(), wText.getText()));

		}
		return retval;

	}

	private void addSettingsArea() {

		// top left composite
		wTopLeft = new Composite(wTop, SWT.NONE);
		FormLayout topLeftLayout = new FormLayout();
		topLeftLayout.marginWidth = 0;
		topLeftLayout.marginHeight = 0;
		wTopLeft.setLayout(topLeftLayout);

		// header line

		// top left tab folder
		wLeftFolder = new CTabFolder(wTopLeft, SWT.BORDER | SWT.RESIZE);
		wLeftFolder.setSimple(false);
		wLeftFolder.setUnselectedImageVisible(true);
		wLeftFolder.setUnselectedCloseVisible(true);
		wLeftFolder.setMaximizeVisible(false);
		wLeftFolder.setMinimizeVisible(true);
		props.setLook(wLeftFolder);

		styleTabFolder(wLeftFolder);

		// implement minimize logic
		wLeftFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {

			@Override
			public void minimize(CTabFolderEvent e) {
				itemSettings.setEnabled(true);
				wTop.SASH_WIDTH = 0;
				wTop.setWeights(new int[] { 0, 100 });
			}

		});

		addFieldSummaryTab();

		prevStepNames = transMeta.getPrevStepNames(stepMeta);
		addInfoStepsTab();

		nextStepNames = transMeta.getNextStepNames(stepMeta);
		addTargetStepsTab();

		// layout tab folder below the label 
		FormData fdLeftFolder = new FormData();
		fdLeftFolder.left = new FormAttachment(0, 0);
		fdLeftFolder.top = new FormAttachment(0, 0);
		fdLeftFolder.right = new FormAttachment(100, 0);
		fdLeftFolder.bottom = new FormAttachment(100, 0);
		wLeftFolder.setLayoutData(fdLeftFolder);

		// set selected item in tab
		wLeftFolder.setSelection(0);

	}

	private void styleTabFolder(CTabFolder folder) {
		//		Display display = folder.getDisplay();
		//		
		//		folder.setSelectionBackground(new Color[] {
		//		        display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
		//		        display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT) }, new int[] {75}, true);
		//		folder.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	}

	private void addScriptPopupMenu() {

		scriptMenu = new Menu(shell, SWT.POP_UP);

		MenuItem addNewItem = new MenuItem(scriptMenu, SWT.PUSH);
		addNewItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.AddNewTab"));
		addNewItem.setImage(addImage);
		addNewItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				RubyScriptMeta script = RubyScriptMeta.createScriptWithUniqueName(collectScripts());
				addScriptTab(script);
				wScriptsFolder.setSelection(wScriptsFolder.getItemCount() - 1); // select newest tab
				highlightSyntax();
			}
		});
		new MenuItem(scriptMenu, SWT.SEPARATOR);

		renameItem = new MenuItem(scriptMenu, SWT.PUSH);
		renameItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.RenameScript"));
		renameItem.setImage(renameImage);
		renameItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				RubyStepRenameDialog d = new RubyStepRenameDialog(shell);
				String newName = d.open(wScriptsFolder.getSelection().getText(), collectInactiveScriptNames());
				if (newName != null) {
					wScriptsFolder.getSelection().setText(newName);
					input.setChanged();
				}
			}
		});

		wScriptsFolder.setMenu(scriptMenu);

		scriptMenu.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event e) {
				renameItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.RenameScript", wScriptsFolder.getSelection().getText()));
			}

		});

	}

	protected List<String> collectInactiveScriptNames() {

		List<String> retval = new ArrayList<String>(wScriptsFolder.getItemCount());
		CTabItem[] items = wScriptsFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			if (i != wScriptsFolder.getSelectionIndex()) {
				retval.add(items[i].getText());
			}
		}
		return retval;

	}

	private void addScriptTab(RubyScriptMeta script) {

		CTabItem item = new CTabItem(wScriptsFolder, SWT.CLOSE);
		item.setText(script.getTitle());

		input.setChanged();
		// TODO: the image should be based on the script type
		if (wScriptsFolder.getItemCount() == 1) {
			item.setImage(rubyImage);
		}

		StyledTextComp wScript = new StyledTextComp(item.getParent(), SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL, script.getTitle());
		wScript.setText(script.getScript());

		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
		wScript.addModifyListener(lsMod);
		item.setControl(wScript);
		wScript.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				hideParseErrors();
				highlightSyntax();
			}
		});

		wScript.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				updateEditingPosition();

			}

			public void keyReleased(KeyEvent e) {
				updateEditingPosition();
			}
		});
		wScript.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				updateEditingPosition();
			}

			public void focusLost(FocusEvent e) {
				updateEditingPosition();
			}
		});
		wScript.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				updateEditingPosition();
			}

			public void mouseDown(MouseEvent e) {
				updateEditingPosition();
			}

			public void mouseUp(MouseEvent e) {
				updateEditingPosition();
			}
		});

		wScript.getStyledText().addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				updateErrorToolTip(e.x, e.y);
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
			}

			@Override
			public void mouseEnter(MouseEvent arg0) {
			}
		});

	}

	protected void updateErrorToolTip(int x, int y) {

		CTabItem item = wScriptsFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();

		parseErrorHelper.updateErrorToolTip(wText, x, y);

	}

	private void updateEditingPosition() {

		CTabItem item = wScriptsFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();

		// calculate line and col
		int caret = wText.getCaretOffset();
		int line = wText.getLineAtOffset(caret) + 1;
		int col = 1;

		String txt = wText.getText();
		caret -= 1;
		while (caret >= 0) {
			if (txt.charAt(caret) != '\n' && txt.charAt(caret) != '\r') {
				caret--;
				col++;
			} else {
				break;
			}
		}

		wlEditingPosition.setText("" + line + " : " + col);
	}

	private void highlightSyntax() {
		CTabItem item = wScriptsFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		syntaxHighlighter.highlight(item.getText(), wText);
	}

	private void checkForParseErrors() {
		CTabItem item = wScriptsFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		parseErrorHelper.showParseErrors(wText, wlSyntaxCheck);
	}

	private void hideParseErrors() {
		CTabItem item = wScriptsFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		parseErrorHelper.hideParseErrors(wText, wlSyntaxCheck);
	}
}

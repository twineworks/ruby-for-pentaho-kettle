package org.typeexit.kettle.plugin.steps.ruby;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta.RubyVersion;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepSamplesHelper.SampleType;
import org.typeexit.kettle.plugin.steps.ruby.meta.OutputFieldMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RoleStepMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta.Role;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyVariableMeta;

public class RubyStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = RubyStepMeta.class; // for i18n purposes

	private RubyStepSyntaxHighlighter syntaxHighlighter;
	private RubyStepParseErrorHelper parseErrorHelper;
	private RubyStepMeta input;
	private boolean changedInDialog;

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

	private Image initScriptImage;

	private Image disposeScriptImage;

	private Image libScriptImage;

	private MenuItem rowScriptItem;

	private MenuItem libScriptItem;

	private MenuItem initScriptItem;

	private MenuItem disposeScriptItem;

	private Image rowScriptImage;
	private Image infoStepImage;

	private Tree wTree;

	private TreeItem inputTreeItem;

	private HashMap<String, TreeItem> infoTreeItems;

	private Image inputImage;

	private Image fieldImage;

	private TreeItem inputFolderTreeItem;

	private Image folderImage;

	private TreeItem outputFolderTreeItem;

	private TreeItem outputTreeItem;

	private Image outputImage;

	private HashMap<String, TreeItem> targetTreeItems;

	private Image targetStepImage;

	private Image fieldChangedImage;

	private TreeItem errorTreeItem;

	private Image errorOutputImage;

	private Image fieldErrorImage;

	private CCombo wRubyCompat;

	private Tree wSamplesTree;

	private File pluginBaseFile;

	private Image transformationImage;

	private Image webDocumentImage;

	private RubyStepSamplesHelper samplesHelper;

	private Menu sampleMenu;

	private MenuItem showSampleItem;

	private int middle;

	private TextVar wGemHome;

	public RubyStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (RubyStepMeta) in;
		NO_YES[0] = BaseMessages.getString(PKG, "System.Combo.No");
		NO_YES[1] = BaseMessages.getString(PKG, "System.Combo.Yes");
	}

	public String open() {

		changed = input.hasChanged();
		changedInDialog = false;

		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		syntaxHighlighter = new RubyStepSyntaxHighlighter();
		parseErrorHelper = new RubyStepParseErrorHelper(input.getRubyVersion());

		props.setLook(shell);
		setShellImage(shell, input);

		lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
				changedInDialog = true;
			}
		};

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "RubyStep.Shell.Title"));

		middle = props.getMiddlePct();

		// load images
		if (!guiResource.getImageMap().containsKey("TypeExitRubyStep:empty16x16")) {
			guiResource.getImageMap().put("TypeExitRubyStep:empty16x16", new Image(display, 16, 16));
		}

		String pluginBaseDir = PluginRegistry.getInstance().findPluginWithId(StepPluginType.class, "TypeExitRubyStep").getPluginDirectory().toString();
		String pluginImageDir = pluginBaseDir + Const.FILE_SEPARATOR + "images" + Const.FILE_SEPARATOR;

		URL pluginBaseURL = PluginRegistry.getInstance().findPluginWithId(StepPluginType.class, "TypeExitRubyStep").getPluginDirectory();

		try {
			pluginBaseFile = new File(pluginBaseURL.toURI());
		} catch (URISyntaxException e) {
			pluginBaseFile = new File(pluginBaseURL.getPath());
		}

		try {

			scriptImage = guiResource.getImage(pluginImageDir + ("libScript.png"));
			checkImage = guiResource.getImage(pluginImageDir + ("check.png"));
			rubyImage = guiResource.getImage(pluginImageDir + ("ruby_16.png"));
			addImage = guiResource.getImage(pluginImageDir + ("addSmall.png"));
			renameImage = guiResource.getImage(pluginImageDir + ("edit.png"));
			rowScriptImage = rubyImage;
			initScriptImage = guiResource.getImage(pluginImageDir + ("startScript.png"));
			disposeScriptImage = guiResource.getImage(pluginImageDir + ("endScript.png"));
			libScriptImage = scriptImage;
			infoStepImage = guiResource.getImage(pluginImageDir + ("info_step.png"));
			targetStepImage = guiResource.getImage(pluginImageDir + ("target_step.png"));
			inputImage = guiResource.getImage(pluginImageDir + ("input.png"));
			outputImage = guiResource.getImage(pluginImageDir + ("output.png"));
			fieldImage = guiResource.getImage(pluginImageDir + ("field.png"));
			fieldChangedImage = guiResource.getImage(pluginImageDir + ("field_changed.png"));
			folderImage = guiResource.getImage(pluginImageDir + ("folder.png"));
			errorOutputImage = guiResource.getImage(pluginImageDir + ("error_output.png"));
			fieldErrorImage = guiResource.getImage(pluginImageDir + ("field_error.png"));
			transformationImage = guiResource.getImage(pluginImageDir + ("transformation.png"));
			webDocumentImage = guiResource.getImage(pluginImageDir + ("web.png"));
		} catch (Exception e) {
			Image empty = guiResource.getImage("TypeExitRubyStep:empty16x16");
			scriptImage = empty;
			checkImage = empty;
			rubyImage = empty;
			addImage = empty;
			renameImage = empty;
			rowScriptImage = empty;
			initScriptImage = empty;
			disposeScriptImage = empty;
			libScriptImage = empty;
			infoStepImage = empty;
			targetStepImage = empty;
			inputImage = empty;
			outputImage = empty;
			fieldImage = empty;
			fieldChangedImage = empty;
			fieldErrorImage = empty;
			folderImage = empty;
			errorOutputImage = empty;
			transformationImage = empty;
			webDocumentImage = empty;
		}

		samplesHelper = new RubyStepSamplesHelper();
		samplesHelper.setFolderImage(folderImage);
		samplesHelper.setSampleImage(fieldImage);
		samplesHelper.setScriptImage(libScriptImage);
		samplesHelper.setTransformationImage(transformationImage);
		samplesHelper.setWebDocumentImage(webDocumentImage);

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
		props.setLook(wSash);
		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Upper part of form  
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/

		// top part 
		wTop = new SashForm(wSash, SWT.HORIZONTAL);

		FormLayout topLayout = new FormLayout();
		topLayout.marginWidth = Const.FORM_MARGIN;
		topLayout.marginHeight = Const.FORM_MARGIN;
		wTop.setLayout(topLayout);
		props.setLook(wTop);

		addLeftArea();
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
		props.setLook(wSeparator);

		// bottom tab folder
		wBottomFolder = new CTabFolder(wBottom, SWT.BORDER | SWT.RESIZE);
		wBottomFolder.setSimple(false);
		wBottomFolder.setUnselectedImageVisible(true);
		wBottomFolder.setUnselectedCloseVisible(false);
		wBottomFolder.setMaximizeVisible(false);
		wBottomFolder.setMinimizeVisible(false);
		props.setLook(wBottomFolder);

		addOutputFieldsTab();

		prevStepNames = transMeta.getPrevStepNames(stepMeta);
		addInfoStepsTab();

		nextStepNames = transMeta.getNextStepNames(stepMeta);
		addTargetStepsTab();

		addScopeVariablesTab();
		addRuntimeTab();

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
				if (!cancel()) {
					e.doit = false;
				}
				;
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
		props.setLook(wPanel);

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

		CTabItem infoStepsItem = new CTabItem(wBottomFolder, SWT.NONE);
		infoStepsItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.InfoSteps.Label"));

		Composite wPanel = new Composite(wBottomFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());
		props.setLook(wPanel);

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

		CTabItem targetStepsItem = new CTabItem(wBottomFolder, SWT.NONE);
		targetStepsItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.TargetSteps.Label"));

		Composite wPanel = new Composite(wBottomFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());
		props.setLook(wPanel);

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
		props.setLook(wPanel);

		wTree = new Tree(wPanel, SWT.H_SCROLL | SWT.V_SCROLL);
		wTree.setHeaderVisible(true);
		TreeColumn column1 = new TreeColumn(wTree, SWT.LEFT);
		column1.setText(BaseMessages.getString(PKG, "RubyStepDialog.TreeColumn.Field"));
		column1.setWidth(180);
		TreeColumn column2 = new TreeColumn(wTree, SWT.LEFT);
		column2.setText(BaseMessages.getString(PKG, "RubyStepDialog.TreeColumn.Type"));
		column2.setWidth(90);
		TreeColumn column3 = new TreeColumn(wTree, SWT.LEFT);
		column3.setText(BaseMessages.getString(PKG, "RubyStepDialog.TreeColumn.AvailableAs"));
		column3.setWidth(160);
		props.setLook(wTree);

		inputFolderTreeItem = new TreeItem(wTree, SWT.NONE);
		inputFolderTreeItem.setText(new String[] { "input", "", "" });
		inputFolderTreeItem.setImage(folderImage);

		outputFolderTreeItem = new TreeItem(wTree, SWT.NONE);
		outputFolderTreeItem.setText(new String[] { "output", "", "" });
		outputFolderTreeItem.setImage(folderImage);

		infoTreeItems = new HashMap<String, TreeItem>();

		// insert markers for info streams
		for (RoleStepMeta s : input.getInfoSteps()) {
			TreeItem item = new TreeItem(inputFolderTreeItem, SWT.NONE);
			item.setText(new String[] { s.getRoleName(), "row stream", "$info_steps[\"" + s.getRoleName() + "\"]" });
			item.setImage(infoStepImage);
			infoTreeItems.put(s.getStepName(), item);
		}

		// insert markers for input stream
		inputTreeItem = new TreeItem(inputFolderTreeItem, SWT.NONE);
		inputTreeItem.setText(new String[] { "input", "row stream", "$input" });
		inputTreeItem.setImage(inputImage);

		// insert markers for target steps
		targetTreeItems = new HashMap<String, TreeItem>();

		// insert markers for target streams
		for (RoleStepMeta s : input.getTargetSteps()) {
			TreeItem item = new TreeItem(outputFolderTreeItem, SWT.NONE);
			item.setText(new String[] { s.getRoleName(), "row stream", "$target_steps[\"" + s.getRoleName() + "\"]" });
			item.setImage(targetStepImage);
			targetTreeItems.put(s.getStepName(), item);
		}

		// insert marker for output stream
		outputTreeItem = new TreeItem(outputFolderTreeItem, SWT.NONE);
		outputTreeItem.setText(new String[] { "output", "row stream", "$output" });
		outputTreeItem.setImage(outputImage);

		// insert marker for error stream
		if (input.getParentStepMeta().isDoingErrorHandling()) {
			errorTreeItem = new TreeItem(outputFolderTreeItem, SWT.NONE);
			errorTreeItem.setText(new String[] { "error", "row stream", "$error" });
			errorTreeItem.setImage(errorOutputImage);
		}

		inputFolderTreeItem.setExpanded(true);
		outputFolderTreeItem.setExpanded(true);

		final Runnable runnable = new Runnable()
		{
			public void run()
			{
				try {
					// collect main input fields
					RowMetaInterface inputFields = transMeta.getPrevStepFields(input.getParentStepMeta());

					// collect fields from input steps
					HashMap<String, RowMetaInterface> infoStepFields = new HashMap<String, RowMetaInterface>();
					for (String step : input.getStepIOMeta().getInfoStepnames()) {
						infoStepFields.put(step, transMeta.getStepFields(step));
					}

					// collect output fields
					RowMetaInterface outputFields = transMeta.getStepFields(stepname);
					RowMetaInterface errorFields = null;

					if (input.getParentStepMeta().isDoingErrorHandling()) {
						errorFields = input.getParentStepMeta().getStepErrorMeta().getErrorFields();
					}

					setTreeFields(inputFields, infoStepFields, outputFields, errorFields);
				}
				catch (KettleException e)
				{
					logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
				}
			}
		};
		new Thread(runnable).start();

		wTree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (e.button != 1)
					return;
				Point point = new Point(e.x, e.y);
				TreeItem item = wTree.getItem(point);
				if (item != null) {
					StyledTextComp wScript = (StyledTextComp) wScriptsFolder.getSelection().getControl();
					wScript.getStyledText().insert(item.getText(2));
				}

			}
		});

		FormData fdTree = new FormData();
		fdTree.left = new FormAttachment(0, 0);
		fdTree.top = new FormAttachment(0, 0);
		fdTree.right = new FormAttachment(100, 0);
		fdTree.bottom = new FormAttachment(100, 0);
		wTree.setLayoutData(fdTree);

		FormData fdPanel = new FormData();
		fdPanel.left = new FormAttachment(0, 0);
		fdPanel.top = new FormAttachment(0, 0);
		fdPanel.right = new FormAttachment(100, 0);
		fdPanel.bottom = new FormAttachment(100, 0);
		wPanel.setLayoutData(fdPanel);

		fieldSummaryItem.setControl(wPanel);

	}

	protected void setTreeFields(final RowMetaInterface inputFields, final HashMap<String, RowMetaInterface> infoFields, final RowMetaInterface outputFields, final RowMetaInterface errorFields) {

		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				inputTreeItem.removeAll();
				if (inputFields != null) {

					for (ValueMetaInterface v : inputFields.getValueMetaList()) {
						TreeItem m = new TreeItem(inputTreeItem, SWT.NONE);
						m.setText(new String[] { v.getName(), v.getTypeDesc(), "$row[\"" + v.getName() + "\"]" });
						m.setImage(fieldImage);
					}

					if (inputFields.size() == 0) {
						inputTreeItem.dispose();
					}
					else {
						inputTreeItem.setExpanded(true);
					}

				}

				for (String step : infoFields.keySet()) {
					TreeItem parent = infoTreeItems.get(step);
					if (parent != null) {
						parent.removeAll();
						RowMetaInterface stepFields = infoFields.get(step);
						for (ValueMetaInterface v : stepFields.getValueMetaList()) {
							TreeItem m = new TreeItem(parent, SWT.NONE);
							m.setText(new String[] { v.getName(), v.getTypeDesc(), "" });
							m.setImage(fieldImage);
						}
						parent.setExpanded(true);
					}
				}

				// output stream
				outputTreeItem.removeAll();
				if (outputFields != null) {

					for (ValueMetaInterface v : outputFields.getValueMetaList()) {
						TreeItem m = new TreeItem(outputTreeItem, SWT.NONE);
						m.setText(new String[] { v.getName(), v.getTypeDesc(), "" });
						m.setImage(input.addsOrChangesField(v.getName()) ? fieldChangedImage : fieldImage);
					}

					outputTreeItem.setExpanded(true);

				}

				// target steps
				for (RoleStepMeta step : input.getTargetSteps()) {
					TreeItem parent = targetTreeItems.get(step.getStepName());
					if (parent != null) {
						parent.removeAll();
						for (ValueMetaInterface v : outputFields.getValueMetaList()) {
							TreeItem m = new TreeItem(parent, SWT.NONE);
							m.setText(new String[] { v.getName(), v.getTypeDesc(), "" });
							m.setImage(input.addsOrChangesField(v.getName()) ? fieldChangedImage : fieldImage);
						}
						parent.setExpanded(true);
					}
				}

				// error stream
				if (errorFields != null && input.getParentStepMeta().isDoingErrorHandling() && errorTreeItem != null) {
					errorTreeItem.removeAll();

					if (inputFields != null) {

						for (ValueMetaInterface v : inputFields.getValueMetaList()) {
							TreeItem m = new TreeItem(errorTreeItem, SWT.NONE);
							m.setText(new String[] { v.getName(), v.getTypeDesc(), "" });
							m.setImage(fieldImage);
						}

					}

					if (errorFields != null) {

						for (ValueMetaInterface v : errorFields.getValueMetaList()) {
							TreeItem m = new TreeItem(errorTreeItem, SWT.NONE);
							m.setText(new String[] { v.getName(), v.getTypeDesc(), "" });
							m.setImage(fieldErrorImage);
						}

					}

					errorTreeItem.setExpanded(true);

				}

			}
		});

	}

	private void addRuntimeTab() {

		CTabItem runtimeItem = new CTabItem(wBottomFolder, SWT.NONE);
		runtimeItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.ExecutionModel.Label"));

		Composite wPanel = new Composite(wBottomFolder, SWT.NONE);
		wPanel.setLayout(new FormLayout());
		props.setLook(wPanel);

		Label lRubyCompat = new Label(wPanel, SWT.RIGHT);
		lRubyCompat.setText(BaseMessages.getString(PKG, "RubyStepDialog.ExecutionModel.Compatibility"));
		props.setLook(lRubyCompat);

		FormData fdlRubyCompat = new FormData();
		fdlRubyCompat.left = new FormAttachment(0, 0);
		fdlRubyCompat.right = new FormAttachment(middle, -margin);
		fdlRubyCompat.top = new FormAttachment(0, margin);
		lRubyCompat.setLayoutData(fdlRubyCompat);

		wRubyCompat = new CCombo(wPanel, SWT.READ_ONLY | SWT.BORDER);
		wRubyCompat.setItems(new String[] { "Ruby 1.8", "Ruby 1.9" });

		props.setLook(wRubyCompat);

		FormData fdRubyCompat = new FormData();
		fdRubyCompat.left = new FormAttachment(middle, 0);
		fdRubyCompat.top = new FormAttachment(0, margin);
		wRubyCompat.setLayoutData(fdRubyCompat);

		wRubyCompat.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				RubyVersion newVersion = wRubyCompat.getSelectionIndex() == 0 ? RubyVersion.RUBY_1_8 : RubyVersion.RUBY_1_9;
				syntaxHighlighter.newRubyVersionSelected(newVersion);
				parseErrorHelper.newRubyVersionSelected(newVersion);
				input.setChanged();
				changedInDialog = true;
			}
		});

		// add gem home folder entry

		Label lGemHome = new Label(wPanel, SWT.RIGHT);
		lGemHome.setText(BaseMessages.getString(PKG, "RubyStepDialog.ExecutionModel.GemHome"));
		props.setLook(lGemHome);
		FormData fdlGemHome = new FormData();
		fdlGemHome.left = new FormAttachment(0, 0);
		fdlGemHome.right = new FormAttachment(middle, -margin);
		fdlGemHome.top = new FormAttachment(wRubyCompat, margin);
		lGemHome.setLayoutData(fdlGemHome);

		wGemHome = new TextVar(transMeta, wPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wGemHome.setText(input.getGemHome());
		props.setLook(wGemHome);
		wGemHome.addModifyListener(lsMod);
		FormData fdGemHome = new FormData();
		fdGemHome.left = new FormAttachment(middle, 0);
		fdGemHome.top = new FormAttachment(wRubyCompat, margin);
		fdGemHome.right = new FormAttachment(100, -margin);
		wGemHome.setLayoutData(fdGemHome);

		runtimeItem.setControl(wPanel);

	}

	private void addScriptArea() {

		// top right composite
		Composite wTopRight = new Composite(wTop, SWT.NONE);
		FormLayout topRightLayout = new FormLayout();
		topRightLayout.marginWidth = 0;
		topRightLayout.marginHeight = 0;
		wTopRight.setLayout(topRightLayout);
		props.setLook(wTopRight);

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
							changedInDialog = true;
							break;
						}
					}
				}
		});

		addScriptFolderDnD();

		addScriptPopupMenu();

		// toolbar below the script window
		wScriptToolBar = new ToolBar(wTopRight, SWT.FLAT | SWT.RIGHT);
		props.setLook(wScriptToolBar);

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
		props.setLook(wlEditingPosition);

		FormData fdPos = new FormData();
		fdPos.left = new FormAttachment(100, -60);
		fdPos.right = new FormAttachment(100, -margin);
		fdPos.bottom = new FormAttachment(100, -margin + 1);
		wlEditingPosition.setLayoutData(fdPos);

		// syntax check result label
		wlSyntaxCheck = new Label(wTopRight, SWT.LEFT);
		wlSyntaxCheck.setText("");
		FormData fdSyntaxCheck = new FormData();
		fdSyntaxCheck.left = new FormAttachment(wScriptToolBar, margin);
		fdSyntaxCheck.bottom = new FormAttachment(100, -margin + 1);
		fdSyntaxCheck.right = new FormAttachment(wlEditingPosition, -margin);
		wlSyntaxCheck.setLayoutData(fdSyntaxCheck);
		props.setLook(wlSyntaxCheck);

	}

	private void addScriptFolderDnD() {

		wScriptsFolder.addListener(SWT.DragDetect, new Listener() {

			Display display = shell.getDisplay();

			CTabFolder folder = wScriptsFolder;
			Tracker t;

			public void handleEvent(Event e) {

				CTabItem dragItem = folder.getItem(folder.toControl(display.getCursorLocation()));
				if (dragItem == null)
					return;

				// open a tracker with current item's bounds
				t = new Tracker(folder, SWT.NONE);

				Rectangle[] rects = { dragItem.getBounds() };
				t.setRectangles(rects);

				t.addListener(SWT.Move, new Listener() {

					@Override
					public void handleEvent(Event e) {

						// when moving the tracker around, set the rectangles to whatever item we're hovering over. if hovering over nothing, no rectangles
						Point p = new Point(e.x, e.y); // display coords
						Point folderPoint = folder.toControl(p);
						CTabItem hoverItem = folder.getItem(folderPoint);
						if (hoverItem != null) {
							t.setRectangles(new Rectangle[] { hoverItem.getBounds() });
						}
						else {
							t.setRectangles(new Rectangle[0]);
						}

					}
				});

				t.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
				t.open();

				// user finished dragging the tab around, let's drop it

				Point folderPoint = folder.toControl(display.getCursorLocation());
				CTabItem dropItem = folder.getItem(folderPoint);
				if (dropItem == null || dropItem == dragItem) {
					return;
				}
				// first find out where this is going
				int idx = folder.indexOf(dropItem);
				if (folder.indexOf(dragItem) < idx) {
					idx++;
				}

				// make a copy of the item we're dragging around, as the original is going to be disposed of
				CTabItem newItem = new CTabItem(folder, dragItem.getStyle(), idx);

				newItem.setText(dragItem.getText());
				newItem.setImage(dragItem.getImage());
				Control c = dragItem.getControl();
				dragItem.setControl(null);
				newItem.setControl(c);

				newItem.setData("role", dragItem.getData("role"));

				dragItem.dispose();
				folder.setSelection(newItem);
				folder.redraw();

			}

		});

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

		// load ruby version
		wRubyCompat.select(input.getRubyVersion() == RubyVersion.RUBY_1_8 ? 0 : 1);

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

	private boolean cancel() {

		if (changedInDialog) {

			MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.APPLICATION_MODAL);
			box.setText(BaseMessages.getString(PKG, "RubyStepDialog.WarningDialogChanged.Title"));
			box.setMessage(BaseMessages.getString(PKG, "RubyStepDialog.WarningDialogChanged.Message", Const.CR));
			int answer = box.open();

			if (answer == SWT.NO) {
				return false;
			}

		}

		stepname = null;
		input.setChanged(changed);
		dispose();
		return true;
	}

	// let the plugin know about the entered data
	private void ok() {

		// if there's syntax error, warn the user
		if (!hasRowScript()) {

			MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.APPLICATION_MODAL);
			box.setText(BaseMessages.getString(PKG, "RubyStepDialog.WarningDialogNoRowScript.Title"));
			box.setMessage(BaseMessages.getString(PKG, "RubyStepDialog.WarningDialogNoRowScript.Message", Const.CR));
			int answer = box.open();

			if (answer == SWT.NO) {
				return;
			}

		}

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

		// set ruby version
		input.setRubyVersion(wRubyCompat.getSelectionIndex() == 0 ? RubyVersion.RUBY_1_8 : RubyVersion.RUBY_1_9);

		// generate ruby vars
		List<RubyVariableMeta> rubyVars = input.getRubyVariables();
		rubyVars.clear();

		int varCount = wScopeVariables.nrNonEmpty();
		for (int i = 0; i < varCount; i++) {
			TableItem t = wScopeVariables.getNonEmpty(i);
			String varName = t.getText(1).trim();
			if (!varName.startsWith("$")) {
				varName = "$" + varName;
			}
			// replace white space with underscores
			varName = varName.replaceAll("\\s", "_");
			rubyVars.add(new RubyVariableMeta(varName, t.getText(2)));
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

		// set gem home
		input.setGemHome(wGemHome.getText());

		// make sure the input finds its info and target step metas alright
		input.searchInfoAndTargetSteps(transMeta.getSteps());
		dispose();
	}

	private boolean hasRowScript() {

		for (CTabItem item : wScriptsFolder.getItems()) {
			if (item.getData("role") == RubyScriptMeta.Role.ROW_SCRIPT) {
				return true;
			}
		}

		return false;
	}

	private List<RubyScriptMeta> collectScripts() {

		List<RubyScriptMeta> retval = new ArrayList<RubyScriptMeta>(wScriptsFolder.getItemCount());
		CTabItem[] items = wScriptsFolder.getItems();
		for (int i = 0; i < items.length; i++) {

			CTabItem item = items[i];
			StyledTextComp wText = (StyledTextComp) item.getControl();
			retval.add(new RubyScriptMeta(item.getText(), wText.getText(), (Role) item.getData("role")));

		}
		return retval;

	}

	private void addLeftArea() {

		// top left composite
		wTopLeft = new Composite(wTop, SWT.NONE);
		FormLayout topLeftLayout = new FormLayout();
		topLeftLayout.marginWidth = 0;
		topLeftLayout.marginHeight = 0;
		wTopLeft.setLayout(topLeftLayout);
		props.setLook(wTopLeft);

		// header line

		// top left tab folder
		wLeftFolder = new CTabFolder(wTopLeft, SWT.BORDER | SWT.RESIZE);
		wLeftFolder.setSimple(false);
		wLeftFolder.setUnselectedImageVisible(true);
		wLeftFolder.setUnselectedCloseVisible(true);
		wLeftFolder.setMaximizeVisible(false);
		wLeftFolder.setMinimizeVisible(true);
		props.setLook(wLeftFolder);

		//styleTabFolder(wLeftFolder);

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
		addSamplesTab();

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

	private void addSamplesTab() {

		CTabItem samplesItem = new CTabItem(wLeftFolder, SWT.NONE);
		samplesItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Samples.Label"));

		Composite wPanel = new Composite(wLeftFolder, SWT.NONE);
		wPanel.setLayout(new FillLayout());
		props.setLook(wPanel);

		wSamplesTree = new Tree(wPanel, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		wSamplesTree.setLayout(new FillLayout());
		wSamplesTree.setHeaderVisible(true);
		props.setLook(wSamplesTree);

		TreeColumn column1 = new TreeColumn(wSamplesTree, SWT.LEFT);
		column1.setText(BaseMessages.getString(PKG, "RubyStepDialog.SampleTreeColumn.Sample"));
		column1.setWidth(280);
		TreeColumn column2 = new TreeColumn(wSamplesTree, SWT.LEFT);
		column2.setText(BaseMessages.getString(PKG, "RubyStepDialog.SampleTreeColumn.Type"));
		column2.setWidth(120);

		File samplesDir = new File(pluginBaseFile.getAbsolutePath() + Const.FILE_SEPARATOR + "samples");

		TreeItem rootTreeItem = new TreeItem(wSamplesTree, SWT.NONE);
		rootTreeItem.setText(new String[] { "samples", "" });
		rootTreeItem.setImage(folderImage);
		rootTreeItem.setData("type", SampleType.DIR);
		rootTreeItem.setData("file", samplesDir);

		samplesHelper.fillSamplesDir(rootTreeItem, samplesDir);
		rootTreeItem.setExpanded(true);

		samplesItem.setControl(wPanel);

		wSamplesTree.addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent e) {
				if (e.button != 1)
					return;
				Point click = new Point(e.x, e.y);
				TreeItem item = wSamplesTree.getItem(click);
				if (item != null) {
					openSample(item);
				}

			}

		});

		addSamplesMenu();

	}

	private void addSamplesMenu() {

		sampleMenu = new Menu(shell, SWT.POP_UP);

		showSampleItem = new MenuItem(sampleMenu, SWT.PUSH);
		showSampleItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.SamplesMenu.ShowSample"));
		//showSampleItem.setImage(libScriptImage);
		showSampleItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				for (TreeItem item : wSamplesTree.getSelection()) {
					openSample(item);
				}
			}
		});

		// menu is about to show, let's enable/disable stuff
		sampleMenu.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event e) {
				showSampleItem.setEnabled(false);
				// any non-folder selection enables the show sample 
				for (TreeItem item : wSamplesTree.getSelection()) {
					switch ((SampleType) item.getData("type")) {
					case TRANS:
					case SCRIPT:
					case WEB:
						showSampleItem.setEnabled(true);
						return;
					}
				}
			}
		});

		wSamplesTree.setMenu(sampleMenu);

	}

	private void openSample(TreeItem item) {

		File f = (File) item.getData("file");
		if (f != null && f.isFile()) {

			SampleType type = (SampleType) item.getData("type");
			switch (type) {

			case SCRIPT:
				try {
					// add a new sample script
					String title = RubyScriptMeta.getUniqueName(item.getText(), collectScripts());

					RubyScriptMeta script = new RubyScriptMeta(title, FileUtils.readFileToString(f, "UTF-8"), Role.LIB_SCRIPT);
					addScriptTab(script);
					wScriptsFolder.setSelection(wScriptsFolder.getItemCount() - 1); // select newest tab

					input.setChanged();
					changedInDialog = true;

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case TRANS:
				// load the file, but make it forget where it came from (prevents the original from be modified accidentally)
				// File tmpFile = new File(System.getProperty("java.io.tmpdir") + Const.FILE_SEPARATOR + f.getName());
				// FileUtils.copyFile(f, tmpFile);
				// Spoon.getInstance().openFile(tmpFile.toString(), false);
				Spoon.getInstance().openFile(f.toString(), false);

				// assuming that the new one is the last one... a shame openFile does not return the opened TransMeta
				TransMeta[] transForms = Spoon.getInstance().getLoadedTransformations();
				TransMeta newOne = transForms[transForms.length - 1];

				// make it a prestine transformation
				newOne.setFilename(null);
				newOne.setChanged();

				if (changedInDialog) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
					messageBox.setText(BaseMessages.getString(PKG, "RubyStepDialog.OpenedSample.Title"));
					messageBox.setMessage(BaseMessages.getString(PKG, "RubyStepDialog.OpenedSample.Message"));
					if (messageBox.open() == SWT.YES) {
						ok();
					}
				} else {
					cancel();
				}

				break;
			case WEB:
				try {
					BareBonesBrowserLaunch.openURL(f.toURI().toURL().toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				break;
			}

		}

	}

	private void styleTabFolder(CTabFolder folder) {
		Display display = folder.getDisplay();

		folder.setSelectionBackground(new Color[] {
							display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
							display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT)
						},
						new int[] { 75 }, true);
		folder.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
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
				input.setChanged();
				changedInDialog = true;

				wScriptsFolder.setSelection(wScriptsFolder.getItemCount() - 1); // select newest tab

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
					changedInDialog = true;
				}
			}
		});
		new MenuItem(scriptMenu, SWT.SEPARATOR);

		rowScriptItem = new MenuItem(scriptMenu, SWT.RADIO);
		rowScriptItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.RowScript"));
		rowScriptItem.setImage(rowScriptImage);
		rowScriptItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					setActiveTabRowScript();
				}
			}
		});

		libScriptItem = new MenuItem(scriptMenu, SWT.RADIO);
		libScriptItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.LibScript"));
		libScriptItem.setImage(libScriptImage);
		libScriptItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					setActiveTabLibScript();
				}
			}
		});

		initScriptItem = new MenuItem(scriptMenu, SWT.RADIO);
		initScriptItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.InitScript"));
		initScriptItem.setImage(initScriptImage);
		initScriptItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					setActiveTabInitScript();
				}
			}
		});

		disposeScriptItem = new MenuItem(scriptMenu, SWT.RADIO);
		disposeScriptItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.DisposeScript"));
		disposeScriptItem.setImage(disposeScriptImage);
		disposeScriptItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					setActiveTabDisposeScript();
				}
			}
		});

		wScriptsFolder.setMenu(scriptMenu);

		// menu is about to show, let's enable/disable stuff
		scriptMenu.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event e) {
				// set the label on the rename item 
				renameItem.setText(BaseMessages.getString(PKG, "RubyStepDialog.Menu.RenameScript", wScriptsFolder.getSelection().getText()));

				// set selection for the correct role
				rowScriptItem.setSelection(false);
				libScriptItem.setSelection(false);
				initScriptItem.setSelection(false);
				disposeScriptItem.setSelection(false);

				switch ((Role) wScriptsFolder.getSelection().getData("role")) {
				case LIB_SCRIPT:
						libScriptItem.setSelection(true);
						break;
					case ROW_SCRIPT:
						rowScriptItem.setSelection(true);
						break;
					case INIT_SCRIPT:
						initScriptItem.setSelection(true);
						break;
					case DISPOSE_SCRIPT:
						disposeScriptItem.setSelection(true);
						break;
					}

				}

		});

	}

	protected void setActiveTabRowScript() {

		CTabItem[] items = wScriptsFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			if (i != wScriptsFolder.getSelectionIndex() && items[i].getData("role") == Role.ROW_SCRIPT) {
				// the old script gets its row script status revoked, simply replaced with lib
				items[i].setData("role", Role.LIB_SCRIPT);
				items[i].setImage(libScriptImage);
			}
		}

		wScriptsFolder.getSelection().setData("role", Role.ROW_SCRIPT);
		wScriptsFolder.getSelection().setImage(rowScriptImage);
		input.setChanged();
		changedInDialog = true;

	}

	protected void setActiveTabInitScript() {

		CTabItem[] items = wScriptsFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			if (i != wScriptsFolder.getSelectionIndex() && items[i].getData("role") == Role.INIT_SCRIPT) {
				// the old script gets its init script status revoked, simply replaced with lib
				items[i].setData("role", Role.LIB_SCRIPT);
				items[i].setImage(libScriptImage);
			}
		}

		wScriptsFolder.getSelection().setData("role", Role.INIT_SCRIPT);
		wScriptsFolder.getSelection().setImage(initScriptImage);
		input.setChanged();
		changedInDialog = true;

	}

	protected void setActiveTabDisposeScript() {

		CTabItem[] items = wScriptsFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			if (i != wScriptsFolder.getSelectionIndex() && items[i].getData("role") == Role.DISPOSE_SCRIPT) {
				// the old script gets its dispose script status revoked, simply replaced with lib
				items[i].setData("role", Role.LIB_SCRIPT);
				items[i].setImage(libScriptImage);
			}
		}

		wScriptsFolder.getSelection().setData("role", Role.DISPOSE_SCRIPT);
		wScriptsFolder.getSelection().setImage(disposeScriptImage);
		input.setChanged();
		changedInDialog = true;

	}

	protected void setActiveTabLibScript() {

		wScriptsFolder.getSelection().setData("role", Role.LIB_SCRIPT);
		wScriptsFolder.getSelection().setImage(libScriptImage);
		input.setChanged();
		changedInDialog = true;

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

		switch (script.getRole()) {
		case DISPOSE_SCRIPT:
			item.setImage(disposeScriptImage);
			break;
		case INIT_SCRIPT:
			item.setImage(initScriptImage);
			break;
		case LIB_SCRIPT:
			item.setImage(libScriptImage);
			break;
		case ROW_SCRIPT:
			item.setImage(rowScriptImage);
			break;
		}

		item.setData("role", script.getRole());

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

		wScript.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event e) {
				highlightSyntax();
				hideParseErrors();
			}
		}
				);

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

	private boolean checkForParseErrors() {

		boolean hasErrors = false;

		for (CTabItem item : wScriptsFolder.getItems()) {
			StyledTextComp wText = (StyledTextComp) item.getControl();
			if (parseErrorHelper.hasParseErrors(wText)) {
				wScriptsFolder.setSelection(item);
				parseErrorHelper.showParseErrors(wText, wlSyntaxCheck);
				hasErrors = true;
				break;
			}
		}

		if (!hasErrors) {
			wlSyntaxCheck.setText("OK");
		}
		return !hasErrors;
	}

	private void hideParseErrors() {
		CTabItem item = wScriptsFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		parseErrorHelper.hideParseErrors(wText, wlSyntaxCheck);
	}
}

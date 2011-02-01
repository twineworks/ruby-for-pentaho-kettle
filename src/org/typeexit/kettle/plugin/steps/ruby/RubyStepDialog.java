package org.typeexit.kettle.plugin.steps.ruby;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

public class RubyStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = RubyStepMeta.class; // for i18n purposes

	private RubyStepSyntaxHighlighter syntaxHighlighter;
	private RubyStepParseErrorHelper parseErrorHelper;
	private RubyStepMeta input;
	
	// output field name

	private CTabFolder wFolder;

	private ModifyListener lsMod;

	private Tree wTree;

	private TableView wFields;

	private Button wClearResultFields;

	private Label wlEditingPosition;
	private Composite wTopLeft;

	private GUIResource guiResource = GUIResource.getInstance();

	private Label wlSyntaxCheck;

	private Label wlScriptFunctions;
	
	int margin = Const.MARGIN;

	private CTabFolder wLeftFolder;

	private SashForm wTop;

	private ToolItem itemSettings;

	private ToolBar wBar;

	public RubyStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (RubyStepMeta) in;
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

		SashForm wSash = new SashForm(shell, SWT.VERTICAL );
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Top form  
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/

		// top part 
		wTop = new SashForm(wSash, SWT.HORIZONTAL);
 		props.setLook(wTop);

		FormLayout topLayout  = new FormLayout();
		topLayout.marginWidth  = Const.FORM_MARGIN;
		topLayout.marginHeight = Const.FORM_MARGIN;
		wTop.setLayout(topLayout);
		
		// top left part
		wTopLeft = new Composite(wTop, SWT.NONE);
		FormLayout topLeftLayout  = new FormLayout();
		topLeftLayout.marginWidth  = 0;
		topLeftLayout.marginHeight = 0;
		wTopLeft.setLayout(topLeftLayout);		
		
		// Tree header line
		wlScriptFunctions = new Label(wTopLeft, SWT.NONE);
		wlScriptFunctions.setText(BaseMessages.getString(PKG, "RubyStepDialog.Tree.Label")); //$NON-NLS-1$
		props.setLook(wlScriptFunctions);
		FormData fdlScriptFunctions = new FormData();
		fdlScriptFunctions.left = new FormAttachment(0, 0);
		fdlScriptFunctions.top  = new FormAttachment(0, 0);
		wlScriptFunctions.setLayoutData(fdlScriptFunctions);
				
		// Tree View 
//		wTree = new Tree(wTopLeft, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
//		props.setLook(wTree);
//	    FormData fdlTree = new FormData();
//		fdlTree.left = new FormAttachment(0, 0);
//		fdlTree.top  = new FormAttachment(wlScriptFunctions, margin);
//		fdlTree.right = new FormAttachment(100, 0);
//		fdlTree.bottom = new FormAttachment(100, 0);
//		wTree.setLayoutData(fdlTree);

		addLeftBar();
		
		// top right part
		Composite wTopRight = new Composite(wTop, SWT.NONE);
		FormLayout topRightLayout  = new FormLayout();
		topRightLayout.marginWidth  = 0;
		topRightLayout.marginHeight = 0;
		wTopRight.setLayout(topRightLayout);		
		
		// Script folder header line
		Label wlScript = new Label(wTopRight, SWT.NONE);
		wlScript.setText(BaseMessages.getString(PKG, "RubyStepDialog.Script.Label")); //$NON-NLS-1$
		props.setLook(wlScript);
		FormData fdlScript = new FormData();
		fdlScript.left = new FormAttachment(0, 0);
		fdlScript.top  = new FormAttachment(0, 0);
		wlScript.setLayoutData(fdlScript);
		
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Script folder 
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/

		wFolder = new CTabFolder(wTopRight, SWT.BORDER | SWT.RESIZE);
		wFolder.setSimple(false);
		wFolder.setUnselectedImageVisible(true);
		wFolder.setUnselectedCloseVisible(true);
		
		wBar = new ToolBar(wTopRight, SWT.FLAT|SWT.RIGHT);

		itemSettings = new ToolItem(wBar, SWT.NONE);
		itemSettings.setImage(guiResource.getImage("ui/images/eScript.png"));
		itemSettings.setText("Advanced Settings");
		itemSettings.setEnabled(false);
		
		itemSettings.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				wTop.SASH_WIDTH = margin;
				wTop.setWeights(new int[]{32,68});
				//itemSettings.setText(" ");
				itemSettings.setEnabled(false);
			}
		});			
		
		ToolItem item = new ToolItem(wBar, SWT.NONE);
		item.setImage(guiResource.getImage("ui/images/check.png"));
		item.setText(BaseMessages.getString(PKG, "RubyStepDialog.CheckSyntax.Label"));
		
		item.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				showParseErrors();
			}
		});

		wBar.pack();
		
		wlEditingPosition = new Label(wTopRight, SWT.RIGHT);
		wlEditingPosition.setText("0 : 0 ");
		
		FormData fdPos= new FormData();
		fdPos.left = new FormAttachment(100, -60);
		fdPos.right = new FormAttachment(100, -margin);
		fdPos.bottom = new FormAttachment(100, -margin+1);
		wlEditingPosition.setLayoutData(fdPos);	
		
		wlSyntaxCheck = new Label(wTopRight, SWT.LEFT);
		wlSyntaxCheck.setText("");
		FormData fdSyntaxCheck = new FormData();
		fdSyntaxCheck.left = new FormAttachment(wBar, 0);
		fdSyntaxCheck.bottom = new FormAttachment(100,-margin+1);
		fdSyntaxCheck.right = new FormAttachment(wlEditingPosition, -margin);
		wlSyntaxCheck.setLayoutData(fdSyntaxCheck);
		
		FormData fdBar = new FormData();
		fdBar.left = new FormAttachment(0, 0);
		fdBar.bottom = new FormAttachment(100, 0);
		wBar.setLayoutData(fdBar);	
		
		
		FormData fdFolder = new FormData();
		fdFolder.left = new FormAttachment(0, 0);
		fdFolder.right = new FormAttachment(100, 0);
		fdFolder.top = new FormAttachment(wlScript, margin);
		fdFolder.bottom = new FormAttachment(wBar, 0);
		wFolder.setLayoutData(fdFolder);
		
		FormData fdTop = new FormData();
		fdTop.left  = new FormAttachment(0, 0);
		fdTop.top   = new FormAttachment(0, 0);
		fdTop.right = new FormAttachment(100, 0);
		fdTop.bottom= new FormAttachment(100, 0);
		wTop.setLayoutData(fdTop);		
		
		wTop.SASH_WIDTH = margin;
		wTop.setWeights(new int[] {31,69});
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Bottom part of form 
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		Composite wBottom = new Composite(wSash, SWT.NONE);
 		props.setLook(wBottom);
		
		FormLayout bottomLayout  = new FormLayout ();
		bottomLayout.marginWidth  = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);
		
		Label wSeparator = new Label(wBottom, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fdSeparator = new FormData();
		fdSeparator.left  = new FormAttachment(0, 0);
		fdSeparator.right = new FormAttachment(100, 0);
		fdSeparator.top   = new FormAttachment(0, -margin);
		wSeparator.setLayoutData(fdSeparator);
		
		Label wlFields = new Label(wBottom, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "RubyStepDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wSeparator, 0);
		wlFields.setLayoutData(fdlFields);
		
		wClearResultFields = new Button(wBottom, SWT.CHECK);
		wClearResultFields.setText(BaseMessages.getString(PKG, "RubyStepDialog.ClearFields.Label")); //$NON-NLS-1$
		props.setLook(wClearResultFields);
		FormData fdClearResultFields = new FormData();
		fdClearResultFields.right = new FormAttachment(100, 0);
		fdClearResultFields.top = new FormAttachment(wSeparator, 0);
		wClearResultFields.setLayoutData(fdClearResultFields);		
		
		final int fieldsRows=0;
		
		ColumnInfo[] colinf=new ColumnInfo[]{
    		 new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.Fieldname"),			ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.Type"),				ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ), //$NON-NLS-1$
    		 new ColumnInfo(BaseMessages.getString(PKG, "RubyStepDialog.ColumnInfo.UpdateExisting"),	ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") }) //$NON-NLS-1$
		};
		
		wFields=new TableView(
			transMeta,
			wBottom, 
			SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
			colinf, 
			fieldsRows,  
			lsMod,
			props
		);
		
		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);

		FormData fdBottom = new FormData();
		fdBottom.left  = new FormAttachment(0, 0);
		fdBottom.top   = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom= new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------
		 * Wrapping up form 
		 ------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		FormData fdSash = new FormData();
		fdSash.left  = new FormAttachment(0, 0);
		fdSash.top   = new FormAttachment(wStepname, margin);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom= new FormAttachment(100, -50);
		wSash.setLayoutData(fdSash);
		
		wSash.SASH_WIDTH = margin;
		wSash.setWeights(new int[] {75,25});		
		
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
		if (wFolder.getItemCount() == 0){
			addScriptTab(RubyScriptMeta.DEFAULT_SCRIPT);
		}
		
		wFolder.setSelection(wFolder.getItem(0));
		
		highlightSyntax();
		
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	// Read data and place it in the dialog
	public void getData() {
		
		wStepname.selectAll();
		
		// load the different scripts
		List<RubyScriptMeta> scripts = input.getScripts();
		for (RubyScriptMeta rubyScriptMeta : scripts) {
			addScriptTab(rubyScriptMeta);
		}
		
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
		
		CTabItem[] items = wFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			
			CTabItem item = items[i];
			StyledTextComp wText = (StyledTextComp) item.getControl();
			scripts.add(new RubyScriptMeta(item.getText(), wText.getText()));
			
		}
		
		dispose();
	}
	
	private void addLeftBar(){
		
		wLeftFolder = new CTabFolder(wTopLeft, SWT.BORDER | SWT.RESIZE);
		wLeftFolder.setSimple(false);
		wLeftFolder.setUnselectedImageVisible(true);
		wLeftFolder.setUnselectedCloseVisible(true);
		wLeftFolder.setMaximizeVisible(false);
		wLeftFolder.setMinimizeVisible(true);
		props.setLook(wLeftFolder);

		// add item
		CTabItem settingsItem = new CTabItem(wLeftFolder, SWT.NONE);		
		settingsItem.setText("Settings");
		
		Label wSettingsLabel = new Label(wLeftFolder, SWT.CENTER); 
		wSettingsLabel.setText("Settings");
		
		settingsItem.setControl(wSettingsLabel);
		
		CTabItem sampleItem = new CTabItem(wLeftFolder, SWT.NONE);		
		sampleItem.setText("Samples");
		
		Label wSamplesLabel = new Label(wLeftFolder, SWT.CENTER); 
		wSamplesLabel.setText("Samples");
		
		sampleItem.setControl(wSamplesLabel);
		
		wLeftFolder.setSelection(settingsItem);
		wLeftFolder.addCTabFolder2Listener(new CTabFolder2Listener() {
			
			@Override
			public void showList(CTabFolderEvent e) {
			}
			
			@Override
			public void restore(CTabFolderEvent e) {
			}
			
			@Override
			public void minimize(CTabFolderEvent e) {
				itemSettings.setEnabled(true);
				wTop.SASH_WIDTH = 0;
				wTop.setWeights(new int[]{0,100});
			}
			
			@Override
			public void maximize(CTabFolderEvent e) {
			}
			
			@Override
			public void close(CTabFolderEvent e) {
			}
		});
		
		FormData fdLeftFolderBar = new FormData();
		fdLeftFolderBar.left = new FormAttachment(0, 0);
		fdLeftFolderBar.top  = new FormAttachment(wlScriptFunctions, margin);
		fdLeftFolderBar.right = new FormAttachment(100, 0);
		fdLeftFolderBar.bottom = new FormAttachment(100, 0);
		wLeftFolder.setLayoutData(fdLeftFolderBar);
		
		
	}
	
	private void addScriptTab(RubyScriptMeta script){
		
		CTabItem item = new CTabItem(wFolder, SWT.CLOSE);		
		item.setText(script.getTitle());
		StyledTextComp wScript = new StyledTextComp(item.getParent(), SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL, script.getTitle());
		wScript.setText(script.getScript());
		
		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
		wScript.addModifyListener(lsMod);
		item.setControl(wScript);
		wScript.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				hideParseErrors();
				highlightSyntax();
			}}
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
				updateToolTipText(e.x, e.y);
			}
			
			@Override
			public void mouseExit(MouseEvent arg0) {
			}
			
			@Override
			public void mouseEnter(MouseEvent arg0) {
			}
		});

	}
	

	protected void updateToolTipText(int x, int y) {
		
		CTabItem item = wFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		
		parseErrorHelper.updateErrorToolTip(wText, x, y);
		
	}

	private void updateEditingPosition(){

		CTabItem item = wFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		
		// calculate line and col
		int caret = wText.getCaretOffset();
		int line = wText.getLineAtOffset(caret)+1;
		int col = 1;
		
		String txt = wText.getText();
		caret -= 1;
		while(caret >= 0){
			if (txt.charAt(caret) != '\n' && txt.charAt(caret) != '\r'){
				caret--;
				col++;
			}
			else{
				break;
			}
		}
		
		wlEditingPosition.setText(""+line+" : "+col);
	}
	
	private void highlightSyntax(){
		CTabItem item = wFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		syntaxHighlighter.highlight(item.getText(), wText);
	}
	
	private void showParseErrors(){
		CTabItem item = wFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		parseErrorHelper.showParseErrors(wText, wlSyntaxCheck);
	}

	private void hideParseErrors() {
		CTabItem item = wFolder.getSelection();
		StyledTextComp wText = (StyledTextComp) item.getControl();
		parseErrorHelper.hideParseErrors(wText, wlSyntaxCheck);
	}
}

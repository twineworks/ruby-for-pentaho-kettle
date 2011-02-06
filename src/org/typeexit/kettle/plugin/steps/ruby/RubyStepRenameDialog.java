package org.typeexit.kettle.plugin.steps.ruby;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.i18n.BaseMessages;

public class RubyStepRenameDialog extends Dialog {

	Shell shell;
	Text text;
	
	String newName;
	List<String> namesTaken;
	boolean nameOk;
	
	private static Class<?> PKG = RubyStepMeta.class; // for i18n purposes

	public RubyStepRenameDialog(Shell parent) {
		super(parent);
	}

	public RubyStepRenameDialog(Shell parent, int style) {
		super(parent, style);
	}
	
	private void cancel(){
		newName = null;
		shell.dispose();
	}
	
	private void ok(){
		newName = text.getText();
		shell.dispose();
	}

	public String open(String preset, List<String> takenNames) {
		
		namesTaken = takenNames;
		
		Shell parent = getParent();
		shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		
		shell.setText(BaseMessages.getString(PKG, "RubyStepDialog.RenameScript", preset));
		shell.setLayout(new GridLayout(2, true));

		text = new Text(shell, SWT.SINGLE | SWT.BORDER);
		
		GridData d = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL);
		d.horizontalSpan = 2;
		text.setLayoutData(d);

		final Button buttonOK = new Button(shell, SWT.PUSH);
		buttonOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		Button buttonCancel = new Button(shell, SWT.PUSH);
		buttonCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				
				nameOk = !namesTaken.contains(text.getText());
				buttonOK.setEnabled(nameOk);
					
			}
		});

		SelectionAdapter lsDef=new SelectionAdapter() { 
			public void widgetDefaultSelected(SelectionEvent e) {
				if (nameOk){
					ok();	
				}
			} 
			
		};
		
		text.addSelectionListener(lsDef);
		buttonOK.addSelectionListener(lsDef);

		buttonCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				cancel();
			}
		});
		
		buttonOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (nameOk){
					ok();	
				}
			}
		});		

		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE){
					cancel();
				}
			}
		});

		// initially the name is fine
		nameOk = true;
		text.setText(preset);

		shell.setDefaultButton(buttonOK);
		
		Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (size.x < 220){
			size.x = 220;
		}
		
		shell.pack();
		shell.layout();
		
		Point cursorPos = shell.getDisplay().getCursorLocation();
		cursorPos.x -= text.getLocation().x;
		cursorPos.y -= text.getLocation().y;
		shell.setLocation(cursorPos);
		shell.open();

		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return newName;
	}
}

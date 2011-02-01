package org.typeexit.kettle.plugin.steps.ruby;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.*;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.*;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

public class RubyStepMeta extends BaseStepMeta implements StepMetaInterface {

	private static final String TAB = "\t";
	private static Class<?> PKG = RubyStepMeta.class; // for i18n purposes
	private List<RubyScriptMeta> scripts;

	public RubyStepMeta() {
		super();
		setDefault();
	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Row Stream Handling 
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

		// do not change row structure

	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		CheckResult cr;

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}

	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Initialization 
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public void setDefault() {
		scripts = new ArrayList<RubyScriptMeta>();
	}

	public RubyStepMeta clone() {
		RubyStepMeta retval = (RubyStepMeta) super.clone();

		// clone scripts 
		if (scripts != null) {
			List<RubyScriptMeta> newScripts = new ArrayList<RubyScriptMeta>();
			for (RubyScriptMeta rubyScriptMeta : scripts) {
				newScripts.add(rubyScriptMeta.clone());
			}
			retval.setScripts(newScripts);
		}

		return retval;
	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Serialization to XML	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public String getXML() throws KettleValueException {

		StringBuffer retval = new StringBuffer(1000);

		retval.append(TAB).append("<scripts>").append(Const.CR);

		for (RubyScriptMeta script : scripts) {
			retval.append(TAB).append(TAB).append("<script>").append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("title", script.getTitle())).append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("body", script.getScript())).append(Const.CR);
			retval.append(TAB).append(TAB).append("</script>").append(Const.CR);
		}

		retval.append(TAB).append("</scripts>").append(Const.CR);
		return retval.toString();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {

			scripts.clear();

			Node scriptNode = XMLHandler.getSubNode(stepnode, "scripts");
			int nrScripts = XMLHandler.countNodes(scriptNode, "script");

			for (int i = 0; i < nrScripts; i++) {
				Node sNode = XMLHandler.getSubNodeByNr(scriptNode, "script", i);
				scripts.add(new RubyScriptMeta(XMLHandler.getTagValue(sNode, "title"), XMLHandler.getTagValue(sNode, "body")));
			}

		} catch (Exception e) {
			throw new KettleXMLException("Template Plugin Unable to read step info from XML node", e);
		}

	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Serialization to Database	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			
			int nrScripts = rep.countNrStepAttributes(id_step, "script_title");
			scripts.clear();
			
			for (int i = 0; i < nrScripts; i++) {
				RubyScriptMeta s = new RubyScriptMeta(
						rep.getStepAttributeString(id_step, i, "script_title"),
						rep.getStepAttributeString(id_step, i, "script_body")
				);
				scripts.add(s);
			}
			
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "RubyStep.Exception.UnexpectedErrorInReadingStepInfo"), e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			
			for (int i = 0; i < scripts.size(); i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "script_title", scripts.get(i).getTitle());
				rep.saveStepAttribute(id_transformation, id_step, i, "script_body", scripts.get(i).getScript());
			}
			
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "RubyStep.Exception.UnableToSaveStepInfoToRepository") + id_step, e);
		}
	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Interface access 	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new RubyStepDialog(shell, meta, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new RubyStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	public StepDataInterface getStepData() {
		return new RubyStepData();
	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Getters and Setters 	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public List<RubyScriptMeta> getScripts() {
		return scripts;
	}

	public void setScripts(List<RubyScriptMeta> scripts) {
		this.scripts = scripts;
	}

}

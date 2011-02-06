package org.typeexit.kettle.plugin.steps.ruby;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.typeexit.kettle.plugin.steps.ruby.meta.OutputFieldMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RoleStepMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta.Role;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyVariableMeta;
import org.w3c.dom.Node;

public class RubyStepMeta extends BaseStepMeta implements StepMetaInterface {

	private static final String TAB = "\t";
	private static Class<?> PKG = RubyStepMeta.class; // for i18n purposes

	private List<RubyScriptMeta> scripts;
	private List<RoleStepMeta> infoSteps;
	private List<RoleStepMeta> targetSteps;
	private List<OutputFieldMeta> outputFields;
	private List<RubyVariableMeta> rubyVariables;
	private List<ValueMetaInterface> affectedFields;
	private boolean clearInputFields;

	public RubyStepMeta() {
		super();
		setDefault();
	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Row Stream Handling 
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {
		
		affectedFields = new ArrayList<ValueMetaInterface>(outputFields.size());
		
		if (clearInputFields){
			r.clear();
		}
		
		for (OutputFieldMeta field : outputFields) {

			if (field.isUpdate()){
				// no updates possible if all fields have been cleared before
				if (clearInputFields){
					throw new IllegalStateException("Field "+field.getName()+" cannot be updated because all input fields were requested to be removed!");	
				}
				
				// have a look at the existing field
				int idx = r.indexOfValue(field.getName());
				
				// if the field is not there, bail out
				if (idx < 0){
					throw new IllegalStateException("Field "+field.getName()+ "cannot be updated. I cannot find it!");
				}
				
				ValueMetaInterface v = r.getValueMeta(idx);
				if (v.getType() != field.getType()){
					// this field needs to be converted to another type
					ValueMetaInterface newValueMeta = new ValueMeta(field.getName(), field.getType());
					newValueMeta.setOrigin(origin);
					r.setValueMeta(idx, newValueMeta);
				}
				
				// add the field to affected fields
				affectedFields.add(r.getValueMeta(idx));				
				
			}
			else{
				// new field
				ValueMetaInterface v = new ValueMeta(field.getName(), field.getType());
				v.setOrigin(origin);
				r.addValueMeta(v);
				
				// add the last to affected fields (might have been renamed)
				v = r.getValueMetaList().get(r.getValueMetaList().size()-1);
				field.setName(v.getName());
				affectedFields.add(v);
				
			}
			
		}
		
	}
	
    public boolean excludeFromRowLayoutVerification(){
        return true;
    }	
	
	@Override
    public StepIOMetaInterface getStepIOMeta() {

		StepIOMetaInterface ioMeta = new StepIOMeta(true, true, true, false, !infoSteps.isEmpty(), !targetSteps.isEmpty());
    	
    	for (RoleStepMeta step : infoSteps) {
    		ioMeta.addStream(new Stream(StreamType.INFO, step.getStepMeta(), step.getRoleName(), StreamIcon.INFO, null));
    	}
    	for (RoleStepMeta step : targetSteps) {
    		ioMeta.addStream(new Stream(StreamType.TARGET, step.getStepMeta(), step.getRoleName(), StreamIcon.TARGET, null));
    	}
    	
    	return ioMeta;
    }

	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (RoleStepMeta info : infoSteps) {
			info.setStepMeta(StepMeta.findStep(steps, info.getStepName()));
		}
		for (RoleStepMeta target : targetSteps) {
			target.setStepMeta(StepMeta.findStep(steps, target.getStepName()));
		}
	}	

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		CheckResult cr;

		// TODO: check if all updated fields are there
		// TODO: check if any field is updated even though the incoming fields are cleared
		
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
	 * Initialization & Cloning
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public void setDefault() {
		scripts = new ArrayList<RubyScriptMeta>();
		infoSteps = new ArrayList<RoleStepMeta>();
		targetSteps = new ArrayList<RoleStepMeta>();
		outputFields = new ArrayList<OutputFieldMeta>();
		rubyVariables = new ArrayList<RubyVariableMeta>();
		clearInputFields = false;
	}

	public RubyStepMeta clone() {
		RubyStepMeta retval = (RubyStepMeta) super.clone();

		// clone scripts 
		if (scripts != null) {
			List<RubyScriptMeta> newScripts = new ArrayList<RubyScriptMeta>(scripts.size());
			for (RubyScriptMeta rubyScriptMeta : scripts) {
				newScripts.add(rubyScriptMeta.clone());
			}
			retval.setScripts(newScripts);
		}
		
		// clone input steps
		if (infoSteps != null){
			List<RoleStepMeta> newInfoSteps = new ArrayList<RoleStepMeta>(infoSteps.size());
			for(RoleStepMeta step : infoSteps){
				newInfoSteps.add(step.clone());
			}
			retval.setInfoSteps(newInfoSteps);
		}
		
		// clone target steps
		if (targetSteps != null){
			List<RoleStepMeta> newTargetSteps = new ArrayList<RoleStepMeta>(targetSteps.size());
			for(RoleStepMeta step : targetSteps){
				newTargetSteps.add(step.clone());
			}
			retval.setTargetSteps(newTargetSteps);
		}		
		
		// clone output fields
		if (outputFields != null){
			List<OutputFieldMeta> newOutputFields = new ArrayList<OutputFieldMeta>(outputFields.size());
			for(OutputFieldMeta outputField : outputFields){
				newOutputFields.add(outputField.clone());
			}
			retval.setOutputFields(newOutputFields);
		}
		
		// clone ruby variables
		if (rubyVariables != null){
			List<RubyVariableMeta> newRubyVariables = new ArrayList<RubyVariableMeta>(rubyVariables.size());
			for(RubyVariableMeta rubyVar : rubyVariables){
				newRubyVariables.add(rubyVar.clone());
			}
			retval.setRubyVariables(newRubyVariables);
		}
		

		return retval;
	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Serialization to XML	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public String getXML() throws KettleValueException {

		StringBuffer retval = new StringBuffer(2000);

		// save scripts
		retval.append(TAB).append("<scripts>").append(Const.CR);

		for (RubyScriptMeta script : scripts) {
			retval.append(TAB).append(TAB).append("<script>").append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("title", script.getTitle())).append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("body", script.getScript())).append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("role", script.getRole().toString())).append(Const.CR);
			retval.append(TAB).append(TAB).append("</script>").append(Const.CR);
		}

		retval.append(TAB).append("</scripts>").append(Const.CR);
		
		// save clear input fields flag
		retval.append(TAB).append(XMLHandler.addTagValue("clearInputFields", clearInputFields)).append(Const.CR);
		
		// save output fields
		retval.append(TAB).append("<outputFields>").append(Const.CR);
		for(OutputFieldMeta out : outputFields){
			retval.append(TAB).append(TAB).append("<outputField>").append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("name", out.getName()));
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("type", out.getType()));
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("update", out.isUpdate()));
			retval.append(TAB).append(TAB).append("</outputField>").append(Const.CR);
		}
		retval.append(TAB).append("</outputFields>").append(Const.CR);
		
		// save ruby variables
		retval.append(TAB).append("<rubyVariables>").append(Const.CR);
		for(RubyVariableMeta rvar : rubyVariables){
			retval.append(TAB).append(TAB).append("<rubyVariable>").append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("name", rvar.getName()));
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("value", rvar.getValue()));
			retval.append(TAB).append(TAB).append("</rubyVariable>").append(Const.CR);
		}
		retval.append(TAB).append("</rubyVariables>").append(Const.CR);
		
		// save info steps
		retval.append(TAB).append("<infoSteps>").append(Const.CR);
		for(RoleStepMeta step : infoSteps){
			retval.append(TAB).append(TAB).append("<infoStep>").append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("name", step.getStepName()));
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("role", step.getRoleName()));
			retval.append(TAB).append(TAB).append("</infoStep>").append(Const.CR);
		}
		retval.append(TAB).append("</infoSteps>").append(Const.CR);
		
		// save target steps
		retval.append(TAB).append("<targetSteps>").append(Const.CR);
		for(RoleStepMeta step : targetSteps){
			retval.append(TAB).append(TAB).append("<targetStep>").append(Const.CR);
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("name", step.getStepName()));
			retval.append(TAB).append(TAB).append(TAB).append(XMLHandler.addTagValue("role", step.getRoleName()));
			retval.append(TAB).append(TAB).append("</targetStep>").append(Const.CR);
		}
		retval.append(TAB).append("</targetSteps>").append(Const.CR);		
		
		
		
		return retval.toString();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {

			// load scripts
			scripts.clear();

			Node scriptNode = XMLHandler.getSubNode(stepnode, "scripts");
			int nrScripts = XMLHandler.countNodes(scriptNode, "script");

			for (int i = 0; i < nrScripts; i++) {
				Node sNode = XMLHandler.getSubNodeByNr(scriptNode, "script", i);
				scripts.add(new RubyScriptMeta(
								XMLHandler.getTagValue(sNode, "title"), 
								XMLHandler.getTagValue(sNode, "body"), 
								Role.valueOf(Const.NVL(XMLHandler.getTagValue(sNode, "role"), Role.LIB_SCRIPT.toString()))
							)
				);
			}
			
			// load clear input fields flag
			clearInputFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "clearInputFields"));
			
			// load ouput fields
			outputFields.clear();
			
			Node outputFieldsNode = XMLHandler.getSubNode(stepnode, "outputFields");
			int nrFields = XMLHandler.countNodes(outputFieldsNode, "outputField");
			
			for(int i=0;i<nrFields;i++){
				Node sNode = XMLHandler.getSubNodeByNr(outputFieldsNode, "outputField", i);
				String name = XMLHandler.getTagValue(sNode, "name");
				int type = Const.toInt(XMLHandler.getTagValue(sNode, "type"), -1) ;
				boolean update = "Y".equalsIgnoreCase(XMLHandler.getTagValue(sNode, "update"));
				outputFields.add(new OutputFieldMeta(name, type, update));
			}
			
			// load ruby variables
			rubyVariables.clear();

			Node rubyVariablesNode = XMLHandler.getSubNode(stepnode, "rubyVariables");
			int nrVars = XMLHandler.countNodes(rubyVariablesNode, "rubyVariable");
			
			for(int i=0;i<nrVars;i++){
				Node sNode = XMLHandler.getSubNodeByNr(rubyVariablesNode, "rubyVariable", i);
				String name = XMLHandler.getTagValue(sNode, "name");
				String value = XMLHandler.getTagValue(sNode, "value");
				rubyVariables.add(new RubyVariableMeta(name, value));
			}
			
			// load info steps
			infoSteps.clear();

			Node infoStepsNode = XMLHandler.getSubNode(stepnode, "infoSteps");
			int nrSteps = XMLHandler.countNodes(infoStepsNode, "infoStep");
			
			for(int i=0;i<nrSteps;i++){
				Node sNode = XMLHandler.getSubNodeByNr(infoStepsNode, "infoStep", i);
				String name = XMLHandler.getTagValue(sNode, "name");
				String value = XMLHandler.getTagValue(sNode, "role");
				infoSteps.add(new RoleStepMeta(name, value));
			}
			
			// load target steps
			targetSteps.clear();

			Node targetStepsNode = XMLHandler.getSubNode(stepnode, "targetSteps");
			int nrTargetSteps = XMLHandler.countNodes(targetStepsNode, "targetStep");
			
			for(int i=0;i<nrTargetSteps;i++){
				Node sNode = XMLHandler.getSubNodeByNr(targetStepsNode, "targetStep", i);
				String name = XMLHandler.getTagValue(sNode, "name");
				String value = XMLHandler.getTagValue(sNode, "role");
				targetSteps.add(new RoleStepMeta(name, value));
			}			
						

		} catch (Exception e) {
			throw new KettleXMLException("Template Plugin Unable to read step info from XML node", e);
		}

	}

	/*------------------------------------------------------------------------------------------------------------------------------------------------
	 * Serialization to Database	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			// save steps
			for (int i = 0; i < scripts.size(); i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "script_title", scripts.get(i).getTitle());
				rep.saveStepAttribute(id_transformation, id_step, i, "script_body", scripts.get(i).getScript());
				rep.saveStepAttribute(id_transformation, id_step, i, "script_role", scripts.get(i).getRole().toString()); 
			}
			
			// save clear input fields flag
			rep.saveStepAttribute(id_transformation, id_step, "clear_input_fields", clearInputFields);
			
			// save ouput fields
			for(int i=0;i<outputFields.size();i++){
				rep.saveStepAttribute(id_transformation, id_step, i, "output_field_name", outputFields.get(i).getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "output_field_type", outputFields.get(i).getType());
				rep.saveStepAttribute(id_transformation, id_step, i, "output_field_update", outputFields.get(i).isUpdate());
			}
			
			// save ruby variables
			for(int i=0;i<rubyVariables.size();i++){
				rep.saveStepAttribute(id_transformation, id_step, i, "ruby_variable_name", rubyVariables.get(i).getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "ruby_variable_value", rubyVariables.get(i).getValue());
			}
			
			// save info steps
			for(int i=0;i<infoSteps.size();i++){
				rep.saveStepAttribute(id_transformation, id_step, i, "info_step_name", infoSteps.get(i).getStepName());
				rep.saveStepAttribute(id_transformation, id_step, i, "info_step_role", infoSteps.get(i).getRoleName());
			}		
			
			// save target steps
			for(int i=0;i<targetSteps.size();i++){
				rep.saveStepAttribute(id_transformation, id_step, i, "target_step_name", targetSteps.get(i).getStepName());
				rep.saveStepAttribute(id_transformation, id_step, i, "target_step_role", targetSteps.get(i).getRoleName());
			}				
			
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "RubyStep.Exception.UnableToSaveStepInfoToRepository") + id_step, e);
		}
	}	
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {

			// load scripts
			int nrScripts = rep.countNrStepAttributes(id_step, "script_title");
			scripts.clear();
			
			for (int i = 0; i < nrScripts; i++) {
				RubyScriptMeta s = new RubyScriptMeta(
						rep.getStepAttributeString(id_step, i, "script_title"),
						rep.getStepAttributeString(id_step, i, "script_body"),
						Role.valueOf(rep.getStepAttributeString(id_step, i, "script_role"))
				);
				scripts.add(s);
			}
			
			// load clear input fields flag
			clearInputFields = rep.getStepAttributeBoolean(id_step, "clear_input_fields");
			
			// load ouput fields
			int nrFields = rep.countNrStepAttributes(id_step, "ouput_field_name");
			outputFields.clear();
			
			for(int i=0;i<nrFields;i++){
				OutputFieldMeta outField = new OutputFieldMeta(
						rep.getStepAttributeString(id_step, i, "output_field_name"),
						(int) rep.getStepAttributeInteger(id_step, i, "output_field_type"),
						rep.getStepAttributeBoolean(id_step, i, "output_field_update")
				);
				outputFields.add(outField);
			}
			
			// load ruby variables
			int nrVars = rep.countNrStepAttributes(id_step, "ruby_variable_name");
			rubyVariables.clear();
			
			for(int i=0;i<nrVars;i++){
				RubyVariableMeta var = new RubyVariableMeta(
					rep.getStepAttributeString(id_step, i, "ruby_variable_name"),
					rep.getStepAttributeString(id_step, i, "ruby_variable_value")
				);
				rubyVariables.add(var);
			}
			
			// load info steps
			int nrSteps = rep.countNrStepAttributes(id_step, "info_step_name");
			infoSteps.clear();
			
			for(int i=0;i<nrSteps;i++){
				RoleStepMeta info = new RoleStepMeta(
					rep.getStepAttributeString(id_step, i, "info_step_name"),
					rep.getStepAttributeString(id_step, i, "info_step_role")
				);
				infoSteps.add(info);
			}			

			// load target steps
			int nrTargetSteps = rep.countNrStepAttributes(id_step, "target_step_name");
			targetSteps.clear();
			
			for(int i=0;i<nrTargetSteps;i++){
				RoleStepMeta target = new RoleStepMeta(
					rep.getStepAttributeString(id_step, i, "target_step_name"),
					rep.getStepAttributeString(id_step, i, "target_step_role")
				);
				targetSteps.add(target);
			}				
			
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "RubyStep.Exception.UnexpectedErrorInReadingStepInfo"), e);
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
	 * Convenience Methods 	
	 ------------------------------------------------------------------------------------------------------------------------------------------------*/
	// returns true if the step has direct input sources (does not consider info steps as input)
	public boolean hasDirectInput(){
		return !this.getParentStepMeta().getParentTransMeta().findPreviousSteps(this.getParentStepMeta(), false).isEmpty();
	}
	
	// returns true if the step has direct input sources as well as info steps attached
	public boolean hasMixedInput(){
		return hasDirectInput() && !infoSteps.isEmpty();
	}
	
	// returns the row script, returns an empty dummy script if there's none defined 
	public RubyScriptMeta getRowScript(){
		
		for(RubyScriptMeta s: scripts){
			if (s.getRole() == Role.ROW_SCRIPT){
				return s;
			}
		}
		
		return new RubyScriptMeta("dummy", "# dummy", Role.ROW_SCRIPT);
	}
	
	// returns how many row scripts are defined (only legal count is 1)
	public int getRowScriptCount(){
		
		int counter = 0;
		for(RubyScriptMeta s: scripts){
			if (s.getRole() == Role.ROW_SCRIPT){
				counter++;
			}
		}
		
		return counter;
	}	
	
	// returns the init script, returns null if there's none defined 
	public RubyScriptMeta getInitScript(){
		
		for(RubyScriptMeta s: scripts){
			if (s.getRole() == Role.INIT_SCRIPT){
				return s;
			}
		}
		
		return null;
	}	
	
	// returns the dispose script, returns null if there's none defined 
	public RubyScriptMeta getDisposeScript(){
		
		for(RubyScriptMeta s: scripts){
			if (s.getRole() == Role.DISPOSE_SCRIPT){
				return s;
			}
		}
		
		return null;
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

	public List<RoleStepMeta> getInfoSteps() {
		return infoSteps;
	}

	public void setInfoSteps(List<RoleStepMeta> infoSteps) {
		this.infoSteps = infoSteps;
	}

	public List<RoleStepMeta> getTargetSteps() {
		return targetSteps;
	}

	public void setTargetSteps(List<RoleStepMeta> targetSteps) {
		this.targetSteps = targetSteps;
	}

	public List<OutputFieldMeta> getOutputFields() {
		return outputFields;
	}

	public void setOutputFields(List<OutputFieldMeta> outputFields) {
		this.outputFields = outputFields;
	}

	public List<RubyVariableMeta> getRubyVariables() {
		return rubyVariables;
	}

	public void setRubyVariables(List<RubyVariableMeta> rubyVariables) {
		this.rubyVariables = rubyVariables;
	}

	public boolean isClearInputFields() {
		return clearInputFields;
	}

	public void setClearInputFields(boolean clearInputFields) {
		this.clearInputFields = clearInputFields;
	}

	public List<ValueMetaInterface> getAffectedFields() {
		return affectedFields;
	}

}

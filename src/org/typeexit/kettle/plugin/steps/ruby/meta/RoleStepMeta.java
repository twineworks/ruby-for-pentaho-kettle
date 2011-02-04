package org.typeexit.kettle.plugin.steps.ruby.meta;

import org.pentaho.di.trans.step.StepMeta;


public class RoleStepMeta implements Cloneable{

	private String stepName;
	private String roleName;
	private StepMeta stepMeta;
	
	public RoleStepMeta(String stepName, String roleName) {
		this.stepName = stepName;
		this.roleName = roleName;
	}
	public String getStepName() {
		if (stepMeta != null){
			stepName = stepMeta.getName();
		}
		return stepName;
	}
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public RoleStepMeta clone() {
		try {
			return (RoleStepMeta) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	public void setStepMeta(StepMeta meta) {
		stepMeta = meta;
	}
	
	public StepMeta getStepMeta(){
		return stepMeta;
	}
	
}

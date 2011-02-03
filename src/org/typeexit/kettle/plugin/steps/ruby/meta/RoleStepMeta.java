package org.typeexit.kettle.plugin.steps.ruby.meta;


public class RoleStepMeta implements Cloneable{

	private String stepName;
	private String roleName;
	
	public RoleStepMeta(String stepName, String roleName) {
		this.stepName = stepName;
		this.roleName = roleName;
	}
	public String getStepName() {
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
	
}

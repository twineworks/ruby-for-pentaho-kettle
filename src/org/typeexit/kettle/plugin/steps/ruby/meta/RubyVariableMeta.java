package org.typeexit.kettle.plugin.steps.ruby.meta;

public class RubyVariableMeta implements Cloneable {
	
	public String name;
	public String value;

	public RubyVariableMeta(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}	
	
	public RubyVariableMeta clone() {
		try {
			return (RubyVariableMeta) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}	

}

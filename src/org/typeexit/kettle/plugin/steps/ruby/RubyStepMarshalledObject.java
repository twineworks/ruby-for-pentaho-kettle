package org.typeexit.kettle.plugin.steps.ruby;

import java.io.Serializable;

public class RubyStepMarshalledObject implements Serializable {

	private static final long serialVersionUID = -5218203315596922258L;
	private String string;
	
	public RubyStepMarshalledObject(String string) {
		super();
		this.string = string;
	}

	public String toString(){
		return string;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

}

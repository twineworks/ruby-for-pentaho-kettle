package org.typeexit.kettle.plugin.steps.ruby.execmodels;

import org.pentaho.di.core.exception.KettleException;
import org.typeexit.kettle.plugin.steps.ruby.RubyStep;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta;

public interface ExecutionModel {
	
	public void setEnvironment(RubyStep step, RubyStepData data, RubyStepMeta meta);
	
	public boolean onInit();
	public void onDispose();
	public boolean onProcessRow() throws KettleException;

}

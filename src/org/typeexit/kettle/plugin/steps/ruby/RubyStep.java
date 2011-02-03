package org.typeexit.kettle.plugin.steps.ruby;

import java.util.ArrayList;
import java.util.List;

import org.jruby.RubyHash;
import org.jruby.RubySymbol;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.ExecutionModel;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.SimpleExecutionModel;


public class RubyStep extends BaseStep implements StepInterface {

	private RubyStepData data;
	private RubyStepMeta meta;
	private ExecutionModel model;
	
	public RubyStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;

		return model.onProcessRow();

	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;

		boolean initSuccess = super.init(smi, sdi);
		
		if (initSuccess){
			model = new SimpleExecutionModel();
			model.setEnvironment(this, data, meta);
			initSuccess = model.onInit();
		}
		return initSuccess;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;
		
		if (model != null){
			model.onDispose();
			model = null;
		}
		
		super.dispose(smi, sdi);
	}


}

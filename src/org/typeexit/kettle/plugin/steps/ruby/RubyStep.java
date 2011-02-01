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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;


public class RubyStep extends BaseStep implements StepInterface {

	private RubyStepData data;
	private RubyStepMeta meta;
	
	public RubyStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;

		Object[] r = getRow(); // get row, blocks when needed!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;
			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			data.inputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			data.inputFieldNames = data.inputRowMeta.getFieldNames();
			data.runtime = data.container.getProvider().getRuntime();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}
		
		for(int i= 0; i< data.inputFieldNames.length;i++){
			data.container.put("$"+data.inputFieldNames[i], r[i]);
		}
		
		data.container.callMethod(data.rubyReceiverObject, "process_row");

		for(int i= 0; i< data.inputFieldNames.length;i++){
			r[i] = data.container.get("$"+data.inputFieldNames[i]);
		}
		
		// write all the values back, assuming they have not changed type
		
		putRow(data.outputRowMeta, r); // copy row to possible alternate rowset(s)

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;

		boolean retval = super.init(smi, sdi);
		
		if (retval){
			
			data.container = RubyStepFactory.createScriptingContainer();
			String script = meta.getScripts().get(0).getScript();
			
			data.container.parse(script, 1);
			data.rubyReceiverObject = data.container.runScriptlet(script);
			
			//data.container.put(data.rubyReceiverObject, "@step", this);
			
			Object result = data.container.callMethod(data.rubyReceiverObject, "init_step", Boolean.class);
			retval = Boolean.valueOf((Boolean)result);
		}
		
		return retval;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;
		
		data.container = null;
		data.rubyReceiverObject = null;

		super.dispose(smi, sdi);
	}


}

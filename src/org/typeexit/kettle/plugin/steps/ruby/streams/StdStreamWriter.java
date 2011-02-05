package org.typeexit.kettle.plugin.steps.ruby.streams;

import java.util.LinkedList;
import java.util.List;

import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.step.BaseStep;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.SimpleExecutionModel;

public class StdStreamWriter {

	private BaseStep step;
	private SimpleExecutionModel model;
	private RubyStepData data;
	private List<Object[]> rowList;
	
	public StdStreamWriter(SimpleExecutionModel model) throws KettleStepException{
		
		this.model = model;
		this.step = model.getStep();
		this.data = model.getData();
		this.rowList = new LinkedList<Object[]>();
		 
	}
	
	public void write(IRubyObject rubyOut) throws KettleException{
		
		// TODO: optimize the call to size() away
		Object[] r = new Object[data.outputRowMeta.size()];
		
		rowList.clear();
		model.fetchRowsFromScriptOutput(rubyOut, r, rowList);

		for(Object[] outRow : rowList){
			step.putRow(data.outputRowMeta, outRow);
			step.incrementLinesWritten();
		}
		
	}
	
}

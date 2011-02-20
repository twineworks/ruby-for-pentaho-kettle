package org.typeexit.kettle.plugin.steps.ruby.streams;

import java.util.LinkedList;
import java.util.List;

import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.SimpleExecutionModel;

public class StdStreamWriter {

	private BaseStep step;
	private SimpleExecutionModel model;
	private RubyStepData data;
	private List<Object[]> rowList;
	private int rowSize; 
	private RowMetaInterface inRow;
	
	public StdStreamWriter(SimpleExecutionModel model) throws KettleStepException{
		
		this.model = model;
		this.step = model.getStep();
		this.data = model.getData();
		this.rowList = new LinkedList<Object[]>();
		
		rowSize = data.outputRowMeta.size();
		
		inRow = new RowMeta();
		 
	}
	
	public void write(IRubyObject rubyOut) throws KettleException{
		
		Object[] r = new Object[rowSize];
		
		rowList.clear();
		model.fetchRowsFromScriptOutput(rubyOut, inRow, r, rowList, data.outputRowMeta.getValueMetaList(), data.outputRowMeta);

		for(Object[] outRow : rowList){
			step.putRow(data.outputRowMeta, outRow);
		}
		
	}
	
}

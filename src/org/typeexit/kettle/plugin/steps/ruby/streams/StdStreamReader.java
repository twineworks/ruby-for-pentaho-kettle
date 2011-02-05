package org.typeexit.kettle.plugin.steps.ruby.streams;

import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.step.BaseStep;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.SimpleExecutionModel;

public class StdStreamReader {
	
	private BaseStep step;
	private SimpleExecutionModel model;
	private RubyStepData data;
	
	public StdStreamReader(SimpleExecutionModel model) throws KettleStepException{
		
		this.model = model;
		this.step = model.getStep();
		this.data = model.getData();
		 
	}
	
	public IRubyObject read() throws KettleException{
		
		Object r[] = step.getRow();

		// signal that there's no more rows coming
		if (r == null){
			return data.runtime.getNil();
		}
		
		IRubyObject rubyRow = model.createRubyInputRow(data.inputRowMeta, r);
		return rubyRow;
	}

	public IRubyObject read(long upTo) throws KettleException{
		
		// request to read <0 rows
		if (upTo < 0) return data.runtime.getNil();
		
		RubyArray arr = data.runtime.newArray();
		int read = 0;
		while(read < upTo){
			IRubyObject o = read();
			if (o.isNil()) break;
			arr.append(o);
			read++;
		}
		
		// request to read from empty stream
		if (arr.size() == 0 && upTo > 0) return data.runtime.getNil();
		
		return arr;
	
	}
	
	public IRubyObject readAll() throws KettleException{
		
		RubyArray arr = data.runtime.newArray();
		
		while(true){
			IRubyObject o = read();
			if (o.isNil()) break;
			arr.append(o);
		}
		
		return arr;
		
	}
	
}

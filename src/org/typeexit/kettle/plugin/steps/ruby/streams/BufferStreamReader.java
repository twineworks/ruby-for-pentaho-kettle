package org.typeexit.kettle.plugin.steps.ruby.streams;

import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleStepException;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.SimpleExecutionModel;

/**
 * Instances of StepStreamReader are designed to be used from within ruby scripts. 
 * They are primarily used to read from info streams.
 */


public class BufferStreamReader {
	
//	private BaseStep step;
//	private SimpleExecutionModel model;
	private RubyStepData data;
	private RubyArray buffer;
	private long readPointer;
	
	public BufferStreamReader(SimpleExecutionModel model, RubyArray buffer) throws KettleStepException{
		
//		this.model = model;
//		this.step = model.getStep();
		this.data = model.getData();
		
		this.buffer = buffer;
		
		readPointer = 0;
		 
	}
	 
	public IRubyObject read(){
		
		IRubyObject row = buffer.entry(readPointer); 
		readPointer += 1;
		return row;
	}

	public IRubyObject read(long upTo){
		
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
	
	public RubyArray readAll(){
		
		RubyArray arr = data.runtime.newArray();
		
		while(true){
			IRubyObject o = read();
			if (o.isNil()) break;
			arr.append(o);
		}
		
		return arr;
		
	}
	
}

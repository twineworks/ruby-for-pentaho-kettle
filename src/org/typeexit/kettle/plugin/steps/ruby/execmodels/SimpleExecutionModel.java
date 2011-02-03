package org.typeexit.kettle.plugin.steps.ruby.execmodels;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.jruby.RubyArray;
import org.jruby.RubyBignum;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.RubyTime;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.javasupport.JavaUtil;
import org.jruby.javasupport.JavaUtilities;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.typeexit.kettle.plugin.steps.ruby.OutputFieldMeta;
import org.typeexit.kettle.plugin.steps.ruby.RubyStep;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepFactory;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMarshalledObject;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta;

public class SimpleExecutionModel implements ExecutionModel{

	private RubyStepData data;
	private RubyStepMeta meta;
	private RubyStep step;
	
	private IRubyObject marshal;
	private IRubyObject bigDecimal;
	
	@Override
	public void setEnvironment(RubyStep step, RubyStepData data, RubyStepMeta meta) {
		this.data = data;
		this.meta = meta;
		this.step = step;
	}

	@Override
	public boolean onInit() {
		
		try{
			data.container = RubyStepFactory.createScriptingContainer(true);
			data.runtime = data.container.getProvider().getRuntime();
			
			// FIXME: fixme when we know how to support multiple script tabs
			data.container.setScriptFilename(meta.getScripts().get(0).getTitle());
			data.container.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.JIT);

			// put the usual stuff into global scope
			data.container.put("$step", step);
			data.rubyScriptObject = data.container.parse(meta.getScripts().get(0).getScript(), 0);
			
		}
		catch(Exception e){
			step.logError("Error Initializing Ruby Scripting Step", e);
			return false;
		}
		
		return true;
	}

	@Override
	public void onDispose() {

		marshal = null;
		bigDecimal = null;

		if (data.container != null){
			data.container.terminate();
		}

		data.container = null;
		data.rubyScriptObject = null;
		data.runtime = null;
		
	}
	
	private IRubyObject getMarshal(){
		if (marshal == null){
			marshal = data.container.parse("Marshal").run();
		}
		return marshal;
	}

	private IRubyObject getBigDecimal(){
		if (bigDecimal == null){
			bigDecimal = data.container.parse("require 'bigdecimal'; BigDecimal").run();
		}
		return bigDecimal;
	}
	
	
	private void onRowStreamInit(){

		data.inputRowMeta = step.getInputRowMeta().clone();
		data.inputFieldNames = data.inputRowMeta.getFieldNames();

		data.outputRowMeta = step.getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, step.getStepname(), null, null, step);

		// TODO: row stream is ready, nothing to do.. well... should maybe initialize info / target steps 
		
	}
	
	private void processOutputHash(Object[] r, RubyHash resultRow) throws KettleException{
		
		// set each field's value from the resultRow
		for (OutputFieldMeta outField : meta.getOutputFields()) {
			
			IRubyObject rubyVal = resultRow.fastARef(JavaEmbedUtils.javaToRuby(data.runtime, outField.getName()));
			
			// convert simple cases automatically
			Object javaValue = null;
			
			switch(outField.getType()){
				case ValueMeta.TYPE_BOOLEAN:
				case ValueMeta.TYPE_INTEGER:
				case ValueMeta.TYPE_STRING:
				case ValueMeta.TYPE_NUMBER:
				case ValueMeta.TYPE_NONE:
					// TODO: provide a meaningful error message if this fails because the user put something strange in here
					javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, outField.getConversionClass()); 
				break;
				case ValueMeta.TYPE_SERIALIZABLE:
					String marshalled = data.container.parse("Marshal").run().callMethod(data.runtime.getCurrentContext(), "dump", rubyVal).toString();
					javaValue = new RubyStepMarshalledObject(marshalled); 
				break;
				case ValueMeta.TYPE_BINARY:
					RubyArray arr = rubyVal.convertToArray(); // TODO: provide meaningful error message if this fails
					byte[] bytes = new byte[arr.size()];
					for(int i=0;i<bytes.length;i++){
						Object rItem = arr.get(i);
						if (rItem instanceof Number){
							bytes[i] = ((Number)rItem).byteValue();
						}
						else{
							throw new KettleException("Found a non-number in Binary field "+outField.getName()+": "+rItem.toString());
						}
					}
					javaValue = bytes;
				break;
				case ValueMeta.TYPE_BIGNUMBER:
					if (rubyVal instanceof RubyFloat){
						javaValue = new BigDecimal(((Double)rubyVal.toJava(Double.class)).doubleValue());
					}
					else{
						javaValue = new BigDecimal(rubyVal.toString());
					}
					
				break;
				case ValueMeta.TYPE_DATE:
					if (rubyVal instanceof RubyFixnum){
						javaValue = new Date(((RubyFixnum)rubyVal).getLongValue());
					}
					else if (rubyVal instanceof RubyTime){
						javaValue = ((RubyTime)rubyVal).getJavaDate();
					}
				break;
					
					
			}
			// TODO: optimize this for each field to know its index in advance
			r[data.outputRowMeta.indexOfValue(outField.getName())] = javaValue;
			
		}

		step.putRow(data.outputRowMeta, r);		
		
	}
	
	@Override
	public boolean onProcessRow() throws KettleException {

		Object[] r = step.getRow();
		
		// only now is the metadata available
		if(step.first){
			onRowStreamInit();
			step.first = false;
		}
		
		// get the next row 
		if (r != null){

			// make sure the row can hold the results
			r = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
			
			// create a hash from the row
			RubyHash rubyRow = new RubyHash(data.runtime); 

			// TODO: optimize this in letting the user decide which fields to insert (it makes a difference for serializable and binary types), user should maybe also choose which object to pass (Adapted Java Object or native ruby type)
			// TODO: May be further optimized by deferring the conversion selection for each field, maybe not 
			// put the field values into the row
			for(int i=0;i<data.inputFieldNames.length;i++){
		
				switch(data.inputRowMeta.getValueMeta(i).getType()){
					case ValueMeta.TYPE_BOOLEAN:
					case ValueMeta.TYPE_INTEGER:
					case ValueMeta.TYPE_STRING:
					case ValueMeta.TYPE_NUMBER:
					case ValueMeta.TYPE_NONE:
						rubyRow.put(data.inputFieldNames[i], r[i]);
						break;
					case ValueMeta.TYPE_SERIALIZABLE:
						if (r[i] instanceof RubyStepMarshalledObject){
							Object restoredObject = getMarshal().callMethod(data.runtime.getCurrentContext(), "restore", data.runtime.newString(r[i].toString()));
							rubyRow.put(data.inputFieldNames[i], restoredObject);
						}
						else{
							// try to put the object in there as it is.. should create a nice adapter for the java object
							rubyRow.put(data.inputFieldNames[i], r[i]);
						}
						break;
					case ValueMeta.TYPE_BINARY:
						// put a ruby array with bytes in there, that is expensive and should be avoided
						rubyRow.put(data.inputFieldNames[i], 
								data.runtime.newArrayNoCopy(JavaUtil.convertJavaArrayToRuby(data.runtime, ArrayUtils.toObject((byte[]) r[i])))
						);
					break;
					
					case ValueMeta.TYPE_BIGNUMBER:
						IRubyObject bigDecimalObject = getBigDecimal().callMethod(data.runtime.getCurrentContext(), "new", data.runtime.newString(((BigDecimal) r[i]).toString()));
						rubyRow.put(data.inputFieldNames[i], bigDecimalObject);
					break;
					
					case ValueMeta.TYPE_DATE:
						rubyRow.put(data.inputFieldNames[i], data.runtime.newTime(((Date)r[i]).getTime()));
					break;

				}
				
			}
			
			// put the row into the container
			data.container.put("$row", rubyRow);
			
			// run the script, the result should give a ruby hash
			IRubyObject result = data.rubyScriptObject.run();

			// skip nil result rows
			if (result.isNil()){
				// skip row
			}
			// ruby hashes are processed instantly
			else if (result instanceof RubyHash){
				processOutputHash(r, (RubyHash) result);	
			}
			// arrays may contain:
			// nil values -> skipped row
			// hashes or anything convertible to hash -> write row
			else if (result instanceof RubyArray){
				RubyArray resultArray = (RubyArray) result;
				int length = resultArray.getLength();
				for(int i=0;i<length;i++){
					IRubyObject o = resultArray.entry(i);
					if (o instanceof RubyHash){
						processOutputHash(r, (RubyHash) o);
					}
					else if (o.isNil()){
						// skip row
					}
					else{
						try{
							RubyHash resultRow = o.convertToHash(); // TODO: test this and provide a meaningful error message if this fails or yields null
							processOutputHash(r, resultRow);
						}
						catch (Exception e){
							throw new KettleException("found non-hash value: "+o.toString()+" in returned array at position: "+i, e);	
						}
						
					}
				}
				
			}
			// try if the result is convertible to a hash and go from there
			else{
				
				try{
					RubyHash resultRow = result.convertToHash(); // TODO: provide a meaningful error message if this fails or yields null
					processOutputHash(r, resultRow);
				}
				catch(Exception e){
					throw new KettleException("script returned non-hash value: "+result.toString()+" as a result ", e);
				}
			}

 			return true;
		}
		else{
			// no more rows coming in
			step.setOutputDone();
			return false;
		}
	}


}

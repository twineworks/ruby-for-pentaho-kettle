package org.typeexit.kettle.plugin.steps.ruby.execmodels;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.jruby.RubyArray;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.RubyTime;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.typeexit.kettle.plugin.steps.ruby.RubyStep;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepFactory;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMarshalledObject;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.OutputFieldMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyVariableMeta;
import org.typeexit.kettle.plugin.steps.ruby.streams.StepStreamReader;
import org.typeexit.kettle.plugin.steps.ruby.streams.StepStreamWriter;
import org.typeexit.kettle.plugin.steps.ruby.streams.StdStreamReader;
import org.typeexit.kettle.plugin.steps.ruby.streams.StdStreamWriter;

public class SimpleExecutionModel implements ExecutionModel {

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

		try {
			data.container = RubyStepFactory.createScriptingContainer(true);
			data.runtime = data.container.getProvider().getRuntime();

			// FIXME: fixme when we know how to support multiple script tabs
			data.container.setScriptFilename(meta.getScripts().get(0).getTitle());
			data.container.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.JIT);

			// put the usual stuff into global scope
			data.container.put("$step", step);

			// put all variables into scope
			for (RubyVariableMeta var : meta.getRubyVariables()) {
				data.container.put(var.getName(), step.environmentSubstitute(var.getValue()));
			}

			// FIXME: fix this when we know how to support multiple script tabs
			data.rubyScriptObject = data.container.parse(meta.getScripts().get(0).getScript(), 0);

			// temporary place for the output a script might produce
			data.rowList = new LinkedList<Object[]>();

		} catch (Exception e) {
			step.logError("Error Initializing Ruby Scripting Step", e);
			return false;
		}

		return true;
	}

	@Override
	public void onDispose() {

		marshal = null;
		bigDecimal = null;

		if (data.container != null) {
			data.container.terminate();
		}

		data.container = null;
		data.rubyScriptObject = null;
		data.runtime = null;

	}

	private IRubyObject getMarshal() {
		if (marshal == null) {
			marshal = data.container.parse("Marshal").run();
		}
		return marshal;
	}

	private IRubyObject getBigDecimal() {
		if (bigDecimal == null) {
			bigDecimal = data.container.parse("require 'bigdecimal'; BigDecimal").run();
		}
		return bigDecimal;
	}

	private void onRowStreamInit() throws KettleException {

		// steps inputRowMeta might be null in case we have info steps only, or there's no input to begin with
		RowMetaInterface inputRowMeta = step.getInputRowMeta();
		if (inputRowMeta == null) {
			inputRowMeta = new RowMeta();
		}

		data.inputRowMeta = inputRowMeta.clone();
		data.inputFieldNames = data.inputRowMeta.getFieldNames();

		data.outputRowMeta = inputRowMeta.clone();
		meta.getFields(data.outputRowMeta, step.getStepname(), null, null, step);

		// put the info steps into ruby scope
		RubyHash infoSteps = new RubyHash(data.runtime);

		int i=0;
		for (StreamInterface stream : meta.getStepIOMeta().getInfoStreams()) {
			StepStreamReader reader = new StepStreamReader(this, stream.getStepname());
			infoSteps.put(meta.getInfoSteps().get(i).getRoleName(), reader);
			i++;
		}
		
		data.container.put("$info_steps", infoSteps);
		
		// put the target steps into ruby scope
		RubyHash targetSteps = new RubyHash(data.runtime);

		int t=0;
		for (StreamInterface stream : meta.getStepIOMeta().getTargetStreams()) {
			StepStreamWriter writer = new StepStreamWriter(this, stream.getStepname());
			targetSteps.put(meta.getTargetSteps().get(t).getRoleName(), writer);
			t++;
		}
		
		data.container.put("$target_steps", targetSteps);
		
		// put the standard streams into scope
		data.container.put("$output", new StdStreamWriter(this));
		data.container.put("$input", new StdStreamReader(this));

	}

	public RubyHash createRubyInputRow(RowMetaInterface rowMeta, Object[] r) {
		// create a hash from the row
		RubyHash rubyRow = new RubyHash(data.runtime);

		// TODO: optimize this in letting the user decide which fields to insert (it makes a difference for serializable and binary types), user should maybe also choose which object to pass (Adapted Java Object or native ruby type)
		// TODO: May be further optimized by deferring the conversion selection for each field
		String[] fieldNames = rowMeta.getFieldNames();
		for (int i = 0; i < fieldNames.length; i++) {

			String field = fieldNames[i];

			switch (rowMeta.getValueMeta(i).getType()) {
			case ValueMeta.TYPE_BOOLEAN:
			case ValueMeta.TYPE_INTEGER:
			case ValueMeta.TYPE_STRING:
			case ValueMeta.TYPE_NUMBER:
			case ValueMeta.TYPE_NONE:
				rubyRow.put(field, r[i]);
				break;
			case ValueMeta.TYPE_SERIALIZABLE:
				if (r[i] instanceof RubyStepMarshalledObject) {
					Object restoredObject = getMarshal().callMethod(data.runtime.getCurrentContext(), "restore", data.runtime.newString(r[i].toString()));
					rubyRow.put(field, restoredObject);
				} else {
					// try to put the object in there as it is.. should create a nice adapter for the java object
					rubyRow.put(field, r[i]);
				}
				break;
			case ValueMeta.TYPE_BINARY:
				// put a ruby array with bytes in there, that is expensive and should be avoided
				rubyRow.put(fieldNames[i],
							data.runtime.newArrayNoCopy(JavaUtil.convertJavaArrayToRuby(data.runtime, ArrayUtils.toObject((byte[]) r[i])))
						);
				break;

			case ValueMeta.TYPE_BIGNUMBER:
				IRubyObject bigDecimalObject = getBigDecimal().callMethod(data.runtime.getCurrentContext(), "new", data.runtime.newString(((BigDecimal) r[i]).toString()));
				rubyRow.put(field, bigDecimalObject);
				break;

			case ValueMeta.TYPE_DATE:
				rubyRow.put(field, data.runtime.newTime(((Date) r[i]).getTime()));
				break;

			}

		}

		return rubyRow;

	}

	private void applyRubyHashToRow(Object[] r, RubyHash resultRow, List<ValueMetaInterface> forFields) throws KettleException {
		
		// set each field's value from the resultRow
		
		for (OutputFieldMeta outField : meta.getOutputFields()) {

			// TODO: the ruby strings for field names can be cached and reused
			// TODO: test what happens if nil values come for each type
			IRubyObject rubyVal = resultRow.fastARef(JavaEmbedUtils.javaToRuby(data.runtime, outField.getName()));

			// convert simple cases automatically
			Object javaValue = null;
			
			// for nil values just put null into the row
			if (!rubyVal.isNil()){
				
				switch (outField.getType()) {
				case ValueMeta.TYPE_BOOLEAN:
				case ValueMeta.TYPE_INTEGER:
				case ValueMeta.TYPE_STRING:
				case ValueMeta.TYPE_NUMBER:
					// TODO: provide a meaningful error message if this fails because the user put something strange in here (maybe handle strings differently by calling to_s)
					javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, outField.getConversionClass());
					break;
				case ValueMeta.TYPE_SERIALIZABLE:
					String marshalled = getMarshal().callMethod(data.runtime.getCurrentContext(), "dump", rubyVal).toString();
					javaValue = new RubyStepMarshalledObject(marshalled);
					break;
				case ValueMeta.TYPE_BINARY:
					// TODO: provide meaningful error message if this fails
					RubyArray arr = rubyVal.convertToArray();

					byte[] bytes = new byte[arr.size()];
					for (int i = 0; i < bytes.length; i++) {
						Object rItem = arr.get(i);
						if (rItem instanceof Number) {
							bytes[i] = ((Number) rItem).byteValue();
						} else {
							throw new KettleException("Found a non-number in Binary field " + outField.getName() + ": " + rItem.toString());
						}
					}
					javaValue = bytes;
					break;
				case ValueMeta.TYPE_BIGNUMBER:
					if (rubyVal instanceof RubyFloat) {
						javaValue = new BigDecimal(((Double) rubyVal.toJava(Double.class)).doubleValue());
					} else {
						javaValue = new BigDecimal(rubyVal.toString());
					}

					break;
				case ValueMeta.TYPE_DATE:
					if (rubyVal instanceof RubyFixnum) {
						javaValue = new Date(((RubyFixnum) rubyVal).getLongValue());
					} else if (rubyVal instanceof RubyTime) {
						javaValue = ((RubyTime) rubyVal).getJavaDate();
					}
					break;

				}
				
			}

			// TODO: optimize this for each field to know its index in advance
			r[data.outputRowMeta.indexOfValue(outField.getName())] = javaValue;

		}

	}

	public void fetchRowsFromScriptOutput(IRubyObject rubyObject, Object[] r, List<Object[]> rowList) throws KettleException {

		// skip nil result rows
		if (rubyObject.isNil()) {
			return;
		}

		// ruby hashes are processed instantly
		if (rubyObject instanceof RubyHash) {
			r = RowDataUtil.resizeArray(data.inputRowMeta.cloneRow(r), data.outputRowMeta.size());
			applyRubyHashToRow(r, (RubyHash) rubyObject, data.outputRowMeta.getValueMetaList());
			rowList.add(r);
			return;
		}

		// arrays are handled recursively:
		if (rubyObject instanceof RubyArray) {
			RubyArray rubyArray = (RubyArray) rubyObject;
			int length = rubyArray.getLength();
			for (int i = 0; i < length; i++) {
				fetchRowsFromScriptOutput(rubyArray.entry(i), r, rowList);
			}
			return;
		}

		// at this point the returned object is not nil, not a hash and not an array, give up (may use convertToHash in future for convertible objects..)
		throw new KettleException("script returned non-hash value: " + rubyObject.toString() + " as a result ");

	}

	@Override
	public boolean onProcessRow() throws KettleException {

		// as calls to getRow() would yield rows from indeterminate sources unless
		// all info streams have been emptied first
		// we opt to enforce to have all info steps or no info steps

		Object[] r = null;

		if (step.first) {
			data.hasDirectInput = meta.hasDirectInput();

			// TODO: since mixed layouts always imply that all rows are read from info first, we could implement that as well, other steps must do that too (at least with 4.x API)

		}

		// directinput means, there's no info steps and at least one step providing data
		if (data.hasDirectInput) {

			r = step.getRow();

			// only now is the metadata available 
			if (step.first) {
				onRowStreamInit();
				step.first = false;
			}

			// get the next row 
			if (r != null) {

				RubyHash rubyRow = createRubyInputRow(data.inputRowMeta, r);

				// put the row into the container
				data.container.put("$row", rubyRow);

				// run the script, the result is one or more rows
				IRubyObject scriptResult = data.rubyScriptObject.run();

				data.rowList.clear();
				fetchRowsFromScriptOutput(scriptResult, r, data.rowList);

				// now if the script has output rows, write them to the main output stream
				for (Object[] outrow : data.rowList) {
					step.putRow(data.outputRowMeta, outrow);
					step.incrementLinesWritten();
				}

				return true;
			} else {
				// no more rows coming in
				step.setOutputDone();
				return false;
			}

		}

		// no direct input means the script is not getting an input row and is executed exactly once
		else {
			if (step.first) {
				onRowStreamInit();
				step.first = false;
			}
			r = new Object[data.outputRowMeta.size()];

			// run the script, the result is one or more rows
			IRubyObject scriptResult = data.rubyScriptObject.run();

			data.rowList.clear();
			fetchRowsFromScriptOutput(scriptResult, r, data.rowList);

			// now if the script has output rows, write them to the main output stream
			for (Object[] outrow : data.rowList) {
				step.putRow(data.outputRowMeta, outrow);
				step.incrementLinesWritten();
			}

			step.setOutputDone();
			return false;
		}

	}

	public RubyStep getStep() {
		return step;
	}

	public RubyStepData getData() {
		return data;
	}

}

package org.typeexit.kettle.plugin.steps.ruby.execmodels;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.jruby.RubyArray;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyHash;
import org.jruby.RubyTime;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.Const;
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
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyVariableMeta;
import org.typeexit.kettle.plugin.steps.ruby.streams.BufferStreamReader;
import org.typeexit.kettle.plugin.steps.ruby.streams.ErrorStreamWriter;
import org.typeexit.kettle.plugin.steps.ruby.streams.StdStreamReader;
import org.typeexit.kettle.plugin.steps.ruby.streams.StdStreamWriter;
import org.typeexit.kettle.plugin.steps.ruby.streams.StepStreamReader;
import org.typeexit.kettle.plugin.steps.ruby.streams.StepStreamWriter;

public class SimpleExecutionModel implements ExecutionModel {

	private RubyStepData data;
	private RubyStepMeta meta;
	private RubyStep step;

	@Override
	public void setEnvironment(RubyStep step, RubyStepData data, RubyStepMeta meta) {
		this.data = data;
		this.meta = meta;
		this.step = step;
	}

	@Override
	public boolean onInit() {

		try {

			data.container = RubyStepFactory.createScriptingContainer(true, meta.getRubyVersion());

			data.runtime = data.container.getProvider().getRuntime();

			// set gem home if specified
			setGemHome();

			data.container.setScriptFilename(meta.getRowScript().getTitle());
			data.rubyScriptObject = data.container.parse(meta.getRowScript().getScript(), 0);

			// put the usual stuff into global scope
			data.container.put("$step", step);
			data.container.put("$trans", step.getDispatcher());

			// put all variables into scope
			for (RubyVariableMeta var : meta.getRubyVariables()) {
				data.container.put(var.getName(), step.environmentSubstitute(var.getValue()));
			}

			// put all script tabs into scope
			RubyHash tabs = new RubyHash(data.runtime);

			for (RubyScriptMeta tab : meta.getScripts()) {
				tabs.put(tab.getTitle(), new ScriptTab(tab, data));
			}

			data.container.put("$tabs", tabs);

			// temporary place for the output a script might produce
			data.rowList = new LinkedList<Object[]>();

			// add << aliases to the java stream writers
			data.container.runScriptlet("JavaUtilities.extend_proxy('org.typeexit.kettle.plugin.steps.ruby.streams.StdStreamWriter') {alias << write}\n");
			data.container.runScriptlet("JavaUtilities.extend_proxy('org.typeexit.kettle.plugin.steps.ruby.streams.ErrorStreamWriter') {alias << write}\n");
			data.container.runScriptlet("JavaUtilities.extend_proxy('org.typeexit.kettle.plugin.steps.ruby.streams.StepStreamWriter') {alias << write}\n");

		} catch (Exception e) {
			step.logError("Error Initializing Ruby Scripting Step", e);
			return false;
		}

		return true;
	}

	private void setGemHome() {

		// if specified directly, take it
		String gemHomeString = step.environmentSubstitute(meta.getGemHome());

		// if not, fall back to RUBY_GEM_HOME
		if (Const.isEmpty(gemHomeString) && !Const.isEmpty(step.getVariable("RUBY_GEM_HOME"))) {
			gemHomeString = step.environmentSubstitute("${RUBY_GEM_HOME}");
		}

		// if that fails, use the standard one
		if (Const.isEmpty(gemHomeString)) {
			gemHomeString = step.getPluginDir() + Const.FILE_SEPARATOR + "gems";
		}

		if (!Const.isEmpty(gemHomeString)) {

			File gemHomePath = new File(gemHomeString);
			gemHomePath = gemHomePath.getAbsoluteFile();

			RubyHash configHash = (RubyHash) data.container.parse("require 'rbconfig'; RbConfig::CONFIG").run();
			configHash.put("default_gem_home", gemHomePath.getAbsolutePath());
		}
	}

	@Override
	public void onDispose() {

		data.marshal = null;
		data.bigDecimal = null;

		if (data.container != null) {
			data.container.terminate();
		}

		data.container = null;
		data.rubyScriptObject = null;
		data.runtime = null;

	}

	private IRubyObject getMarshal() {
		if (data.marshal == null) {
			data.marshal = data.container.parse("Marshal").run();
		}
		return data.marshal;
	}

	private IRubyObject getBigDecimal() {
		if (data.bigDecimal == null) {
			data.bigDecimal = data.container.parse("require 'bigdecimal'; BigDecimal").run();
		}
		return data.bigDecimal;
	}

	private void initMainRowStream() throws KettleException {

		// steps inputRowMeta might be null in case we have info steps only, or there's no input to begin with
		RowMetaInterface inputRowMeta = step.getInputRowMeta();
		if (inputRowMeta == null) {
			inputRowMeta = new RowMeta();
		}

		data.inputRowMeta = inputRowMeta.clone();
		data.inputFieldNames = data.inputRowMeta.getFieldNames();

		data.outputRowMeta = inputRowMeta.clone();
		meta.getFields(data.outputRowMeta, step.getStepname(), null, null, step);

		data.cacheFieldNames(data.inputRowMeta);
		data.cacheFieldNames(data.outputRowMeta);

		data.baseRowMeta = meta.isClearInputFields() ? data.emptyRowMeta : data.inputRowMeta;

		// put the standard streams into ruby scope
		data.container.put("$output", new StdStreamWriter(this));
		data.container.put("$input", new StdStreamReader(this));

		if (meta.getParentStepMeta().isDoingErrorHandling()) {

			data.errorRowMeta = meta.getParentStepMeta().getStepErrorMeta().getErrorFields().clone();
			data.stepErrorMeta = meta.getParentStepMeta().getStepErrorMeta();
			data.cacheFieldNames(data.errorRowMeta);

			data.container.put("$error", new ErrorStreamWriter(this));
		}

		// put the target steps into ruby scope
		RubyHash targetSteps = new RubyHash(data.runtime);

		int t = 0;
		for (StreamInterface stream : meta.getStepIOMeta().getTargetStreams()) {
			StepStreamWriter writer = new StepStreamWriter(this, stream.getStepname());
			targetSteps.put(meta.getTargetSteps().get(t).getRoleName(), writer);
			t++;
		}

		data.container.put("$target_steps", targetSteps);

	}

	public RubyHash createRubyInputRow(RowMetaInterface rowMeta, Object[] r) throws KettleException {

		// create a hash for the row, they are not reused on purpose, so the scripting user can safely use them to store entire rows between invocations
		RubyHash rubyRow = new RubyHash(data.runtime);

		String[] fieldNames = rowMeta.getFieldNames();
		for (int i = 0; i < fieldNames.length; i++) {

			String field = fieldNames[i];
			// null values don't need no special treatment, they'll become nil
			if (r[i] == null) {
				rubyRow.put(field, null);
			} else {

				ValueMetaInterface vm = rowMeta.getValueMeta(i);

				switch (vm.getType()) {
				case ValueMeta.TYPE_BOOLEAN:
					rubyRow.put(field, vm.getBoolean(r[i]));
					break;
				case ValueMeta.TYPE_INTEGER:
					rubyRow.put(field, vm.getInteger(r[i]));
					break;
				case ValueMeta.TYPE_STRING:
					rubyRow.put(field, vm.getString(r[i]));
					break;
				case ValueMeta.TYPE_NUMBER:
					rubyRow.put(field, vm.getNumber(r[i]));
					break;
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
					// put a ruby array with bytes in there, that is expensive and should probably be avoided
					rubyRow.put(fieldNames[i],
								data.runtime.newArrayNoCopy(JavaUtil.convertJavaArrayToRuby(data.runtime, ArrayUtils.toObject((byte[]) vm.getBinary(r[i]))))
								);

					break;

				case ValueMeta.TYPE_BIGNUMBER:
					IRubyObject bigDecimalObject = getBigDecimal().callMethod(data.runtime.getCurrentContext(), "new", data.runtime.newString((vm.getBigNumber(r[i])).toString()));
					rubyRow.put(field, bigDecimalObject);
					break;

				case ValueMeta.TYPE_DATE:
					rubyRow.put(field, data.runtime.newTime((vm.getDate(r[i])).getTime()));
					break;

				}

			}

		}

		return rubyRow;

	}

	private void applyRubyHashToRow(Object[] r, RubyHash resultRow, List<ValueMetaInterface> forFields, RowMetaInterface forRow) throws KettleException {

		// set each field's value from the resultRow
		for (ValueMetaInterface outField : forFields) {

			IRubyObject rubyVal = resultRow.fastARef(data.rubyStringCache.get(outField.getName()));

			// convert simple cases automatically
			Object javaValue = null;

			// for nil values just put null into the row
			if (rubyVal != null && !rubyVal.isNil()) {

				// TODO: provide a meaningful error message if conversion fails because the user put non-convertible results in there (like a string saying "true"/"false" for the bool type)
				switch (outField.getType()) {
				case ValueMeta.TYPE_BOOLEAN:
					javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, Boolean.class);
					break;
				case ValueMeta.TYPE_INTEGER:
					javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, Long.class);
					break;
				case ValueMeta.TYPE_STRING:
					javaValue = rubyVal.toString();
					break;
				case ValueMeta.TYPE_NUMBER:
					javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, Double.class);
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

			r[data.fieldIndexCache.get(forRow).get(outField.getName())] = javaValue;
		}

	}

	public void fetchRowsFromScriptOutput(IRubyObject rubyObject, RowMetaInterface inRow, Object[] r, List<Object[]> rowList, List<ValueMetaInterface> forFields, RowMetaInterface forRow) throws KettleException {

		// skip nil result rows
		if (rubyObject.isNil()) {
			return;
		}

		// ruby hashes are processed instantly
		if (rubyObject instanceof RubyHash) {
			// clone the row only if necessary
			if (rowList.size() > 0) {
				r = RowDataUtil.resizeArray(inRow.cloneRow(r), forRow.size());
			} else {
				r = RowDataUtil.resizeArray(r, forRow.size());
			}
			applyRubyHashToRow(r, (RubyHash) rubyObject, forFields, forRow);
			rowList.add(r);
			return;
		}

		// arrays are handled recursively:
		if (rubyObject instanceof RubyArray) {
			RubyArray rubyArray = (RubyArray) rubyObject;
			int length = rubyArray.getLength();
			for (int i = 0; i < length; i++) {
				fetchRowsFromScriptOutput(rubyArray.entry(i), inRow, r, rowList, forFields, forRow);
			}
			return;
		}

		// at this point the returned object is not nil, not a hash and not an array, let's ignore the output but warn in the log
		step.logBasic("WARNING: script returned non-hash value: " + rubyObject.toString() + " as a result ");

	}

	@Override
	public boolean onProcessRow() throws KettleException {

		// as calls to getRow() would yield rows from indeterminate sources unless
		// all info streams have been emptied first
		// we opt to enforce to have all info steps or no info steps

		Object[] r = null;

		if (step.first) {
			data.hasDirectInput = meta.hasDirectInput();
			// call the init script here rather than in the init section. It guarantees that other steps are fully initialized.
			if (meta.getInitScript() != null) {
				data.container.runScriptlet(new StringReader(meta.getInitScript().getScript()), meta.getInitScript().getTitle());
			}

			// this must be done before the first call to getRow() in case there are info streams present
			initInfoRowStreams();
		}

		// directinput means, there's no info steps and at least one step providing data
		if (data.hasDirectInput) {

			r = step.getRow();

			// only now is the metadata available 
			if (step.first) {
				initMainRowStream();
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
				fetchRowsFromScriptOutput(scriptResult, data.baseRowMeta, r, data.rowList, meta.getAffectedFields(), data.outputRowMeta);

				// now if the script has output rows, write them to the main output stream
				for (Object[] outrow : data.rowList) {
					step.putRow(data.outputRowMeta, outrow);
				}

				return true;
			} else {

				// run the end script here rather then on dispose end, ensures that the row streams are still up, so user can choose to 
				// write "summary" rows and the like 
				if (meta.getDisposeScript() != null) {
					data.container.runScriptlet(meta.getDisposeScript().getScript());
				}

				// no more rows coming in
				step.setOutputDone();
				return false;
			}

		}

		// no direct input means the script is not getting an input row and is executed exactly once
		else {
			if (step.first) {
				initMainRowStream();
				step.first = false;
			}
			r = new Object[data.outputRowMeta.size()];

			// run the script, the result is one or more rows
			IRubyObject scriptResult = data.rubyScriptObject.run();

			data.rowList.clear();
			fetchRowsFromScriptOutput(scriptResult, data.baseRowMeta, r, data.rowList, meta.getAffectedFields(), data.outputRowMeta);

			// now if the script has output rows, write them to the main output stream
			for (Object[] outrow : data.rowList) {
				step.putRow(data.outputRowMeta, outrow);
			}

			// run the end script here rather then on dispose end, ensures that the row streams are still up, so user can choose to 
			// write "summary" rows and the like 
			if (meta.getDisposeScript() != null) {
				data.container.runScriptlet(meta.getDisposeScript().getScript());
			}

			step.setOutputDone();
			return false;
		}

	}

	private void initInfoRowStreams() throws KettleException {

		// put the info steps into ruby scope
		RubyHash infoSteps = new RubyHash(data.runtime);

		int i = 0;
		for (StreamInterface stream : meta.getStepIOMeta().getInfoStreams()) {

			StepStreamReader reader = new StepStreamReader(this, stream.getStepname());

			// if there's direct input connected as well as info streams present, the info streams *must* be prefetched as per 4.0 API
			if (data.hasDirectInput) {
				RubyArray allRows = reader.readAll();
				BufferStreamReader bReader = new BufferStreamReader(this, allRows);
				infoSteps.put(meta.getInfoSteps().get(i).getRoleName(), bReader);
			} else {
				infoSteps.put(meta.getInfoSteps().get(i).getRoleName(), reader);
			}

			i++;
		}

		data.container.put("$info_steps", infoSteps);

	}

	public RubyStep getStep() {
		return step;
	}

	public RubyStepData getData() {
		return data;
	}

}

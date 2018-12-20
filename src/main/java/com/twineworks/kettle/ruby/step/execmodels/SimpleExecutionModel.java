/*
 * Ruby for pentaho kettle
 * Copyright (C) 2017 Twineworks GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.twineworks.kettle.ruby.step.execmodels;

import com.twineworks.kettle.ruby.step.*;
import com.twineworks.kettle.ruby.step.meta.RubyScriptMeta;
import com.twineworks.kettle.ruby.step.meta.RubyVariableMeta;
import com.twineworks.kettle.ruby.step.streams.*;
import org.apache.commons.lang.ArrayUtils;
import org.jruby.*;
import org.jruby.embed.EvalFailedException;
import org.jruby.exceptions.ThreadKill;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

      data.forcedHalt = false;

      data.container = RubyStepFactory.createScriptingContainer(true);

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
      data.container.runScriptlet("JavaUtilities.extend_proxy('com.twineworks.kettle.ruby.step.streams.StdStreamWriter') {alias << write}\n");
      data.container.runScriptlet("JavaUtilities.extend_proxy('com.twineworks.kettle.ruby.step.streams.ErrorStreamWriter') {alias << write}\n");
      data.container.runScriptlet("JavaUtilities.extend_proxy('com.twineworks.kettle.ruby.step.streams.StepStreamWriter') {alias << write}\n");

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

      String gemHome = gemHomePath.getAbsolutePath();
      data.container.runScriptlet("require 'rubygems/defaults/jruby';Gem::Specification.add_dir '" + gemHome + "' unless Gem::Specification.dirs.member?( '" + gemHome + "' )");

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

  @Override
  public void onStopRunning() throws KettleException {

    // allow a few seconds for normal shutdown (i.e. completion of single row processing), before forcibly shutting things down
    new Thread() {
      public void run() {
        try {
          Thread.sleep(5000);
          forceStopRubyThreads();
        } catch (InterruptedException ignored) {
        }
      }
    }.start();


  }

  private void forceStopRubyThreads() {

    // if the container is disposed already, bail out
    if (data.container == null) return;

    // try to kill all threads once
    if (!data.forcedHalt) {
      data.forcedHalt = true;
    } else {
      return;
    }

    if (data.runtime != null) {

      RubyThread[] threads = data.runtime.getThreadService().getActiveRubyThreads();

      for (int i = 0; i < threads.length; i++) {
        try {
          threads[i].kill();
        } catch (ThreadKill e) {
        }
      }

      data.runtime.tearDown();

    }


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

  private IRubyObject getIPAddr() {
    if (data.ipAddr == null) {
      data.ipAddr = data.container.parse("require 'ipaddr'; IPAddr").run();
    }
    return data.ipAddr;
  }

  private void initMainRowStream() throws KettleException {

    // steps inputRowMeta might be null in case we have info steps only, or there's no input to begin with

    RowMetaInterface inputRowMeta = step.getInputRowMeta();
    if (inputRowMeta == null) {
      // when steps connect, but there's no rows, there's also no input row meta
      if (data.hasDirectInput){
        inputRowMeta = step.getTransMeta().getPrevStepFields(step.getStepMeta());
      }
      // when steps don't connect, there's no fields
      else{
        inputRowMeta = new RowMeta();
      }

    }

    data.inputRowMeta = inputRowMeta.clone();
    data.inputFieldNames = data.inputRowMeta.getFieldNames();

    data.outputRowMeta = inputRowMeta.clone();
    meta.getFields(data.outputRowMeta, step.getStepname(), null, null, step, null, null);

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
          case ValueMetaInterface.TYPE_BOOLEAN:
            rubyRow.put(field, vm.getBoolean(r[i]));
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            rubyRow.put(field, vm.getInteger(r[i]));
            break;
          case ValueMetaInterface.TYPE_STRING:
            rubyRow.put(field, vm.getString(r[i]));
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            rubyRow.put(field, vm.getNumber(r[i]));
            break;
          case ValueMetaInterface.TYPE_NONE:
            rubyRow.put(field, r[i]);
            break;
          case ValueMetaInterface.TYPE_SERIALIZABLE:
            if (r[i] instanceof RubyStepMarshalledObject) {
              Object restoredObject = getMarshal().callMethod(data.runtime.getCurrentContext(), "restore", data.runtime.newString(r[i].toString()));
              rubyRow.put(field, restoredObject);
            } else {
              // try to put the object in there as it is.. should create a nice adapter for the java object
              rubyRow.put(field, r[i]);
            }
            break;
          case ValueMetaInterface.TYPE_BINARY:
            // put a ruby array with bytes in there, that is expensive and should probably be avoided
            rubyRow.put(fieldNames[i],
              data.runtime.newArrayNoCopy(JavaUtil.convertJavaArrayToRuby(data.runtime, ArrayUtils.toObject((byte[]) vm.getBinary(r[i]))))
            );

            break;

          case ValueMetaInterface.TYPE_BIGNUMBER:
            IRubyObject bigDecimalObject = getBigDecimal().callMethod(data.runtime.getCurrentContext(), "new", data.runtime.newString((vm.getBigNumber(r[i])).toString()));
            rubyRow.put(field, bigDecimalObject);
            break;

          case ValueMetaInterface.TYPE_DATE:
            rubyRow.put(field, data.runtime.newTime((vm.getDate(r[i])).getTime()));
            break;

          case ValueMetaInterface.TYPE_TIMESTAMP:
            ValueMetaTimestamp vmTimestamp = (ValueMetaTimestamp) vm;
            Timestamp ts = vmTimestamp.getTimestamp(r[i]);
            RubyTime rubyTime = data.runtime.newTime(ts.getTime()/1000*1000);
            rubyTime.setNSec(ts.getNanos());
            rubyRow.put(field, rubyTime);
            break;

          case ValueMetaInterface.TYPE_INET:
            ValueMetaInternetAddress vmInet = (ValueMetaInternetAddress) vm;
            InetAddress ip = vmInet.getInternetAddress(r[i]);
            IRubyObject ipObject = getIPAddr().callMethod(data.runtime.getCurrentContext(), "new", data.runtime.newString(ip.getHostAddress()));
            rubyRow.put(field, ipObject);
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
          case ValueMetaInterface.TYPE_BOOLEAN:
            javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, Boolean.class);
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, Long.class);
            break;
          case ValueMetaInterface.TYPE_STRING:
            javaValue = rubyVal.toString();
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            javaValue = JavaEmbedUtils.rubyToJava(data.runtime, rubyVal, Double.class);
            break;
          case ValueMetaInterface.TYPE_SERIALIZABLE:
            String marshalled = getMarshal().callMethod(data.runtime.getCurrentContext(), "dump", rubyVal).toString();
            javaValue = new RubyStepMarshalledObject(marshalled);
            break;
          case ValueMetaInterface.TYPE_BINARY:
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
          case ValueMetaInterface.TYPE_BIGNUMBER:
            if (rubyVal instanceof RubyFloat) {
              javaValue = new BigDecimal((Double) rubyVal.toJava(Double.class));
            } else {
              javaValue = new BigDecimal(rubyVal.toString());
            }

            break;
          case ValueMetaInterface.TYPE_DATE:
            if (rubyVal instanceof RubyFixnum) {
              javaValue = new Date(((RubyFixnum) rubyVal).getLongValue());
            } else if (rubyVal instanceof RubyTime) {
              javaValue = ((RubyTime) rubyVal).getJavaDate();
            }
            else{
              throw new KettleException("cannot convert ruby value "+rubyVal.toString()+" to java Date");
            }
            break;

          case ValueMetaInterface.TYPE_TIMESTAMP:
            if (rubyVal instanceof RubyFixnum) {
              javaValue = new java.sql.Timestamp(((RubyFixnum) rubyVal).getLongValue());
            } else if (rubyVal instanceof RubyTime) {
              RubyTime time = (RubyTime) rubyVal;
              long millis = time.getDateTime().getMillis();
              Timestamp ts = new java.sql.Timestamp(millis/1000*1000);
              ts.setNanos((int) ((time.getNSec())+(millis%1000*1000000)));
              javaValue = ts;
            }
            else{
              throw new KettleException("cannot convert ruby value "+rubyVal.toString()+" to java.sql.Timestamp");
            }
            break;

          case ValueMetaInterface.TYPE_INET:
            Long longNum = (Long) data.container.callMethod(rubyVal, "to_i");
            javaValue = toInetAddress(longNum.intValue());
            break;

        }

      }

      r[data.fieldIndexCache.get(forRow).get(outField.getName())] = javaValue;
    }

  }

  private byte[] toIPByteArray(int addr){
    return new byte[]{(byte)(addr>>>24), (byte)(addr>>>16), (byte)(addr>>>8), (byte)addr};
  }

  private InetAddress toInetAddress(int addr){
    try {
      return InetAddress.getByAddress(toIPByteArray(addr));
    } catch (UnknownHostException e) {
      //should never happen
      return null;
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
    try {

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

    } catch (EvalFailedException e) {
      if (!data.forcedHalt) {
        throw new KettleException(e);
      }
      // transformation has been stopped
      return false;
    } catch (ThreadKill e) {
      if (!data.forcedHalt) {
        throw new KettleException(e);
      }
      // transformation has been stopped
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

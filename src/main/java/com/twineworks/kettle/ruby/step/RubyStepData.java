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

package com.twineworks.kettle.ruby.step;

import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepErrorMeta;

import java.util.HashMap;
import java.util.List;

public class RubyStepData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface errorRowMeta;

  public String[] inputFieldNames;

  // ruby language runtime related
  public ScriptingContainer container;
  public Ruby runtime;
  public EmbedEvalUnit rubyScriptObject;

  // set to false if a step has no input steps or all input steps are info steps
  public boolean hasDirectInput;

  // this is used as a temporary holder for rows returned by the ruby script
  public List<Object[]> rowList;
  public StepErrorMeta stepErrorMeta;
  public IRubyObject bigDecimal;
  public IRubyObject ipAddr;
  public IRubyObject marshal;

  public HashMap<String, RubyString> rubyStringCache = new HashMap<String, RubyString>();
  public HashMap<RowMetaInterface, HashMap<String, Integer>> fieldIndexCache = new HashMap<RowMetaInterface, HashMap<String, Integer>>();
  public RowMetaInterface emptyRowMeta = new RowMeta();
  public RowMetaInterface baseRowMeta;

  public boolean forcedHalt = false;

  public RubyStepData() {
    super();

  }

  public void cacheFieldNames(RowMetaInterface rowMeta) {

    HashMap<String, Integer> fieldCache = new HashMap<String, Integer>();
    int i = 0;
    for (ValueMetaInterface field : rowMeta.getValueMetaList()) {
      rubyStringCache.put(field.getName(), runtime.newString(field.getName()));
      fieldCache.put(field.getName(), i++);
    }

    fieldIndexCache.put(rowMeta, fieldCache);

  }
}


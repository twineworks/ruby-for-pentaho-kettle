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

package com.twineworks.kettle.ruby.step.streams;

import com.twineworks.kettle.ruby.step.RubyStepData;
import com.twineworks.kettle.ruby.step.execmodels.SimpleExecutionModel;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.step.BaseStep;

public class StdStreamReader {

  private BaseStep step;
  private SimpleExecutionModel model;
  private RubyStepData data;

  public StdStreamReader(SimpleExecutionModel model) throws KettleStepException {

    this.model = model;
    this.step = model.getStep();
    this.data = model.getData();

  }

  public IRubyObject read() throws KettleException {

    Object r[] = step.getRow();

    // signal that there's no more rows coming
    if (r == null) {
      return data.runtime.getNil();
    }

    IRubyObject rubyRow = model.createRubyInputRow(data.inputRowMeta, r);
    return rubyRow;
  }

  public IRubyObject read(long upTo) throws KettleException {

    // request to read <0 rows
    if (upTo < 0) return data.runtime.getNil();

    RubyArray arr = data.runtime.newArray();
    int read = 0;
    while (read < upTo) {
      IRubyObject o = read();
      if (o.isNil()) break;
      arr.append(o);
      read++;
    }

    // request to read from empty stream
    if (arr.size() == 0 && upTo > 0) return data.runtime.getNil();

    return arr;

  }

  public RubyArray readAll() throws KettleException {

    RubyArray arr = data.runtime.newArray();

    while (true) {
      IRubyObject o = read();
      if (o.isNil()) break;
      arr.append(o);
    }

    return arr;

  }

}

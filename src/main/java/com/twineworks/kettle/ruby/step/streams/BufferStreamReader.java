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
import org.pentaho.di.core.exception.KettleStepException;

/**
 * Instances of StepStreamReader are designed to be used from within ruby scripts.
 * They are primarily used to read from info streams.
 */


public class BufferStreamReader {

  private RubyStepData data;
  private RubyArray buffer;
  private long readPointer;

  public BufferStreamReader(SimpleExecutionModel model, RubyArray buffer) throws KettleStepException {

    this.data = model.getData();

    this.buffer = buffer;

    readPointer = 0;

  }

  public IRubyObject read() {

    IRubyObject row = buffer.entry(readPointer);
    readPointer += 1;
    return row;
  }

  public IRubyObject read(long upTo) {

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

  public RubyArray readAll() {

    RubyArray arr = data.runtime.newArray();

    while (true) {
      IRubyObject o = read();
      if (o.isNil()) break;
      arr.append(o);
    }

    return arr;

  }

}

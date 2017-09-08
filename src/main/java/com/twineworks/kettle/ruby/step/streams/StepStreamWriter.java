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
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;

import java.util.LinkedList;
import java.util.List;

public class StepStreamWriter {

  private RowSet rs;
  private BaseStep step;
  private SimpleExecutionModel model;
  private RubyStepData data;
  private List<Object[]> rowList;
  private int rowSize;
  private RowMetaInterface inRow;

  public StepStreamWriter(SimpleExecutionModel model, String srcStepName) throws KettleStepException {

    this.model = model;
    this.step = model.getStep();
    this.data = model.getData();
    this.rowList = new LinkedList<Object[]>();

    rs = step.findOutputRowSet(srcStepName);

    rowSize = data.outputRowMeta.size();
    inRow = new RowMeta();

  }

  public void write(IRubyObject rubyOut) throws KettleException {

    Object[] r = new Object[rowSize];

    rowList.clear();
    model.fetchRowsFromScriptOutput(rubyOut, inRow, r, rowList, data.outputRowMeta.getValueMetaList(), data.outputRowMeta);

    for (Object[] outRow : rowList) {
      rs.putRow(data.outputRowMeta, outRow);
      step.incrementLinesWritten();
    }

  }

}

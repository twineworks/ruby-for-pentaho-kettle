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

import com.twineworks.kettle.ruby.step.execmodels.ExecutionModel;
import com.twineworks.kettle.ruby.step.execmodels.SimpleExecutionModel;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;


public class RubyStep extends BaseStep implements StepInterface {

  private RubyStepData data;
  private RubyStepMeta meta;
  private ExecutionModel model;

  public RubyStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
    super(s, stepDataInterface, c, t, dis);
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

    meta = (RubyStepMeta) smi;
    data = (RubyStepData) sdi;

    return model.onProcessRow();

  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (RubyStepMeta) smi;
    data = (RubyStepData) sdi;

    boolean initSuccess = super.init(smi, sdi);

    if (initSuccess) {
      model = new SimpleExecutionModel();
      model.setEnvironment(this, data, meta);
      initSuccess = model.onInit();
    }
    return initSuccess;
  }

  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (RubyStepMeta) smi;
    data = (RubyStepData) sdi;

    if (model != null) {
      model.onDispose();
      model = null;
    }

    super.dispose(smi, sdi);
  }


  public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException {

    if (model != null) {
      model.onStopRunning();
    }
  }


  // convenience method to find this plugin's directory
  public String getPluginDir() {

    URL pluginBaseURL = PluginRegistry.getInstance().findPluginWithId(StepPluginType.class, "TypeExitRubyStep").getPluginDirectory();

    File pluginBaseFile;

    try {
      pluginBaseFile = new File(pluginBaseURL.toURI());
    } catch (URISyntaxException e) {
      pluginBaseFile = new File(pluginBaseURL.getPath());
    }

    return pluginBaseFile.getAbsolutePath();

  }


}

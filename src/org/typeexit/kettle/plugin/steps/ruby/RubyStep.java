package org.typeexit.kettle.plugin.steps.ruby;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.ExecutionModel;
import org.typeexit.kettle.plugin.steps.ruby.execmodels.SimpleExecutionModel;


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
		
		if (initSuccess){
			model = new SimpleExecutionModel();
			model.setEnvironment(this, data, meta);
			initSuccess = model.onInit();
		}
		return initSuccess;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RubyStepMeta) smi;
		data = (RubyStepData) sdi;
		
		if (model != null){
			model.onDispose();
			model = null;
		}
		
		super.dispose(smi, sdi);
	}

	// convenience method to find this plugin's directory
	public String getPluginDir(){
		
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

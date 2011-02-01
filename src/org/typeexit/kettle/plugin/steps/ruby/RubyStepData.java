package org.typeexit.kettle.plugin.steps.ruby;

import org.jruby.Ruby;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class RubyStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public Object rubyReceiverObject;
	public ScriptingContainer container;
	public RowMetaInterface inputRowMeta;
	public String[] inputFieldNames;
	public Ruby runtime;
	
    public RubyStepData()
	{
		super();
	}
}
	

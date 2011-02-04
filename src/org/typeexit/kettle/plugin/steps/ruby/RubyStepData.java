package org.typeexit.kettle.plugin.steps.ruby;

import java.util.List;

import org.jruby.Ruby;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class RubyStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public RowMetaInterface inputRowMeta;
	public String[] inputFieldNames;

	// ruby language runtime related
	public ScriptingContainer container;
	public Ruby runtime;
	public EmbedEvalUnit rubyScriptObject;
	
	// set to false if a step has no input steps or all input steps are info steps
	public boolean hasDirectInput;
	
	// this is used as a temporary holder for rows returned by the ruby script
	public List<Object[]> rowList;
	
    public RubyStepData()
	{
		super();
	}
}
	

package org.typeexit.kettle.plugin.steps.ruby;

import java.util.HashMap;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepErrorMeta;

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
	public IRubyObject marshal;
	
	public HashMap<String, RubyString> rubyStringCache = new HashMap<String, RubyString>();
	public HashMap<RowMetaInterface, HashMap<String, Integer>> fieldIndexCache = new HashMap<RowMetaInterface, HashMap<String, Integer>>();
	
    public RubyStepData()
	{
		super();
		
	}
    
    public void cacheFieldNames(RowMetaInterface rowMeta){

    	HashMap<String, Integer> fieldCache = new HashMap<String, Integer>();
    	int i=0;
    	for(ValueMetaInterface field: rowMeta.getValueMetaList()){
			rubyStringCache.put(field.getName(), runtime.newString(field.getName()));
			fieldCache.put(field.getName(), i++);
		}
    	
    	fieldIndexCache.put(rowMeta, fieldCache);
    	
    }
}
	

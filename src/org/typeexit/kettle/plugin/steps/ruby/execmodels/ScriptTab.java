package org.typeexit.kettle.plugin.steps.ruby.execmodels;

import org.typeexit.kettle.plugin.steps.ruby.RubyStepData;
import org.typeexit.kettle.plugin.steps.ruby.meta.RubyScriptMeta;

public class ScriptTab {

	private String title;
	private String script;
	private String role;
	private RubyStepData data;
	private boolean hasRun;
	
	protected ScriptTab(RubyScriptMeta meta, RubyStepData data){
		title = meta.getTitle();
		role = meta.getRole().name();
		script = meta.getScript();
		
		hasRun = false;
		this.data = data; 
	}

	public String getTitle() {
		return title;
	}

	public String getScript() {
		return script;
	}

	public String getRole() {
		return role;
	}
	
	public void load(){
		data.container.runScriptlet(script);
	}
	
	public void require(){
		
		if (!hasRun){
			hasRun = true;
			data.container.runScriptlet(script);
		}
	}
	
	
}

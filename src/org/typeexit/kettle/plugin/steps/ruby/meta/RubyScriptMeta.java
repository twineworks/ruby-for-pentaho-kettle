package org.typeexit.kettle.plugin.steps.ruby.meta;

import java.util.ArrayList;
import java.util.List;

public class RubyScriptMeta implements Cloneable {

	public static final RubyScriptMeta DEFAULT_SCRIPT = new RubyScriptMeta("Ruby Script", "# your script goes here my friend");
	private String title;
	private String script;
	
	public RubyScriptMeta(){
		super();
	}

	public RubyScriptMeta(String title, String script) {
		super();
		this.title = title;
		this.script = script;
	}

	public static RubyScriptMeta createScriptWithUniqueName(List<RubyScriptMeta> existing){
		
		String title = "New Script";
		List<String> existingTitles = extractTitles(existing);
		
		int num = 1;
		String baseTitle = title;
		while(existingTitles.contains(title)){
			title = baseTitle + " "+num;
			num += 1;
		}
		
		RubyScriptMeta retval = DEFAULT_SCRIPT.clone();
		retval.setTitle(title);
		
		return retval;
	} 
	
	private static List<String> extractTitles(List<RubyScriptMeta> scripts){
		ArrayList<String> titles = new ArrayList<String>(scripts.size());
		
		for (RubyScriptMeta script : scripts) {
			titles.add(script.getTitle());
		}
		
		return titles;
		
	}
	
	public RubyScriptMeta clone() {
		try {
			return (RubyScriptMeta) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}

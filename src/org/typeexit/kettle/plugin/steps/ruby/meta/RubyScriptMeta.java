package org.typeexit.kettle.plugin.steps.ruby.meta;

import java.util.ArrayList;
import java.util.List;

public class RubyScriptMeta implements Cloneable {

	
	private String title;
	private String script;
	private Role role;

	public static final RubyScriptMeta DEFAULT_SCRIPT = new RubyScriptMeta("Ruby Script", "# your script goes here \n\nputs \"Hello World!\"", Role.ROW_SCRIPT);
	
	static public enum Role {
		LIB_SCRIPT,
		ROW_SCRIPT,
		INIT_SCRIPT,
		DISPOSE_SCRIPT
	};
	
	public RubyScriptMeta(){
		super();
	}

	public RubyScriptMeta(String title, String script, Role role) {
		super();
		this.title = title;
		this.script = script;
		this.role = role;
	}

	public static RubyScriptMeta createScriptWithUniqueName(List<RubyScriptMeta> existing){
		
		String	title = getUniqueName("New Script", existing);
		
		RubyScriptMeta retval = DEFAULT_SCRIPT.clone();
		retval.setRole(Role.LIB_SCRIPT);
		retval.setTitle(title);
		
		return retval;
	} 
	
	public static String getUniqueName(String title, List<RubyScriptMeta> existing){
		
		List<String> existingTitles = extractTitles(existing);
		
		int num = 1;
		String baseTitle = title;
		while(existingTitles.contains(title)){
			title = baseTitle + " "+num;
			num += 1;
		}
		
		return title;
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}

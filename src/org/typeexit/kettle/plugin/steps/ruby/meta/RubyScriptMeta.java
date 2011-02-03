package org.typeexit.kettle.plugin.steps.ruby.meta;

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

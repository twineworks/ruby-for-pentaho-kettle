package org.typeexit.kettle.plugin.steps.ruby;

import java.util.ArrayList;
import java.util.List;

import org.jruby.CompatVersion;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta.RubyVersion;

public class RubyStepFactory {

//	private static ScriptingContainer containerInstance;
	
//	synchronized public static ScriptingContainer getScriptingContainer(){
//		
//		if (containerInstance == null){
//			containerInstance = createScriptingContainer();
//		}
//		
//		return containerInstance;
//		
//	}
	
	synchronized public static ScriptingContainer createScriptingContainer(boolean withPersistentLocalVars, RubyVersion rubyVersion){
		
		ScriptingContainer c = new ScriptingContainer(LocalContextScope.SINGLETHREAD, (withPersistentLocalVars)?LocalVariableBehavior.PERSISTENT:LocalVariableBehavior.TRANSIENT);
		
		c.setClassLoader(ScriptingContainer.class.getClassLoader());

		List<String> paths = new ArrayList<String>();
		paths.add(c.getHomeDirectory());
		paths.add(ScriptingContainer.class.getProtectionDomain().getCodeSource().getLocation().toString());
		c.setLoadPaths(paths); 
		
		// TODO: add kettle directory to path? 
		
		switch(rubyVersion){
		case RUBY_1_8:
			c.setCompatVersion(CompatVersion.RUBY1_8);
			break;
		case RUBY_1_9:
			c.setCompatVersion(CompatVersion.RUBY1_9);
			break;
		}
		
		return c;
		
	}
	
}

package org.typeexit.kettle.plugin.steps.ruby;

import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
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
		
		switch(rubyVersion){
		case RUBY_1_8:
			c.setCompatVersion(CompatVersion.RUBY1_8);
			break;
		case RUBY_1_9:
			c.setCompatVersion(CompatVersion.RUBY1_9);
			break;
		}
		
		c.setCompileMode(CompileMode.JIT);

		c.setRunRubyInProcess(false);
		ClassLoader loader = ScriptingContainer.class.getClassLoader();
		c.setClassLoader(loader);
		
//		List<String> paths = new ArrayList<String>();
//		paths.add(c.getHomeDirectory());
//		paths.add(ScriptingContainer.class.getProtectionDomain().getCodeSource().getLocation().toString());
//		c.setLoadPaths(paths); 
		
		// TODO: add kettle directory to path? 
		
		return c;
		
	}
	
}

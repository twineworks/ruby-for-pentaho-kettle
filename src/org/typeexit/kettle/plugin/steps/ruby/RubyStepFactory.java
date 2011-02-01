package org.typeexit.kettle.plugin.steps.ruby;

import java.util.ArrayList;
import java.util.List;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

public class RubyStepFactory {

	private static ScriptingContainer containerInstance;
	
	synchronized public static ScriptingContainer getScriptingContainer(){
		
		if (containerInstance == null){
			containerInstance = createScriptingContainer();
		}
		
		return containerInstance;
		
	}
	
	synchronized public static ScriptingContainer createScriptingContainer(){
		
		ScriptingContainer c = new ScriptingContainer(LocalContextScope.THREADSAFE);
		c.setClassLoader(ScriptingContainer.class.getClassLoader());

		List<String> paths = new ArrayList<String>();
		paths.add(c.getHomeDirectory());
		paths.add(ScriptingContainer.class.getProtectionDomain().getCodeSource().getLocation().toString());
		c.setLoadPaths(paths); 
		
		// TODO: add kettle directory ? and 
		//data.container.setCompileMode(CompileMode.JIT);
		
		return c;
		
	}
	
	
}

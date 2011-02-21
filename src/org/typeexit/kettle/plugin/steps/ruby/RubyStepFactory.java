package org.typeexit.kettle.plugin.steps.ruby;

import java.lang.reflect.Field;

import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.jruby.util.JRubyClassLoader;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta.RubyVersion;

public class RubyStepFactory {

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
		
		// make sure JDBC drivers do not! get unregistered, see http://jira.codehaus.org/browse/JRUBY-5528 
		JRubyClassLoader rubyLoader = c.getProvider().getRuntime().getJRubyClassLoader();
		try {
			Field unloaderField = rubyLoader.getClass().getDeclaredField("unloader");
			unloaderField.setAccessible(true);
			unloaderField.set(rubyLoader, new Runnable(){public void run() {}});
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		
		
//		List<String> paths = new ArrayList<String>();
//		paths.add(c.getHomeDirectory());
//		paths.add(ScriptingContainer.class.getProtectionDomain().getCodeSource().getLocation().toString());
//		c.setLoadPaths(paths); 
				
		return c;
		
	}
	
}

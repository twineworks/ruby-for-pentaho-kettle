package org.typeexit.kettle.plugin.steps.ruby;

import org.jruby.embed.ScriptingContainer;
import org.typeexit.kettle.plugin.steps.ruby.RubyStepMeta.RubyVersion;

import junit.framework.TestCase;

public class TestFactory extends TestCase{

	public void testFactory(){
		
		ScriptingContainer c = RubyStepFactory.createScriptingContainer(false, RubyVersion.RUBY_1_8);
		c.runScriptlet("puts \"Ruby version: #{RUBY_VERSION}\"");
		
	}
	
	public void testJDBCDrivers(){
		// this is a regression test for JDBC drivers disappearing from the JVM, since jruby will
		// remove them by default.
		// The factory is supposed to create a container that does not do that
		
		// this script will raise an exception if the org.h2.Driver got unregistered
		// see http://jira.codehaus.org/browse/JRUBY-5528 
		String testScript = "require 'java'" + "\n" +
				"Java::org.h2.Driver" + "\n" +
				"url = 'jdbc:h2:mem:MyDatabase'" + "\n" +
				"conn = java.sql.DriverManager.get_connection(url, 'H2', '')" + "\n" +
				"conn.close";

		// if the script can be executed repeatedly, all is fine. It raises an exception
		// if the driver got unregistered at some point.
		for (int i=1;i<=5;i++){
			ScriptingContainer c = RubyStepFactory.createScriptingContainer(false, RubyVersion.RUBY_1_8);
			c.runScriptlet(testScript);
			c.terminate();
			System.out.println("Testing JDBC Driver persistence: iteration "+i);
		}
		
	}
	
}

/*
 * Ruby for pentaho kettle
 * Copyright (C) 2017 Twineworks GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.twineworks.kettle.ruby.step;

import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class RubyStepFactory {

  synchronized public static ScriptingContainer createScriptingContainer(boolean withPersistentLocalVars) {

    ScriptingContainer c = new ScriptingContainer(LocalContextScope.SINGLETHREAD, (withPersistentLocalVars) ? LocalVariableBehavior.PERSISTENT : LocalVariableBehavior.TRANSIENT);
    c.setCompileMode(CompileMode.JIT);

    c.setRunRubyInProcess(false);
    ClassLoader loader = ScriptingContainer.class.getClassLoader();
    c.setClassLoader(loader);

    // does it make sense to include more in the class path?

//		List<String> paths = new ArrayList<String>();
//		paths.add(c.getHomeDirectory());
//		paths.add(ScriptingContainer.class.getProtectionDomain().getCodeSource().getLocation().toString());
//		c.setLoadPaths(paths); 

    return c;

  }

}

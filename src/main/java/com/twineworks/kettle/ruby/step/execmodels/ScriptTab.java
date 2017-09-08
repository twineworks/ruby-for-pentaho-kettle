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

package com.twineworks.kettle.ruby.step.execmodels;

import com.twineworks.kettle.ruby.step.RubyStepData;
import com.twineworks.kettle.ruby.step.meta.RubyScriptMeta;

public class ScriptTab {

  private String title;
  private String script;
  private String role;
  private RubyStepData data;
  private boolean hasRun;

  protected ScriptTab(RubyScriptMeta meta, RubyStepData data) {
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

  public void load() {
    data.container.runScriptlet(script);
  }

  public void require() {

    if (!hasRun) {
      hasRun = true;
      data.container.runScriptlet(script);
    }
  }


}

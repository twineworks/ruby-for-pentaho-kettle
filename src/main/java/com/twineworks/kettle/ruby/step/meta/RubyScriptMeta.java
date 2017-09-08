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

package com.twineworks.kettle.ruby.step.meta;

import java.util.ArrayList;
import java.util.List;

public class RubyScriptMeta implements Cloneable {


  public static final RubyScriptMeta DEFAULT_SCRIPT = new RubyScriptMeta("Ruby Script", "# your script goes here \n\nputs \"Hello World!\"", Role.ROW_SCRIPT);
  private String title;
  private String script;
  private Role role;

  public RubyScriptMeta() {
    super();
  }

  public RubyScriptMeta(String title, String script, Role role) {
    super();
    this.title = title;
    this.script = script;
    this.role = role;
  }

  public static RubyScriptMeta createScriptWithUniqueName(List<RubyScriptMeta> existing) {

    String title = getUniqueName("New Script", existing);

    RubyScriptMeta retval = DEFAULT_SCRIPT.clone();
    retval.setRole(Role.LIB_SCRIPT);
    retval.setTitle(title);

    return retval;
  }

  public static String getUniqueName(String title, List<RubyScriptMeta> existing) {

    List<String> existingTitles = extractTitles(existing);

    int num = 1;
    String baseTitle = title;
    while (existingTitles.contains(title)) {
      title = baseTitle + " " + num;
      num += 1;
    }

    return title;
  }

  private static List<String> extractTitles(List<RubyScriptMeta> scripts) {
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

  static public enum Role {
    LIB_SCRIPT,
    ROW_SCRIPT,
    INIT_SCRIPT,
    DISPOSE_SCRIPT
  }

}

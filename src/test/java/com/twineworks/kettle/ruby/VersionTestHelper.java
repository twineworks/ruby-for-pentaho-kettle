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

package com.twineworks.kettle.ruby;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.fail;

public class VersionTestHelper {

  public static final String projectVersion;

  static {
    InputStream inputStream = KettleTestHelper.class.getClassLoader().getResourceAsStream("com/twineworks/kettle/ruby/version.properties");
    if (inputStream == null){
      fail("cannot determine plugin directory location");
    }
    Properties props = new Properties();
    try {
      props.load(inputStream);
    } catch (IOException e) {
      fail(e.getMessage(), e);
    }

    projectVersion = props.getProperty("project.version");

  }

}

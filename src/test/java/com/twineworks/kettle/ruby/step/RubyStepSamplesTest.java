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

import com.twineworks.kettle.ruby.FileSystemTestHelper;
import com.twineworks.kettle.ruby.KettleTestHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pentaho.di.trans.Trans;

import java.util.ArrayList;
import java.util.List;

import static com.twineworks.kettle.ruby.KettleTestHelper.runTransform;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class RubyStepSamplesTest {

  private String testFile;

  public RubyStepSamplesTest(String testFile, String label) {
    this.testFile = testFile;
  }

  @BeforeClass
  public static void beforeAll() throws Exception {
    KettleTestHelper.initKettle();
  }

  @Parameters(name = "{1}")
  public static Iterable<Object[]> params() throws Exception {
    List<String> files = FileSystemTestHelper.find("glob:**/step/samples/**/*.ktr", KettleTestHelper.pluginDir);
    List<Object[]> params = new ArrayList<>();
    int prefixLength = KettleTestHelper.pluginDir.length() + "/step/samples/".length();
    for (String file : files) {
      params.add(new Object[]{
        file,  // ktr to run
        file.substring(prefixLength)} // test name
      );
    }

    return params;
  }

  @Test
  public void completes_successfully() throws Exception {
    Trans trans = runTransform(testFile, 20);

    assertThat(trans.isFinished()).isTrue();
    assertThat(trans.getErrors()).isEqualTo(0);
  }

}

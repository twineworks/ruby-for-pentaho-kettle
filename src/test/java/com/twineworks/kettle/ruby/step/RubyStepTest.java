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

import com.twineworks.kettle.ruby.KettleTestHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.trans.Trans;

import static com.twineworks.kettle.ruby.KettleTestHelper.runTransform;
import static org.assertj.core.api.Assertions.assertThat;

public class RubyStepTest {
  @BeforeClass
  public static void beforeAll() throws Exception {
    KettleTestHelper.initKettle();
  }

  @Test
  public void accepts_null_values() throws Exception {
    Trans trans = runTransform("src/test/resources/etl/step/accepting_nulls/test_null_handling.ktr", 20);

    assertThat(trans.isFinished()).isTrue();
    assertThat(trans.getErrors()).isEqualTo(0);
  }

  @Test
  public void supports_lazy_conversion_values() throws Exception {
    Trans trans = runTransform("src/test/resources/etl/step/lazy_conversion/lazy_conversion.ktr", 20);

    assertThat(trans.isFinished()).isTrue();
    assertThat(trans.getErrors()).isEqualTo(0);
  }

  @Test
  public void converts_ruby_values_to_kettle_values() throws Exception {
    Trans trans = runTransform("src/test/resources/etl/step/type_out_conversion/type_out_conversion.ktr", 20);

    assertThat(trans.isFinished()).isTrue();
    assertThat(trans.getErrors()).isEqualTo(0);
  }

  @Test
  public void updates_fields_in_empty_stream() throws Exception {
    Trans trans = runTransform("src/test/resources/etl/step/update_fields_in_empty_stream/update_fields_in_empty_stream.ktr", 20);

    assertThat(trans.isFinished()).isTrue();
    assertThat(trans.getErrors()).isEqualTo(0);
  }

}

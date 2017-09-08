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

import org.pentaho.di.core.row.ValueMeta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class OutputFieldMeta implements Cloneable {

  private String name;
  private int type;
  private boolean update;
  private Class<?> conversionClass;

  public OutputFieldMeta(String name, int type, boolean update) {
    this.name = name;
    setType(type);
    this.update = update;

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;

    switch (type) {
      case ValueMeta.TYPE_NONE:
        conversionClass = void.class;
        break;
      case ValueMeta.TYPE_INTEGER:
        conversionClass = Long.class;
        break;
      case ValueMeta.TYPE_BOOLEAN:
        conversionClass = Boolean.class;
        break;
      case ValueMeta.TYPE_DATE:
        conversionClass = Date.class;
        break;
      case ValueMeta.TYPE_BIGNUMBER:
        conversionClass = BigDecimal.class;
        break;
      case ValueMeta.TYPE_NUMBER:
        conversionClass = Double.class;
        break;
      case ValueMeta.TYPE_SERIALIZABLE:
        conversionClass = Serializable.class;
        break;
      case ValueMeta.TYPE_STRING:
        conversionClass = String.class;
        break;
    }

  }

  public boolean isUpdate() {
    return update;
  }

  public void setUpdate(boolean update) {
    this.update = update;
  }

  public Class<?> getConversionClass() {
    return this.conversionClass;
  }

  public OutputFieldMeta clone() {
    try {
      return (OutputFieldMeta) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

}

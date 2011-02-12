package org.typeexit.kettle.plugin.steps.ruby.meta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import org.pentaho.di.core.row.ValueMeta;

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

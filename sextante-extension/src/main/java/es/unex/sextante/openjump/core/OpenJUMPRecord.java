package es.unex.sextante.openjump.core;

import com.vividsolutions.jump.feature.Feature;

import es.unex.sextante.dataObjects.IRecord;

public class OpenJUMPRecord implements IRecord {

	private final Object[] m_Values;

	public OpenJUMPRecord(Feature feature) {

		m_Values = feature.getAttributes();

	}

	public Object getValue(int iField) {

		return m_Values[iField];

	}

	public Object[] getValues() {

		return m_Values;

	}

}

package es.unex.sextante.openjump.core;

import com.vividsolutions.jump.feature.Feature;

import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.RecordImpl;
import org.locationtech.jts.geom.Geometry;

public class OpenJUMPFeature implements IFeature {

	private final Feature m_Feature;

	public OpenJUMPFeature(Feature feature) {

		m_Feature = feature;

	}

	public Geometry getGeometry() {

		return m_Feature.getGeometry();

	}

	public IRecord getRecord() {

		Object[] allAttrs = m_Feature.getAttributes();
		Object[] attrs = new Object[allAttrs.length - 1];
		System.arraycopy(allAttrs, 1, attrs, 0,attrs.length);
		return new RecordImpl(attrs);

	}

}

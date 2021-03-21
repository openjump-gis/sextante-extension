/*******************************************************************************
 LinkPointsToLinesAlgorithm.java
 Autor: Fco. José Peñarrubia (fjp@scolab.es)
 Copyright (C) SCOLAB Software Colaborativo S.L.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/

package org.gvsig.scolab;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;

/**
 * @author Francisco José Peñarrubia (fjp@scolab.es)
 * First param: Point layer with a field in deegrees
 * Second param: Field Name
 * Third param: lenght of segment
 * Useful if you have points + angle to render text and wants to reproject it.
 *  
 */
public class CreateSegmentFromPointAndAngleAlgorithm extends GeoAlgorithm {
	public static final String RESULT = "RESULT";
	public static final String FIELD_ANGLE = "FIELD_ANGLE";
	public static final String LENGTH = "LENGTH";
	public static final String LAYER  = "LAYER";

	GeometryFactory geomFact = new GeometryFactory();

	/* (non-Javadoc)
	 * @see es.unex.sextante.core.GeoAlgorithm#defineCharacteristics()
	 */
	@Override
	public void defineCharacteristics() {

		setGroup(Sextante.getText("Tools_for_point_layers")); //$NON-NLS-1$
		this.setName(Sextante.getText("create_segment_from_point_angle")); //$NON-NLS-1$
		try {
			m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("original_layer"), //$NON-NLS-1$ //$NON-NLS-2$
					AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);

			m_Parameters.addTableField(FIELD_ANGLE, Sextante.getText("angle_field_in_deegrees"),
					"LAYER", true);

			m_Parameters.addNumericalValue(LENGTH, Sextante.getText("length"), //$NON-NLS-1$ //$NON-NLS-2$
					10.0, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

			addOutputVectorLayer("RESULT", Sextante.getText("new_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE); //$NON-NLS-1$

		} catch (RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		} catch (UndefinedParentParameterNameException e) {
			Sextante.addErrorToLog(e);
		} catch (OptionalParentParameterException e) {
			Sextante.addErrorToLog(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.unex.sextante.StandardExtension.core.StandardExtensionGeoAlgorithm
	 * #processAlgorithm()
	 */
	@Override
	public boolean processAlgorithm() {

		int i = 0;

		try {
			IVectorLayer layer = m_Parameters
					.getParameterValueAsVectorLayer(LAYER); //$NON-NLS-1$

			IVectorLayer result = getNewVectorLayer(RESULT, Sextante.getText("new_layer"), layer.SHAPE_TYPE_LINE, //$NON-NLS-1$
					layer.getFieldTypes(), layer.getFieldNames());

			double length = m_Parameters.getParameterValueAsDouble(LENGTH);

			int fieldIndex = m_Parameters.getParameterValueAsInt(FIELD_ANGLE);

			int iShapeCount = layer.getShapesCount();
			m_Task.setProgressText(Sextante.getText("exporting")); //$NON-NLS-1$
			IFeatureIterator iter = layer.iterator();
			while (iter.hasNext() && setProgress(i, iShapeCount)) {
				try {
					IFeature feat = iter.next();
					Geometry geom = feat.getGeometry();
					Coordinate c = geom.getCoordinate();
					double angDeg = Double.parseDouble(feat.getRecord().getValue(fieldIndex).toString());
					double angle = Math.toRadians(angDeg);
					double w = Math.cos(angle) * length;
					double h = Math.sin(angle) * length;
					Coordinate lastPoint = new Coordinate(c.x + w, c.y + h);
					Coordinate[] coordinates = new Coordinate[2];
					coordinates[0] = c;
					coordinates[1] = lastPoint;
					Geometry newGeom = geomFact.createLineString(coordinates);
					result.addFeature(newGeom, feat.getRecord().getValues());
				}
				catch (IteratorException ex) {
					ex.printStackTrace();
				}
				i++;

			}


		} catch (GeoAlgorithmExecutionException e) {
			e.printStackTrace();
			return false;
		}

		return !m_Task.isCanceled();
	}

}
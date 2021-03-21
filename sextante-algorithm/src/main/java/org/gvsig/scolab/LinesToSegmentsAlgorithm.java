/*******************************************************************************
 LinkPointsToLinesAlgorithm.java
 Autor: Fco. Jose Penarrubia (fjp@scolab.es)
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
import org.locationtech.jts.geom.LineString;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

/**
 * @author Francisco José Peñarrubia (fjp@scolab.es) 
 * Algorithm to create a layer with one record for each segment of line (useful sometimes for networks).
 * 
 */
public class LinesToSegmentsAlgorithm
extends
GeoAlgorithm {
	GeometryFactory geomFact = new GeometryFactory();


	/* (non-Javadoc)
	 * @see es.unex.sextante.core.GeoAlgorithm#defineCharacteristics()
	 */
	@Override
	public void defineCharacteristics() {

		setGroup(Sextante.getText("Tools_for_line_layers")); //$NON-NLS-1$
		this.setName(Sextante.getText("LineToSegments2")); //$NON-NLS-1$
		try {
			m_Parameters.addInputVectorLayer("LAYER", Sextante.getText("original_layer"), //$NON-NLS-1$ //$NON-NLS-2$
					AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);

			addOutputVectorLayer("RESULT", Sextante.getText("new_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE); //$NON-NLS-1$

		}
		catch (final RepeatedParameterNameException e) {
			e.printStackTrace();
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
			final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer("LAYER"); //$NON-NLS-1$

			final IVectorLayer result = getNewVectorLayer("RESULT", Sextante.getText("new_layer"), layer.getShapeType(), //$NON-NLS-1$
					layer.getFieldTypes(), layer.getFieldNames());

			final int iShapeCount = layer.getShapesCount();
			m_Task.setProgressText(Sextante.getText("exporting")); //$NON-NLS-1$
			final IFeatureIterator iter = layer.iterator();
			while (iter.hasNext() && setProgress(i, iShapeCount)) {
				try {
					final IFeature feat = iter.next();
					final Geometry geom = feat.getGeometry();
					for (int n = 0; n < geom.getNumGeometries(); n++) {
						Geometry part = geom.getGeometryN(n);
						Coordinate[] coords = part.getCoordinates();
						for (int k = 0; k < coords.length-1; k++) {
							Coordinate[] minicoords = new Coordinate[2];
							minicoords[0] = coords[k];
							minicoords[1] = coords[k+1];
							LineString geom2 = geomFact.createLineString(minicoords);
							result.addFeature(geom2, feat.getRecord().getValues());
						}
					}

				}
				catch (final IteratorException ex) {
					ex.printStackTrace();
				}
				i++;

			}


		}
		catch (final GeoAlgorithmExecutionException e) {
			e.printStackTrace();
			return false;
		}

		return !m_Task.isCanceled();
	}

}

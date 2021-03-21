/*******************************************************************************
 OrderPointsAlgorithm.java
  Autor: Fco. Jos� Pe�arrubia (fjp@scolab.es)
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

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.quadtree.Quadtree;

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

public class OrderPointsAlgorithm
extends
GeoAlgorithm {
	GeometryFactory geomFact = new GeometryFactory();

	private class PointAlong {
		Coordinate closestPoint;

		double     acumulatedDist;

		IFeature   featLine;


		public IFeature getFeatLine() {
			return featLine;
		}


		public void setFeatLine(final IFeature featLine) {
			this.featLine = featLine;
		}


		public PointAlong(final Coordinate c,
				final double d) {
			this.closestPoint = c;
			this.acumulatedDist = d;
		}

	}

	private class MyPoint
	implements
	Comparable {
		IFeature originalFeat;

		Integer  idLine;

		int      order;

		double   distToLine;

		double   distAlong;

		Point    pOnLine;


		public MyPoint(final IFeature originalFeat,
				final Integer idLine,
				final Point pOnLine,
				final double acumulatedDist) {
			this.idLine = idLine;
			this.originalFeat = originalFeat;
			this.pOnLine = pOnLine;
			this.distAlong = acumulatedDist;
			final Point originalPoint = (Point) originalFeat.getGeometry();
			final Coordinate c1 = new Coordinate(originalPoint.getX(), originalPoint.getY());
			final Coordinate c2 = new Coordinate(pOnLine.getX(), pOnLine.getY());
			distToLine = c1.distance(c2);
		}


		@Override
		public int compareTo(final Object arg0) {
			final MyPoint aux = (MyPoint) arg0;
			if (idLine > aux.idLine) {
				return 1;
			}
			else {
				if (idLine == aux.idLine) // Asociados a la misma l�nea.
					// Miramos la distancia
				{
					if (distAlong > aux.distAlong) {
						return 1;
					}
					else if (distAlong == aux.distAlong) {
						return 0;
					}
					else {
						return -1;
					}
				}
				else {
					// idLine menor
					return -1;
				}
			}
			// return -1; // No se deber�a llegar aqu�.
		}
	}


	/**
	 * Closest ID to this point. -1 if out from tolerance.
	 * 
	 * @param x
	 * @param y
	 * @param tolerance
	 * @param nearest .
	 *                Point to receive the nearest point ON arc.
	 * @return
	 */
	public PointAlong findClosestLine(final Quadtree tree,
			final double x,
			final double y,
			final double tolerance) {
		final Coordinate p = new Coordinate(x, y);
		final Envelope env = new Envelope(p);
		env.expandBy(tolerance);
		final List<IFeature> feats = tree.query(env);
		double minDist = tolerance;
		PointAlong resul = null;
		for (int i = 0; i < feats.size(); i++) {
			final IFeature feat = feats.get(i);
			final Geometry geom = feat.getGeometry();
			final PointAlong pAlong = getNearestPoint(p, geom, tolerance);

			if (pAlong != null) {
				final Coordinate nearest = pAlong.closestPoint;
				final double dist = nearest.distance(p);
				if (dist < minDist) {
					minDist = dist;
					resul = pAlong;
					resul.setFeatLine(feat);
				}
			}
		}
		return resul;

	}


	protected PointAlong getNearestPoint(final Coordinate c,
			final Geometry geom,
			final double tolerance) {
		PointAlong resul = null;

		Coordinate[] coords = geom.getCoordinates();
		double minDist = tolerance;
		Coordinate from = null, first = null;

		double longAcumulada = 0;
		double longReal = 0;
		Coordinate cOrig = null;
		Coordinate closestPoint = null;
		Coordinate closestP = null;

		for (int ig = 0; ig < geom.getNumGeometries(); ig++) {
			final Geometry geomN = geom.getGeometryN(ig);
			coords = geomN.getCoordinates();

			for (int i = 0; i < coords.length; i++) {
				// while not done
				if (i == 0) {
					from = coords[0];
					first = from;
					cOrig = from;
				}
				else {
					// System.out.println("SEG_LINETO");
					final Coordinate to = coords[i];
					final LineSegment line = new LineSegment(from, to);
					closestPoint = line.closestPoint(c);
					final double dist = c.distance(closestPoint);
					if ((dist < minDist)) {
						closestP = closestPoint;
						minDist = dist;
						longAcumulada = longReal;
						cOrig = from;
					}
					longReal += line.getLength();
					from = to;
				}
			}
			if (closestP == null) {
				continue;
			}
			final double dist = cOrig.distance(closestP);
			final double longBuscada = longAcumulada + dist;
			resul = new PointAlong(closestP, longBuscada);
		}
		return resul;
	}


	@Override
	public void defineCharacteristics() {

		setGroup(Sextante.getText("Tools_for_line_layers")); //$NON-NLS-1$
		this.setName(Sextante.getText("Assign_points_to_route")); //$NON-NLS-1$
		try {
			m_Parameters.addInputVectorLayer("LINE_LAYER", Sextante.getText("line_layer"), //$NON-NLS-1$
					AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);

			m_Parameters.addTableField("LINE_ID", Sextante.getText("line_id"), "LINE_LAYER"); //$NON-NLS-1$ //$NON-NLS-2$

			m_Parameters.addInputVectorLayer("POINT_LAYER", Sextante.getText("points_to_order"), //$NON-NLS-1$
					AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);

			m_Parameters.addNumericalValue("TOLERANCE", Sextante.getText("tolerance_to_search"), 100.0, //$NON-NLS-1$
					AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

			m_Parameters.addNumericalValue("STEP", Sextante.getText("step_to_new_order"), 5, //$NON-NLS-1$
					AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);

			m_Parameters.addBoolean("BOOL_SNAP", Sextante.getText("snap_points_to_line"), true); //$NON-NLS-1$

			addOutputVectorLayer("RESULT", Sextante.getText("ordered_points")); //$NON-NLS-1$
		}
		catch (final RepeatedParameterNameException e) {
			e.printStackTrace();
		}
		catch (final UndefinedParentParameterNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final OptionalParentParameterException e) {
			// TODO Auto-generated catch block
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
		// Indexamos la capa de l�neas y recorremos uno por uno
		// todos los puntos. Seleccionamos las l�neas dentro de la tolerancia
		// buscamos la m�s cercana y asociamos la distancia a la l�na m�s
		// cercan.
		// luego reordenamos los puntos en funci�n de esa l�nea y su distancia.
		// Es decir, cada punto tendr� un idLineaCercana y un Order, definido
		// dentro
		// de esa l�nea.

		final int i;

		// TODO: Probar con un TreeSet, seguramente la ordenaci�n es mucho m�s
		// r�pida.
		// ArrayList arrayMyPoints = new ArrayList();
		final TreeSet arrayMyPoints = new TreeSet();
		try {
			final IVectorLayer layerPoints = m_Parameters.getParameterValueAsVectorLayer("POINT_LAYER"); //$NON-NLS-1$
			final IVectorLayer layerLines = m_Parameters.getParameterValueAsVectorLayer("LINE_LAYER"); //$NON-NLS-1$
			// layerLines.createSpatialIndex();
			final Quadtree tree = createSpatialIndex(layerLines);

			final int idFieldId = m_Parameters.getParameterValueAsInt("LINE_ID"); //$NON-NLS-1$

			final String fieldLinesID = layerLines.getFieldName(idFieldId);

			final int step = m_Parameters.getParameterValueAsInt("STEP"); //$NON-NLS-1$
			final double tol = m_Parameters.getParameterValueAsDouble("TOLERANCE"); //$NON-NLS-1$
			final boolean bDoSnap = m_Parameters.getParameterValueAsBoolean("BOOL_SNAP"); //$NON-NLS-1$

			final int numOriginalFields = layerPoints.getFieldCount();
			final Class types[] = new Class[numOriginalFields + 2];
			final String fieldNames[] = new String[numOriginalFields + 2];
			for (int iField = 0; iField < numOriginalFields; iField++) {
				types[iField] = layerPoints.getFieldType(iField);
				final String name = layerPoints.getFieldName(iField);
				if (name.equalsIgnoreCase("newOrder")) {
					throw new RuntimeException(Sextante.getText("new_order_exists")); //$NON-NLS-1$
				}
				if (name.equalsIgnoreCase(fieldLinesID)) {
					throw new RuntimeException(Sextante.getText("field_already_exists") + fieldLinesID //$NON-NLS-1$
							+ Sextante.getText("in_points_layer_change_name")); //$NON-NLS-1$
				}
				fieldNames[iField] = name;
				types[numOriginalFields] = layerLines.getFieldType(idFieldId);
			}
			types[numOriginalFields + 1] = Integer.class;
			fieldNames[numOriginalFields] = fieldLinesID;
			fieldNames[numOriginalFields + 1] = "newOrder"; //$NON-NLS-1$

			final IVectorLayer result = getNewVectorLayer("RESULT", Sextante //$NON-NLS-1$
					.getText("ORDERED"), IVectorLayer.SHAPE_TYPE_POINT, //$NON-NLS-1$
					types, fieldNames);
			final int iShapeCount = layerPoints.getShapesCount();
			final int iTotalNumberOfSteps = iShapeCount;
			final IFeatureIterator iter = layerPoints.iterator();
			int j = 0;
			while (iter.hasNext() && setProgress(j, iShapeCount)) {
				final IFeature featOrig = iter.next();
				final Object[] values = featOrig.getRecord().getValues();
				final Object[] outputValues = new Object[values.length];
				m_Task.setProgress(j, iTotalNumberOfSteps);
				final Coordinate pOriginal = featOrig.getGeometry().getCoordinate();
				final PointAlong pAlong = findClosestLine(tree, pOriginal.x, pOriginal.y, tol);
				if (pAlong != null) {
					final Object id = pAlong.getFeatLine().getRecord().getValue(idFieldId);
					Integer idLinea = null;
					if (id instanceof Double) {
						final Double d = (Double) id;
						idLinea = d.intValue();
					}
					if (id instanceof Integer) {
						idLinea = (Integer) id;
					}
					final Point snappedPoint = geomFact.createPoint(pAlong.closestPoint);
					final MyPoint myP = new MyPoint(featOrig, idLinea, snappedPoint, pAlong.acumulatedDist);
					arrayMyPoints.add(myP);
				}
				else {
					System.out.println(Sextante.getText("The_point") + j //$NON-NLS-1$
							+ Sextante.getText("is_out_of_tolerance")); //$NON-NLS-1$
				}
				j++;
			}

			// Collections.sort(arrayMyPoints);
			final Iterator it = arrayMyPoints.iterator();
			int order = 0;
			final Object[] row = new Object[numOriginalFields + 2];
			Integer ant = null;
			m_Task.setProgressText(Sextante.getText("Generating_points_layer")); //$NON-NLS-1$
			// for (i = 0; i < arrayMyPoints.size(); i++) {
			// MyPoint p = (MyPoint) arrayMyPoints.get(i);
			while (it.hasNext()) {
				final MyPoint p = (MyPoint) it.next();
				if (p.idLine != ant) {
					order = 0;
				}
				final Object[] att = p.originalFeat.getRecord().getValues();
				for (int iField = 0; iField < att.length; iField++) {
					row[iField] = att[iField];
				}
				row[numOriginalFields] = p.idLine;
				row[numOriginalFields + 1] = new Integer(order);
				// 2 opciones: Puedes guardar el punto sobre la linea, o el
				// punto en su
				// ubicaci�n original,
				if (bDoSnap) {
					result.addFeature(p.pOnLine, row);
				}
				else {
					result.addFeature(p.originalFeat.getGeometry(), row);
				}
				order = order + step;
				ant = p.idLine;
			}
		}
		catch (final GeoAlgorithmExecutionException e) {
			e.printStackTrace();
			return false;
		}

		return !m_Task.isCanceled();
	}


	public Quadtree createSpatialIndex(final IVectorLayer layer) throws IteratorException {
		final int iShapeCount = layer.getShapesCount();
		final Quadtree tree = new Quadtree();
		final IFeatureIterator iter = layer.iterator();
		int i = 0;
		while (iter.hasNext() && setProgress(i, iShapeCount)) {
			final IFeature feat = iter.next();
			tree.insert(feat.getGeometry().getEnvelopeInternal(), feat);
			i++;
		}
		return tree;
	}

}

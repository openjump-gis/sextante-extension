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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
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

/**
 * @author Francisco Jose Penarrubia (fjp@scolab.es) Algorithm to link points to lines. Each point from the point input layer will
 *         be connected to the closest line in line input layer. If a field named "Length" is found in line layer, it will be
 *         recalculated The algorithm will output a new line layer with lines from point to closest line. The algorithm may be
 *         useful in water networks to join watermeters to pipes. If you need to split lines after, you can intersect this layer
 *         with the original layer
 */
public class LinkPointsToLinesAlgorithm
extends
GeoAlgorithm {
	GeometryFactory geomFact    = new GeometryFactory();
	private int     fieldLength = -1;

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
		IFeature   originalFeat;

		Integer    idLine;

		int        order;

		double     distToLine;

		double     distAlong;

		PointAlong pOnLine;


		public MyPoint(final IFeature originalFeat,
				final Integer idLine,
				final PointAlong pOnLine) {
			this.idLine = idLine;
			this.originalFeat = originalFeat;
			this.pOnLine = pOnLine;
			this.distAlong = pOnLine.acumulatedDist;
			final Point originalPoint = (Point) originalFeat.getGeometry();
			final Coordinate c1 = new Coordinate(originalPoint.getX(), originalPoint.getY());
			distToLine = c1.distance(pOnLine.closestPoint);
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


	/* (non-Javadoc)
	 * @see es.unex.sextante.core.GeoAlgorithm#defineCharacteristics()
	 */
	@Override
	public void defineCharacteristics() {

		setGroup(Sextante.getText("Tools_for_line_layers")); //$NON-NLS-1$
		this.setName(Sextante.getText("Link_points_to_lines")); //$NON-NLS-1$
		try {
			m_Parameters.addInputVectorLayer("LINE_LAYER", Sextante.getText("origina_line_layer"), //$NON-NLS-1$ //$NON-NLS-2$
					AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
			m_Parameters.addTableField("LINE_ID", Sextante.getText("line_id"), "LINE_LAYER"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


			m_Parameters.addInputVectorLayer("POINT_LAYER", Sextante.getText("points_to_split"), //$NON-NLS-1$ //$NON-NLS-2$
					AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);

			m_Parameters.addNumericalValue("TOLERANCE", Sextante.getText("tolerance_to_search"), 100.0, //$NON-NLS-1$ //$NON-NLS-2$
					AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

			addOutputVectorLayer("RESULT", Sextante.getText("new_line_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE); //$NON-NLS-1$
			addOutputVectorLayer("RESULT2", Sextante.getText("splitted_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE); //$NON-NLS-1$

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

			final double tol = m_Parameters.getParameterValueAsDouble("TOLERANCE"); //$NON-NLS-1$

			final int numOriginalFields = layerPoints.getFieldCount();
			final Class types[] = new Class[numOriginalFields + 1];
			final String fieldNames[] = new String[numOriginalFields + 1];
			for (int iField = 0; iField < numOriginalFields; iField++) {
				types[iField] = layerPoints.getFieldType(iField);
				final String name = layerPoints.getFieldName(iField);
				fieldNames[iField] = name;
				types[numOriginalFields] = layerLines.getFieldType(idFieldId);
			}
			types[numOriginalFields] = Integer.class;
			fieldNames[numOriginalFields] = fieldLinesID;

			final IVectorLayer result = getNewVectorLayer("RESULT", Sextante.getText("NEW_LINES"), IVectorLayer.SHAPE_TYPE_LINE, //$NON-NLS-1$
					types, fieldNames);
			final IVectorLayer result2 = getNewVectorLayer(
					"RESULT2", Sextante.getText("splitted_lines"), IVectorLayer.SHAPE_TYPE_LINE, //$NON-NLS-1$
					layerLines.getFieldTypes(), layerLines.getFieldNames());

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

					final MyPoint myP = new MyPoint(featOrig, idLinea, pAlong);
					arrayMyPoints.add(myP);
				}
				else {
					System.out.println(Sextante.getText("the_point") + j //$NON-NLS-1$
							+ Sextante.getText("is_out_of_tolerance")); //$NON-NLS-1$
				}
				j++;
			}

			// Collections.sort(arrayMyPoints);
			final Iterator<MyPoint> it = arrayMyPoints.iterator();
			final Object[] row = new Object[numOriginalFields + 1];
			Integer ant = null;
			int order = 0;
			m_Task.setProgressText(Sextante.getText("generating_union_segments")); //$NON-NLS-1$
			// for (i = 0; i < arrayMyPoints.size(); i++) {
			// MyPoint p = (MyPoint) arrayMyPoints.get(i);
			ArrayList<MyPoint> coordsSplit = new ArrayList<MyPoint>();
			final Hashtable<Integer, ArrayList<MyPoint>> featToSplit = new Hashtable<Integer, ArrayList<MyPoint>>();
			while (it.hasNext()) {
				final MyPoint p = it.next();
				if ((ant != null) && (p.idLine.compareTo(ant) != 0)) {
					if (coordsSplit.size() > 0) { // no es el primer segmento
						featToSplit.put(ant, coordsSplit);
						//						addSplittedFeatures(coordsSplit, result2);
					}
					order = 0;
					coordsSplit = new ArrayList<MyPoint>();
				}

				final Object[] att = p.originalFeat.getRecord().getValues();
				for (int iField = 0; iField < att.length; iField++) {
					row[iField] = att[iField];
				}
				row[numOriginalFields] = p.idLine;
				final Coordinate[] coords = new Coordinate[2];
				coords[0] = p.originalFeat.getGeometry().getCoordinates()[0];
				coords[1] = p.pOnLine.closestPoint;
				final LineString line = geomFact.createLineString(coords);
				result.addFeature(line, row);
				coordsSplit.add(p);
				order = order + 1;
				ant = p.idLine.intValue();
			}
			featToSplit.put(ant, coordsSplit);
			//			addSplittedFeatures(coordsSplit, result2);
			final IFeatureIterator iterLin = layerLines.iterator();
			j = 0;

			fieldLength = layerLines.getFieldIndexByName("LENGTH"); //$NON-NLS-1$


			m_Task.setProgressText(Sextante.getText("generating_splitted_lines")); //$NON-NLS-1$
			while (iterLin.hasNext() && setProgress(j, iShapeCount)) {
				final IFeature featLine = iterLin.next();
				final Object id = featLine.getRecord().getValue(idFieldId);
				Integer idLinea = null;
				if (id instanceof Double) {
					final Double d = (Double) id;
					idLinea = d.intValue();
				}
				if (id instanceof Integer) {
					idLinea = (Integer) id;
				}
				if (featToSplit.containsKey(idLinea)) {
					coordsSplit = featToSplit.get(idLinea);
					addSplittedFeatures(coordsSplit, result2);
				}
				else {
					result2.addFeature(featLine);
				}
				m_Task.setProgress(j, iTotalNumberOfSteps);
				j++;
			}


		}
		catch (final GeoAlgorithmExecutionException e) {
			e.printStackTrace();
			return false;
		}

		return !m_Task.isCanceled();
	}


	/**
	 * Recibimos una lista con los puntos a situar sobre una misma feature (ordenados). Vamos a recorrer la gemetr�a y cuando
	 * encontremos un punto entre 2, sacamos un registro para result2
	 * 
	 * @param coordsSplit
	 * @param result2
	 */
	private void addSplittedFeatures(final ArrayList<MyPoint> coordsSplit,
			final IVectorLayer result2) {
		final MyPoint first = coordsSplit.get(0);
		final Geometry g = first.pOnLine.featLine.getGeometry();
		final Object[] atts = first.pOnLine.featLine.getRecord().getValues();
		LineString part = null;
		int splitIndex = 0;
		for (int i = 0; i < g.getNumGeometries(); i++) {
			final Geometry aux = g.getGeometryN(i);
			final Coordinate[] coords = aux.getCoordinates();
			CoordinateList coordsList = new CoordinateList();
			double distAlong = 0;
			Coordinate from = null;
			Coordinate firstC = null;
			for (int j = 0; j < coords.length; j++) {
				if (j == 0) {
					from = coords[0];
					firstC = from;
					coordsList.add(from);
				}
				else {
					// System.out.println("SEG_LINETO");
					final Coordinate to = coords[j];
					final LineSegment line = new LineSegment(from, to);
					distAlong += line.getLength();
					if (splitIndex >= coordsSplit.size()) { // Si ya hemos tratado todos los closestPoint, no quedan m�s y metemos el resto de coordinates
						coordsList.add(to);
						continue;
					}
					if (distAlong < coordsSplit.get(splitIndex).distAlong) {
						coordsList.add(to);
					}
					else // hay uno o varios closestPoint entre estas dos coordenadas
					{
						while (coordsSplit.get(splitIndex).pOnLine.acumulatedDist < distAlong) {
							if (coordsSplit.get(splitIndex).pOnLine.closestPoint != from) {
								coordsList.add(coordsSplit.get(splitIndex).pOnLine.closestPoint);
								part = geomFact.createLineString(coordsList.toCoordinateArray());
								if (fieldLength != -1) {
									atts[fieldLength] = part.getLength();
								}
								result2.addFeature(part, atts);

								coordsList = new CoordinateList();
								coordsList.add(coordsSplit.get(splitIndex).pOnLine.closestPoint);
							}
							splitIndex++;
							if (splitIndex >= coordsSplit.size()) {
								coordsList.add(to);
								break;
							}
						} // while
					}
					from = to;
				}
			} // for j
			if (coordsList.size() > 1) {
				part = geomFact.createLineString(coordsList.toCoordinateArray());
				if (fieldLength != -1) {
					atts[fieldLength] = part.getLength();
				}
				result2.addFeature(part, atts);
			}
		} // for i
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

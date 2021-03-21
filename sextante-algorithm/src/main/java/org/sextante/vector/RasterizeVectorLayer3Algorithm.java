

package org.sextante.vector;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.rasterWrappers.GridCell;

/**
 * 
 * @author giuseppe aruta (August 2020)
 * modified from RasterizeVectorLayerAlgorithm plugin from Sextante
 * to solve some bugs (internal polygons with the save attrib value are
 * considered as nodata area)
 *
 */

public class RasterizeVectorLayer3Algorithm extends GeoAlgorithm
{
	private double NO_DATA;
	public static final String LAYER = "LAYER";
	public static final String FIELD = "FIELD";
	public static final String RESULT = "RESULT";
	private int m_iField;
	private int m_iNX;
	private int m_iNY;
	private IVectorLayer m_Layer;
	private IRasterLayer m_Result;
	private AnalysisExtent m_Extent;
	private double minValue;

	@Override
	public void defineCharacteristics() {
		this.setName(Sextante.getText("Rasterize_vector_layer")+ " (3)");
		this.setGroup(Sextante.getText("Rasterization_and_interpolation"));
		this.setUserCanDefineAnalysisExtent(true);
		try {
			this.m_Parameters.addInputVectorLayer("LAYER", Sextante.getText("Vector_layer"), -1, true);
			this.m_Parameters.addTableField("FIELD", Sextante.getText("Field"), "LAYER");
			this.addOutputRasterLayer("RESULT", Sextante.getText("Result"));
		}
		catch (UndefinedParentParameterNameException e) {
			Sextante.addErrorToLog(e);
		}
		catch (OptionalParentParameterException e2) {
			Sextante.addErrorToLog(e2);
		}
		catch (RepeatedParameterNameException e3) {
			Sextante.addErrorToLog(e3);
		}
	}

	@Override
	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
		int iShape = 1;
		this.NO_DATA = this.m_OutputFactory.getDefaultNoDataValue();
		this.m_Layer = this.m_Parameters.getParameterValueAsVectorLayer("LAYER");
		this.m_iField = this.m_Parameters.getParameterValueAsInt("FIELD");
		(this.m_Result = this.getNewRasterLayer("RESULT", String.valueOf(this.m_Layer.getName()) + Sextante.getText("[rasterizedo]"), 5)).setNoDataValue(this.NO_DATA);

		this.m_Extent = this.m_Result.getWindowGridExtent();
		this.m_iNX = this.m_Extent.getNX();
		this.m_iNY = this.m_Extent.getNY();
		final Coordinate[] coords = { new Coordinate(this.m_Extent.getXMin(), this.m_Extent.getYMin()), new Coordinate(this.m_Extent.getXMin(), this.m_Extent.getYMax()), new Coordinate(this.m_Extent.getXMax(), this.m_Extent.getYMax()), new Coordinate(this.m_Extent.getXMax(), this.m_Extent.getYMin()), new Coordinate(this.m_Extent.getXMin(), this.m_Extent.getYMin()) };
		final GeometryFactory gf = new GeometryFactory();
		final LinearRing ring = gf.createLinearRing(coords);
		final Polygon extent = gf.createPolygon(ring, (LinearRing[])null);
		int i = 0;
		IRecord record;
		double dValue;
		final SimpleStats stats = new SimpleStats();
		final IFeatureIterator iterB = this.m_Layer.iterator();
		final int iTotal = this.m_Layer.getShapesCount();
		while (iterB.hasNext() && setProgress(i, iTotal)) {
			final IFeature feature = iterB.next();
			try {
				dValue = Double.parseDouble(feature.getRecord().getValue(this.m_iField).toString());
				stats.addValue(dValue);
			}
			catch (final Exception e) {
			}
			i++;
		}
		iterB.close();

		minValue = stats.getMin();
		this.m_Result.assign(minValue);
		final IFeatureIterator iterA = this.m_Layer.iterator();

		while (iterA.hasNext() && setProgress(i, iTotal)) {
			final IFeature featureA = iterA.next();

			record = featureA.getRecord();
			try {
				dValue = Double.parseDouble(record.getValue(this.m_iField).toString());
				++iShape;
			}
			catch (Exception e) {
				dValue = iShape;
				++iShape;
			}
			final Geometry geom = featureA.getGeometry();
			for(int g=0; g<geom.getNumGeometries(); g++){

				if (geom.intersects(extent)) {
					if(geom.getGeometryN(g).getGeometryType().equals("Polygon") || geom.getGeometryN(g).getGeometryType().equals("MultiPolygon")  ) {
						Polygon polygon = (Polygon) geom.getGeometryN(g);
						doPolygon(polygon, dValue);

					} else   if(geom.getGeometryN(g).getGeometryType().equals("LineString") || geom.getGeometryN(g).getGeometryType().equals("MultiLineString") ){

						LineString lineString = (LineString) geom.getGeometryN(g);
						doLine(lineString, dValue);
					} else   if(geom.getGeometryN(g).getGeometryType().equals("Point") || geom.getGeometryN(g).getGeometryType().equals("MultiPoint") ){

						Point point = (Point) geom.getGeometryN(g);
						doPoint(point, dValue);
					}



				}
			}
			i++;
		}
		iterA.close();


		return !this.m_Task.isCanceled();
	}









	private void doPolygon(final Geometry geom, final double dValue) {
		final GeometryFactory gf = new GeometryFactory();
		for (int i = 0; i < geom.getNumGeometries(); ++i) {
			final Polygon poly = (Polygon)geom.getGeometryN(i);
			LinearRing lr = gf.createLinearRing(poly.getExteriorRing().getCoordinates());
			Polygon part = gf.createPolygon(lr, (LinearRing[])null);
			PreparedGeometry targetPrep
			= PreparedGeometryFactory.prepare(part);
			this.doPolygonPart(targetPrep, dValue, false);
			for (int j = 0; j < poly.getNumInteriorRing(); ++j) {
				lr = gf.createLinearRing(poly.getInteriorRingN(j).getCoordinates());
				part = gf.createPolygon(lr, (LinearRing[])null);
				this.doPolygonPart(targetPrep, dValue, false);
			}
		}
	}

	private void doPolygonPart(final PreparedGeometry geom, final double dValue, final boolean bIsHole) {
		final Coordinate p = new Coordinate();
		final boolean[] bCrossing = new boolean[this.m_iNX];
		final Envelope extent = geom.getGeometry().getEnvelopeInternal();
		int xStart = (int)((extent.getMinX() - this.m_Extent.getXMin()) / this.m_Extent.getCellSize()) - 1;
		if (xStart < 0) {
			xStart = 0;
		}
		int xStop = (int)((extent.getMaxX() - this.m_Extent.getXMin()) / this.m_Extent.getCellSize()) + 1;
		if (xStop >= this.m_iNX) {
			xStop = this.m_iNX - 1;
		}
		final Coordinate[] points = geom.getGeometry().getCoordinates();
		int y = 0;
		for (double yPos = this.m_Extent.getYMax(); y < this.m_iNY; ++y, yPos -= this.m_Extent.getCellSize()) {
			if (yPos >= extent.getMinY() && yPos <= extent.getMaxY()) {
				Arrays.fill(bCrossing, false);
				final Coordinate pLeft = new Coordinate(this.m_Extent.getXMin() - 1.0, yPos);
				final Coordinate pRight = new Coordinate(this.m_Extent.getXMax() + 1.0, yPos);
				Coordinate pb = points[points.length - 1];
				for (int iPoint = 0; iPoint < points.length; ++iPoint) {
					final Coordinate pa = pb;
					pb = points[iPoint];
					if ((pa.y <= yPos && yPos < pb.y) || (pa.y > yPos && yPos >= pb.y)) {
						this.getCrossing(p, pa, pb, pLeft, pRight);
						int ix = (int)((p.x - this.m_Extent.getXMin()) / this.m_Extent.getCellSize() + 1.0);
						if (ix < 0) {
							ix = 0;
						}
						else if (ix >= this.m_iNX) {
							ix = this.m_iNX - 1;
						}
						bCrossing[ix] = !bCrossing[ix];
					}
				}
				int x = xStart;
				boolean bFill = false;
				while (x <= xStop) {
					if (bCrossing[x]) {
						bFill = !bFill;
					}
					if (bFill) {
						final double dPrevValue = this.m_Result.getCellValueAsDouble(x, y);
						if (bIsHole) {
							//	if (dPrevValue == dValue) {
							this.m_Result.setNoData(x, y);
							//	}
						}
						else if (dPrevValue == minValue) {
							this.m_Result.setCellValue(x, y, dValue);
						}
					}
					++x;
				}
			}
		}

	}

	private void doLine(final Geometry geom, final double dValue) {
		for (int i = 0; i < geom.getNumGeometries(); ++i) {
			final PreparedGeometry part = PreparedGeometryFactory.prepare(geom.getGeometryN(i));
			this.doLineString(part, dValue);
		}
	}

	private void doLineString(final PreparedGeometry geom, final double dValue) {
		final Coordinate[] coords = geom.getGeometry().getCoordinates();
		for (int i = 0; i < coords.length - 1; ++i) {
			final double x = coords[i].x;
			final double y = coords[i].y;
			final double x2 = coords[i + 1].x;
			final double y2 = coords[i + 1].y;
			this.writeSegment(x, y, x2, y2, dValue);
		}
	}

	private void writeSegment(double x, double y, final double x2, final double y2, final double dValue) {
		double dx = Math.abs(x2 - x);
		double dy = Math.abs(y2 - y);
		if (dx > 0.0 || dy > 0.0) {
			double n;
			if (dx > dy) {
				dx = (n = dx / this.m_Result.getWindowCellSize());
				dy /= dx;
				dx = this.m_Result.getWindowCellSize();
			}
			else {
				dy = (n = dy / this.m_Result.getWindowCellSize());
				dx /= dy;
				dy = this.m_Result.getWindowCellSize();
			}
			if (x2 < x) {
				dx = -dx;
			}
			if (y2 < y) {
				dy = -dy;
			}
			for (double d = 0.0; d <= n; ++d, x += dx, y += dy) {
				if (this.m_Extent.contains(x, y)) {
					final GridCell cell = this.m_Extent.getGridCoordsFromWorldCoords(x, y);
					this.m_Result.setCellValue(cell.getX(), cell.getY(), dValue);
				}
			}
		}
	}

	private void doPoint(final Geometry geometry, final double dValue) {
		final Coordinate coord = geometry.getCoordinate();
		final GridCell cell = this.m_Extent.getGridCoordsFromWorldCoords(coord.x, coord.y);
		this.m_Result.setCellValue(cell.getX(), cell.getY(), dValue);
	}

	private boolean getCrossing(final Coordinate crossing, final Coordinate a1, final Coordinate a2, final Coordinate b1, final Coordinate b2) {
		final double a_dx = a2.x - a1.x;
		final double a_dy = a2.y - a1.y;
		final double b_dx = b2.x - b1.x;
		final double b_dy = b2.y - b1.y;
		final double div;
		if ((div = a_dx * b_dy - b_dx * a_dy) != 0.0) {
			final double lambda = ((b1.x - a1.x) * b_dy - b_dx * (b1.y - a1.y)) / div;
			crossing.x = a1.x + lambda * a_dx;
			crossing.y = a1.y + lambda * a_dy;
			return true;
		}
		return false;
	}
}
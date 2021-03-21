package org.openjump.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;



public class InteriorPointFinder {
	private GeometryFactory factory = new GeometryFactory();

	public Coordinate findPoint(Geometry paramGeometry) {
		if (paramGeometry.isEmpty())
			return new Coordinate(0.0D, 0.0D); 
		if (paramGeometry.getDimension() == 0)
			return paramGeometry.getCoordinate(); 
		if (paramGeometry instanceof GeometryCollection) {
			if (paramGeometry.getDimension() == 2)
				return findPoint(widestGeometry(paramGeometry)); 
			return findPoint(paramGeometry.getGeometryN(0));
		} 
		Geometry geometry1 = envelopeMiddle(paramGeometry);
		if (geometry1 instanceof org.locationtech.jts.geom.Point)
			return geometry1.getCoordinate(); 
		Geometry geometry2 = geometry1.intersection(paramGeometry);
		Geometry geometry3 = widestGeometry(geometry2);
		return centre(geometry3.getEnvelopeInternal());
	}

	protected Geometry widestGeometry(Geometry paramGeometry) {
		if (!(paramGeometry instanceof GeometryCollection))
			return paramGeometry; 
		return widestGeometry((GeometryCollection)paramGeometry);
	}

	private Geometry widestGeometry(GeometryCollection paramGeometryCollection) {
		if (paramGeometryCollection.isEmpty())
			return paramGeometryCollection; 
		Geometry geometry = paramGeometryCollection.getGeometryN(0);
		for (byte b = 1; b < paramGeometryCollection.getNumGeometries(); b++) {
			if (paramGeometryCollection.getGeometryN(b).getEnvelopeInternal().getWidth() > geometry
					.getEnvelopeInternal().getWidth())
				geometry = paramGeometryCollection.getGeometryN(b); 
		} 
		return geometry;
	}

	protected Geometry envelopeMiddle(Geometry paramGeometry) {
		Envelope envelope = paramGeometry.getEnvelopeInternal();
		if (envelope.getWidth() == 0.0D)
			return this.factory.createPoint(centre(envelope)); 
		double avgEnvelopeY = (envelope.getMinY()+envelope.getMaxY())/2;
		return this.factory.createLineString(new Coordinate[] { new Coordinate(envelope
				.getMinX(), 
				avgEnvelopeY), new Coordinate(envelope
						.getMaxX(), 
						avgEnvelopeY) });
	}

	public Coordinate centre(Envelope envelope) {
		double avgEnvelopeX = (envelope.getMinX()+envelope.getMaxY())/2;
		double avgEnvelopeY = (envelope.getMinY()+envelope.getMaxY())/2;
		return new Coordinate(avgEnvelopeX, 
				avgEnvelopeY);
	}
}

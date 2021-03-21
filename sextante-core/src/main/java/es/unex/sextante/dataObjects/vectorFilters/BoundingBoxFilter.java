package es.unex.sextante.dataObjects.vectorFilters;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.IFeature;

public class BoundingBoxFilter
         implements
            IVectorLayerFilter {

   private final Geometry m_Rect;


   public BoundingBoxFilter(final Geometry rect) {

      m_Rect = rect;

   }


   public BoundingBoxFilter(final AnalysisExtent extent) {

      final GeometryFactory gf = new GeometryFactory();
      final Coordinate coords[] = new Coordinate[5];
      coords[0] = new Coordinate(extent.getXMin(), extent.getYMin());
      coords[1] = new Coordinate(extent.getXMin(), extent.getYMax());
      coords[2] = new Coordinate(extent.getXMax(), extent.getYMax());
      coords[3] = new Coordinate(extent.getXMax(), extent.getYMin());
      coords[4] = coords[0];
      m_Rect = gf.createPolygon(gf.createLinearRing(coords), null);

   }


   public Geometry getBoundingBox() {

      return m_Rect;

   }


   public boolean accept(final IFeature feature,
                         final int iIndex) {

      return (feature.getGeometry().intersects(m_Rect));

   }

}

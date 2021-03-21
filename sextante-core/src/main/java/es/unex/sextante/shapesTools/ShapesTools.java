package es.unex.sextante.shapesTools;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.outputs.IOutputChannel;

/**
 * Additional methods for working with vector layers
 * 
 * @author Victor Olaya
 * 
 */

public class ShapesTools {

   /**
    * Returns a new vector layer identical to a given one but with additional fields in its attributes table
    * 
    * @param outputFactory
    *                the output factory to use to create the new layer
    * @param layer
    *                the base layer
    * @param channel
    *                the output channel for the resulting layer
    * @param sFieldNames
    *                an array with field names of the new fields to add
    * @param addedValues
    *                the values to add
    * @param fieldTypes
    *                an array with field types of the fields to add
    * @return a new layer with added attributes
    * @throws UnsupportedOutputChannelException
    * @throws IteratorException
    */
   public static IVectorLayer addFields(final OutputFactory outputFactory,
                                        final IVectorLayer layer,
                                        final IOutputChannel channel,
                                        final String[] sFieldNames,
                                        final Object[][] addedValues,
                                        final Class fieldTypes[]) throws UnsupportedOutputChannelException {

      int i;
      final String sOrgFields[] = layer.getFieldNames();
      final String sFields[] = new String[sOrgFields.length + sFieldNames.length];
      final Object values[] = new Object[sOrgFields.length + sFieldNames.length];
      final Class[] orgTypes = layer.getFieldTypes();
      final Class types[] = new Class[sOrgFields.length + sFieldNames.length];
      for (i = 0; i < sOrgFields.length; i++) {
         sFields[i] = sOrgFields[i];
         types[i] = orgTypes[i];
      }
      for (i = 0; i < sFieldNames.length; i++) {
         sFields[i + sOrgFields.length] = sFieldNames[i];
         types[i + sOrgFields.length] = fieldTypes[i];
      }

      final IVectorLayer output = outputFactory.getNewVectorLayer(layer.getName(), layer.getShapeType(), types, sFields, channel,
               layer.getCRS());

      int j = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext()) {
         try {
            final IFeature feature = iter.next();
            final IRecord record = feature.getRecord();
            final Object[] orgValues = record.getValues();
            for (i = 0; i < orgValues.length; i++) {
               values[i] = orgValues[i];
            }
            for (i = 0; i < sFieldNames.length; i++) {
               values[i + orgValues.length] = addedValues[i][j];
            }
            output.addFeature(feature.getGeometry(), values);
         }
         catch (final Exception e) {
            e.printStackTrace();
         }
         j++;
      }
      iter.close();
      //layer.close();

      return output;

   }


   /**
    * Creates a circle
    * 
    * @param x
    *                the x coordinate of the center
    * @param y
    *                the y coordinate of the center
    * @param radius
    *                the radius of the circle
    * @return a circle-shaped polygon
    */
   public static Geometry createCircle(final double x,
                                       final double y,
                                       final double radius) {

      final int SIDES = 100;
      final Coordinate coords[] = new Coordinate[SIDES];
      for (int i = 0; i < SIDES; i++) {
         final double angle = ((double) i / (double) SIDES) * 2 * Math.PI;
         final double dx = Math.cos(angle) * radius;
         final double dy = Math.sin(angle) * radius;
         coords[i] = new Coordinate(x + dx, y + dy);
      }
      coords[coords.length - 1] = coords[0];

      final GeometryFactory factory = new GeometryFactory();
      final LinearRing ring = factory.createLinearRing(coords);
      final Polygon polygon = factory.createPolygon(ring, null);

      return polygon;

   }

}

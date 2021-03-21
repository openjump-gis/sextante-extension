package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoPoint;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a point
 * 
 * @author volaya
 * 
 */
public class ParameterPoint
         extends
            Parameter {

   @Override
   public String getParameterTypeName() {

      return "Point";

   }


   @Override
   public IRasterLayer getParameterValueAsRasterLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public I3DRasterLayer getParameterValueAs3DRasterLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public ITable getParameterValueAsTable() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public IVectorLayer getParameterValueAsVectorLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public int getParameterValueAsInt() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public double getParameterValueAsDouble() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean getParameterValueAsBoolean() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public String getParameterValueAsString() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Point2D) m_ParameterValue);
      }
      throw new NullParameterValueException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoPoint) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof Point2D) {
         m_ParameterValue = value;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public Class getParameterClass() {

      return Point2D.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

   }


   public static Parameter deserialize(final KXmlParser parser) {

      final ParameterPoint param = new ParameterPoint();

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final Point2D pt = (Point2D) m_ParameterValue;
      final String s = "\"" + Double.toString(pt.getX()) + "," + Double.toString(pt.getY()) + "\"";

      return s;

   }


   @Override
   public boolean isParameterValueCorrect() {

      return m_ParameterValue != null;

   }

}

package es.unex.sextante.parameters;

import java.awt.geom.Point2D;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a boolean value
 * 
 * @author volaya
 * 
 */
public class ParameterBoolean
         extends
            Parameter {

   @Override
   public String getParameterTypeName() {

      return "Boolean";

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
   public String getParameterValueAsString() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Boolean) m_ParameterValue).toString();
      }
      throw new NullParameterValueException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean getParameterValueAsBoolean() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Boolean) m_ParameterValue).booleanValue();
      }
      throw new NullParameterValueException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoBoolean) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof Boolean) {
         m_ParameterValue = value;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public Class getParameterClass() {

      return Boolean.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) {}


   public static Parameter deserialize(final KXmlParser parser) {

      final ParameterBoolean param = new ParameterBoolean();

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final Boolean b = (Boolean) m_ParameterValue;

      return "\"" + b.toString() + "\"";

   }


   @Override
   public boolean isParameterValueCorrect() {

      return true;

   }

}

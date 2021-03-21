package es.unex.sextante.parameters;

/**
 * A parameter representing a layer or a table
 * @author volaya
 *
 */
import java.awt.geom.Point2D;
import java.io.IOException;

import org.kxml2.io.KXmlSerializer;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

public abstract class ParameterDataObject
         extends
            Parameter {

   protected static final String MANDATORY = "mandatory";


   @Override
   public abstract String getParameterTypeName();


   @Override
   public abstract boolean setParameterValue(Object value);


   @Override
   public abstract boolean setParameterAdditionalInfo(AdditionalInfo additionalInfo);


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
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoDataObject aido = (AdditionalInfoDataObject) m_ParameterAdditionalInfo;
      if (aido != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, MANDATORY);
         serializer.attribute(null, VALUE, Boolean.valueOf(aido.getIsMandatory()).toString());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


}

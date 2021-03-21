

package es.unex.sextante.tridimensional.interpolation;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;


public class Interpolation3DAlgorithm
         extends
            GeoAlgorithm {


   private static final String RESULT = "RESULT";
   private static final String FIELD  = "FIELD";
   private static final String POINTS = "POINTS";
   private Object              m_Result;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("3D_Interpolation"));
      setGroup(Sextante.getText("3D"));

      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), IVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), POINTS);
         addOutput3DRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer pointsLayer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_Result = getNew3DRasterLayer(RESULT, pointsLayer.getName(), I3DRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      //TODO: implement interpolation algorithm

      return !m_Task.isCanceled();

   }


   @Override
   public boolean is3D() {

      return true;

   }


   @Override
   public boolean isActive() {

      return false;

   }

}

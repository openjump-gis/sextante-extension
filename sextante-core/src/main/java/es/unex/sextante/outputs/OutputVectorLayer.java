package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;

/**
 * An output representing a vector layer
 * 
 * @author volaya
 * 
 */
public class OutputVectorLayer
         extends
            Output {

   public static final int SHAPE_TYPE_POLYGON   = IVectorLayer.SHAPE_TYPE_POLYGON;
   public static final int SHAPE_TYPE_POINT     = IVectorLayer.SHAPE_TYPE_POINT;
   public static final int SHAPE_TYPE_LINE      = IVectorLayer.SHAPE_TYPE_LINE;
   public static final int SHAPE_TYPE_UNDEFINED = -1;

   private int             m_iShapeType;
   private String          m_sInputLayerToOverwrite;


   @Override
   public void setOutputObject(final Object obj) {

      if ((obj instanceof IVectorLayer) || (obj == null)) {
         m_Object = obj;
      }

   }


   @Override
   public String getCommandLineParameter() {

      if (m_OutputChannel == null) {
         return "\"#\"";
      }
      else {
         return "\"" + m_OutputChannel.getAsCommandLineParameter() + "\"";
      }

   }


   /**
    * Returns the shape type of this output vector layer
    * 
    * @return the shape type of this output vector layer
    */
   public int getShapeType() {

      return m_iShapeType;

   }


   /**
    * Sets the shape type of this output vector layer
    * 
    * @param shapeType
    *                The shape type of this output
    */
   public void setShapeType(final int shapeType) {

      m_iShapeType = shapeType;

   }


   @Override
   public Output getNewInstance() {

      final Output out = super.getNewInstance();
      ((OutputVectorLayer) out).setShapeType(m_iShapeType);
      ((OutputVectorLayer) out).setInputLayerToOverwrite(m_sInputLayerToOverwrite);

      return out;

   }


   @Override
   public void setObjectData(final Output output) {

      super.setObjectData(output);
      if (output instanceof OutputVectorLayer) {
         this.setShapeType(((OutputVectorLayer) output).getShapeType());
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("vector");

   }


   /**
    * Returns the name of the input parameter that this output can overwrite
    * 
    * @return the name of the input parameter that this output can overwrite
    */
   public String getInputLayerToOverwrite() {

      return m_sInputLayerToOverwrite;

   }


   /**
    * Sets the name of the input parameter that this output can overwrite. Returns null if it cannot overwrite.
    * 
    * @param inputLayerToOverwrite
    *                the name of the input parameter that this output can overwrite
    */
   public void setInputLayerToOverwrite(final String inputLayerToOverwrite) {

      m_sInputLayerToOverwrite = inputLayerToOverwrite;

   }


   public boolean canOverwrite() {

      return m_sInputLayerToOverwrite != null;

   }


}

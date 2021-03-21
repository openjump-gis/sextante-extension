package es.unex.sextante.modeler.elements;

import es.unex.sextante.dataObjects.IVectorLayer;

public class ModelElementVectorLayer
         implements
            IModelElement {

   public static final int SHAPE_TYPE_POLYGON   = IVectorLayer.SHAPE_TYPE_POLYGON;
   public static final int SHAPE_TYPE_POINT     = IVectorLayer.SHAPE_TYPE_POINT;
   public static final int SHAPE_TYPE_LINE      = IVectorLayer.SHAPE_TYPE_LINE;
   public static final int SHAPE_TYPE_UNDEFINED = -1;

   private int             m_iShapeType;


   public int getShapeType() {

      return m_iShapeType;

   }


   public void setShapeType(final int shapeType) {

      m_iShapeType = shapeType;

   }


   @Override
   public String toString() {

      return this.getClass().toString() + "," + Integer.toString(m_iShapeType);

   }


}

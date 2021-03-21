package es.unex.sextante.vectorTools.addEventTheme;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class AddEventThemeAlgorithm
         extends
            GeoAlgorithm {

   public static final String TABLE  = "TABLE";
   public static final String XFIELD = "XFIELD";
   public static final String RESULT = "RESULT";
   public static final String YFIELD = "YFIELD";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int j;
      int iFieldX, iFieldY;
      ITable table;
      String[] sFields;
      Class[] types;

      table = m_Parameters.getParameterValueAsTable(TABLE);
      iFieldX = m_Parameters.getParameterValueAsInt(XFIELD);
      iFieldY = m_Parameters.getParameterValueAsInt(YFIELD);

      sFields = new String[table.getFieldCount()];
      types = new Class[table.getFieldCount()];

      for (j = 0; j < table.getFieldCount(); j++) {
         sFields[j] = table.getFieldName(j);
         types[j] = table.getFieldType(j);
      }

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Layer_from_table"), IVectorLayer.SHAPE_TYPE_POINT,
               types, sFields);
      final IRecordsetIterator iter = table.iterator();
      final GeometryFactory geomFac = new GeometryFactory();
      while (iter.hasNext()) {
         try {
            final IRecord record = iter.next();
            final String sX = record.getValue(iFieldX).toString();
            final String sY = record.getValue(iFieldY).toString();
            final double dX = Double.parseDouble(sX);
            final double dY = Double.parseDouble(sY);
            final Point pt = geomFac.createPoint(new Coordinate(dX, dY));
            output.addFeature(pt, record.getValues());
         }
         catch (final NumberFormatException e) {}
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("Points_layer_from_table"));
      setGroup(Sextante.getText("Tools_for_point_layers"));

      try {
         m_Parameters.addInputTable(TABLE, Sextante.getText("Table"), true);
         m_Parameters.addTableField(XFIELD, Sextante.getText("X"), TABLE);
         m_Parameters.addTableField(YFIELD, Sextante.getText("Y"), TABLE);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT);
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

}

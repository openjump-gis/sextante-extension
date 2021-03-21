

package es.unex.sextante.tridimensional.line3DFromTable;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

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


public class Line3DFromTableAlgorithm
         extends
            GeoAlgorithm {

   public static final String TABLE  = "TABLE";
   public static final String XFIELD = "XFIELD";
   public static final String YFIELD = "YFIELD";
   public static final String ZFIELD = "ZFIELD";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final int j;
      int iFieldX, iFieldY, iFieldZ;
      ITable table;
      String[] sFields;
      Class[] types;

      table = m_Parameters.getParameterValueAsTable(TABLE);
      iFieldX = m_Parameters.getParameterValueAsInt(XFIELD);
      iFieldY = m_Parameters.getParameterValueAsInt(YFIELD);
      iFieldZ = m_Parameters.getParameterValueAsInt(ZFIELD);

      sFields = new String[] { "ID" };
      types = new Class[] { Integer.class };

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("3DLineFromTable"), IVectorLayer.SHAPE_TYPE_LINE,
               types, sFields);
      final ArrayList<Coordinate> coordsArray = new ArrayList<Coordinate>();
      final IRecordsetIterator iter = table.iterator();
      final GeometryFactory geomFac = new GeometryFactory();
      while (iter.hasNext()) {
         try {
            final IRecord record = iter.next();
            final String sX = record.getValue(iFieldX).toString();
            final String sY = record.getValue(iFieldY).toString();
            final String sZ = record.getValue(iFieldZ).toString();
            final double dX = Double.parseDouble(sX);
            final double dY = Double.parseDouble(sY);
            final double dZ = Double.parseDouble(sZ);
            coordsArray.add(new Coordinate(dX, dY, dZ));
         }
         catch (final NumberFormatException e) {
         }
      }
      iter.close();

      if (coordsArray.size() > 1) {
         final LineString line = geomFac.createLineString(coordsArray.toArray(new Coordinate[0]));
         output.addFeature(line, new Object[] { new Integer(1) });
      }
      else {
         throw new GeoAlgorithmExecutionException(Sextante.getText("LessThanTwoPointsInTable"));
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("3DLineFromTable"));
      setGroup(Sextante.getText("3D"));

      try {
         m_Parameters.addInputTable(TABLE, Sextante.getText("Table"), true);
         m_Parameters.addTableField(XFIELD, Sextante.getText("X"), TABLE);
         m_Parameters.addTableField(YFIELD, Sextante.getText("Y"), TABLE);
         m_Parameters.addTableField(ZFIELD, Sextante.getText("Z"), TABLE);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);
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
   public boolean isActive() {

      return false;

   }


}

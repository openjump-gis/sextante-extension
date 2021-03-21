

package es.unex.sextante.vectorTools.placePointsOnLines;

import java.util.ArrayList;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.FixedTableModel;


public class PlacePointsOnLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String                 RESULT = "RESULT";
   public static final String                 FIELD  = "FIELD";
   public static final String                 TABLE  = "TABLE";
   public static final String                 LINES  = "LINES";

   private IVectorLayer                       m_Output;
   private HashMap<String, ArrayList<Double>> m_Map;
   private int                                m_iField;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      m_Map = new HashMap<String, ArrayList<Double>>();

      IVectorLayer lines;

      try {
         m_iField = m_Parameters.getParameterValueAsInt(FIELD);
         lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
         final FixedTableModel table = (FixedTableModel) m_Parameters.getParameterValueAsObject(TABLE);
         for (int j = 0; j < table.getRowCount(); j++) {
            final String sName = table.getValueAt(j, 0).toString();
            final String sDistance = table.getValueAt(j, 1).toString();
            ArrayList<Double> distances = m_Map.get(sName);
            if (distances == null) {
               distances = new ArrayList<Double>();
               m_Map.put(sName, distances);
            }
            distances.add(Double.parseDouble(sDistance));
         }

         m_Output = getNewVectorLayer(RESULT, Sextante.getText("Points") + "(" + lines.getName() + ")",
                  IVectorLayer.SHAPE_TYPE_POINT, new Class[] { String.class }, new String[] { "ID" });

         i = 0;
         final int iShapeCount = lines.getShapesCount();
         final IFeatureIterator iter = lines.iterator();
         while (iter.hasNext() && setProgress(i, iShapeCount)) {
            final IFeature feature = iter.next();
            processLine(feature);
            i++;
         }
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }

      return !m_Task.isCanceled();

   }


   private void processLine(final IFeature feature) {

      final String sName = feature.getRecord().getValue(m_iField).toString();

      final ArrayList<Double> distances = m_Map.get(sName);

      if (distances != null) {
         for (int i = 0; i < distances.size(); i++) {
            addPoint(feature, distances.get(i));
         }
      }

   }


   private void addPoint(final IFeature feature,
                         final Double dist) {

      final GeometryFactory gf = new GeometryFactory();
      boolean bPointAdded = false;
      double dDist = 0;
      double dDistToNextPoint;

      final Coordinate[] coords = feature.getGeometry().getCoordinates();

      for (int i = 0; i < coords.length - 1; i++) {
         dDistToNextPoint = coords[i].distance(coords[i + 1]);
         if (dDist + dDistToNextPoint > dist.doubleValue()) {
            final double dDistToPoint = dist.doubleValue() - dDist;
            final double dRatio = dDistToPoint / dDistToNextPoint;
            final double dDifX = coords[i + 1].x - coords[i].x;
            final double dDifY = coords[i + 1].y - coords[i].y;
            final Coordinate coord = new Coordinate(coords[i].x + dDifX * dRatio, coords[i].y + dDifY * dRatio);
            m_Output.addFeature(gf.createPoint(coord), feature.getRecord().getValues());
            bPointAdded = true;
            break;
         }
         dDist += dDistToNextPoint;
      }

      if (!bPointAdded) {
         final String sName = feature.getRecord().getValue(m_iField).toString();
         Sextante.addWarningToLog(Sextante.getText("Could_not_add_point_distance_too_large") + ": " + sName + ","
                                  + dist.toString());
      }


   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Place_point_on_line_at_distance"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), LINES);
         m_Parameters.addFixedTable(TABLE, Sextante.getText("Table"), new String[] { Sextante.getText("Name"),
                  Sextante.getText("Distance") }, 1, false);
         addOutputVectorLayer(RESULT, Sextante.getText("Points"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }

}

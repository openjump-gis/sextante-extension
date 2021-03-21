package es.unex.sextante.statisticalMethods.regression;

import java.text.DecimalFormat;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.regression.Regression;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.rasterWrappers.GridCell;

public class RegressionAlgorithm
         extends
            GeoAlgorithm {

   public static final String METHOD          = "METHOD";
   public static final String RESIDUALS       = "RESIDUALS";
   public static final String RESULT          = "RESULT";
   public static final String FIELD           = "FIELD";
   public static final String POINTS          = "POINTS";
   public static final String RASTER          = "RASTER";
   public static final String REGRESSION_DATA = "REGRESSION_DATA";

   private int                m_iField;
   private IVectorLayer       m_Residuals;
   private IRasterLayer       m_Window;
   private IRasterLayer       m_Result;
   Regression                 m_Regression;
   private IVectorLayer       m_Points;


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { "y = a + b * x", "y = a + b / x", "y = a / (b - x)", "y = a * x^b", "y = a e^(b * x)",
               "y = a + b * ln(x)" };

      this.setName(Sextante.getText("Regression"));
      setGroup(Sextante.getText("Statistical_methods"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), "POINTS");
         m_Parameters.addInputRasterLayer(RASTER, Sextante.getText("Raster_layer"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Equation"), sMethod);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
         addOutputVectorLayer(RESIDUALS, Sextante.getText("Residuals"), OutputVectorLayer.SHAPE_TYPE_POINT);
         addOutputText(REGRESSION_DATA, Sextante.getText("Regression_values"));
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

      final String sFieldNames[] = { Sextante.getText("Real_value"), Sextante.getText("Predictor_value"),
               Sextante.getText("Estimated_value"), Sextante.getText("Diference"), Sextante.getText("Variance") };
      final Class types[] = { Double.class, Double.class, Double.class, Double.class, Double.class };

      m_iField = m_Parameters.getParameterValueAsInt(FIELD);
      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);

      if (m_Points.getShapesCount() == 0) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Invalid_input_data"));
      }

      m_Window = m_Parameters.getParameterValueAsRasterLayer(RASTER);

      m_Result = getNewRasterLayer(RESULT, Sextante.getText("Regression"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      final AnalysisExtent extent = m_Result.getWindowGridExtent();
      m_Window.setWindowExtent(extent);

      m_Residuals = getNewVectorLayer(RESIDUALS, Sextante.getText("Residuals"), IVectorLayer.SHAPE_TYPE_POINT, types, sFieldNames);

      calculateRegression();
      calculateResultingGrid();
      calculateResiduals();
      addRegressionInfo();

      return !m_Task.isCanceled();

   }


   private void addRegressionInfo() {

      final DecimalFormat df = new DecimalFormat("####.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Regression"));
      doc.addHeader(Sextante.getText("Regression"), 2);
      doc.startUnorderedList();
      doc.addListElement(m_Regression.getExpression());
      doc.addListElement("R2 = " + df.format(m_Regression.getR2()));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(REGRESSION_DATA, Sextante.getText("Regression_values"), doc.getHTMLCode());

   }


   private void calculateResiduals() throws IteratorException {

      double dValue;
      double dGridValue;
      final Object value[] = new Object[5];
      final AnalysisExtent extent = m_Result.getWindowGridExtent();
      GridCell cell;
      final double v = 100. / m_Regression.getYVar();

      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate pt = geom.getCoordinate();
         dValue = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
         cell = extent.getGridCoordsFromWorldCoords(pt.x, pt.y);
         dGridValue = m_Result.getCellValueAsDouble(cell.getX(), cell.getY());
         if (!m_Result.isNoDataValue(dGridValue)) {
            value[0] = new Double(dValue);
            value[1] = new Double(dValue = m_Window.getValueAt(pt.x, pt.y));
            value[2] = new Double(dValue = m_Regression.getY(dValue));
            value[3] = new Double(dValue = dValue - ((Double) value[0]).doubleValue());
            value[4] = new Double(dValue * v);
            m_Residuals.addFeature(geom, value);
         }
      }
      iter.close();

   }


   private void calculateResultingGrid() {

      int x, y;
      int iNX, iNY;
      double dValue;

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            dValue = m_Window.getCellValueAsDouble(x, y);
            if (m_Window.isNoDataValue(dValue)) {
               m_Result.setNoData(x, y);
            }
            else {
               m_Result.setCellValue(x, y, m_Regression.getY(dValue));
            }
         }
      }

   }


   private void calculateRegression() throws GeoAlgorithmExecutionException {

      double dValue, dGridValue;
      m_Regression = new Regression();
      int iType = 1;

      try {
         iType = m_Parameters.getParameterValueAsInt(METHOD) + 1;
      }
      catch (final Exception e) {}

      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate pt = geom.getCoordinate();
         dGridValue = m_Window.getValueAt(pt.x, pt.y);
         if (!m_Window.isNoDataValue(dGridValue)) {
            try {
               dValue = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
               m_Regression.addValue(dGridValue, dValue);
            }
            catch (final NumberFormatException e) {}
         }
      }
      iter.close();
      boolean bReturn = m_Regression.calculate(iType);
      if (!bReturn) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Could_not_calculate_regression"));
      }

   }

}

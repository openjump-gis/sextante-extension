package es.unex.sextante.statisticalMethods.multipleRegression;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
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
import es.unex.sextante.math.regression.MultipleRegression;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.rasterWrappers.GridCell;

public class MultipleRegressionAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT           = "INPUT";
   public static final String FIELD           = "FIELD";
   public static final String POINTS          = "POINTS";
   public static final String RESULT          = "RESULT";
   public static final String RESIDUALS       = "RESIDUALS";
   public static final String REGRESSION_INFO = "REGRESSION_INFO";

   private int                m_iField;
   private IVectorLayer       m_Residuals;
   private IRasterLayer       m_Windows[];
   private IRasterLayer       m_Result;
   private MultipleRegression m_Regression;
   private ArrayList          m_RasterLayers;
   private IVectorLayer       m_Points;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Statistical_methods"));
      this.setName(Sextante.getText("Multiple_regression"));

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), POINTS);
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Predictors"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, false);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
         addOutputVectorLayer(RESIDUALS, Sextante.getText("Residuals"), OutputVectorLayer.SHAPE_TYPE_POINT);
         addOutputText(REGRESSION_INFO, Sextante.getText("Regression_values"));
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

      int i;

      final String sFieldNames[] = { Sextante.getText("Real_value"), Sextante.getText("Estimated_value"),
               Sextante.getText("Diference") };
      final Class types[] = { Double.class, Double.class, Double.class };

      m_RasterLayers = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);
      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);

      if ((m_RasterLayers.size() == 0) || (m_Points.getShapesCount() < 3)) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Invalid_input_data"));
      }

      m_Result = getNewRasterLayer(RESULT, Sextante.getText("Multiple_regression"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      m_Windows = new IRasterLayer[m_RasterLayers.size()];

      for (i = 0; i < m_RasterLayers.size(); i++) {
         m_Windows[i] = (IRasterLayer) m_RasterLayers.get(i);
         m_Windows[i].setWindowExtent(m_Result.getWindowGridExtent());
         m_Windows[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);
      }

      m_Residuals = getNewVectorLayer(RESIDUALS, Sextante.getText("Residuals"), IVectorLayer.SHAPE_TYPE_POINT, types, sFieldNames);

      calculateRegression();
      if (!m_Task.isCanceled()) {
         calculateResultingGrid();
         calculateResiduals();
         createAdditionalInfo();
      }

      return !m_Task.isCanceled();

   }


   private void calculateResiduals() throws IteratorException {

      double dValue;
      double dGridValue;
      final Object value[] = new Object[3];
      final AnalysisExtent extent = m_Result.getWindowGridExtent();
      GridCell cell;

      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate pt = geom.getCoordinate();
         dValue = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
         cell = extent.getGridCoordsFromWorldCoords(pt.x, pt.y);
         dGridValue = m_Result.getCellValueAsDouble(cell.getX(), cell.getY());
         if (!m_Result.isNoDataValue(dGridValue)) {
            value[1] = new Double(dValue);
            value[0] = new Double(dGridValue);
            value[2] = new Double(dGridValue - dValue);
            m_Residuals.addFeature(geom, value);
         }
      }
      iter.close();

   }


   private void calculateResultingGrid() {

      int i;
      int x, y;
      int iNX, iNY;
      double z;
      double dValue;
      boolean bNoDataValue;

      iNX = m_Windows[0].getNX();
      iNY = m_Windows[0].getNY();

      setProgressText(Sextante.getText("Calculating_regression"));

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            bNoDataValue = false;
            z = m_Regression.getConstant();
            for (i = 0; i < m_Windows.length; i++) {
               dValue = m_Windows[i].getCellValueAsDouble(x, y);
               if (m_Windows[i].isNoDataValue(dValue)) {
                  m_Result.setNoData(x, y);
                  bNoDataValue = true;
                  break;
               }
               else {
                  z += dValue * m_Regression.getCoeff(i);
               }
            }
            if (!bNoDataValue) {
               m_Result.setCellValue(x, y, z);
            }
         }
      }

   }


   private void calculateRegression() throws GeoAlgorithmExecutionException {

      int i, j;
      int iCount;
      double dValuePt;
      final double dValueGrid[] = new double[m_Windows.length];
      boolean bNoDataValue;

      m_Regression = new MultipleRegression(m_Windows.length);

      i = 0;
      iCount = m_Points.getShapesCount();
      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate pt = geom.getCoordinate();
         bNoDataValue = false;
         for (j = 0; j < m_Windows.length; j++) {
            dValueGrid[j] = m_Windows[j].getValueAt(pt.x, pt.y);
            if (m_Windows[j].isNoDataValue(dValueGrid[j])) {
               bNoDataValue = true;
               break;
            }
         }
         if (!bNoDataValue) {
            try {
               dValuePt = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
               m_Regression.addValue(dValueGrid, dValuePt);
            }
            catch (final NumberFormatException e) {}
         }
      }
      iter.close();

      if (m_Task.isCanceled()) {
         return;
      }
      else {
         boolean bResult = m_Regression.calculate();
         if (!bResult) {
            throw new GeoAlgorithmExecutionException(Sextante.getText("Could_not_calculate_regression"));
         }
      }
   }


   private void createAdditionalInfo() {

      int i, j;
      IRasterLayer layer;

      final DecimalFormat df = new DecimalFormat("##.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Multiple_regression"));
      doc.addHeader(Sextante.getText("Multiple_regression"), 2);
      doc.startUnorderedList();

      final StringBuffer sb = new StringBuffer(" Y = " + Double.toString(m_Regression.getConstant()));

      for (i = 0; i < m_RasterLayers.size(); i++) {

         if (((j = m_Regression.getOrdered(i)) >= 0) && (j < m_RasterLayers.size())) {
            layer = (IRasterLayer) m_RasterLayers.get(j);
            sb.append(" + " + Double.toString(m_Regression.getCoeff(j)) + " * [" + layer.getName() + "]");
         }
      }
      doc.addListElement(sb.toString());
      doc.closeUnorderedList();

      doc.startUnorderedList();
      doc.addHeader(Sextante.getText("Correlation"), 2);

      for (i = 0; i < m_RasterLayers.size(); i++) {
         if (((j = m_Regression.getOrdered(i)) >= 0) && (j < m_RasterLayers.size())) {
            layer = (IRasterLayer) m_RasterLayers.get(j);
            doc.addListElement(Integer.toString(i + 1) + ": R2 = " + df.format(100.0 * m_Regression.getR2(j)) + "["
                               + df.format(100.0 * m_Regression.getR2Change(j)) + "] -> " + layer.getName());
         }
      }

      doc.closeUnorderedList();
      doc.close();

      addOutputText(REGRESSION_INFO, Sextante.getText("Regression_values"), doc.getHTMLCode());


   }

}

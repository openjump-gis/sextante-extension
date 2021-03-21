package es.unex.sextante.gui.cmd;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.StringTokenizer;

import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.AlgorithmNotFoundException;
import es.unex.sextante.gui.exceptions.CommandLineException;
import es.unex.sextante.gui.exceptions.OutputExtentNotSetException;
import es.unex.sextante.gui.exceptions.WrongNumberOfParametersException;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.outputs.OverwriteOutputChannel;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;

/**
 * This class has methods to parse command-line expressions and create instances of SEXTANTE algorithms from them
 * 
 * @author volaya
 * 
 */
public class Parser {

   /**
    * Returns a ready-to-be-executed instance of a given algorithm, based on its command-line name and an array of strings
    * representing parameter values
    * 
    * @param sAlgName
    *                the command-line name of the algorithm
    * @param args
    *                an array of string representing parameter values
    * @return an instance of the algorithm with the given parameters
    * @throws CommandLineException
    */
   public static GeoAlgorithm getAlgorithm(final String sAlgName,
                                           final String[] args) throws CommandLineException {

      int i;
      ParametersSet ps;
      Parameter param;
      String sValue;

      final GeoAlgorithm algorithm = Sextante.getAlgorithmFromCommandLineName(sAlgName);

      if (algorithm == null) {
         throw new AlgorithmNotFoundException();
      }

      GeoAlgorithm alg;
      try {
         alg = algorithm.getNewInstance();
      }
      catch (final Exception e) {
         throw new CommandLineException("Could not instantiate algorithm");
      }
      final int iParamCount = alg.getNumberOfParameters() + alg.getNumberOfOutputObjects();

      if ((args == null) || (args.length != iParamCount)) {
         throw new WrongNumberOfParametersException();
      }

      ps = alg.getParameters();
      for (int j = 0; j < 2; j++) { //twice to avoid errors with dependencies
         for (i = 0; i < alg.getNumberOfParameters(); i++) {
            param = ps.getParameter(i);
            sValue = args[i].trim();
            if (!setParameter(alg, param, sValue) && (j == 1)) {
               throw new CommandLineException("Invalid parameter: " + sValue);
            }
         }
         final OutputObjectsSet ooSet = alg.getOutputObjects();
         for (i = 0; i < ooSet.getOutputObjectsCount(); i++) {
            final Output out = ooSet.getOutput(i);
            if ((out instanceof Output3DRasterLayer) || (out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer)
                || (out instanceof OutputTable)) {
               sValue = args[i + alg.getNumberOfParameters()].trim();
               if (sValue.equals("@")) {
                  if (out instanceof OutputVectorLayer) {
                     final OutputVectorLayer ovl = (OutputVectorLayer) out;
                     if (ovl.canOverwrite()) {
                        try {
                           final Parameter paramToOverwrite = alg.getParameters().getParameter(
                                    ((OutputVectorLayer) out).getInputLayerToOverwrite());
                           final IVectorLayer layerToOverwrite = paramToOverwrite.getParameterValueAsVectorLayer();
                           out.setOutputChannel(new OverwriteOutputChannel(layerToOverwrite));
                        }
                        catch (final Exception e) {
                           out.setOutputChannel(new FileOutputChannel(null));
                        }
                     }
                     else {
                        out.setOutputChannel(new FileOutputChannel(null));
                     }
                  }
               }
               else if (sValue.equals("#")) {
                  out.setOutputChannel(new FileOutputChannel(null));
               }
               else {
                  out.setOutputChannel(new FileOutputChannel(sValue.trim()));
               }

               i++;
            }
         }
      }
      if (adjustOutputExtent(alg)) {
         return alg;
      }
      else {
         throw new OutputExtentNotSetException();
      }


   }


   private static boolean adjustOutputExtent(final GeoAlgorithm alg) {

      if (!alg.getUserCanDefineAnalysisExtent()) {
         return true;
      }

      if (CommandLineData.getAutoExtent()) {
         if (!alg.adjustOutputExtent()) {
            return false;
         }
      }
      else {
         final AnalysisExtent ge = CommandLineData.getAnalysisExtent();
         if (ge != null) {
            alg.setAnalysisExtent(ge);
         }
         else {
            return false;
         }
      }

      return true;

   }


   private static boolean setParameter(final GeoAlgorithm alg,
                                       final Parameter param,
                                       final String sValue) {

      try {
         if (param instanceof ParameterDataObject) {
            if (!sValue.equals("#")) {
               final Object obj = SextanteGUI.getInputFactory().getInputFromName(sValue);
               if (obj != null) {
                  if (param instanceof ParameterVectorLayer) {
                     if (!param.setParameterValue(obj)) {
                        return false;
                     }
                  }
                  else if (param instanceof ParameterRasterLayer) {
                     param.setParameterValue(obj);
                  }
                  else if (param instanceof ParameterTable) {
                     param.setParameterValue(obj);
                  }
               }
               else {
                  return false;
               }
            }
            else {
               final AdditionalInfoDataObject ai = (AdditionalInfoDataObject) param.getParameterAdditionalInfo();
               if (ai.getIsMandatory()) {
                  return false;
               }
               else {
                  param.setParameterValue(null);
               }
            }
         }
         else if (param instanceof ParameterNumericalValue) {
            if (!sValue.equals("#")) {
               param.setParameterValue(new Double(Double.parseDouble(sValue)));
            }
         }
         else if (param instanceof ParameterString) {
            if (!sValue.equals("#")) {
               param.setParameterValue(sValue);
            }
         }
         else if (param instanceof ParameterString) {
            param.setParameterValue(sValue);
         }
         else if (param instanceof ParameterFixedTable) {
            return setParameterFixedTable(param, sValue);
         }
         else if (param instanceof ParameterBoolean) {
            if (!sValue.equals("#")) {
               boolean bValue;
               if (sValue.equals("true")) {
                  bValue = true;
               }
               else if (sValue.equals("false")) {
                  bValue = false;
               }
               else {
                  return false;
               }
               param.setParameterValue(new Boolean(bValue));
            }
         }
         else if (param instanceof ParameterSelection) {
            if (!sValue.equals("#")) {
               final int iIndex = Integer.parseInt(sValue);
               final AdditionalInfoSelection ai = (AdditionalInfoSelection) param.getParameterAdditionalInfo();
               if ((iIndex < 0) || (iIndex > ai.getValues().length - 1)) {
                  return false;
               }
               else {
                  param.setParameterValue(new Integer(Integer.parseInt(sValue)));
               }
            }
         }
         else if (param instanceof ParameterMultipleInput) {
            if (!sValue.equals("#")) {
               return setParameterMultipleInput(param, sValue);
            }
            else {
               final AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
               if (ai.getIsMandatory()) {
                  return false;
               }
               else {
                  param.setParameterValue(new ArrayList());
               }
            }
         }
         else if (param instanceof ParameterPoint) {
            final StringTokenizer st = new StringTokenizer(sValue, ",");
            if (st.countTokens() != 2) {
               return false;
            }
            final double x = Double.parseDouble(st.nextToken());
            final double y = Double.parseDouble(st.nextToken());
            param.setParameterValue(new Point2D.Double(x, y));
         }
         else if (param instanceof ParameterTableField) {
            final ParametersSet ps = alg.getParameters();
            final AdditionalInfoTableField ai = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
            final String sParentName = ai.getParentParameterName();
            final Parameter parent = ps.getParameter(sParentName);
            if (parent == null) {
               return false;
            }
            String[] sNames;
            if (parent instanceof ParameterVectorLayer) {
               final IVectorLayer vect = parent.getParameterValueAsVectorLayer();
               sNames = vect.getFieldNames();
            }
            else if (parent instanceof ParameterTable) {
               final ITable table = parent.getParameterValueAsTable();
               sNames = table.getFieldNames();
            }
            else {
               return false;
            }

            int iIndex = -1;
            try {
               iIndex = Integer.parseInt(sValue);
            }
            catch (final NumberFormatException nfe) {
               for (int i = 0; i < sNames.length; i++) {
                  if (sValue.equals(sNames[i])) {
                     iIndex = i;
                     break;
                  }
               }
            }
            if ((iIndex >= 0) && (iIndex < sNames.length)) {
               param.setParameterValue(new Integer(iIndex));
            }
            else {
               return false;
            }
         }
         else if (param instanceof ParameterBand) {
            final ParametersSet ps = alg.getParameters();
            final AdditionalInfoBand ai = (AdditionalInfoBand) param.getParameterAdditionalInfo();
            final String sParentName = ai.getParentParameterName();
            final Parameter parent = ps.getParameter(sParentName);
            if (parent == null) {
               return false;
            }
            if (parent instanceof ParameterRasterLayer) {
               final IRasterLayer raster = parent.getParameterValueAsRasterLayer();
               final int iIndex = Integer.parseInt(sValue);
               if ((iIndex > 0) && (iIndex <= raster.getBandsCount())) {
                  param.setParameterValue(new Integer(iIndex - 1));
               }
               else {
                  return false;
               }
            }
            else {
               return false;
            }

         }

      }
      catch (final Exception e) {
         return false;
      }

      return param.isParameterValueCorrect();

   }


   private static boolean setParameterFixedTable(final Parameter param,
                                                 final String sValue) {

      boolean bIsNumberOfRowsFixed;
      int iCols, iRows;
      int iCol, iRow;
      int iToken = 0;
      FixedTableModel tableModel;
      final StringTokenizer st = new StringTokenizer(sValue, ",");
      String sToken;
      AdditionalInfoFixedTable ai;
      try {
         ai = (AdditionalInfoFixedTable) param.getParameterAdditionalInfo();
         iCols = ai.getColsCount();
         iRows = (st.countTokens() / iCols);
         bIsNumberOfRowsFixed = ai.isNumberOfRowsFixed();
         tableModel = new FixedTableModel(ai.getCols(), iRows, bIsNumberOfRowsFixed);

         if (bIsNumberOfRowsFixed) {
            if (iRows != ai.getRowsCount()) {
               return false;
            }
         }
         else {
            if (st.countTokens() % iCols != 0) {
               return false;
            }
         }

         while (st.hasMoreTokens()) {
            iRow = (int) Math.floor(iToken / (double) iCols);
            iCol = iToken % iCols;
            sToken = st.nextToken().trim();
            tableModel.setValueAt(sToken, iRow, iCol);
            iToken++;
         }

         param.setParameterValue(tableModel);
         return true;
      }
      catch (final Exception e) {
         return false;
      }

   }


   private static boolean setParameterMultipleInput(final Parameter param,
                                                    final String sValue) {

      final ArrayList array = new ArrayList();
      final StringTokenizer st = new StringTokenizer(sValue, ",");
      String sToken;
      Object obj = null;
      AdditionalInfoMultipleInput ai;
      int iType;
      try {
         ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
         iType = ai.getDataType();
         while (st.hasMoreTokens()) {
            sToken = st.nextToken().trim();
            if (iType == AdditionalInfoMultipleInput.DATA_TYPE_RASTER) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
            }
            else if (iType == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
            }
            else if (iType == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
               if (((IVectorLayer) obj).getShapeType() != IVectorLayer.SHAPE_TYPE_LINE) {
                  return false;
               }
            }
            else if (iType == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
               if (((IVectorLayer) obj).getShapeType() != IVectorLayer.SHAPE_TYPE_POLYGON) {
                  return false;
               }
            }
            else if (iType == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
               if (((IVectorLayer) obj).getShapeType() != IVectorLayer.SHAPE_TYPE_POLYGON) {
                  return false;
               }
            }
            else if (iType == AdditionalInfoMultipleInput.DATA_TYPE_TABLE) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
            }
            else if (iType == AdditionalInfoMultipleInput.DATA_TYPE_BAND) {
               obj = SextanteGUI.getInputFactory().getInputFromName(sToken);
               sToken = st.nextToken().trim();
               final int iBand = Integer.parseInt(sToken) - 1;
               if (obj != null) {
                  if ((iBand >= 0) && (iBand < ((IRasterLayer) obj).getBandsCount())) {
                     obj = new RasterLayerAndBand((IRasterLayer) obj, iBand);
                  }
                  else {
                     return false;
                  }
               }
            }
            else {
               return false;
            }

            if (obj == null) {
               return false;
            }
            else {
               array.add(obj);
            }
         }
         param.setParameterValue(array);
         return true;
      }
      catch (final Exception e) {
         return false;
      }

   }

}

package es.unex.sextante.gui.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import bsh.EvalError;
import bsh.Interpreter;
import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoString;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.WrongScriptException;
import es.unex.sextante.gui.settings.SextanteScriptsSettings;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class ScriptAlgorithm
         extends
            GeoAlgorithm {


   private static Interpreter m_Interpreter;
   private String             m_sDescriptionFile;


   public void initialize(final String sDescriptionFile) throws WrongScriptException {

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteScriptsSettings.SCRIPTS_FOLDER);
      m_sDescriptionFile = sFolder + File.separator + sDescriptionFile;
      setName(sDescriptionFile.substring(0, sDescriptionFile.indexOf(".")));
      setGroup("Scripts");

      setIsDeterminatedProcess(false);
      setUserCanDefineAnalysisExtent(true);
      defineCharacteristicsFromDescriptionFile();

      FileReader reader;
      try {
         reader = new FileReader(m_sDescriptionFile);
         m_Interpreter.eval(reader);
      }
      catch (final Exception e) {
         throw new WrongScriptException(e.getMessage());
      }


   }


   private void defineCharacteristicsFromDescriptionFile() throws WrongScriptException {

      try {
         final BufferedReader input = new BufferedReader(new FileReader(m_sDescriptionFile));
         String sLine = input.readLine().trim();
         while (sLine != null) {
            if (sLine.startsWith("//")) {
               processParameterLine(sLine);
            }
            sLine = input.readLine();
         }
         input.close();
      }
      catch (final Exception e) {

         throw new WrongScriptException(e.getMessage());
      }


   }


   private void processParameterLine(String sLine) throws WrongScriptException {

      Parameter param = null;
      Output out = null;
      AdditionalInfo additionalInfo = null;
      sLine = sLine.replace("/", "");
      final String[] sTokens = sLine.split("=");
      if (sTokens[1].toLowerCase().equals("raster")) {
         param = new ParameterRasterLayer();
         additionalInfo = new AdditionalInfoRasterLayer(true);
      }
      else if (sTokens[1].toLowerCase().equals("vector")) {
         param = new ParameterVectorLayer();
         additionalInfo = new AdditionalInfoVectorLayer(AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
      }
      else if (sTokens[1].toLowerCase().equals("table")) {
         param = new ParameterTable();
         additionalInfo = new AdditionalInfoTable(true);
      }
      else if (sTokens[1].toLowerCase().equals("multiple raster")) {
         param = new ParameterMultipleInput();
         additionalInfo = new AdditionalInfoMultipleInput(AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
      }
      else if (sTokens[1].toLowerCase().equals("multiple vector")) {
         param = new ParameterMultipleInput();
         additionalInfo = new AdditionalInfoMultipleInput(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY, true);

      }
      else if (sTokens[1].toLowerCase().equals("boolean")) {
         param = new ParameterBoolean();
         additionalInfo = new AdditionalInfoBoolean(true);
      }
      else if (sTokens[1].toLowerCase().equals("number")) {
         param = new ParameterNumericalValue();
         additionalInfo = new AdditionalInfoNumericalValue(AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0,
                  Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
      }
      else if (sTokens[1].toLowerCase().equals("string")) {
         param = new ParameterString();
         additionalInfo = new AdditionalInfoString();
      }
      else if (sTokens[1].toLowerCase().equals("output raster")) {
         out = new OutputRasterLayer();
      }
      else if (sTokens[1].toLowerCase().equals("output vector")) {
         out = new OutputVectorLayer();
      }
      else if (sTokens[1].toLowerCase().equals("output table")) {
         out = new OutputTable();
      }

      if (param != null) {
         param.setParameterName(sTokens[0]);
         param.setParameterDescription(sTokens[0]);
         param.setParameterAdditionalInfo(additionalInfo);
         try {
            m_Parameters.addParameter(param);
         }
         catch (final RepeatedParameterNameException e) {
            throw new WrongScriptException(e.getMessage());
         }
      }
      else if (out != null) {
         out.setDescription(sTokens[0]);
         out.setName(sTokens[0]);
         m_OutputObjects.add(out);
      }
      else {
         throw new WrongScriptException();
      }
   }


   @Override
   public void defineCharacteristics() {}


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final StringBuffer sb = new StringBuffer(getName() + "(");

      for (int i = 0; i < m_Parameters.getNumberOfParameters(); i++) {
         final Parameter param = m_Parameters.getParameter(i);
         sb.append(param.getCommandLineParameter() + ", ");
      }

      for (int j = 0; j < m_OutputObjects.getOutputObjectsCount(); j++) {
         final Output out = m_OutputObjects.getOutput(j);
         sb.append(out.getCommandLineParameter() + ", ");
      }

      final String sCommand = sb.substring(0, sb.length() - 2) + ")";
      try {
         m_Interpreter.eval(sCommand);
      }
      catch (final EvalError e) {
         e.printStackTrace();
         throw new GeoAlgorithmExecutionException(e.getErrorText());
      }

      //we return false because output objects should not be processed
      return false;

   }


   @Override
   public String getCommandLineName() {

      return getName();

   }


   public static void resetInterpreter() {

      m_Interpreter = new Interpreter();
      m_Interpreter.getNameSpace().importCommands("es.unex.sextante.gui.cmd.bshcommands");


   }


   public String getDescriptionFile() {

      return m_sDescriptionFile;

   }

}

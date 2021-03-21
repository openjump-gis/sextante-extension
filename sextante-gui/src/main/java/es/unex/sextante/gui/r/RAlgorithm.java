package es.unex.sextante.gui.r;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoString;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.WrongScriptException;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.ImageContainer;
import es.unex.sextante.outputs.NullOutputChannel;
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
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;


public class RAlgorithm
         extends
            GeoAlgorithm {

   private static final String R_CONSOLE_OUTPUT     = "R_CONSOLE_OUTPUT";
   private static final String R_PLOT_OUTPUT        = "R_PLOT_OUTPUT";
   private boolean             m_bShowConsoleOutput = false;
   private ArrayList<String>   m_Commands           = new ArrayList<String>();
   private ArrayList<String>   m_VerboseCommands    = new ArrayList<String>();
   private String              m_sDescriptionFile;
   private String              m_sChartFilepath     = null;
   private boolean             m_bShowPlotOutput;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      RUtils.executeR(this);

      if (m_bShowConsoleOutput) {
         m_OutputObjects.getOutput(R_CONSOLE_OUTPUT).setOutputObject(RUtils.getConsoleOutput());
      }

      if (m_bShowPlotOutput) {
         final ImageContainer cont = new ImageContainer(m_sChartFilepath);
         m_OutputObjects.getOutput(R_PLOT_OUTPUT).setOutputObject(cont);
      }

      return !m_Task.isCanceled();

   }


   public String[] getFullSetOfRCommands() throws GeoAlgorithmExecutionException {

      final String[] importCommands = getImportCommands();
      final String[] exportCommands = getExportCommands();
      final String[] customCommands = getRCommands();

      final String[] allCommands = new String[importCommands.length + customCommands.length + exportCommands.length];
      System.arraycopy(importCommands, 0, allCommands, 0, importCommands.length);
      System.arraycopy(customCommands, 0, allCommands, importCommands.length, customCommands.length);
      System.arraycopy(exportCommands, 0, allCommands, importCommands.length + customCommands.length, exportCommands.length);

      return allCommands;

   }


   private String[] getExportCommands() throws GeoAlgorithmExecutionException {

      final ArrayList<String> commands = new ArrayList<String>();

      for (int i = 0; i < m_OutputObjects.getOutputObjectsCount(); i++) {
         final Output out = m_OutputObjects.getOutput(i);
         if (out instanceof OutputRasterLayer) {
            final IOutputChannel channel = getOutputChannel(out.getName());
            if (channel instanceof FileOutputChannel) {
               final FileOutputChannel foc = (FileOutputChannel) channel;
               String sFilename = foc.getFilename();
               if (!sFilename.endsWith("tif")) {
                  sFilename = sFilename + ".tif";
               }
               foc.setFilename(sFilename);
               out.setOutputChannel(foc);
               sFilename = sFilename.replace("\\", "/");
               commands.add("writeGDAL(" + out.getName() + ",\"" + sFilename + "\")");
            }
            else if (channel instanceof NullOutputChannel) {
               String sFilename = SextanteGUI.getOutputFactory().getTempRasterLayerFilename();
               sFilename = sFilename.replace("\\", "/");
               commands.add("writeGDAL(" + out.getName() + ",\"" + sFilename + "\")");
            }
            else {
               throw new UnsupportedOutputChannelException();
            }
         }
         else if (out instanceof OutputVectorLayer) {
            final IOutputChannel channel = getOutputChannel(out.getName());
            if (channel instanceof FileOutputChannel) {
               final FileOutputChannel foc = (FileOutputChannel) channel;
               String sFilepath = foc.getFilename();
               if (!sFilepath.endsWith("shp")) {
                  sFilepath = sFilepath + ".shp";
               }
               foc.setFilename(sFilepath);
               out.setOutputChannel(foc);
               final File file = new File(sFilepath);
               String sFilename = file.getName();
               sFilename = sFilename.substring(0, sFilename.lastIndexOf("."));
               commands.add("writeOGR(" + out.getName() + ",\"" + file.getParent().replace("\\", "/") + "\",\"" + sFilename
                            + "\", driver=\"ESRI Shapefile\")");
            }
            else if (channel instanceof NullOutputChannel) {
               final String sFilepath = SextanteGUI.getOutputFactory().getTempVectorLayerFilename();
               final File file = new File(sFilepath);
               String sFilename = file.getName();
               sFilename = sFilename.substring(0, sFilename.lastIndexOf("."));
               commands.add("writeOGR(" + out.getName() + ",\"" + file.getParent().replace("\\", "/") + "\",\"" + sFilename
                            + "\", driver=\"ESRI Shapefile\")");
            }
            else {
               throw new UnsupportedOutputChannelException();
            }
         }

      }

      if (m_bShowPlotOutput) {
         commands.add("dev.off()");
      }

      return commands.toArray(new String[0]);

   }


   private String[] getImportCommands() throws GeoAlgorithmExecutionException {

      final ArrayList<String> commands = new ArrayList<String>();

      commands.add("library(\"rgdal\")");

      commands.add("x=c(" + Double.toString(m_AnalysisExtent.getXMin()) + "," + Double.toString(m_AnalysisExtent.getXMax()) + ")");
      commands.add("y=c(" + Double.toString(m_AnalysisExtent.getYMin()) + "," + Double.toString(m_AnalysisExtent.getYMax()) + ")");
      commands.add("xy <- cbind(x,y)");
      commands.add("boundingBox <- SpatialPoints(xy)");
      commands.add("cellsize=" + Double.toString(m_AnalysisExtent.getCellSize()));

      for (int i = 0; i < m_Parameters.getNumberOfParameters(); i++) {
         final Parameter param = m_Parameters.getParameter(i);
         if (param instanceof ParameterRasterLayer) {
            final IOutputChannel channel = (param.getParameterValueAsVectorLayer()).getOutputChannel();
            if (channel instanceof FileOutputChannel) {
               String sFilepath = ((FileOutputChannel) channel).getFilename();
               if (!sFilepath.toLowerCase().endsWith("asc") && !!sFilepath.toLowerCase().endsWith("tif")) {
                  throw new RExecutionException(Sextante.getText("unsupported_file_format"));
               }
               sFilepath = sFilepath.replace("\\", "/");
               commands.add(param.getParameterName() + " = " + "readGDAL(\"" + sFilepath + "\"");
            }
            else {
               throw new RExecutionException(Sextante.getText("error_non_file_based_input"));
            }
         }
         else if (param instanceof ParameterVectorLayer) {
            final IOutputChannel channel = (param.getParameterValueAsVectorLayer()).getOutputChannel();
            if (channel instanceof FileOutputChannel) {
               String sFilepath = ((FileOutputChannel) channel).getFilename();
               if (!sFilepath.toLowerCase().endsWith("shp")) {
                  throw new RExecutionException(Sextante.getText("unsupported_file_format"));
               }
               String sFilename = new File(sFilepath).getName();
               sFilename = sFilename.substring(0, sFilename.lastIndexOf("."));
               sFilepath = sFilepath.replace("\\", "/");
               commands.add(param.getParameterName() + " = " + "readOGR(\"" + sFilepath + "\",layer=\"" + sFilename + "\")");
            }
            else {
               throw new RExecutionException(Sextante.getText("error_non_file_based_input"));
            }
         }
         else if (param instanceof ParameterTableField) {
            final int iField = param.getParameterValueAsInt();
            commands.add(param.getParameterName() + "=" + Integer.toString(iField + 1));
         }
         else if (param instanceof ParameterNumericalValue) {
            final double dValue = param.getParameterValueAsDouble();
            commands.add(param.getParameterName() + "=" + Double.toString(dValue));
         }
         else if (param instanceof ParameterString) {
            final String s = param.getParameterValueAsString();
            commands.add(param.getParameterName() + "=" + s);
         }
         else if (param instanceof ParameterBoolean) {
            final boolean b = param.getParameterValueAsBoolean();
            if (b) {
               commands.add(param.getParameterName() + "=TRUE");
            }
            else {
               commands.add(param.getParameterName() + "=FALSE");
            }
         }
         else if (param instanceof ParameterMultipleInput) {
            final ArrayList list = (ArrayList) param.getParameterValueAsObject();
            for (int j = 0; j < list.size(); j++) {
               final ILayer layer = (ILayer) list.get(j);
               if (layer instanceof IRasterLayer) {
                  final IOutputChannel channel = layer.getOutputChannel();
                  if (channel instanceof FileOutputChannel) {
                     String sFilepath = ((FileOutputChannel) channel).getFilename();
                     if (!sFilepath.toLowerCase().endsWith("asc") && !!sFilepath.toLowerCase().endsWith("tif")) {
                        throw new RExecutionException(Sextante.getText("unsupported_file_format"));
                     }
                     sFilepath = sFilepath.replace("\\", "/");
                     commands.add("tempvar" + Integer.toString(j) + " = " + "readGDAL(\"" + sFilepath + "\"");
                  }
                  else {
                     throw new RExecutionException(Sextante.getText("error_non_file_based_input"));
                  }
               }
               else if (layer instanceof IVectorLayer) {
                  final IOutputChannel channel = layer.getOutputChannel();
                  if (channel instanceof FileOutputChannel) {
                     String sFilepath = ((FileOutputChannel) channel).getFilename();
                     if (!sFilepath.toLowerCase().endsWith("shp")) {
                        throw new RExecutionException(Sextante.getText("unsupported_file_format"));
                     }
                     String sFilename = new File(sFilepath).getName();
                     sFilename = sFilename.substring(0, sFilename.lastIndexOf("."));
                     sFilepath = sFilepath.replace("\\", "/");
                     commands.add("tempvar" + Integer.toString(j) + " = " + "readOGR(\"" + sFilepath + "\",layer=\"" + sFilename
                                  + "\")");
                  }
                  else {
                     throw new RExecutionException(Sextante.getText("error_non_file_based_input"));
                  }
               }
            }
            final StringBuffer sb = new StringBuffer();
            sb.append(param.getParameterName());
            sb.append(" = c(");
            for (int j = 0; j < list.size(); j++) {
               if (j != 0) {
                  sb.append(",");
               }
               sb.append("tempvar" + Integer.toString(j));
            }
            sb.append(")\n");
         }
      }

      if (m_bShowPlotOutput) {
         m_sChartFilepath = m_OutputFactory.getTempFilenameWithoutExtension() + ".png";
         m_sChartFilepath = m_sChartFilepath.replace("\\", "/");
         commands.add("png(\"" + m_sChartFilepath + "\")");
      }

      return commands.toArray(new String[0]);

   }


   private String[] getRCommands() {

      return m_Commands.toArray(new String[0]);

   }


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(false);

   }


   public void initialize(final String sDescriptionFile) throws WrongScriptException {

      final String sFolder = RUtils.getScriptsFolder();
      m_sDescriptionFile = sFolder + File.separator + sDescriptionFile;
      setName(sDescriptionFile.substring(0, sDescriptionFile.lastIndexOf(".")).replace("_", " "));
      setGroup("R-Scripts");

      setIsDeterminatedProcess(false);
      setUserCanDefineAnalysisExtent(true);
      defineCharacteristicsFromDescriptionFile();

   }


   private void defineCharacteristicsFromDescriptionFile() throws WrongScriptException {

      try {
         final BufferedReader input = new BufferedReader(new FileReader(m_sDescriptionFile));
         String sLine = input.readLine().trim();
         while (sLine != null) {
            if (sLine.startsWith("//")) {
               processParameterLine(sLine);
            }
            else if (sLine.startsWith(">")) {
               sLine = sLine.substring(1);
               m_Commands.add(sLine.trim());
               m_VerboseCommands.add(sLine.trim());
               if (!m_bShowConsoleOutput) {
                  addOutputText(R_CONSOLE_OUTPUT, "R Output");
               }
               m_bShowConsoleOutput = true;
            }
            else {
               m_Commands.add(sLine.trim());
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

      if (sLine.toLowerCase().equals("showplots")) {
         m_bShowPlotOutput = true;
         addOutputImage(R_PLOT_OUTPUT, "R Plots");
         return;
      }

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
      else if (sTokens[1].toLowerCase().startsWith("field")) {
         param = new ParameterTableField();
         final String parent = sTokens[1].substring("field".length()).trim();
         additionalInfo = new AdditionalInfoTableField(parent, true);
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
      else if (sTokens[1].toLowerCase().equals("group")) {
         setGroup(sTokens[0]);
         return;
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
   public GeoAlgorithm getNewInstance() throws InstantiationException, IllegalAccessException {

      final RAlgorithm alg = this.getClass().newInstance();
      alg.setOutputObjects(m_OutputObjects.getNewInstance());
      alg.setName(this.getName());
      alg.setGroup(this.getGroup());
      alg.setParameters(m_Parameters.getNewInstance());
      alg.setIsDeterminatedProcess(false);
      alg.setUserCanDefineAnalysisExtent(getUserCanDefineAnalysisExtent());
      alg.m_Commands = (ArrayList<String>) m_Commands.clone();
      alg.m_VerboseCommands = (ArrayList<String>) m_VerboseCommands.clone();
      alg.m_bShowConsoleOutput = m_bShowConsoleOutput;
      alg.m_bShowPlotOutput = m_bShowPlotOutput;

      return alg;

   }


   @Override
   public String getCommandLineName() {

      final String sName = "r:" + getName().toLowerCase().replace(" ", "");
      return sName;

   }


   public ArrayList<String> getVerboseCommands() {

      return m_VerboseCommands;

   }


   public String getFilename() {

      return m_sDescriptionFile;

   }

}

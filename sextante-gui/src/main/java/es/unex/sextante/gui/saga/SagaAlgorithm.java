

package es.unex.sextante.gui.saga;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.NullOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterVectorLayer;


public class SagaAlgorithm
         extends
            GeoAlgorithm {

   private String                  m_sDescriptionFile;
   private int                     m_iExportedLayers;
   private HashMap<String, String> m_ExportedLayers;
   private String                  m_sLibraryName;


   public void initialize(final String sDescriptionFile) throws UnwrappableSagaAlgorithmException {

      m_sDescriptionFile = sDescriptionFile;

      setIsDeterminatedProcess(true);
      setUserCanDefineAnalysisExtent(false);
      defineCharacteristicsFromDescriptionFile();

   }


   private void defineCharacteristicsFromDescriptionFile() throws UnwrappableSagaAlgorithmException {


      try {
         String sLastParentParameterName = null;
         final BufferedReader input = new BufferedReader(new FileReader(m_sDescriptionFile));
         String sLine = input.readLine().trim();
         while (sLine != null) {
            sLine = sLine.trim();
            boolean bReadLine = true;
            if (sLine.startsWith("library name")) {
               m_sLibraryName = sLine.split("\t")[1];
               setGroup(SagaLibraryNames.getDecoratedLibraryName(m_sLibraryName));

            }
            if (sLine.startsWith("module name")) {
               setName(sLine.split("\t")[1]);
            }
            if (sLine.contains("Olaya")) { //exclude my own algorithms. They are all in SEXTANTE and have been improved and further tested
               throw new UnwrappableSagaAlgorithmException();
            }
            if (sLine.startsWith("-")) {
               String sName, sDescription;
               try {
                  sName = sLine.substring(1, sLine.indexOf(":"));
                  sDescription = sLine.substring(sLine.indexOf(">") + 1).trim();
               }
               catch (final Exception e) {//for some reason boolean params have a different syntax
                  sName = sLine.substring(1, sLine.indexOf("\t"));
                  sDescription = sLine.substring(sLine.indexOf("\t") + 1).trim();
               }
               if (SagaBlackList.isInBlackList(sName, getGroup())) {
                  throw new UnwrappableSagaAlgorithmException();
               }
               sLine = input.readLine();
               if (sLine.contains("Data object") && sLine.contains("File")) {
                  throw new UnwrappableSagaAlgorithmException();
               }
               if (sLine.contains("Table")) {
                  if (sLine.contains("input")) {
                     m_Parameters.addInputTable(sName, sDescription, !sLine.contains("optional"));
                     sLastParentParameterName = sName;
                  }
                  else if (sLine.contains("Static")) {
                     sLine = input.readLine().trim();
                     final String sNumber = sLine.split(" ")[0];
                     final int iNumber = Integer.parseInt(sNumber);
                     final String[] sColNames = new String[iNumber];
                     for (int i = 0; i < sColNames.length; i++) {
                        sLine = input.readLine();
                        sColNames[i] = sLine.split("]")[1];
                     }
                     //can't get info about rows from description
                     m_Parameters.addFixedTable(sName, sDescription, sColNames, 3, false);
                  }
                  else if (sLine.contains("field")) {
                     if (sLastParentParameterName == null) {
                        throw new UnwrappableSagaAlgorithmException();
                     }
                     m_Parameters.addTableField(sName, sDescription, sLastParentParameterName);
                  }
                  else {
                     addOutputTable(sName, sDescription);
                  }
               }
               if (sLine.contains("Grid")) {
                  if (sLine.contains("input")) {
                     if (sLine.contains("list")) {
                        m_Parameters.addMultipleInput(sName, sDescription, AdditionalInfoMultipleInput.DATA_TYPE_RASTER,
                                 !sLine.contains("optional"));
                     }
                     else {
                        m_Parameters.addInputRasterLayer(sName, sDescription, !sLine.contains("optional"));
                     }
                  }
                  else {
                     addOutputRasterLayer(sName, sDescription);
                  }
               }
               else if (sLine.contains("Shapes")) {
                  if (sLine.contains("input")) {
                     if (sLine.contains("list")) {
                        m_Parameters.addMultipleInput(sName, sDescription, AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY,
                                 !sLine.contains("optional"));
                     }
                     else {
                        m_Parameters.addInputVectorLayer(sName, sDescription, AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
                                 !sLine.contains("optional"));
                        sLastParentParameterName = sName;
                     }
                  }
                  else {
                     addOutputVectorLayer(sName, sDescription, OutputVectorLayer.SHAPE_TYPE_UNDEFINED);
                  }
               }
               else if (sLine.contains("Floating")) {
                  m_Parameters.addNumericalValue(sName, sDescription, 0, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
               }
               else if (sLine.contains("Integer")) {
                  m_Parameters.addNumericalValue(sName, sDescription, 0, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
               }
               else if (sLine.contains("Boolean")) {
                  m_Parameters.addBoolean(sName, sDescription, true);
               }
               else if (sLine.contains("Text")) {
                  m_Parameters.addString(sName, sDescription);
               }
               else if (sLine.contains("Choice")) {
                  input.readLine();
                  final ArrayList<String> options = new ArrayList<String>();
                  sLine = input.readLine().trim();
                  while ((sLine != null) && !(sLine.trim().startsWith("-"))) {
                     options.add(sLine);
                     sLine = input.readLine();
                  }
                  m_Parameters.addSelection(sName, sDescription, options.toArray(new String[0]));
                  if (sLine == null) {
                     break;
                  }
                  else {
                     bReadLine = false;
                  }
               }
            }
            if (bReadLine) {
               sLine = input.readLine();
            }
         }
         input.close();
      }
      catch (final Exception e) {
         //SagaAlgorithmProvider.getSagaLogHandler().addMessage(e.getMessage());
         throw new UnwrappableSagaAlgorithmException();
      }


   }


   @Override
   public void defineCharacteristics() {
   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final ArrayList<String> commands = new ArrayList<String>();
      m_ExportedLayers = new HashMap<String, String>();

      //resolve temporary output files
      for (int i = 0; i < m_OutputObjects.getOutputDataObjectsCount(); i++) {
         final Output out = m_OutputObjects.getOutput(i);
         out.setOutputChannel(this.getOutputChannel(out.getName()));
      }

      //1: Export rasters to sgrd. only ASC and TIF are supported.
      //   Vector layers must be in shapefile format and tables in dbf format. We check that.
      for (int i = 0; i < m_Parameters.getNumberOfParameters(); i++) {
         final Parameter param = m_Parameters.getParameter(i);
         if (param instanceof ParameterRasterLayer) {
            final ParameterRasterLayer raster = (ParameterRasterLayer) param;
            final IRasterLayer layer = raster.getParameterValueAsRasterLayer();
            if (layer == null) {
               continue;
            }
            commands.add(exportRasterLayer(layer));
         }
         if (param instanceof ParameterVectorLayer) {
            final ParameterVectorLayer vector = (ParameterVectorLayer) param;
            final IVectorLayer layer = vector.getParameterValueAsVectorLayer();
            if (layer == null) {
               continue;
            }
            final IOutputChannel channel = layer.getOutputChannel();
            if (channel instanceof FileOutputChannel) {
               final String sFilename = ((FileOutputChannel) channel).getFilename();
               if (!sFilename.toLowerCase().endsWith("shp")) {
                  throw new SagaExecutionException(Sextante.getText("unsupported_file_format"));
               }
            }
            else {
               throw new SagaExecutionException(Sextante.getText("error_non_file_based_input"));
            }
         }
         if (param instanceof ParameterTable) {
            final ParameterTable paramTable = (ParameterTable) param;
            final ITable table = paramTable.getParameterValueAsTable();
            final IOutputChannel channel = table.getOutputChannel();
            if (channel instanceof FileOutputChannel) {
               final String sFilename = ((FileOutputChannel) channel).getFilename();
               if (!sFilename.toLowerCase().endsWith("dbf")) {
                  throw new SagaExecutionException(Sextante.getText("unsupported_file_format"));
               }
            }
            else {
               throw new SagaExecutionException(Sextante.getText("error_non_file_based_input"));
            }
         }
         if (param instanceof ParameterMultipleInput) {
            final ArrayList list = (ArrayList) param.getParameterValueAsObject();
            if ((list == null) || (list.size() == 0)) {
               continue;
            }
            final AdditionalInfoMultipleInput aimi = (AdditionalInfoMultipleInput) ((ParameterMultipleInput) param).getParameterAdditionalInfo();
            if (aimi.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_RASTER) {
               for (int j = 0; j < list.size(); j++) {
                  commands.add(exportRasterLayer((IRasterLayer) list.get(j)));
               }
            }
            else if (aimi.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY) {
               for (int j = 0; j < list.size(); j++) {
                  final IVectorLayer layer = (IVectorLayer) list.get(j);
                  if (layer == null) {
                     continue;
                  }
                  final IOutputChannel channel = layer.getOutputChannel();
                  if (channel instanceof FileOutputChannel) {
                     final String sFilename = ((FileOutputChannel) channel).getFilename();
                     if (!sFilename.toLowerCase().endsWith("shp")) {
                        throw new SagaExecutionException(Sextante.getText("unsupported_file_format"));
                     }
                  }
                  else {
                     throw new SagaExecutionException(Sextante.getText("error_non_file_based_input"));
                  }
               }
            }
         }
      }

      //2: set parameters and outputs
      final StringBuffer sCommand = new StringBuffer(m_sLibraryName + " \"" + getName() + "\"");
      for (int i = 0; i < m_Parameters.getNumberOfParameters(); i++) {
         final Parameter param = m_Parameters.getParameter(i);
         final Object paramObj = param.getParameterValueAsObject();
         if (param instanceof ParameterRasterLayer) {
            if (paramObj == null) {
               continue;
            }
            final String sFilename = ((FileOutputChannel) ((IRasterLayer) paramObj).getOutputChannel()).getFilename();
            sCommand.append(" -" + param.getParameterName() + " " + m_ExportedLayers.get(sFilename));
         }
         else if ((param instanceof ParameterVectorLayer) || (param instanceof ParameterTable)) {
            if (paramObj == null) {
               continue;
            }
            final String sFilename = ((FileOutputChannel) ((IDataObject) paramObj).getOutputChannel()).getFilename();
            sCommand.append(" -" + param.getParameterName() + " " + sFilename);
         }
         else if (param instanceof ParameterMultipleInput) {
            if (paramObj == null) {
               continue;
            }
            final ArrayList list = (ArrayList) paramObj;
            if (list.size() == 0) {
               continue;
            }
            sCommand.append(" -" + param.getParameterName() + " ");
            final AdditionalInfoMultipleInput aimi = (AdditionalInfoMultipleInput) ((ParameterMultipleInput) param).getParameterAdditionalInfo();
            if (aimi.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_RASTER) {
               for (int j = 0; j < list.size(); j++) {
                  final IDataObject dataObject = (IDataObject) list.get(j);
                  final String sFilename = ((FileOutputChannel) dataObject.getOutputChannel()).getFilename();
                  sCommand.append(m_ExportedLayers.get(sFilename));
                  if (j < list.size() - 1) {
                     sCommand.append(";");
                  }
               }
            }
            else {
               for (int j = 0; j < list.size(); j++) {
                  final IDataObject dataObject = (IDataObject) list.get(j);
                  sCommand.append(((FileOutputChannel) dataObject.getOutputChannel()).getFilename());
                  if (j < list.size() - 1) {
                     sCommand.append(";");
                  }
               }
            }
         }
         else if (param instanceof ParameterSelection) {
            sCommand.append(" -" + param.getParameterName() + " " + Integer.toString(param.getParameterValueAsInt()));
         }
         else if (param instanceof ParameterBoolean) {
            if (param.getParameterValueAsBoolean()) {
               sCommand.append(" -" + param.getParameterName());
            }
         }
         else {
            sCommand.append(" -" + param.getParameterName() + " " + param.getParameterValueAsString());
         }
      }
      for (int i = 0; i < m_OutputObjects.getOutputObjectsCount(); i++) {
         final Output out = m_OutputObjects.getOutput(i);
         if (out instanceof OutputRasterLayer) {
            final IOutputChannel channel = getOutputChannel(out.getName());
            if (channel instanceof FileOutputChannel) {
               final FileOutputChannel foc = (FileOutputChannel) channel;
               String sFilename = foc.getFilename();
               if (!sFilename.endsWith("asc") && !sFilename.endsWith("tif")) {
                  sFilename = sFilename + ".tif";
               }
               foc.setFilename(sFilename);
               out.setOutputChannel(foc);
               sFilename = SextanteGUI.getOutputFactory().getTempFolder() + File.separator + new File(sFilename).getName()
                           + ".sgrd";
               sCommand.append(" -" + out.getName() + " " + sFilename);
            }
            else if (channel instanceof NullOutputChannel) {
               final String sFilename = SextanteGUI.getOutputFactory().getTempRasterLayerFilename();
               sCommand.append(" -" + out.getName() + " " + sFilename + ".sgrd");
            }
            else {
               throw new UnsupportedOutputChannelException();
            }
         }
         else if (out instanceof OutputVectorLayer) {
            final IOutputChannel channel = getOutputChannel(out.getName());
            if (channel instanceof FileOutputChannel) {
               final FileOutputChannel foc = (FileOutputChannel) channel;
               String sFilename = foc.getFilename();
               if (!sFilename.endsWith("shp")) {
                  sFilename = sFilename + ".shp";
               }
               foc.setFilename(sFilename);
               out.setOutputChannel(foc);
               sCommand.append(" -" + out.getName() + " " + sFilename);
            }
            else if (channel instanceof NullOutputChannel) {
               final String sFilename = SextanteGUI.getOutputFactory().getTempVectorLayerFilename();
               sCommand.append(" -" + out.getName() + " " + sFilename);
            }
            else {
               throw new UnsupportedOutputChannelException();
            }
         }
         else if (out instanceof OutputTable) {
            final IOutputChannel channel = getOutputChannel(out.getName());
            if (channel instanceof FileOutputChannel) {
               final FileOutputChannel foc = (FileOutputChannel) channel;
               String sFilename = foc.getFilename();
               if (!sFilename.endsWith("dbf")) {
                  sFilename = sFilename + ".dbf";
               }
               foc.setFilename(sFilename);
               out.setOutputChannel(foc);
               sCommand.append(" -" + out.getName() + " " + sFilename);
            }
            else if (channel instanceof NullOutputChannel) {
               final String sFilename = SextanteGUI.getOutputFactory().getTempTableFilename();
               sCommand.append(" -" + out.getName() + " " + sFilename);
            }
            else {
               throw new UnsupportedOutputChannelException();
            }
         }

      }

      commands.add(sCommand.toString());

      //3:Export resulting raster layers
      for (int i = 0; i < m_OutputObjects.getOutputObjectsCount(); i++) {
         final Output out = m_OutputObjects.getOutput(i);
         if (out instanceof OutputRasterLayer) {
            final IOutputChannel channel = getOutputChannel(out.getName());
            if (!(channel instanceof FileOutputChannel)) {
               if (channel instanceof NullOutputChannel) {
                  continue;
               }
               else {
                  throw new UnsupportedOutputChannelException();
               }
            }
            final FileOutputChannel foc = (FileOutputChannel) channel;
            String sFilename = foc.getFilename();
            if (!sFilename.endsWith("asc") && !sFilename.endsWith("tif")) {
               sFilename = sFilename + ".tif";
            }
            final String sFilename2 = SextanteGUI.getOutputFactory().getTempFolder() + File.separator
                                      + new File(sFilename).getName() + ".sgrd";
            if (sFilename.endsWith("asc")) {
               commands.add("io_grid 0 -GRID " + sFilename2 + " -FORMAT 1 -FILE " + sFilename);
            }
            else {
               commands.add("io_gdal 1 -GRIDS " + sFilename2 + " -FORMAT 1 -FILE " + sFilename);
            }
         }
      }


      //4 Run SAGA

      SagaUtils.createSagaBatchJobFileFromSagaCommands(commands.toArray(new String[0]));
      SagaUtils.executeSaga(this);

      return !m_Task.isCanceled();

   }


   private String exportRasterLayer(final IRasterLayer layer) throws SagaExecutionException {

      String sFilename;
      final IOutputChannel channel = layer.getOutputChannel();
      if (channel instanceof FileOutputChannel) {
         sFilename = ((FileOutputChannel) channel).getFilename();
         if (!sFilename.toLowerCase().endsWith("tif") && !sFilename.toLowerCase().endsWith("asc")) {
            throw new SagaExecutionException(Sextante.getText("unsupported_file_format"));
         }
      }
      else {
         throw new SagaExecutionException(Sextante.getText("error_non_file_based_input"));
      }
      final String sExt = sFilename.substring(sFilename.lastIndexOf(".") + 1);
      final String sDestFilename = getTempFilename();
      m_ExportedLayers.put(((FileOutputChannel) channel).getFilename(), sDestFilename);
      if (sExt.toLowerCase().equals("tif")) {
         return "io_grid_image 1 -OUT_GRID " + sDestFilename + " -FILE " + sFilename + " -METHOD 0";
      }
      else {
         return "io_grid 1 -GRID " + sDestFilename + " -FILE " + sFilename;

      }

   }


   private String getTempFilename() {

      m_iExportedLayers++;
      final String sPath = SextanteGUI.getOutputFactory().getTempFolder();
      final String sFilename = sPath + File.separator + Long.toString(Calendar.getInstance().getTimeInMillis())
                               + Integer.toString(m_iExportedLayers) + ".sgrd";

      return sFilename;
   }


   @Override
   public String getCommandLineName() {

      final String sName = "saga:" + getName().toLowerCase().replace(" ", "");
      return sName;

   }


   @Override
   public GeoAlgorithm getNewInstance() throws InstantiationException, IllegalAccessException {

      final SagaAlgorithm alg = this.getClass().newInstance();
      alg.setOutputObjects(m_OutputObjects.getNewInstance());
      alg.setName(this.getName());
      alg.setGroup(this.getGroup());
      alg.setParameters(m_Parameters.getNewInstance());
      alg.setIsDeterminatedProcess(true);
      alg.setUserCanDefineAnalysisExtent(getUserCanDefineAnalysisExtent());
      alg.m_sDescriptionFile = m_sDescriptionFile;
      alg.m_sLibraryName = m_sLibraryName;

      return alg;

   }


   public void updateProgress(final int iPartial,
                              final int iTotal) {

      setProgress(iPartial, iTotal);

   }


}

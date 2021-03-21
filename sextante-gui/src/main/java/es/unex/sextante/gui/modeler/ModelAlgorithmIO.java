

package es.unex.sextante.gui.modeler;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.exceptions.ModelIOException;
import es.unex.sextante.modeler.elements.ModelElement3DRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementFactory;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.modeler.elements.ModelElementRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementTable;
import es.unex.sextante.modeler.elements.ModelElementVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;


public class ModelAlgorithmIO {

   private static String       encoding                 = "ISO-8859-1";
   private static final String MODEL                    = "model";
   private static final String NAME                     = "name";
   private static final String GROUP                    = "group";
   private static final String CLASS                    = "class";
   private static final String MODEL_FILE               = "model_file";
   private static final String WPS_URL                  = "wps_url";
   private static final String WPS_PROCESS_IDENTIFIER   = "wps_process_id";
   private static final String GRASS_PROCESS_IDENTIFIER = "grass_process_id";
   private static final String DESCRIPTION              = "description";
   private static final String ALGORITHM                = "algorithm";
   private static final String DATA_OBJECT              = "data_object";
   private static final String ASSIGNMENT               = "assignment";
   private static final String ASSIGNEDTO               = "assigned_to";
   private static final String KEY                      = "key";
   private static final String VALUE                    = "value";
   private static final String INPUT                    = "input";
   private static final String COORDS                   = "coords";
   private static final String OUTPUT                   = "output";
   private static final String ALG_CMD_LINE_NAME        = "alg_cmd_line_name";


   public static boolean saveAsJava(final ModelAlgorithm alg,
                                    final File file) {

      try {
         final FileWriter writer = new FileWriter(file);
         final BufferedWriter out = new BufferedWriter(writer);
         out.write(ModelCodeCreator.getJavaCode(alg));
         out.close();
         writer.close();
         return true;
      }
      catch (final IOException e) {
         e.printStackTrace();
         return false;
      }

   }


   public static boolean save(final ModelerPanel panel,
                              final File file) {

      int i;
      Writer writer = null;
      final ModelAlgorithm modelAlg = panel.getAlgorithm();

      try {
         writer = new FileWriter(file);
         final KXmlSerializer serializer = new KXmlSerializer();
         serializer.setOutput(writer);
         serializer.startDocument(encoding, new Boolean(true));
         serializer.text("\n\t");
         serializer.startTag(null, MODEL);
         serializer.attribute(null, NAME, modelAlg.getName());
         serializer.attribute(null, GROUP, modelAlg.getGroup());

         final ArrayList algKeys = modelAlg.getAlgorithmKeys();
         final ArrayList algs = modelAlg.getAlgorithms();
         final ParametersSet ps = modelAlg.getParameters();
         final HashMap dao = panel.getDataObjects();
         final ArrayList assignmentsArray = modelAlg.getInputAssignments();

         //Algorithms
         for (i = 0; i < algs.size(); i++) {
            final GeoAlgorithm alg = (GeoAlgorithm) algs.get(i);
            serializer.text("\n");
            serializer.text("\t\t");
            serializer.startTag(null, ALGORITHM);
            String sKey = (String) algKeys.get(i);
            serializer.attribute(null, KEY, sKey);
            serializer.attribute(null, ALG_CMD_LINE_NAME, alg.getCommandLineName());
            serializer.attribute(null, COORDS, getCoordsAsString(panel.getModelGraphPanel().getCoords(), sKey));
            final HashMap assignments = (HashMap) assignmentsArray.get(i);
            final Set set = assignments.keySet();
            final Iterator iter = set.iterator();
            while (iter.hasNext()) {
               sKey = (String) iter.next();
               serializer.text("\n");
               serializer.text("\t\t\t");
               serializer.startTag(null, ASSIGNMENT);
               serializer.attribute(null, KEY, sKey);
               String sAssignment = (String) assignments.get(sKey);
               if (sAssignment == null) {
                  sAssignment = "null";
               }
               serializer.attribute(null, ASSIGNEDTO, sAssignment);
               serializer.endTag(null, ASSIGNMENT);
            }
            serializer.text("\n");
            serializer.text("\t\t");
            serializer.endTag(null, ALGORITHM);
         }

         //Parameters
         final int iCount = ps.getNumberOfParameters();
         for (i = 0; i < iCount; i++) {
            final String sKey = ps.getParameter(i).getParameterName();
            final String sCoords = getCoordsAsString(panel.getModelGraphPanel().getCoords(), sKey);
            ps.getParameter(i).serialize(serializer, sCoords);
         }

         //data objects
         final Set set = dao.keySet();
         final Iterator iter = set.iterator();
         while (iter.hasNext()) {
            final String sKey = (String) iter.next();
            if (sKey.indexOf("INPUT") != 0) {
               final ObjectAndDescription oad = (ObjectAndDescription) dao.get(sKey);
               serializer.text("\n");
               serializer.text("\t\t");
               serializer.startTag(null, DATA_OBJECT);
               serializer.attribute(null, KEY, sKey);
               final String sValue = getValueAsString(oad.getObject());
               serializer.attribute(null, VALUE, sValue);
               serializer.attribute(null, DESCRIPTION, oad.getDescription());
               serializer.endTag(null, DATA_OBJECT);
            }
         }

         //Outputs
         final OutputObjectsSet ooSet = modelAlg.getOutputObjects();
         for (int j = 0; j < ooSet.getOutputObjectsCount(); j++) {
            final Output out = ooSet.getOutput(j);
            serializer.text("\n");
            serializer.text("\t\t");
            serializer.startTag(null, OUTPUT);
            serializer.attribute(null, KEY, out.getName());
            serializer.attribute(null, CLASS, out.getClass().toString());
            serializer.attribute(null, DESCRIPTION, out.getDescription());
            serializer.endTag(null, OUTPUT);
         }

         serializer.text("\n");
         serializer.text("\t");
         serializer.endTag(null, MODEL);
         serializer.text("\n");
         serializer.startDocument(encoding, new Boolean(true));
         writer.close();
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

      try {
         writer.close();
      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }

      return true;

   }


   private static String getCoordsAsString(final HashMap coords,
                                           final String sKey) {

      final Rectangle2D rect = (Rectangle2D) coords.get(sKey);

      if (rect == null) {
         return "null";
      }
      else {
         final StringBuffer sb = new StringBuffer("");
         sb.append(Integer.toString((int) rect.getMinX()));
         sb.append(",");
         sb.append(Integer.toString((int) rect.getMinY()));
         sb.append(",");
         sb.append(Integer.toString((int) rect.getWidth()));
         sb.append(",");
         sb.append(Integer.toString((int) rect.getHeight()));
         return sb.toString();
      }

   }


   private static String getValueAsString(final Object ob) {

      if (ob.getClass().equals(ArrayList.class)) {
         final ArrayList array = (ArrayList) ob;
         final StringBuffer s = new StringBuffer();
         for (int i = 0; i < array.size(); i++) {
            s.append((String) array.get(i));
            if (i < array.size() - 1) {
               s.append(",");
            }
         }
         return s.toString();
      }
      /*if (ob instanceof IModelElement){
      	return ob.getClass().toString();
      }*/
      else {
         return ob.toString();
      }
   }


   public static ModelAlgorithm open(final File file,
                                     final HashMap dao,
                                     final ArrayList inputKeys,
                                     final HashMap coords) {

      final ModelAlgorithm model = new ModelAlgorithm();
      final KXmlParser parser = new KXmlParser();
      final ParametersSet ps = model.getParameters();
      GeoAlgorithm alg = null;
      String sAlgName = null;
      final OutputObjectsSet ooSet = model.getOutputObjects();

      model.setFilename(file.getAbsolutePath());

      try {

         parser.setInput(new FileInputStream(file), encoding);

         int tag = parser.nextTag();
         boolean bOut = false;

         if (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            while ((tag != XmlPullParser.END_DOCUMENT) && !bOut) {
               switch (tag) {
                  case XmlPullParser.START_TAG:
                     if (parser.getName().compareTo(MODEL) == 0) {
                        final String sName = parser.getAttributeValue("", NAME);
                        model.setName(sName);
                        final String sGroup = parser.getAttributeValue("", GROUP);
                        model.setGroup(sGroup);
                     }
                     else if (parser.getName().compareTo(ALGORITHM) == 0) {
                        alg = getAlgorithm(parser.getAttributeValue("", ALG_CMD_LINE_NAME));
                        sAlgName = parser.getAttributeValue("", KEY);
                        final String sCoords = parser.getAttributeValue("", COORDS);
                        if (alg == null) {
                           throw new ModelIOException();
                        }
                        model.addAlgorithm(alg, sAlgName);
                        if (!sCoords.equals("null")) {
                           coords.put(sAlgName, getCoordsFromString(sCoords));
                        }
                     }
                     else if (parser.getName().compareTo(DATA_OBJECT) == 0) {
                        final String sKey = parser.getAttributeValue("", KEY);
                        final String sValue = parser.getAttributeValue("", VALUE);
                        final String sDescription = parser.getAttributeValue("", DESCRIPTION);
                        addDataObject(sKey, sValue, sDescription, dao);
                     }
                     else if (parser.getName().compareTo(OUTPUT) == 0) {

                        final String sKey = parser.getAttributeValue("", KEY);
                        final String sValue = parser.getAttributeValue("", CLASS);
                        final String sDescription = parser.getAttributeValue("", DESCRIPTION);
                        addOutputObject(sKey, sValue, sDescription, ooSet, dao);
                     }
                     else if (parser.getName().compareTo(INPUT) == 0) {
                        final String sCoords = parser.getAttributeValue("", COORDS);
                        Rectangle2D rect = null;
                        if (!sCoords.equals("null")) {
                           rect = getCoordsFromString(sCoords);
                        }
                        final Parameter param = Parameter.deserialize(parser);
                        if (param == null) {
                           return null;
                        }
                        ps.addParameter(param);
                        inputKeys.add(param.getParameterName());
                        final ObjectAndDescription oad = new ObjectAndDescription(param.getParameterDescription(),
                                 ModelElementFactory.getParameterAsModelElement(param));
                        dao.put(param.getParameterName(), oad);
                        if (rect != null) {
                           coords.put(param.getParameterName(), getCoordsFromString(sCoords));
                        }
                     }
                     else if (parser.getName().compareTo(ASSIGNMENT) == 0) {
                        final String sKey = parser.getAttributeValue("", KEY);
                        String sAssignment = parser.getAttributeValue("", ASSIGNEDTO);
                        if (sAssignment.equals("null")) {
                           sAssignment = null;
                        }
                        model.addInputAsignment(sKey, sAssignment, sAlgName);
                     }
                     //}
                     break;
                  case XmlPullParser.END_TAG:
                     if (parser.getName().compareTo(MODEL) == 0) {
                        bOut = true;
                     }
                     break;
                  case XmlPullParser.TEXT:
                     break;
               }
               if (!bOut) {
                  tag = parser.next();
               }
            }
         }

      }
      catch (final Exception e) {
         e.printStackTrace();
         Sextante.addErrorToLog(e);
         return null;

      }

      return model;

   }


   private static void addOutputObject(final String sKey,
                                       final String sValue,
                                       final String sDescription,
                                       final OutputObjectsSet ooSet,
                                       final HashMap dao) {

      Output out = null;
      if (sValue.equals(OutputRasterLayer.class.toString())) {
         out = new OutputRasterLayer();
      }
      else if (sValue.equals(Output3DRasterLayer.class.toString())) {
         out = new Output3DRasterLayer();
      }
      else if (sValue.equals(OutputVectorLayer.class.toString())) {
         out = new OutputVectorLayer();
         final ObjectAndDescription oad = (ObjectAndDescription) dao.get(sKey);
         if (oad != null) {
            final ModelElementVectorLayer mevl = (ModelElementVectorLayer) oad.getObject();
            ((OutputVectorLayer) out).setShapeType(mevl.getShapeType());
         }
      }
      else if (sValue.equals(OutputTable.class.toString())) {
         out = new OutputTable();
      }
      else {
         return;
      }

      out.setDescription(sDescription);
      out.setName(sKey);
      ooSet.add(out);

   }


   public static ModelAlgorithm open(final File file,
                                     final ModelerPanel panel) {

      ModelAlgorithm model;
      final HashMap dao = panel.getDataObjects();
      final ArrayList inputKeys = panel.getInputKeys();
      final HashMap coords = panel.getModelGraphPanel().getCoords();

      panel.getModelGraphPanel().resetCoords();

      model = open(file, dao, inputKeys, coords);

      panel.getModelGraphPanel().setAlgorithm(model);

      return model;

   }


   private static Rectangle2D getCoordsFromString(final String sCoords) {

      final Rectangle2D rect = new Rectangle2D.Double();

      final String[] coords = sCoords.split("\\,");
      rect.setRect(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]),
               Double.parseDouble(coords[3]));

      return rect;

   }


   private static void addDataObject(final String sKey,
                                     final String sValue,
                                     final String sDescription,
                                     final HashMap dao) {

      if (sKey.indexOf("INNERPARAM") == 0) {
         if (sDescription.equals("Numerical Value")) {
            if (sValue.indexOf(".") != -1) {
               dao.put(sKey, new ObjectAndDescription(sDescription, new Double(Double.parseDouble(sValue))));
            }
            else {
               dao.put(sKey, new ObjectAndDescription(sDescription, new Integer(Integer.parseInt(sValue))));
            }
         }
         if (sDescription.equals("String") || sDescription.equals("Field")) {
            dao.put(sKey, new ObjectAndDescription(sDescription, sValue));
         }
         else if (sDescription.equals("Selection") || sDescription.equals("Band")) {
            dao.put(sKey, new ObjectAndDescription(sDescription, new Integer(Integer.parseInt(sValue))));
         }
         else if (sDescription.equals("Multiple Input")) {
            final ArrayList array = new ArrayList();
            final String[] sElements = sValue.split("\\,");
            for (int i = 0; i < sElements.length; i++) {
               array.add(sElements[i]);
            }
            dao.put(sKey, new ObjectAndDescription(sDescription, array));
         }
         else if (sDescription.equals("Fixed Table")) {
            final String sRows[] = sValue.split("\\,");
            final int iRows = sRows.length;
            String[] data = sRows[0].split("\\|");
            final int iCols = data.length;
            final String sColNames[] = new String[iCols];
            for (int i = 0; i < iCols; i++) {
               sColNames[i] = Integer.toString(i + 1);
            }
            final FixedTableModel ftm = new FixedTableModel(sColNames, iRows, false);

            for (int i = 0; i < iRows; i++) {
               String sRow = sRows[i].replaceAll("\\[", "");
               sRow = sRow.replaceAll("\\]", "");
               data = sRow.split("\\|");
               for (int j = 0; j < iCols; j++) {
                  ftm.setValueAt(data[j], i, j);
               }
            }
            dao.put(sKey, new ObjectAndDescription(sDescription, ftm));
         }
         else if (sDescription.equals("Point")) {
            final String[] sCoords = sValue.split("\\,");
            final double x = Double.parseDouble(sCoords[0]);
            final double y = Double.parseDouble(sCoords[1]);
            dao.put(sKey, new ObjectAndDescription(sDescription, new Point2D.Double(x, y)));
         }
         else if (sDescription.equals("Boolean")) {
            dao.put(sKey, new ObjectAndDescription(sDescription, new Boolean(sValue.equals("true"))));
         }

      }
      else if (sKey.indexOf("INPUT") == 0) {
         //ignore this
      }
      else {
         final String[] sTokens = sValue.split(",");
         if (sTokens[0].equals(ModelElementRasterLayer.class.toString())) {
            final ModelElementRasterLayer merl = new ModelElementRasterLayer();
            merl.setNumberOfBands(Integer.parseInt(sTokens[1]));
            dao.put(sKey, new ObjectAndDescription(sDescription, merl));
         }
         else if (sTokens[0].equals(ModelElementTable.class.toString())) {
            dao.put(sKey, new ObjectAndDescription(sDescription, new ModelElementTable()));
         }
         else if (sTokens[0].equals(ModelElement3DRasterLayer.class.toString())) {
            dao.put(sKey, new ObjectAndDescription(sDescription, new ModelElement3DRasterLayer()));
         }
         else if (sTokens[0].equals(ModelElementNumericalValue.class.toString())) {
            dao.put(sKey, new ObjectAndDescription(sDescription, new ModelElementNumericalValue()));
         }
         else if (sTokens[0].equals(ModelElementVectorLayer.class.toString())) {
            final ModelElementVectorLayer mevl = new ModelElementVectorLayer();
            mevl.setShapeType(Integer.parseInt(sTokens[1]));
            dao.put(sKey, new ObjectAndDescription(sDescription, mevl));
         }
      }

   }


   private static GeoAlgorithm getAlgorithm(final String sAlgorithmCommandLineName) {

      try {
         return Sextante.getAlgorithmFromCommandLineName(sAlgorithmCommandLineName).getNewInstance();
      }
      catch (final Exception e) {
         return null;
      }

   }


   public static GeoAlgorithm loadModelAsAlgorithm(final String sFilename) {

      final File file = new File(sFilename);
      try {
         if (file.getName().endsWith("model")) {
            final ArrayList inputKeys = new ArrayList();
            final HashMap dao = new HashMap();
            final HashMap coords = new HashMap();
            final ModelAlgorithm alg = open(file, dao, inputKeys, coords);
            if (alg != null) {
               alg.getInputs().clear();
               final Set set = dao.keySet();
               final Iterator iter = set.iterator();
               while (iter.hasNext()) {
                  final String sKey = (String) iter.next();
                  final ObjectAndDescription oad = (ObjectAndDescription) dao.get(sKey);
                  alg.getInputs().put(sKey, oad.getObject());
               }
            }
            return alg;
         }
         else {
            return null;
         }
      }
      catch (final Exception e) {
         return null;
      }

   }


   public static GeoAlgorithm[] loadModelsAsAlgorithms(final String modelsFolder) {

      int i;
      File folder;
      String[] contents = null;
      final ArrayList algsArray = new ArrayList();

      try {
         folder = new File(modelsFolder);
         contents = folder.list();
      }
      catch (final Exception e) {
         return new GeoAlgorithm[0];
      }

      if (contents != null) {
         for (i = 0; i < contents.length; i++) {
            final File file = new File(folder, contents[i]);
            try {
               if (file.getName().endsWith("model")) {
                  final ArrayList inputKeys = new ArrayList();
                  final HashMap dao = new HashMap();
                  final HashMap coords = new HashMap();
                  final ModelAlgorithm alg = open(file, dao, inputKeys, coords);
                  if (alg != null) {
                     alg.getInputs().clear();
                     final Set set = dao.keySet();
                     final Iterator iter = set.iterator();
                     while (iter.hasNext()) {
                        final String sKey = (String) iter.next();
                        final ObjectAndDescription oad = (ObjectAndDescription) dao.get(sKey);
                        alg.getInputs().put(sKey, oad.getObject());
                     }
                     algsArray.add(alg);
                  }
               }
            }
            catch (final Exception e) {
            };
         }

         final GeoAlgorithm[] algs = new GeoAlgorithm[algsArray.size()];
         for (i = 0; i < algsArray.size(); i++) {
            algs[i] = (GeoAlgorithm) algsArray.get(i);
         }

         return algs;
      }
      else {
         return new GeoAlgorithm[0];
      }
   }

}

package es.unex.sextante.gui.help;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputChart;
import es.unex.sextante.outputs.OutputNumericalValue;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputText;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class HelpIO {

   private static String encoding = "ISO-8859-1";
   private static String HELP     = "help";


   public static void save(final ArrayList elements,
                           final String sFilename) {

      try {
         final File file = new File(sFilename);
         if (!file.exists()) {
            final String dir = file.getParent();
            new File(dir).mkdir();
            file.createNewFile();
         }
         final Writer writer = new FileWriter(file);
         final KXmlSerializer serializer = new KXmlSerializer();
         serializer.setOutput(writer);
         serializer.startDocument(encoding, new Boolean(true));
         serializer.text("\n\t");
         serializer.startTag(null, HELP);
         for (int i = 0; i < elements.size(); i++) {
            final HelpElement element = (HelpElement) elements.get(i);
            element.serialize(serializer);
         }
         serializer.text("\n\t");
         serializer.endTag(null, HELP);
         serializer.text("\n");
         serializer.startDocument(encoding, new Boolean(true));
         writer.close();
      }
      catch (final IOException e) {
         e.printStackTrace();
      }

   }


   public static ArrayList open(final String sFilename) {

      ArrayList images = null;
      final ArrayList elements = new ArrayList();
      HelpElement element = null;
      final KXmlParser parser = new KXmlParser();

      try {
         final File file = new File(sFilename);
         parser.setInput(new FileInputStream(file), encoding);
         int tag = parser.nextTag();
         boolean bOut = false;

         if (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            while ((tag != XmlPullParser.END_DOCUMENT) && !bOut) {
               switch (tag) {
                  case XmlPullParser.START_TAG:
                     if (parser.getName().compareTo(HELP) == 0) {}
                     else if (parser.getName().compareTo(HelpElement.ELEMENT) == 0) {
                        images = new ArrayList();
                        final String sText = parser.getAttributeValue("", HelpElement.TEXT);
                        final String sName = parser.getAttributeValue("", HelpElement.NAME);
                        final String sDescription = parser.getAttributeValue("", HelpElement.DESCRIPTION);
                        final int iType = Integer.parseInt(parser.getAttributeValue("", HelpElement.TYPE));
                        element = new HelpElement();
                        element.setText(sText);
                        element.setName(sName);
                        element.setType(iType);
                        element.setDescription(sDescription);
                     }
                     else if (parser.getName().compareTo(ImageAndDescription.IMAGE) == 0) {
                        final ImageAndDescription iad = new ImageAndDescription();
                        final String sImageFilename = parser.getAttributeValue("", ImageAndDescription.FILE);
                        final String sDesc = parser.getAttributeValue("", ImageAndDescription.DESCRIPTION);
                        iad.setDescription(sDesc);
                        iad.setFilename(sImageFilename);
                        images.add(iad);
                     }
                     break;
                  case XmlPullParser.END_TAG:
                     if (parser.getName().compareTo(HELP) == 0) {
                        bOut = true;
                     }
                     else if (parser.getName().compareTo(HelpElement.ELEMENT) == 0) {
                        element.setImages(images);
                        elements.add(element);
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
         return null;
      }
      return elements;

   }


   /**
    * Returns the help associated with a given geoalgorithm as a html-formatted string
    * 
    * @param alg
    *                the geoalgorithm
    * @param sFilename
    *                the filename where help for the passed algorithm is stored
    * @return a html-formatted string with help for the given algorithm
    */

   public static String getHelpAsHTMLCode(final GeoAlgorithm alg,
                                          final String sFilename) {

      HelpElement element;
      final ArrayList list = open(sFilename);
      HashMap elements;
      final String sPath = "file:///" + sFilename.substring(0, sFilename.lastIndexOf(File.separator)) + File.separator;

      if (list != null) {
         elements = createMap(list);
      }
      else {
         elements = new HashMap();
      }

      final HTMLDoc doc = new HTMLDoc();

      doc.open(alg.getName());
      doc.addHeader(Sextante.getText(alg.getName()), 1);

      doc.addHeader(Sextante.getText("Description"), 2);

      element = (HelpElement) elements.get("DESCRIPTION");
      if (element != null) {
         doc.addParagraph(element.getTextAsFormattedHTML());
         for (int j = 0; j < element.getImages().size(); j++) {
            final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
            doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
         }
      }

      doc.addHeader(Sextante.getText("Parameters"), 2);

      final ParametersSet params = alg.getParameters();

      doc.startUnorderedList();
      for (int i = 0; i < params.getNumberOfParameters(); i++) {
         final Parameter param = params.getParameter(i);
         String sParam = param.getParameterDescription();
         sParam = "<b>" + sParam + "[" + getParameterTypeName(param) + "]: </b>";
         element = (HelpElement) elements.get(param.getParameterName());
         if (element != null) {
            sParam = sParam + element.getTextAsFormattedHTML();
         }
         doc.addListElement(sParam);
         if (element != null) {
            for (int j = 0; j < element.getImages().size(); j++) {
               final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
               doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
            }
         }

      }

      doc.closeUnorderedList();

      doc.addHeader(Sextante.getText("Outputs"), 2);

      element = (HelpElement) elements.get("OUTPUT_DESCRIPTION");
      if (element != null) {
         doc.addParagraph(element.getTextAsFormattedHTML());
         for (int j = 0; j < element.getImages().size(); j++) {
            final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
            doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
         }
      }

      doc.startUnorderedList();
      final OutputObjectsSet oo = alg.getOutputObjects();
      String sOutputType = "";
      for (int i = 0; i < oo.getOutputObjectsCount(); i++) {
         final Output out = oo.getOutput(i);
         String sOutput = out.getDescription();
         if (out instanceof OutputRasterLayer) {
            sOutputType = Sextante.getText("Raster_Layer");
         }
         else if (out instanceof Output3DRasterLayer) {
            sOutputType = Sextante.getText("3D_Raster_layer");
         }
         else if (out instanceof OutputVectorLayer) {
            sOutputType = Sextante.getText("Vector_Layer");
            final OutputVectorLayer ovl = (OutputVectorLayer) out;
            switch (ovl.getShapeType()) {
               case OutputVectorLayer.SHAPE_TYPE_UNDEFINED:
               default:
                  sOutputType = sOutputType + " - " + Sextante.getText("Any_type");
                  break;
               case OutputVectorLayer.SHAPE_TYPE_LINE:
                  sOutputType = sOutputType + " - " + Sextante.getText("Line");
                  break;
               case OutputVectorLayer.SHAPE_TYPE_POLYGON:
                  sOutputType = sOutputType + " - " + Sextante.getText("Polygon");
                  break;
               case OutputVectorLayer.SHAPE_TYPE_POINT:
                  sOutputType = sOutputType + " - " + Sextante.getText("Point");
                  break;
            }
         }
         else if (out instanceof OutputTable) {
            sOutputType = Sextante.getText("Table");
         }
         else if (out instanceof OutputChart) {
            sOutputType = Sextante.getText("graph-chart");
         }
         else if (out instanceof OutputText) {
            sOutputType = Sextante.getText("Text");
         }
         else if (out instanceof OutputNumericalValue) {
            sOutputType = Sextante.getText("Numerical_value");
         }
         sOutput = "<b>" + sOutput + "[" + sOutputType + "]: </b>";
         element = (HelpElement) elements.get(out.getName());
         if (element != null) {
            sOutput = sOutput + element.getTextAsFormattedHTML();
         }
         doc.addListElement(sOutput);
         if (element != null) {
            for (int j = 0; j < element.getImages().size(); j++) {
               final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
               doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
            }
         }

      }
      doc.closeUnorderedList();

      doc.addHeader(Sextante.getText("Additional_information"), 2);

      element = (HelpElement) elements.get("ADDITIONAL_INFO");
      if (element != null) {
         doc.addParagraph(element.getTextAsFormattedHTML());
         for (int j = 0; j < element.getImages().size(); j++) {
            final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
            doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
         }
      }

      doc.addHeader(Sextante.getText("Command_line"), 2);

      String sText = alg.getCommandLineHelp();
      sText = sText.replaceAll("\n", "<br>");
      sText = sText.replace("   ", " &nbsp ");
      doc.addCourierText(sText);

      doc.addParagraph("");

      element = (HelpElement) elements.get("EXTENSION_AUTHOR");
      if (element != null) {
         doc.addParagraph("<i>" + Sextante.getText("Algorithm_created_by") + " " + element.getText() + "</i>");
         for (int j = 0; j < element.getImages().size(); j++) {
            final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
            doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
         }
      }

      element = (HelpElement) elements.get("HELP_AUTHOR");
      if (element != null) {
         doc.addParagraph("<i>" + Sextante.getText("Help_file_created_by") + " " + element.getText() + "</i>");
         for (int j = 0; j < element.getImages().size(); j++) {
            final ImageAndDescription iad = (ImageAndDescription) element.getImages().get(j);
            doc.addImageAndDescription(sPath + iad.getFilename(), iad.getDescription());
         }
      }

      doc.close();

      return doc.getHTMLCode();

   }


   private static String getParameterTypeName(final Parameter param) {

      String s = Sextante.getText(param.getParameterTypeName().replace(' ', '_'));

      if (param instanceof ParameterVectorLayer) {
         try {
            final AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
            switch (ai.getShapeType()) {
               case AdditionalInfoVectorLayer.SHAPE_TYPE_ANY:
               default:
                  s = s + " - " + Sextante.getText("Any_type");
                  break;
               case AdditionalInfoVectorLayer.SHAPE_TYPE_LINE:
                  s = s + " - " + Sextante.getText("Line");
                  break;
               case AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON:
                  s = s + " - " + Sextante.getText("Polygon");
                  break;
               case AdditionalInfoVectorLayer.SHAPE_TYPE_POINT:
                  s = s + " - " + Sextante.getText("Point");
                  break;
            }
         }
         catch (final NullParameterAdditionalInfoException e) {}
      }

      return s;

   }


   public static HashMap createMap(final ArrayList list) {

      final HashMap map = new HashMap();

      for (int i = 0; i < list.size(); i++) {
         final HelpElement element = (HelpElement) list.get(i);
         map.put(element.getName(), element);
      }

      return map;

   }


   /**
    * Returns true if the help file associated with an algorithm contains a given search string
    * 
    * @param alg
    *                the GeoAlgorithm
    * @param string
    *                a string to search
    * @return true if the help file associated with the algorithm contains the search string
    */
   public static boolean containsStringInHelpFile(final GeoAlgorithm alg,
                                                  final String string) {

      String line;

      BufferedReader br = null;
      InputStreamReader is = null;
      FileInputStream fis = null;

      final String sName = alg.getName().toLowerCase();
      if (sName.indexOf(string) != -1) {
         return true;
      }

      try {
         final String sFilename = SextanteGUI.getAlgorithmHelpFilename(alg, false);
         fis = new FileInputStream(sFilename);
         is = new InputStreamReader(fis);
         br = new BufferedReader(is);

         while (null != (line = br.readLine())) {
            line = line.toLowerCase();
            if (line.indexOf(string) != -1) {
               br.close();
               is.close();
               fis.close();
               return true;
            }
         }
      }
      catch (final Exception e) {
         //Sextante.addErrorToLog(e);
      }
      finally {
         try {
            br.close();
            is.close();
            fis.close();
         }
         catch (final Exception e) {}
      }

      return false;

   }


   /**
    * Returns the help filename for a given algorithm
    * 
    * @param alg
    *                the GeoAlgorithm
    * @param bForceCurrentLocale
    *                if true, returns the path to the current locale, even if it does not exist. If false, it will return the path
    *                corresponding to the default locale (english) in case the one corresponding to the current locale does not
    *                exist.
    * 
    * @return the help filename for a given algorithm
    */
   //   public static String getHelpFilename(final GeoAlgorithm alg,
   //                                        final boolean bForceCurrentLocale) {
   //
   //      String sPath;
   //      String sFilename;
   //
   //      if (alg instanceof ModelAlgorithm) {
   //         final String sModelFilename = ((ModelAlgorithm) alg).getFilename();
   //         sFilename = sModelFilename.substring(sModelFilename.lastIndexOf(File.separator));
   //         sFilename = sFilename + ".xml";
   //         sPath = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
   //      }
   //      else if (alg instanceof GrassAlgorithm) {
   //         sFilename = alg.getName() + ".html";
   //         if (sFilename.contains("(")) {
   //            //GRASS algorithm siblings
   //            sFilename = sFilename.substring(0, sFilename.indexOf("(") - 1) + ".html";
   //         }
   //         sPath = SextanteGUI.getGrassFolder() + File.separator + "docs" + File.separator + "html";
   //      }
   //      else {
   //         sFilename = alg.getCommandLineName() + ".xml";
   //         sPath = getHelpPath(alg, bForceCurrentLocale);
   //      }
   //
   //      return sPath + File.separator + sFilename;
   //
   //   }
   /**
    * Returns the path where help files for a given algorithm are found
    * 
    * @param alg
    *                the GeoAlgorithm
    * @param bForceCurrentLocale
    *                if true, returns the path to the current locale, even if it does not exist. If false, it will return the path
    *                corresponding to the default locale (english) in case the one corresponding to the current locale does not
    *                exist.
    * @return the help path for this algorithm
    */
   public static String getHelpPath(final GeoAlgorithm alg,
                                    final boolean bForceLocale) {

      String sPackage = alg.getClass().getPackage().toString();
      sPackage = sPackage.substring(8);
      String sPath = SextanteGUI.getHelpPath() + File.separator + Locale.getDefault().getLanguage() + File.separator + sPackage;

      final File dir = new File(sPath);
      if (!dir.exists() && !bForceLocale) {
         sPath = SextanteGUI.getHelpPath() + File.separator + Locale.ENGLISH.getLanguage() + File.separator + sPackage;
      }

      return sPath;

   }


   public static String getHelpFile(final String sTopic) {

      String sPath = SextanteGUI.getHelpPath() + File.separator + Locale.getDefault().getLanguage();

      final File dir = new File(sPath);
      if (!dir.exists()) {
         sPath = SextanteGUI.getHelpPath() + File.separator + Locale.ENGLISH.getLanguage();
      }
      return sPath + File.separator + "general" + File.separator + sTopic + ".html";

   }

}

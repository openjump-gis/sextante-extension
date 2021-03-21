package es.unex.sextante.other.localiza;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class LocalizaAlgorithm
         extends
            GeoAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName("localiza");
      setGroup("localiza");

      try {
         m_Parameters.addFilepath("FILEPATH", "filepath", true, false, "txt");
         addOutputVectorLayer("LAYER", "Resultado");
      }
      catch (final RepeatedParameterNameException e) {
         e.printStackTrace();
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final GeometryFactory gf = new GeometryFactory();
      final String sFolder = m_Parameters.getParameterValueAsString("FILEPATH");
      final HashMap<String, Point> map = new HashMap<String, Point>();
      boolean bFirstLine = true;
      String sID = null;
      final Class[] types = new Class[] { String.class, String.class };
      final String[] sFields = new String[] { "ID", "Población" };
      final IVectorLayer layer = getNewVectorLayer("LAYER", "Resultado", OutputVectorLayer.SHAPE_TYPE_POINT, types, sFields);

      BufferedReader input;
      try {
         input = new BufferedReader(new FileReader(sFolder + File.separator + "SECCION.txt"));
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            final String[] sTokens = sLine.split(" ");
            if (bFirstLine) {
               sID = sTokens[0];
            }
            else {
               final Point pt = gf.createPoint(new Coordinate(Double.parseDouble(sTokens[1]), Double.parseDouble(sTokens[3])));
               map.put(sID, pt);
            }
            bFirstLine = !bFirstLine;
         }
         input.close();

         input = new BufferedReader(new FileReader(sFolder + File.separator + "poblacion.txt"));
         while ((sLine = input.readLine()) != null) {
            final String[] sTokens = sLine.split(" ");
            sID = sLine.substring(0, 3).trim();
            final String sPop = sLine.substring(4).trim();
            layer.addFeature(map.get(sID), new Object[] { sID, sPop });
         }
         input.close();
      }
      catch (final IOException e) {
         e.printStackTrace();
      }


      return true;
   }

}

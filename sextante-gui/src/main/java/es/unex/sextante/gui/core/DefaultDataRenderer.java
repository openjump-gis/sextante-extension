

package es.unex.sextante.gui.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;


public abstract class DefaultDataRenderer
         implements
            IDataRenderer {

   private HashMap<String, HashMap<String, Object>> m_Map = new HashMap<String, HashMap<String, Object>>();
   private final String                             m_sFilename;


   public DefaultDataRenderer(final String sFile) {

      m_sFilename = sFile;

   }


   public Object getRenderingForLayer(final String sAlgCmdName,
                                      final String sOutputLayer) {

      final HashMap<String, Object> algRenderer = m_Map.get(sAlgCmdName);
      if (algRenderer != null) {
         return algRenderer.get(sOutputLayer);
      }
      else {
         return null;
      }

   }


   public void open() {
      try {
         if (!new File(m_sFilename).exists()) {
            return;
         }
         final FileInputStream fis = new FileInputStream(m_sFilename);
         final ObjectInputStream ois = new ObjectInputStream(fis);
         m_Map = (HashMap<String, HashMap<String, Object>>) ois.readObject();
         ois.close();
         fis.close();
      }
      catch (final Exception e) {
         e.printStackTrace();
      }

   }


   public void save() {

      try {
         final FileOutputStream fileOut = new FileOutputStream(m_sFilename);
         final ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(m_Map);
         out.close();
         fileOut.close();
      }
      catch (final IOException e) {
         e.printStackTrace();
      }


   }


   public void setRenderingForAlgorithm(final String algCmdName,
                                        final HashMap<String, Object> renderingData) {

      m_Map.put(algCmdName, renderingData);

   }

}

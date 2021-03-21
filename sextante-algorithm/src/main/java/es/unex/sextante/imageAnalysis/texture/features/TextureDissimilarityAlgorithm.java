package es.unex.sextante.imageAnalysis.texture.features;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.imageAnalysis.texture.base.BaseTextureAnalysisAlgorithm;

public class TextureDissimilarityAlgorithm
         extends
            BaseTextureAnalysisAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      this.setName(Sextante.getText("Texture_Dissimilarity"));

   }


   @Override
   protected double getTextureFeature() {

      double dDissimilarity = 0;
      for (int i = 0; i < GRAYSCALE_LEVELS; i++) {
         for (int j = 0; j < GRAYSCALE_LEVELS; j++) {
            dDissimilarity += (m_GLCM[i][j] * Math.abs(i - j));
         }
      }

      return dDissimilarity;

   }

}

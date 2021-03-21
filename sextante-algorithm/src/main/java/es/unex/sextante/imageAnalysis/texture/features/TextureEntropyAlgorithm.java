package es.unex.sextante.imageAnalysis.texture.features;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.imageAnalysis.texture.base.BaseTextureAnalysisAlgorithm;

public class TextureEntropyAlgorithm
         extends
            BaseTextureAnalysisAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      this.setName(Sextante.getText("Texture_Entropy"));

   }


   @Override
   protected double getTextureFeature() {

      double dEntropy = 0;
      for (int i = 0; i < GRAYSCALE_LEVELS; i++) {
         for (int j = 0; j < GRAYSCALE_LEVELS; j++) {
            dEntropy += (m_GLCM[i][j] * -1 * Math.log(m_GLCM[i][j]));
         }
      }

      return dEntropy;

   }

}

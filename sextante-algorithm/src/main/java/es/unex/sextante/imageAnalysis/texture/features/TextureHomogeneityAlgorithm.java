package es.unex.sextante.imageAnalysis.texture.features;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.imageAnalysis.texture.base.BaseTextureAnalysisAlgorithm;

public class TextureHomogeneityAlgorithm
         extends
            BaseTextureAnalysisAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      this.setName(Sextante.getText("Texture_Homogeneity"));

   }


   @Override
   protected double getTextureFeature() {

      double dHomogeneity = 0;
      for (int i = 0; i < GRAYSCALE_LEVELS; i++) {
         for (int j = 0; j < GRAYSCALE_LEVELS; j++) {
            dHomogeneity += (m_GLCM[i][j] / (1 - (i - j) * (i - j)));
         }
      }

      return dHomogeneity;

   }

}

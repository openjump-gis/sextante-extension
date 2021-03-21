package es.unex.sextante.imageAnalysis.texture.features;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.imageAnalysis.texture.base.BaseTextureAnalysisAlgorithm;

public class TextureContrastAlgorithm
         extends
            BaseTextureAnalysisAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      this.setName(Sextante.getText("Texture_Contrast"));

   }


   @Override
   protected double getTextureFeature() {

      double dContrast = 0;
      for (int i = 0; i < GRAYSCALE_LEVELS; i++) {
         for (int j = 0; j < GRAYSCALE_LEVELS; j++) {
            dContrast += (m_GLCM[i][j] * (i - j) * (i - j));
         }
      }

      return dContrast;

   }

}

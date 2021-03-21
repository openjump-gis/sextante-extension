package es.unex.sextante.imageAnalysis.texture.features;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.imageAnalysis.texture.base.BaseTextureAnalysisAlgorithm;

public class TextureASMAlgorithm
         extends
            BaseTextureAnalysisAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      this.setName(Sextante.getText("Texture_ASM"));

   }


   @Override
   protected double getTextureFeature() {

      double dASM = 0;
      for (int i = 0; i < GRAYSCALE_LEVELS; i++) {
         for (int j = 0; j < GRAYSCALE_LEVELS; j++) {
            dASM += (m_GLCM[i][j] * m_GLCM[i][j]);
         }
      }

      return dASM;

   }

}

package es.unex.sextante.gui.core;

import es.unex.sextante.core.GeoAlgorithm;

public interface IToolboxRightButtonAction {

   public void execute(GeoAlgorithm alg);


   public String getDescription();


   public boolean canBeExecutedOnAlgorithm(GeoAlgorithm alg);

}

package es.unex.sextante.gui.algorithm;

import javax.swing.JPanel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.gui.exceptions.LayerCannotBeOverwrittenException;
import es.unex.sextante.gui.exceptions.OverwrittingNotAllowedException;

/**
 * An abstract class to extend by all parameter panels that take parameter values from the user for a geoalgorithm. If your
 * geoalgorithm needs a more elaborated panel than the default one, extend this class naming the derived class with the name of
 * the algorithm and adding the suffix "ParametersPanel".
 * 
 * For instance, for an algorithm named RasterOperationAlgorithm, you should create a class named RasterOperationParametersPanel.
 * SEXTANTE will look for that class and if it cannot find it, it will use the default parameters panel instead.
 * 
 */

public abstract class GeoAlgorithmParametersPanel
         extends
            JPanel {

   /**
    * Inits the panel with the needs of a given algorithm
    * 
    * @param algorithm
    *                the geoalgorithm
    */
   public abstract void init(GeoAlgorithm algorithm);


   /**
    * Assigns the parameters entered by the user to the algorithm
    * 
    */
   public abstract void assignParameters() throws WrongInputException, OverwrittingNotAllowedException,
                                          LayerCannotBeOverwrittenException;


   /**
    * Sets the value of a parameter in the panel, if possible. This should be called to set predefined values or values previously
    * used, so it is easier for the user to fill the required fields.
    * 
    * @param sParameterName
    *                The name of the parameter to set
    * @param sValue
    *                the value to set, expressed as the corresponding command line argument
    */
   public abstract void setParameterValue(String sParameterName,
                                          String sValue);


   /**
    * Sets the value of an output object in the panel, if possible. This should be called to set predefined values or values
    * previously used, so it is easier for the user to fill the required fields.
    * 
    * @param sOutputName
    *                The name of the output object to set
    * @param sValue
    *                the value to set, expressed as the corresponding command line argument
    */
   public abstract void setOutputValue(String sOutputName,
                                       String sValue);

}

package es.unex.sextante.gui.core;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.gui.settings.Setting;

public interface IAlgorithmProvider {

   /**
    * Performs the initialization needed to load the corresponding algorithm into the algorithm provider
    */
   public void initialize();


   /**
    * Returns the name of the algorithm provider. This will be used as the group name in the toolbox
    * 
    * @return
    */
   public String getName();


   /**
    * Returns a map with algorithm command-line names as keys and geoalgorithms as values
    * 
    * @return a map with algorithm command-line names as keys and geoalgorithms as values
    */
   public HashMap<String, GeoAlgorithm> getAlgorithms();


   /**
    * Returns a map of custom parameter panels. Keys are algorithm command-line names, and values are the corresponding panel
    * classes
    * 
    * @return a map of custom parameter panels.
    */
   public HashMap<String, Class> getCustomModelerParameterPanels();


   /**
    * Returns a map of custom modeler parameter panels. Keys are algorithm command-line names, and values are the corresponding
    * panel classes
    * 
    * @return a map of custom modeler parameter panels.
    */
   public HashMap<String, Class> getCustomParameterPanels();


   /**
    * Returns the icon to use for the algorithms provided by this provider
    * 
    * @return the icon to use for the algorithms provided by this provider
    */
   public ImageIcon getIcon();


   /**
    * Returns the settings object corresponding to this provider
    * 
    * @return the settings object corresponding to this provider
    */
   public Setting getSettings();


   /**
    * Updates the list of algorithms in this provider
    */
   public void update();


   /**
    * 
    * Returns the help associated with the passed algorithm as a HTML string or a URL
    * 
    * @param alg
    *                the algorithm
    * @return the help associated with the passed algorithm as a HTML string or a URL
    */
   public Object getAlgorithmHelp(GeoAlgorithm alg);


   /**
    * Returns the filename where the help for a given algorithm is stored
    * 
    * @param alg
    *                the algorithm
    * @param bForceCurrentLocale
    *                if true it will return the current locale folder, even if the help file does not exists
    * @return
    */
   public String getAlgorithmHelpFilename(GeoAlgorithm alg,
                                          boolean bForceCurrentLocale);


   /**
    * Returns true if the help files associated with the algorithms from this provider can be edited by the user
    * 
    * @return true if the help files associated with the algorithms from this provider can be edited by the user
    */
   public boolean canEditHelp();


   /**
    * Returns the toolbox actions to be added to the toolbox by this algorithm provider.
    * 
    * @return A Map containing toolbox actions to be added to the toolbox by this algorithm provider
    */
   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions();


   /**
    * Return the right button actions to be added to the toolbox by this algorithm provider. These actions should work on the
    * particular type of algorithms provided by the provider
    * 
    * @return the right button actions to be added to the toolbox by this algorithm provider.
    */
   public IToolboxRightButtonAction[] getToolboxRightButtonActions();


}

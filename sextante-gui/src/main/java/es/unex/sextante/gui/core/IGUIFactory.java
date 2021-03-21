

package es.unex.sextante.gui.core;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JDialog;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.gui.exceptions.WrongViewNameException;
import es.unex.sextante.gui.history.DateAndCommand;
import es.unex.sextante.gui.modeler.GeoAlgorithmModelerParametersPanel;
import es.unex.sextante.gui.modeler.ModelAlgorithm;
import es.unex.sextante.gui.toolbox.ToolboxPanel;


/**
 * a GUI factory implements method to show SEXTANTE elements, adapted to the particular framework being used, and the graphical
 * characteristics of the application SEXTANTE is running onto.
 * 
 * @author volaya
 * 
 */
public interface IGUIFactory {


   public static final int OK     = 1;
   public static final int CANCEL = 0;


   /**
    * Shows the SEXTANTE toolbox
    */
   public void showToolBoxDialog();


   /**
    * Shows the dialog for the specified algorithm
    * 
    * @param alg
    *                a GeoAlgorithm
    * @param parent
    *                the parent dialog (usually the toolbox). If null, the main frame is used
    * @param command
    *                a list of previous commands to use instead of all the ones available in the history. If null, all commands
    *                from the history are used
    * @return GUIFactory.OK if the user accepted the execution of the algorithm. GUIFactory.CANCEL if he canceled or the dialog
    *         was not shown due to lack of input data
    */
   public int showAlgorithmDialog(GeoAlgorithm alg,
                                  JDialog parent,
                                  ArrayList<DateAndCommand> command);


   /**
    * Shows the dialog to add the selected algorithm to a model using the graphical modeler
    * 
    * @param algorithm
    *                a GeoAlgorithm
    * @param sName
    *                the name of the algorithm
    * @param sDescription
    *                the description of the algorithm
    * @param modelAlgorithm
    *                the model to add the algorithm to
    * @param dataObjects
    *                the set of data objects currently in the model
    * @param parent
    *                the parent dialog
    * @return GUIFactory.OK if the user accepted the addition of the algorithm. GUIFactory.CANCEL if he canceled or the dialog was
    *         not shown due to lack of input data
    */
   public int showAlgorithmDialogForModeler(GeoAlgorithm algorithm,
                                            String sName,
                                            String sDescription,
                                            ModelAlgorithm modelAlgorithm,
                                            HashMap dataObjects,
                                            JDialog parent);


   /**
    * Show the settings dialog
    * 
    * @param panel
    *                The toolbox panel
    * @param parent
    *                the parent dialog from which this method was invoked
    */
   public void showSettingsDialog(ToolboxPanel panel,
                                  JDialog parent);


   /**
    * Shows the modeler dialog
    */
   public void showModelerDialog();


   /**
    * Opens a model and shows the modeler dialog.
    * 
    * @param alg
    *                the model. it will be reopened from its filename, that meaning that it must have been saved before opening.
    *                If it has not been save and its filename is null, the dialog will open with no model.
    */
   public void showModelerDialog(ModelAlgorithm alg);


   /**
    * Shows the help editing dialog for the specified algorithm
    * 
    * @param alg
    *                the algorithm
    */
   public void showHelpEditionDialog(GeoAlgorithm alg);


   /**
    * Shows a dialog containing help associated with the specified algorithm
    * 
    * @param algorithm
    *                the algorithm
    */
   public void showHelpDialog(GeoAlgorithm algorithm);


   /**
    * Shows a dialog containing the help associated with the passed topic
    * 
    * @param sTopic
    * 
    */
   public void showHelpDialog(String sTopic);


   /**
    * Shows the help manager window
    */
   public void showHelpWindow();


   /**
    * Shows the additional results dialog, only if there is at least one additional result
    * 
    * @param components
    *                a list of components representing additional
    */
   public void showAdditionalResultsDialog(ArrayList components);


   /**
    * Shows the data-explorer dialog
    */
   public void showDataExplorer();


   /**
    * Shows the history dialog
    */
   public void showHistoryDialog();


   /**
    * Shows the command-line dialog
    */
   public void showCommandLineDialog();


   /**
    * Shows a simple dialog with the specified component as its only content
    * 
    * @param component
    *                the component to add to the dialog
    * @param text
    *                the title of the dialog
    */
   public void showGenericInfoDialog(Component component,
                                     String text);


   /**
    * Shows the batch processing dialog for the specified algorithm
    * 
    * @param alg
    *                The algorithm
    * @param parent
    *                the parent dialog (usually the toolbox)
    */
   public void showBatchProcessingDialog(GeoAlgorithm alg,
                                         JDialog parent);


   /**
    * Shows the batch processing dialog using data from the GIS interface (not from files)for the specified algorithm
    * 
    * @param alg
    *                The algorithm
    * @param parent
    *                the parent dialog (usually the toolbox)
    */
   public void showBatchProcessingFromGISDialog(GeoAlgorithm alg,
                                                JDialog parent);


   /**
    * If the toolbox is visible, it updates its list of algorithms
    */
   public void updateToolbox();


   /**
    * If the history is visible, it updates its content. This should be called from the history component itself when an algorithm
    * is executed from it, to update it with the results of that algorithm.
    */
   public void updateHistory();


   /**
    * Returns the list of predefined coordinates to be used as input for algorithms that require coordinate pairs.
    */
   public ArrayList<NamedPoint> getCoordinatesList();


   /**
    * Returns the Class of the default parameters panel for algorithms
    * 
    * @return the class of the default parameters panel
    */
   public Class getDefaultParametersPanel();


   /**
    * Returns the class of the default parameters panel for algorithms when executed from the modeler interface
    * 
    * @return the class of the default parameters panel
    */
   public Class<? extends GeoAlgorithmModelerParametersPanel> getDefaultModelerParametersPanel();


   /**
    * adds a data object (layer) to a view designated by a given name
    * 
    * @param obj
    * @param sViewName
    * @throws WrongViewNameException
    */
   public void addToView(IDataObject obj,
                         String sViewName) throws WrongViewNameException;


   /**
    * Returns toolbox actions specific of this GUI (that is, of this application)
    * 
    * @return toolbox actions specific of this GUI
    */
   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions();


   public void showGenericDialog(String sName,
                                 Component component);


}

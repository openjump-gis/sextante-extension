package es.unex.sextante.gui.modeler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;

import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.modeler.elements.ModelElementRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementTable;
import es.unex.sextante.modeler.elements.ModelElementVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterDataObject;

/**
 * An abstract class to extend by all parameter panels that take parameter values from the user for a geoalgorithm to be added to
 * a model
 * 
 * @author volaya
 * 
 */
public abstract class GeoAlgorithmModelerParametersPanel
         extends
            JPanel {

   protected HashMap        m_DataObjects;
   protected GeoAlgorithm   m_Algorithm;
   protected String         m_sAlgorithmName;
   protected String         m_sAlgorithmDescription;
   protected ModelAlgorithm m_GlobalAlgorithm;
   protected int            m_iInnerParameter;


   /**
    * inits the panel
    * 
    * @param panel
    *                the algorithm dialog that contains this panel
    */
   public void init(final AlgorithmDialog panel) {

      m_Algorithm = panel.getAlgorithm();
      m_GlobalAlgorithm = panel.getModelAlgorithm();
      m_DataObjects = panel.getDataObjects();
      m_sAlgorithmName = panel.getAlgorithmName();
      m_sAlgorithmDescription = panel.getAlgorithmDescription();
      m_iInnerParameter = 0;

      initGUI();

   }


   /**
    * Constructs the GUI
    */
   protected abstract void initGUI();


   /**
    * adds the algorithm to the model
    * 
    * @return true if the parameters entered by the user are correct and sufficient
    */
   public abstract boolean assignParameters(HashMap map);


   protected Object getParameterValue(final Parameter param) {

      final String sKey = m_GlobalAlgorithm.getInputAsignment(param.getParameterName(), m_Algorithm);
      return m_DataObjects.get(sKey);

   }


   protected ObjectAndDescription[] getElementsOfClass(final Class classOfInput,
                                                       final boolean bIncludeOptionalInputs) {

      String sKey;
      ObjectAndDescription oad;
      final ArrayList objects = new ArrayList();
      Parameter param;
      AdditionalInfoDataObject additionalInfo;
      final ParametersSet ps = m_GlobalAlgorithm.getParameters();;
      final Set set = m_DataObjects.keySet();
      final Iterator iter = set.iterator();
      while (iter.hasNext()) {
         sKey = (String) iter.next();
         oad = (ObjectAndDescription) m_DataObjects.get(sKey);
         if (oad.getObject().getClass().equals(classOfInput)) {
            try {
               param = ps.getParameter(sKey);
               if (param instanceof ParameterDataObject) {
                  additionalInfo = (AdditionalInfoDataObject) ((ParameterDataObject) param).getParameterAdditionalInfo();
                  if (bIncludeOptionalInputs || additionalInfo.getIsMandatory()) {
                     if (!isParameterProducedByThisProcess(sKey) && !dependsOnThisProcess(sKey)) {
                        objects.add(new ObjectAndDescription(oad.getDescription(), sKey));
                     }
                  }
               }
               else {
                  if (!isParameterProducedByThisProcess(sKey) && !dependsOnThisProcess(sKey)) {
                     objects.add(new ObjectAndDescription(oad.getDescription(), sKey));
                  }
               }

            }
            catch (final WrongParameterIDException e) {
               if (!isParameterProducedByThisProcess(sKey) && !dependsOnThisProcess(sKey)) {
                  objects.add(new ObjectAndDescription(oad.getDescription(), sKey));
               }
            }
            catch (final NullParameterAdditionalInfoException e) {
               Sextante.addErrorToLog(e);
            }
         }
      }

      final ObjectAndDescription[] ret = new ObjectAndDescription[objects.size()];
      for (int i = 0; i < objects.size(); i++) {
         ret[i] = (ObjectAndDescription) objects.get(i);
      }

      Arrays.sort(ret);

      return ret;

   }


   private boolean dependsOnThisProcess(final String sKey) {

      int i;
      final ArrayList algorithmKeys = m_GlobalAlgorithm.getAlgorithmKeys();
      String sAlgKey;

      final Object obj = getObjectFromKey(sKey);

      if (obj == null) {
         return false;
      }
      else {
         if (obj instanceof GeoAlgorithm) {
            final HashMap assignments = m_GlobalAlgorithm.getInputAssignments(sKey);
            final Set set = assignments.keySet();
            final Iterator iter = set.iterator();
            String sAssignmentKey;
            String sAssignment;
            while (iter.hasNext()) {
               sAssignmentKey = (String) iter.next();
               sAssignment = (String) assignments.get(sAssignmentKey);
               if (dependsOnThisProcess(sAssignment)) {
                  return true;
               }
            }
         }
         else if (obj instanceof ObjectAndDescription) {
            final Class clazz = ((ObjectAndDescription) obj).getObject().getClass();
            if (clazz.equals(ModelElementRasterLayer.class) || clazz.equals(ModelElementVectorLayer.class)
                || clazz.equals(ModelElementTable.class)) {
               if (isProducedBy(sKey, m_sAlgorithmName)) {
                  return true;
               }
               for (i = 0; i < algorithmKeys.size(); i++) {
                  sAlgKey = (String) algorithmKeys.get(i);
                  if (isProducedBy(sKey, sAlgKey)) {
                     if (dependsOnThisProcess(sAlgKey)) {
                        return true;
                     }
                  }

               }
            }
            //T**********
         }
      }

      return false;

   }


   public Object getObjectFromKey(final String sObjectKey) {

      int i;
      String sKey;
      final Object obj = m_DataObjects.get(sObjectKey);

      if (obj != null) {
         return obj;
      }

      final ArrayList algKeys = m_GlobalAlgorithm.getAlgorithmKeys();

      for (i = 0; i < algKeys.size(); i++) {
         sKey = (String) algKeys.get(i);
         if (sKey.equals(sObjectKey)) {
            return m_GlobalAlgorithm.getAlgorithm(sKey);
         }
      }

      return null;

   }


   private boolean isParameterProducedByThisProcess(final String sKey) {

      return sKey.endsWith(m_sAlgorithmName);

   }


   private boolean isProducedBy(final String sParamKey,
                                final String sProccesKey) {

      return sParamKey.endsWith(sProccesKey);

   }


   protected String getInnerParameterKey() {

      return ("INNERPARAM" + Integer.toString(m_iInnerParameter++) + m_sAlgorithmName);

   }

}

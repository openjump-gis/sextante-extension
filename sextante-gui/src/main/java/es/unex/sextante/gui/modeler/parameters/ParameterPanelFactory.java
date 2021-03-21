package es.unex.sextante.gui.modeler.parameters;

import javax.swing.JDialog;

import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class ParameterPanelFactory {

   public static ParameterPanel getParameterPanel(final Parameter parameter,
                                                  final ModelerPanel modelerPanel,
                                                  final JDialog parent) {

      if (parameter instanceof ParameterBand) {
         return getNewRasterBandPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterBoolean) {
         return getNewBooleanPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterFixedTable) {
         return getNewFixedTablePanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterMultipleInput) {
         return getNewMultipleInputPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterNumericalValue) {
         return getNewNumericalValuePanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterPoint) {
         return getNewPointPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterRasterLayer) {
         return getNewRasterLayerPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterVectorLayer) {
         return getNewVectorLayerPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterTable) {
         return getNewTablePanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterTableField) {
         return getNewTableFieldPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterString) {
         return getNewStringPanel(modelerPanel, parent);
      }
      if (parameter instanceof ParameterSelection) {
         return getNewSelectionPanel(modelerPanel, parent);
      }
      else {
         return null;
      }

   }


   private static ParameterPanel getNewNumericalValuePanel(final ModelerPanel modelerPanel,
                                                           final JDialog parent) {

      if (parent != null) {
         return new NumericalValuePanel(parent, modelerPanel);
      }
      else {
         return new NumericalValuePanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewBooleanPanel(final ModelerPanel modelerPanel,
                                                    final JDialog parent) {

      if (parent != null) {
         return new BooleanPanel(parent, modelerPanel);
      }
      else {
         return new BooleanPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewRasterLayerPanel(final ModelerPanel modelerPanel,
                                                        final JDialog parent) {

      if (parent != null) {
         return new RasterLayerPanel(parent, modelerPanel);
      }
      else {
         return new RasterLayerPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewRasterBandPanel(final ModelerPanel modelerPanel,
                                                       final JDialog parent) {

      if (parent != null) {
         return new RasterBandPanel(parent, modelerPanel);
      }
      else {
         return new RasterBandPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewVectorLayerPanel(final ModelerPanel modelerPanel,
                                                        final JDialog parent) {

      if (parent != null) {
         return new VectorLayerPanel(parent, modelerPanel);
      }
      else {
         return new VectorLayerPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewStringPanel(final ModelerPanel modelerPanel,
                                                   final JDialog parent) {

      if (parent != null) {
         return new StringPanel(parent, modelerPanel);
      }
      else {
         return new StringPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewTableFieldPanel(final ModelerPanel modelerPanel,
                                                       final JDialog parent) {

      if (parent != null) {
         return new TableFieldPanel(parent, modelerPanel);
      }
      else {
         return new TableFieldPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewTablePanel(final ModelerPanel modelerPanel,
                                                  final JDialog parent) {

      if (parent != null) {
         return new TablePanel(parent, modelerPanel);
      }
      else {
         return new TablePanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewPointPanel(final ModelerPanel modelerPanel,
                                                  final JDialog parent) {

      if (parent != null) {
         return new PointPanel(parent, modelerPanel);
      }
      else {
         return new PointPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewFixedTablePanel(final ModelerPanel modelerPanel,
                                                       final JDialog parent) {

      if (parent != null) {
         return new FixedTablePanel(parent, modelerPanel);
      }
      else {
         return new FixedTablePanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewSelectionPanel(final ModelerPanel modelerPanel,
                                                      final JDialog parent) {

      if (parent != null) {
         return new SelectionPanel(parent, modelerPanel);
      }
      else {
         return new SelectionPanel(modelerPanel);
      }

   }


   private static ParameterPanel getNewMultipleInputPanel(final ModelerPanel modelerPanel,
                                                          final JDialog parent) {

      if (parent != null) {
         return new MultipleInputPanel(parent, modelerPanel);
      }
      else {
         return new MultipleInputPanel(modelerPanel);
      }

   }

}

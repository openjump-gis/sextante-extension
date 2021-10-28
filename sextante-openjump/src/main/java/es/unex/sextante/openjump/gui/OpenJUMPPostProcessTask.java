package es.unex.sextante.openjump.gui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import org.openjump.core.rasterimage.RasterImageLayer;
import org.openjump.sextante.core.ObjectAndDescription;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.additionalResults.TableTools;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.settings.SextanteGeneralSettings;
import es.unex.sextante.openjump.core.OpenJUMPOutputFactory;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.NullOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputText;
import es.unex.sextante.outputs.OutputVectorLayer;

public class OpenJUMPPostProcessTask implements Runnable {

    private final OutputObjectsSet m_Output;
    private final boolean m_bShowResultsDialog;

    public OpenJUMPPostProcessTask(final GeoAlgorithm algorithm,
            final boolean showResultsDialog) {

        m_Output = algorithm.getOutputObjects();
        m_bShowResultsDialog = showResultsDialog;

    }

    @Override
    public void run() {

        addResults();

    }

    private boolean addResults() {

        String sDescription;
        final boolean bShowAdditionalPanel = false;
        final boolean bUseInternalNames = Boolean.getBoolean(SextanteGUI
                .getSettingParameterValue(SextanteGeneralSettings.USE_INTERNAL_NAMES));
        final boolean bModiFyResultsNames = Boolean.getBoolean(SextanteGUI
                .getSettingParameterValue(SextanteGeneralSettings.MODIFY_NAMES));

        for (int i = 0; i < m_Output.getOutputObjectsCount(); i++) {

            final Output out = m_Output.getOutput(i);
            sDescription = out.getDescription();
            final IOutputChannel channel = out.getOutputChannel();
            final Object object = out.getOutputObject();
            if ((out instanceof OutputRasterLayer)
                    || (out instanceof Output3DRasterLayer)
                    || (out instanceof OutputTable)
                    || (out instanceof OutputVectorLayer)) {
                if (bUseInternalNames) {
                    sDescription = out.getName();
                } else if (bModiFyResultsNames) {
                    sDescription = SextanteGUI.modifyResultName(sDescription);
                }
                if ((channel instanceof NullOutputChannel) || (channel == null)) {
                    continue;
                }
            }
            if (object instanceof IRasterLayer) {
                final OpenJUMPOutputFactory factory = (OpenJUMPOutputFactory) SextanteGUI
                        .getOutputFactory();
                final WorkbenchContext context = factory.getContext();
                final IRasterLayer layer = (IRasterLayer) object;
                RasterImageLayer openJUMPLayer = (RasterImageLayer) layer
                        .getBaseDataObject();
                if (openJUMPLayer == null) {
                    openJUMPLayer = (RasterImageLayer) ((IRasterLayer) SextanteGUI
                            .getInputFactory()
                            .openDataObjectFromFile(
                                    ((FileOutputChannel) channel).getFilename()))
                            .getBaseDataObject();
                }
                context.getLayerManager().addLayerable(
                        StandardCategoryNames.WORKING, openJUMPLayer);
                SextanteGUI.getInputFactory().addDataObject(layer);
            } else if (object instanceof IVectorLayer) {
                final OpenJUMPOutputFactory factory = (OpenJUMPOutputFactory) SextanteGUI
                        .getOutputFactory();
                final WorkbenchContext context = factory.getContext();
                final IVectorLayer layer = (IVectorLayer) object;
                Layer openJUMPLayer = (Layer) layer.getBaseDataObject();
                if (openJUMPLayer == null) {
                    openJUMPLayer = (Layer) ((IVectorLayer) SextanteGUI
                            .getInputFactory()
                            .openDataObjectFromFile(
                                    ((FileOutputChannel) channel).getFilename()))
                            .getBaseDataObject();
                }
                context.getLayerManager().addLayer(
                        StandardCategoryNames.WORKING, openJUMPLayer);
                SextanteGUI.getInputFactory().addDataObject(layer);
            } else if (out instanceof OutputTable) {
                try {
                    final JScrollPane jScrollPane = TableTools
                            .getScrollableTablePanelFromITable((ITable) object);
                    // [Giuseppe Aruta 2017-12-11] moved output to OpenJUMP
                    // Internal Frame
                    org.openjump.sextante.gui.additionalResults.AdditionalResults
                            .addComponentAndShow(new ObjectAndDescription(
                                    sDescription, jScrollPane));
                    // AdditionalResults.addComponent(new ObjectAndDescription(
                    // sDescription, jScrollPane));
                    // bShowAdditionalPanel = true;

                } catch (final Exception e) {
                    Sextante.addErrorToLog(e);
                }
            } else if (out instanceof OutputText) {
                JTextPane jTextPane;
                JScrollPane jScrollPane;
                jTextPane = new JTextPane();
                jTextPane.setEditable(false);
                jTextPane.setContentType("text/html");
                jTextPane.setText((String) object);
                jScrollPane = new JScrollPane();
                jScrollPane.setViewportView(jTextPane);
                jScrollPane
                        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                jTextPane.setBorder(BorderFactory
                        .createEtchedBorder(BevelBorder.LOWERED));
                // [Giuseppe Aruta 2017-12-11] moved output to OpenJUMP Internal
                // Frame
                org.openjump.sextante.gui.additionalResults.AdditionalResults
                        .addComponentAndShow(new ObjectAndDescription(
                                sDescription, jScrollPane));
                // AdditionalResults.addComponent(new ObjectAndDescription(
                // sDescription, jScrollPane));
                // org.openjump.core.ui.plugin.additionalResults.AdditionalResults
                // .addComponent(new ObjectAndDescription(sDescription,
                // jScrollPane));
                // bShowAdditionalPanel = true;
            } else if (object instanceof Component) {
                // [Giuseppe Aruta 2017-12-11] moved output to OpenJUMP Internal
                // Frame
                org.openjump.sextante.gui.additionalResults.AdditionalResults
                        .addComponentAndShow(new ObjectAndDescription(
                                sDescription, object));
                // AdditionalResults.addComponent(new ObjectAndDescription(
                // sDescription, object));
                // //
                // org.openjump.core.ui.plugin.additionalResults.AdditionalResults
                // .addComponent(new ObjectAndDescription(sDescription,
                // object));
                // bShowAdditionalPanel = true;
            } else if (out instanceof Output3DRasterLayer) {
                JOptionPane.showMessageDialog(SextanteGUI.getMainFrame(),
                        Sextante.getText("3d_not_supported"),
                        Sextante.getText("Warning"),
                        JOptionPane.WARNING_MESSAGE);
            }

        }

     //   if (bShowAdditionalPanel && m_bShowResultsDialog) {
            // [Giuseppe Aruta 2017-12-11] moved output to OpenJUMP Internal
            // Frame
     //       org.openjump.sextante.gui.additionalResults.AdditionalResults
     //               .showPanel();
            // AdditionalResults.showPanel();
    //    }

        return true;

    }

}

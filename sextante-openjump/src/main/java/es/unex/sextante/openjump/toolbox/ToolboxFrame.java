package es.unex.sextante.openjump.toolbox;

import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.toolbox.IToolboxDialog;
import es.unex.sextante.gui.toolbox.ToolboxPanel;


/**
 * Deprecated. The frame should be defined at Sextante-GUI.class level
 */
@Deprecated
public class ToolboxFrame extends JInternalFrame implements IToolboxDialog {

    /**
     * 
     */
    // [Giuseppe Aruta 2017-12-11] moodified class from
    // es.unex.sextante.gui.toolbox.ToolboxDialog
    // in order to open as an OpenJUMP internal frame
    private static final long serialVersionUID = -6608836827062468343L;
    private ToolboxPanel m_Panel;

    /**
     * Constructor
     * 
     * @param parent
     *            the parent frame
     */
    public ToolboxFrame(final Frame parent) {

        // super("SEXTANTE", true);
        setTitle("SEXTANTE");
        setResizable(true);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);

        setSize(400, 600);
        setLayer(JLayeredPane.MODAL_LAYER);
        //
        // this.setResizable(false);

        initialize();
        // this.setLocationRelativeTo(null);
    }

    public void initialize() {

        // final URL res = getClass().getClassLoader().getResource(
        // "images/sextante_toolbox.gif");
        // if (res != null) {
        // } else {
        // }
        // [Giuseppe Aruta 2017-12-11] adopted internal Sextante
        final ImageIcon icon = new ImageIcon(getClass().getResource(
                "sextante_toolbox2.gif"));

        m_Panel = new ToolboxPanel(this, null, icon);
        setContentPane(m_Panel);

        m_Panel.fillTreesWithAllAlgorithms();

    }

    /**
     * Returns the toolbox panel contained in this dialog
     * 
     * @return the toolbox panel contained in this dialog
     */
    public ToolboxPanel getToolboxPanel() {

        return m_Panel;

    }

    @Override
    public void setAlgorithmsCount(final int iCount) {

        setTitle("SEXTANTE" + " - " + Sextante.getText("Toolbox") + " ("
                + Integer.toString(iCount) + Sextante.getText(" Tools") + ")");

    }

    @Override
    public JDialog getDialog() {
        // TODO Auto-generated method stub
        return null;
    }

}

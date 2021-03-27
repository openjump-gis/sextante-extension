package es.unex.sextante.openjump.extensions;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.CoordinateListMetrics;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.NamedPoint;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.openjump.language.I18NPlug;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Following Victor Olaya Blog
 * (http://sextantegis.blogspot.it/2009/05/herramientas-para-usuarios-gvsig.html)
 * this functionality allows to interactive get coordinates of points from a view.
 * Thus points can be used later on Sextante Algorithms
 * @author Giuseppe Aruta oct 2016
 */
public class SextantePickCoordinatesTool extends NClickTool {

    PlugInContext context;
    public static final String NAME = I18NPlug
            .getI18N("es.unex.sextante.kosmo.extensions.SextantePickCooridnates.pick-coordinates");

    public SextantePickCoordinatesTool(PlugInContext context) {
        super(1);
        this.context = context;
        setStroke(new BasicStroke(2));
        setMetricsDisplay(new CoordinateListMetrics());
        allowSnapping();
    }

    protected Point getPoint() throws NoninvertibleTransformException {
        return new GeometryFactory().createPoint((Coordinate) getCoordinates()
                .get(0));
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("bullseye.png"));
    }

    public Cursor getCursor() {
        return createCursor(new ImageIcon(getClass().getResource(
                "pick_coordinates_tool.gif")).getImage());
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        Coordinate coordinate = (Coordinate) getCoordinates().get(0);
        Point2D wcPoint = CoordUtil.toPoint2D(coordinate);

        String sPointName = JOptionPane.showInputDialog(
                panel,
                "X: " + Double.toString(wcPoint.getX()) + "\n" + "Y: "
                        + Double.toString(wcPoint.getY()) + "\n" + NAME + ":",
                "[" + Sextante.getText("Name") + "]");

        if (sPointName != null) {
            NamedPoint namedPoint = new NamedPoint(sPointName, wcPoint);
            SextanteGUI.getGUIFactory().getCoordinatesList().add(namedPoint);
        }

    }

    public void mouseMoved(MouseEvent me) {

        String text = "";

        try {
            Coordinate coordinate = getPanel().getViewport().toModelCoordinate(
                    me.getPoint());

            text = Sextante.getText("Coordinate") + "= X: " + coordinate.x
                    + "   Y: " + coordinate.y;

        } catch (Exception ex) {
            text = "???";
        }

        getPanel().getContext().setStatusMessage(text);

    }
}

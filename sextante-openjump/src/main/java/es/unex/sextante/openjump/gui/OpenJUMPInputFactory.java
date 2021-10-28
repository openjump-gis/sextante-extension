package es.unex.sextante.openjump.gui;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.geotools.dbffile.DbfFile;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.openjump.core.apitools.IOTools;
import org.openjump.core.geomutils.GeoUtils;
import org.openjump.core.rasterimage.GeoTiffConstants;
import org.openjump.core.rasterimage.GridAscii;
import org.openjump.core.rasterimage.GridFloat;
import org.openjump.core.rasterimage.RasterImageIO;
import org.openjump.core.rasterimage.RasterImageLayer;
import org.openjump.core.rasterimage.WorldFileHandler;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFField;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.imagery.ReferencedImageStyle;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;

import es.unex.sextante.core.AbstractInputFactory;
import es.unex.sextante.core.NamedExtent;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.openjump.core.OpenJUMPOutputFactory;
import es.unex.sextante.openjump.core.OpenJUMPRasterLayer;
import es.unex.sextante.openjump.core.OpenJUMPTable;
import es.unex.sextante.openjump.core.OpenJUMPVectorLayer;

/**
 * An input factory to get data objects from openJUMP into SEXTANTE
 * 
 * @author volaya
 * 
 */
public class OpenJUMPInputFactory extends AbstractInputFactory {

    private final WorkbenchContext m_Context;

    public OpenJUMPInputFactory(final WorkbenchContext context) {

        m_Context = context;

    }

    public void createDataObjects() {

        final ArrayList<IDataObject> layers = new ArrayList<>();
        final List<Layerable> layerables = m_Context.getLayerManager().getLayerables(
                Layerable.class);

        for (Layerable layerable : layerables) {

            if (layerable instanceof Layer) {
                // [Giuseppe Aruta Oct 2016] - Table
                // if a layer has null geometries,
                // but featureclooection is not empty
                // than is loaded into Sextante as table
                FeatureCollection feat = ((Layer) layerable)
                        .getFeatureCollectionWrapper().getUltimateWrappee();
                if (isTable((Layer) layerable)) {
                    IDataObject obj = new OpenJUMPTable();
                    ((OpenJUMPTable)obj).create(feat);
                    obj.setName(layerable.getName());
                    layers.add(obj);
                } else {
                    // [Giuseppe Aruta Oct 2016] - Vector
                    // We exclude image files loaded
                    // via Layer.class
                    // Also we exclude mixed geometries: Sextante doesn't decode
                    // them anyhow for vector analysis
                    if (!feat.isEmpty() && !isImageLayer((Layer) layerable)
                            && !isMixedGeometryType((Layer) layerable)) {
                        OpenJUMPVectorLayer obj = new OpenJUMPVectorLayer();
                        obj.create((Layer) layerable);
                        layers.add(obj);
                    }
                }
                // [Giuseppe Aruta Oct 2016] - Raster
            } else if (layerable instanceof RasterImageLayer) {
                try {
                    OpenJUMPRasterLayer obj = new OpenJUMPRasterLayer();
                    obj.create((RasterImageLayer) layerable);
                    layers.add(obj);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        m_Objects = new IDataObject[layers.size()];
        for (int i = 0; i < layers.size(); i++) {
            m_Objects[i] = layers.get(i);
        }

    }

    /**
     * Layer.class
     * 
     * @return true if the layer (Layer.class) belongs form an image file (eg.
     *         JPG, TIF, ECW)
     */
    public static boolean isImageLayer(Layer layer) {
        return layer.getStyle(ReferencedImageStyle.class) != null;
    }

    public static boolean isMixedGeometryType(Layer layer) {
        FeatureCollectionWrapper featureCollection = layer
                .getFeatureCollectionWrapper();
        @SuppressWarnings("unchecked")
        List<Feature> featureList = featureCollection.getFeatures();
        BitSet layerBit = new BitSet();
        BitSet currFeatureBit = new BitSet();
        if (featureList.size() > 0) {
            Geometry firstGeo = (featureList.iterator().next())
                    .getGeometry();
            layerBit = GeoUtils.setBit(layerBit, firstGeo); // this is the layer
                                                            // type
        }
        for (Feature feature : featureList) {
            Geometry geo = feature.getGeometry();
            currFeatureBit = GeoUtils.setBit(currFeatureBit, geo);
        }
        if ((layerBit.get(GeoUtils.pointBit) && currFeatureBit
                .get(GeoUtils.lineBit))
                || (layerBit.get(GeoUtils.polyBit) && currFeatureBit
                        .get(GeoUtils.lineBit))
                || (layerBit.get(GeoUtils.pointBit) && currFeatureBit
                        .get(GeoUtils.polyBit))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Boolean (Layer.class). True if all the layer geometries are empty
     * (Geometrycollection empty). Workaround to decode .csv files and to load
     * in Sextante as table
     * 
     * @return
     */

//    public static boolean isTable(Layer layer) {
 //         FeatureCollectionWrapper featureCollection = layer
 //                 .getFeatureCollectionWrapper();
 //         List featureList = featureCollection.getFeatures();
 //        Geometry nextGeo = null;
 //         for (@SuppressWarnings("unchecked")
 //        Iterator<FeatureCollectionWrapper> i = featureList.iterator(); i
 //                .hasNext();) {
 //            Feature feature = (Feature) i.next();
 //             nextGeo = feature.getGeometry();
 //         }
 //        if (!featureCollection.isEmpty() && nextGeo.isEmpty()) {
 //            return true;
 //        } else {
 //             return false;
 //        }
 //    }

    //[Giuseppe Aruta 2017_11_19, got from from Michael's correction bug
    // #466 Wierd behaviour gettin envelope from a geometry 
    
    public static boolean isTable(Layer layer) {
        Envelope env = layer.getFeatureCollectionWrapper().getEnvelope();
        return env.isNull();
    }
    
    
    public NamedExtent[] getPredefinedExtents() {

        return new NamedExtent[0];

    }

    public String[] getRasterLayerInputExtensions() {

        return new String[] { "tif" };

    }

    public String[] getTableInputExtensions() {

        return new String[] { "dbf" };

    }

    public String[] getVectorLayerInputExtensions() {

        return new String[] { "shp" };

    }

    public IDataObject openDataObjectFromFile(final String sFilename) {

        if (sFilename.endsWith("shp")) {
            FeatureCollection fc;
            try {
                fc = IOTools.loadShapefile(sFilename);
            } catch (final Exception e) {
                e.printStackTrace();
                return null;
            }
            final LayerManager layerManager = new LayerManager();
            final Layer layer = new Layer("",
                    layerManager.generateLayerFillColor(), fc, layerManager);
            final OpenJUMPVectorLayer vectorLayer = new OpenJUMPVectorLayer();
            vectorLayer.create(layer);
            return vectorLayer;
        } else if (sFilename.endsWith("tif")) {
            try {
                final WorkbenchContext context = ((OpenJUMPOutputFactory) SextanteGUI
                        .getOutputFactory()).getContext();
                final Point imageDimensions = RasterImageIO
                        .getImageDimensions(sFilename);
                final Envelope env = getGeoReferencing(sFilename, true,
                        imageDimensions, context);
                final RasterImageLayer rasterLayer = new RasterImageLayer(
                        new File(sFilename).getName(),
                        ((OpenJUMPOutputFactory) SextanteGUI.getOutputFactory())
                                .getContext().getLayerManager(), sFilename,
                        null, env);
                final OpenJUMPRasterLayer layer = new OpenJUMPRasterLayer();
                layer.create(rasterLayer);
                return layer;
            } catch (final Exception e) {
                return null;
            }

        } else if (sFilename.endsWith("dbf")) {
            return openTable(sFilename);
        } else {
            return null;
        }

    }

    private ITable openTable(final String sFilename) {

        try {
            final DbfFile mydbf = new DbfFile(sFilename);

            final FeatureSchema fs = new FeatureSchema();

            // fill in schema
            fs.addAttribute("GEOMETRY", AttributeType.GEOMETRY);

            FeatureCollection featureCollection;

            final int numfields = mydbf.getNumFields();

            for (int j = 0; j < numfields; j++) {
                final AttributeType type = AttributeType.toAttributeType(mydbf
                        .getFieldType(j));
                fs.addAttribute(mydbf.getFieldName(j), type);
            }

            featureCollection = new FeatureDataset(fs);

            for (int x = 0; x < mydbf.getLastRec(); x++) {
                final Feature feature = new BasicFeature(fs);
                final byte[] s = mydbf.GetDbfRec(x);

                for (int y = 0; y < numfields; y++) {
                    feature.setAttribute(y + 1, mydbf.ParseRecordColumn(s, y));
                }

                featureCollection.add(feature);
            }

            mydbf.close();

            final OpenJUMPTable table = new OpenJUMPTable();
            table.create(featureCollection);

            return table;
        } catch (final Exception e) {
            return null;
        }

    }

    public void close(final String sName) {

        final LayerManager lm = m_Context.getLayerManager();
        final Layer layer = lm.getLayer(sName);
        lm.remove(layer);

    }

    public String[] get3DRasterLayerInputExtensions() {

        return new String[] { "asc3d" };

    }

    protected Envelope getGeoReferencing(final String fileName,
            final boolean allwaysLookForTFWExtension,
            final Point imageDimensions, final WorkbenchContext context)
            throws IOException {

        Envelope env = null;

        WorldFileHandler worldFileHandler = new WorldFileHandler(fileName,
                allwaysLookForTFWExtension);

        if (imageDimensions == null) {
            return null;
        }

        if (worldFileHandler.isWorldFileExistentForImage() != null) {
            // logger.printDebug(PirolPlugInMessages.getString("worldfile-found"));
            env = worldFileHandler.readWorldFile(imageDimensions.x,
                    imageDimensions.y);
        }

        if (env == null) {

            boolean isGeoTiff = false;

            if (fileName.toLowerCase().endsWith(".tif")
                    || fileName.toLowerCase().endsWith(".tiff")) {
                // logger.printDebug("checking for GeoTIFF");

                Coordinate tiePoint = null, pixelOffset = null, pixelScale = null;
                double[] doubles = null;

                final FileSeekableStream fileSeekableStream = new FileSeekableStream(
                        fileName);
                final TIFFDirectory tiffDirectory = new TIFFDirectory(
                        fileSeekableStream, 0);

                final TIFFField[] availTags = tiffDirectory.getFields();

                for (int i = 0; i < availTags.length; i++) {
                    if (availTags[i].getTag() == GeoTiffConstants.ModelTiepointTag) {
                        doubles = availTags[i].getAsDoubles();

                        if (doubles.length != 6) {
                            // logger.printError("unsupported value for ModelTiepointTag ("
                            // + GeoTiffConstants.ModelTiepointTag + ")");
                            context.getWorkbench()
                                    .getFrame()
                                    .warnUser(
                                            "unsupported value for ModelTiepointTag ("
                                                    + GeoTiffConstants.ModelTiepointTag
                                                    + ")");
                            break;
                        }

                        if ((doubles[0] != 0) || (doubles[1] != 0)
                                || (doubles[2] != 0)) {
                            if (doubles[2] == 0) {
                                pixelOffset = new Coordinate(doubles[0],
                                        doubles[1]);
                            } else {
                                pixelOffset = new Coordinate(doubles[0],
                                        doubles[1], doubles[2]);
                            }
                        }

                        if (doubles[5] == 0) {
                            tiePoint = new Coordinate(doubles[3], doubles[4]);
                        } else {
                            tiePoint = new Coordinate(doubles[3], doubles[4],
                                    doubles[5]);
                        }

                        // logger.printDebug("ModelTiepointTag (po): " +
                        // pixelOffset);
                        // logger.printDebug("ModelTiepointTag (tp): " +
                        // tiePoint);
                    } else if (availTags[i].getTag() == GeoTiffConstants.ModelPixelScaleTag) {
                        // Karteneinheiten pro pixel x bzw. y

                        doubles = availTags[i].getAsDoubles();

                        if (doubles[2] == 0) {
                            pixelScale = new Coordinate(doubles[0], doubles[1]);
                        } else {
                            pixelScale = new Coordinate(doubles[0], doubles[1],
                                    doubles[2]);
                        }

                        // logger.printDebug("ModelPixelScaleTag (ps): " +
                        // pixelScale);
                    } else {
                        // logger.printDebug("tiff field: " +
                        // availTags[i].getType() + ", "+ availTags[i].getTag()
                        // + ", "+ availTags[i].getCount());
                    }

                }

                fileSeekableStream.close();

                if ((tiePoint != null) && (pixelScale != null)) {
                    isGeoTiff = true;
                    Coordinate upperLeft = null, lowerRight = null;

                    if (pixelOffset == null) {
                        upperLeft = tiePoint;
                    } else {
                        upperLeft = new Coordinate(tiePoint.x
                                - (pixelOffset.x * pixelScale.x), tiePoint.y
                                - (pixelOffset.y * pixelScale.y));
                    }

                    lowerRight = new Coordinate(upperLeft.x
                            + (imageDimensions.x * pixelScale.x), upperLeft.y
                            - (imageDimensions.y * pixelScale.y));

                    // logger.printDebug("upperLeft: " + upperLeft);
                    // logger.printDebug("lowerRight: " + lowerRight);

                    env = new Envelope(upperLeft, lowerRight);
                }

            } else if (fileName.toLowerCase().endsWith(".flt")) {
                isGeoTiff = true;
                final GridFloat gf = new GridFloat(fileName);

                final Coordinate upperLeft = new Coordinate(gf.getXllCorner(),
                        gf.getYllCorner() + gf.getnRows() * gf.getCellSize());
                final Coordinate lowerRight = new Coordinate(gf.getXllCorner()
                        + gf.getnCols() * gf.getCellSize(), gf.getYllCorner());

                env = new Envelope(upperLeft, lowerRight);

            } else if (fileName.toLowerCase().endsWith(".asc")) {
                isGeoTiff = true;
                final GridAscii ga = new GridAscii(fileName);

                final Coordinate upperLeft = new Coordinate(ga.getXllCorner(),
                        ga.getYllCorner() + ga.getnRows() * ga.getCellSize());
                final Coordinate lowerRight = new Coordinate(ga.getXllCorner()
                        + ga.getnCols() * ga.getCellSize(), ga.getYllCorner());

                env = new Envelope(upperLeft, lowerRight);
            }

            if (!isGeoTiff || (env == null)) {
                return null;
            }
            // creating world file
            worldFileHandler = new WorldFileHandler(fileName, true);
            worldFileHandler.writeWorldFile(env, imageDimensions.x,
                    imageDimensions.y);
        }

        return env;
    }

}

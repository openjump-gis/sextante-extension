package es.unex.sextante.openjump.core;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.locationtech.jts.geom.Envelope;
import org.openjump.core.rasterimage.GeoTiffConstants;
import org.openjump.core.rasterimage.RasterImageLayer;
import org.openjump.core.rasterimage.Stats;
import org.openjump.core.rasterimage.TiffTags;
import org.openjump.core.rasterimage.WorldFileHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFCodec;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.AbstractRasterLayer;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

public class OpenJUMPRasterLayer extends AbstractRasterLayer {

    private String m_sFilename;
    private String m_sName = "";
    private AnalysisExtent m_LayerExtent;
    private double m_dNoDataValue;
    private Raster m_Raster;
    RasterImageLayer m_Layer;

    public void create(RasterImageLayer layer) throws IOException {
        this.m_Layer = layer;
        this.m_Raster = layer.getRasterData(null);
        this.m_sName = layer.getName();
        this.m_sFilename = layer.getImageFileName();
        Envelope env = layer.getWholeImageEnvelope();
        this.m_LayerExtent = new AnalysisExtent();
        this.m_LayerExtent.setCellSize((env.getMaxX() - env.getMinX())
                / this.m_Raster.getWidth());
        this.m_LayerExtent.setXRange(env.getMinX(), env.getMaxX(), true);
        this.m_LayerExtent.setYRange(env.getMinY(), env.getMaxY(), true);
        this.m_dNoDataValue = layer.getNoDataValue();

        // [Giuseppe Aruta 30 Gen. 2018] deactivated as OJ calculate anyhow
        // statistics (and writes .xml file) when loads raster
        // m_Stats = stats(layer);
        // ------------------------------------------
        // [Giuseppe Aruta 30 Gen. 2018] - Uncomment [8 Oct. 2016] and reused
        // the previous version (Sextante
        // 0.6) as the previous method duplicates layer name ("rastername" ->
        // "rastername (2)").
        // this behaviour used to create confusion on some Sextante Algorithm,
        // like Calculus>Raster Calculator
        // ------------------------------------------
        // [sstein 26 Oct. 2010] using the new method instead
        // so I do not need to change the code in all the cases
        // where #.create(layer) is used

        // System.out.println("creation of raster layer");
        // create(layer, true);
    }

    public void create(RasterImageLayer layer, boolean loadFromFile)
            throws IOException {
        if (!loadFromFile) {
            this.m_Layer = layer;

            // [sstein 2 Aug 2010], changed so we work now with the raster and
            // not the image, which may be scaled for display.
            // m_Raster = layer.getImage().getData();
            this.m_Raster = layer.getRasterData(null);

            this.m_sName = layer.getName();
            this.m_sFilename = layer.getImageFileName();
            Envelope env = layer.getWholeImageEnvelope();
            this.m_LayerExtent = new AnalysisExtent();
            // [sstein 18 Mar 2013], set cell size first, and then the extent,
            // otherwise maxX and maxY will be reset
            this.m_LayerExtent.setCellSize((env.getMaxX() - env.getMinX())
                    / this.m_Raster.getWidth());
            this.m_LayerExtent.setXRange(env.getMinX(), env.getMaxX(), true);
            this.m_LayerExtent.setYRange(env.getMinY(), env.getMaxY(), true);
            // [Giuseppe Aruta 8 Oct. 2016] using selected rasterlayer no data
            // value instead
            // m_dNoDataValue = DEFAULT_NO_DATA_VALUE;
            this.m_dNoDataValue = layer.getNoDataValue();
        } else {
            RasterImageLayer rasterLayer = new RasterImageLayer(
                    layer.getName(), layer.getLayerManager(),
                    layer.getImageFileName(), null,
                    layer.getWholeImageEnvelope());
            this.m_Layer = rasterLayer;
            this.m_Raster = rasterLayer.getRasterData(null);

            this.m_sName = rasterLayer.getName();
            this.m_sFilename = rasterLayer.getImageFileName();
            Envelope env = rasterLayer.getWholeImageEnvelope();
            this.m_LayerExtent = new AnalysisExtent();
            // [sstein 18 Mar 2013], set cell size first, and then the extent,
            // otherwise maxX and maxY will be reset
            m_LayerExtent.setCellSize((env.getMaxX() - env.getMinX())
                    / m_Raster.getWidth());
            m_LayerExtent.setXRange(env.getMinX(), env.getMaxX(), true);
            m_LayerExtent.setYRange(env.getMinY(), env.getMaxY(), true);
            // [Giuseppe Aruta 8 Oct. 2016] using selected rasterlayer no data
            // value instead
            m_dNoDataValue = layer.getNoDataValue();
            // m_dNoDataValue = DEFAULT_NO_DATA_VALUE;
        }
    }

    public void create(String name, String filename, AnalysisExtent ge,
            int dataType, int numBands, Object crs) {
        this.m_Raster = RasterFactory.createBandedRaster(dataType, ge.getNX(),
                ge.getNY(), numBands, null);

        OpenJUMPOutputFactory fact = (OpenJUMPOutputFactory) SextanteGUI
                .getOutputFactory();

        Envelope envelope = new Envelope();
        envelope.init(ge.getXMin(), ge.getXMax(), ge.getYMin(), ge.getYMax());
        ColorModel colorModel = PlanarImage.createColorModel(this.m_Raster
                .getSampleModel());
        BufferedImage bufimg = new BufferedImage(colorModel,
                (WritableRaster) this.m_Raster, false, null);

        this.m_Layer = new RasterImageLayer(name, fact.getContext()
                .getLayerManager(), filename, bufimg, envelope);
        this.m_sName = name;
        this.m_sFilename = filename;
        this.m_LayerExtent = ge;
        // [Giuseppe Aruta 8 Oct. 2016] using Sextante GUI to get no data value
        // instead
        m_dNoDataValue = SextanteGUI.getOutputFactory().getDefaultNoDataValue();
        // m_dNoDataValue = DEFAULT_NO_DATA_VALUE;
    }

    @Override
    public int getBandsCount() {
        if (this.m_Raster != null) {
            return this.m_Raster.getNumBands();
        }
        return 0;
    }

    @Override
    public double getCellValueInLayerCoords(int x, int y, int band) {
        try {
            if (this.m_Raster != null) {
                return this.m_Raster.getSampleDouble(x, y, band);
            }
            return getNoDataValue();
        } catch (Exception e) {
        }
        return getNoDataValue();
    }

    @Override
    public int getDataType() {
        if (this.m_Raster != null) {
            return this.m_Raster.getDataBuffer().getDataType();
        }
        return 5;
    }

    @Override
    public double getLayerCellSize() {
        if (this.m_LayerExtent != null) {
            return this.m_LayerExtent.getCellSize();
        }
        return 0.0D;
    }

    @Override
    public AnalysisExtent getLayerGridExtent() {
        return this.m_LayerExtent;
    }

    @Override
    public double getNoDataValue() {
        return this.m_dNoDataValue;
    }

    @Override
    public void setCellValue(int x, int y, int band, double value) {
        if (((this.m_Raster instanceof WritableRaster))
                && (getWindowGridExtent().containsCell(x, y))) {
            ((WritableRaster) this.m_Raster).setSample(x, y, band, value);
        }
    }

    @Override
    public void setNoDataValue(double noDataValue) {
        this.m_dNoDataValue = noDataValue;
    }

    @Override
    public Object getCRS() {
        return null;
    }

    /**
     * Returns the extent covered by the layer
     * 
     * @return the extent of the layer
     */
    @Override
    public Rectangle2D getFullExtent() {
        if (this.m_Layer != null) {
            Envelope envelope = this.m_Layer.getWholeImageEnvelope();
            return new Rectangle2D.Double(envelope.getMinX(),
                    envelope.getMinY(), envelope.getWidth(),
                    envelope.getHeight());
        }
        return null;
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void postProcess() throws Exception {

        if (m_Layer != null) {

            final FileOutputStream tifOut = new FileOutputStream(m_sFilename);
            final TIFFEncodeParam param = new TIFFEncodeParam();
            param.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
            TIFFField[] tiffFields = new TIFFField[2];

            // [Giuseppe Aruta 8 Oct. 2016] the following parameters come from
            // RasterImageIO class
            // and add cell size/no data value and Tie point to the new created
            // file
            // Cell size
            tiffFields[0] = new TIFFField(GeoTiffConstants.ModelPixelScaleTag,
                    TIFFField.TIFF_DOUBLE, 2, getLayerCellSize());
            // No data
            String noDataS = Double.toString(getNoDataValue());
            byte[] bytes = noDataS.getBytes();
            tiffFields[0] = new TIFFField(TiffTags.TIFFTAG_GDAL_NODATA,
                    TIFFField.TIFF_BYTE, noDataS.length(), bytes);
            // Tie point
            final Envelope envelope = m_Layer.getWholeImageEnvelope();
            tiffFields[1] = new TIFFField(GeoTiffConstants.ModelTiepointTag,
                    TIFFField.TIFF_DOUBLE, 6, new double[] { 0, 0, 0,
                            envelope.getMinX(), envelope.getMaxY(), 0 });
            param.setExtraFields(tiffFields);

            final TIFFImageEncoder encoder = (TIFFImageEncoder) TIFFCodec
                    .createImageEncoder("tiff", tifOut, param);
            // -- [sstein 2 Aug 2010]
            // BufferedImage image = layer.getImage().getAsBufferedImage();
            final ColorModel colorModel = PlanarImage.createColorModel(m_Raster
                    .getSampleModel());
            final BufferedImage image = new BufferedImage(colorModel,
                    (WritableRaster) m_Raster, false, null);
            // -- end
            encoder.encode(image);
            tifOut.close();

            /* save geodata: */
            // final Envelope envelope = m_Layer.getWholeImageEnvelope();

            final WorldFileHandler worldFileHandler = new WorldFileHandler(
                    m_sFilename, false);
            worldFileHandler.writeWorldFile(envelope, image.getWidth(),
                    image.getHeight());
            // [Giuseppe Aruta 30 Gen. 2018] deactivated as OJ calculate anyhow
            // statistics (and writes .xml file) when loads raster
            // String outXML = m_sFilename + ".aux.xml";
            // writeXLM(new File(outXML));
            // Switch RAM mode of the RasterImage
            m_Layer.setImageFileName(m_sFilename);
            m_Layer.setNeedToKeepImage(false);

        }

    }

    public boolean export(String sFilename) {
        if (sFilename.endsWith("asc")) {
            return exportToArcInfoASCIIFile(sFilename);
        }
        if (sFilename.endsWith("tif")) {
            return exportToGeoTIFFFile(sFilename);
        }
        return exportToGeoTIFFFile(sFilename);
    }

    private boolean exportToGeoTIFFFile(String sFilename) {
        try {
            FileOutputStream tifOut = new FileOutputStream(this.m_sFilename);
            TIFFEncodeParam param = new TIFFEncodeParam();
            param.setCompression(1);
            TIFFField[] tiffFields = new TIFFField[3];

            tiffFields[0] = new TIFFField(33550, 12, 2, new double[] {
                    getLayerCellSize(), getLayerCellSize() });

            String noDataS = Double.toString(getNoDataValue());
            byte[] bytes = noDataS.getBytes();
            tiffFields[1] = new TIFFField(42113, 1, noDataS.length(), bytes);

            Envelope envelope = this.m_Layer.getWholeImageEnvelope();
            tiffFields[2] = new TIFFField(33922, 12, 6, new double[] { 0.0D,
                    0.0D, 0.0D, envelope.getMinX(), envelope.getMaxY(), 0.0D });
            param.setExtraFields(tiffFields);
            TIFFImageEncoder encoder = (TIFFImageEncoder) TIFFCodec
                    .createImageEncoder("tiff", tifOut, param);

            ColorModel colorModel = PlanarImage.createColorModel(this.m_Raster
                    .getSampleModel());
            BufferedImage image = new BufferedImage(colorModel,
                    (WritableRaster) this.m_Raster, false, null);

            encoder.encode(image);
            tifOut.close();

            WorldFileHandler worldFileHandler = new WorldFileHandler(
                    this.m_sFilename, false);
            worldFileHandler.writeWorldFile(envelope, image.getWidth(),
                    image.getHeight());

            this.m_Layer.setImageFileName(this.m_sFilename);
            this.m_Layer.setNeedToKeepImage(false);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean exportToArcInfoASCIIFile(String sFilename) {
        try {
            FileWriter f = new FileWriter(sFilename);
            BufferedWriter fout = new BufferedWriter(f);
            DecimalFormat df = new DecimalFormat("##.###");
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
            df.setDecimalSeparatorAlwaysShown(true);

            fout.write("ncols " + this.m_LayerExtent.getNX());
            fout.newLine();
            fout.write("nrows " + this.m_LayerExtent.getNY());
            fout.newLine();
            fout.write("xllcorner " + this.m_LayerExtent.getXMin());
            fout.newLine();
            fout.write("yllcorner " + this.m_LayerExtent.getYMin());
            fout.newLine();

            fout.write("cellsize " + this.m_LayerExtent.getCellSize());
            fout.newLine();
            fout.write("nodata_value " + getNoDataValue());
            fout.newLine();
            for (int i = 0; i < this.m_LayerExtent.getNY(); i++) {
                for (int j = 0; j < this.m_LayerExtent.getNX(); j++) {
                    fout.write(df.format(getCellValueAsDouble(j, i)) + " ");
                }
                fout.newLine();
            }
            fout.close();
            f.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return this.m_sName;
    }

    @Override
    public void setName(String sName) {
        this.m_sName = sName;
        if (this.m_Layer != null) {
            this.m_Layer.setName(sName);
        }
    }

    @Override
    public void free() {
    }

    @Override
    public Object getBaseDataObject() {
        return this.m_Layer;
    }

    @Override
    public IOutputChannel getOutputChannel() {
        return new FileOutputChannel(this.m_sFilename);
    }

    public String getFilename() {
        return this.m_sFilename;
    }

    // [Giuseppe Aruta 30 Gen. 2018] The following code is used to a) calculate
    // statistics of the layer
    // b) resume srs from input raster c) write sidecar .xml file with statistic
    // e srs.
    // I deactivated for now as OJ will rewrite .xml file anyhow when loads
    // output file and srs writing need more
    // test.
    private Stats m_Stats;

    public static Stats stats(RasterImageLayer layer) {
        return layer.getMetadata().getStats();
    }

    public void writeXML(File auxXmlFile) throws Exception {
        Stats stats = m_Stats;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc;

        Element pamDatasetElement;
        NodeList pamRasterBandNodeList;
        doc = docBuilder.newDocument();

        // Check if PAMDataset element exists and, if not, create it
        String pamDatasetTagName = "PAMDataset";
        pamDatasetElement = (Element) doc.getElementsByTagName(
                pamDatasetTagName).item(0);
        if (pamDatasetElement == null) {
            pamDatasetElement = doc.createElement(pamDatasetTagName);
        }

        String pamRasterBandTagName = "PAMRasterBand";
        String pamRasterSridTagName = "SRS";
        String bandAttribute = "band";
        String metadataElementName = "Metadata";

        String SRID = null;
        // String fileSourcePath = m_Layer.getImageFileName();
        // //// String srsCode = m_Layer.getSRSInfo().getCode();

        // SRSInfo srsInfo = SridLookupTable.getSrsAndUnitFromCode(srsCode);

        /*
         * String extension = FileUtil.getExtension(m_sFilename).toLowerCase();
         * 
         * if (extension.equals("tif") || extension.equals("tiff")) {
         * TiffTags.TiffMetadata metadata = TiffTags.readMetadata(new File(
         * fileSourcePath)); if (metadata.isGeoTiff()) {
         * 
         * srsInfo = metadata.getSRSInfo(); } else { srsInfo =
         * ProjUtils.getSRSInfoFromAuxiliaryFile(fileSourcePath);
         * 
         * } } else { srsInfo =
         * ProjUtils.getSRSInfoFromAuxiliaryFile(fileSourcePath);
         * 
         * }
         */
        // m_Srid = srsCode;

        // //// if (!srsCode.equals("0")) {
        // //// SRID = SridLookupTable.getOGCWKTFromWkidCode(srsCode);
        // //// if (!SRID.isEmpty()) {
        // ////
        // //// Element SRS = doc.createElement(pamRasterSridTagName);
        // //// SRS.appendChild(doc.createTextNode(SRID));
        // //// pamDatasetElement.appendChild(doc);
        // //// }
        // //// }

        pamRasterBandNodeList = pamDatasetElement
                .getElementsByTagName(pamRasterBandTagName);
        if (pamRasterBandNodeList != null
                && pamRasterBandNodeList.getLength() > 0) {
            for (int b = 0; b < pamRasterBandNodeList.getLength(); b++) {
                Element pamRasterBandElement = (Element) pamRasterBandNodeList
                        .item(b);
                int bandNr = Integer.parseInt(pamRasterBandElement
                        .getAttribute(bandAttribute));

                if (bandNr == b + 1) {

                    Element metadataElement = (Element) pamRasterBandElement
                            .getElementsByTagName(metadataElementName).item(0);
                    metadataElement = updateMetadataElement(doc,
                            metadataElement, m_Layer, bandNr);

                    pamRasterBandElement.appendChild(metadataElement);
                    pamDatasetElement.appendChild(pamRasterBandElement);

                }
            }
        } else {
            for (int b = 0; b < stats.getBandCount(); b++) {

                Element pamRasterBandElement = doc
                        .createElement(pamRasterBandTagName);
                Attr attr = doc.createAttribute(bandAttribute);
                attr.setValue(Integer.toString(b + 1));
                pamRasterBandElement.setAttributeNode(attr);

                Element metadataElement = doc
                        .createElement(metadataElementName);
                metadataElement = updateMetadataElement(doc, metadataElement,
                        m_Layer, b + 1);
                pamRasterBandElement.appendChild(metadataElement);
                pamDatasetElement.appendChild(pamRasterBandElement);
            }

            doc.appendChild(pamDatasetElement);
        }

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(auxXmlFile);
        transformer.transform(source, result);

    }

    private Element updateMetadataElement(Document doc,
            Element metadataElement, RasterImageLayer layer, int band) {
        Stats stats = m_Stats;

        Element mdi = doc.createElement("MDI");
        mdi.setAttribute("key", "STATISTICS_MINIMUM");
        mdi.setTextContent(Double.toString(stats.getMin(band)));
        metadataElement.appendChild(mdi);

        mdi = doc.createElement("MDI");
        mdi.setAttribute("key", "STATISTICS_MAXIMUM");
        mdi.setTextContent(Double.toString(stats.getMax(band)));
        metadataElement.appendChild(mdi);

        mdi = doc.createElement("MDI");
        mdi.setAttribute("key", "STATISTICS_MEAN");
        mdi.setTextContent(Double.toString(stats.getMean(band)));
        metadataElement.appendChild(mdi);

        mdi = doc.createElement("MDI");
        mdi.setAttribute("key", "STATISTICS_STDDEV");
        mdi.setTextContent(Double.toString(stats.getStdDev(band)));
        metadataElement.appendChild(mdi);

        return metadataElement;

    }

}



package es.unex.sextante.devel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class BenchmarkAlgorithm
extends
GeoAlgorithm {

	public static final String INPUT = "INPUT";


	@Override
	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

		int x, y;
		int iNX, iNY;
		long lTime, lTime2;


		lTime = System.currentTimeMillis();
		double dSum = 0;
		for (x = 0; x < 100000000; x++) {
			dSum++;
		}
		lTime2 = System.currentTimeMillis();
		System.out.println("A simple loop:" + Long.toString(lTime2 - lTime));

		IRasterLayer layer = m_Parameters.getParameterValueAsRasterLayer(INPUT);
		layer.setFullExtent();
		iNX = layer.getWindowGridExtent().getNX();
		iNY = layer.getWindowGridExtent().getNY();
		lTime = System.currentTimeMillis();
		for (y = 0; y < iNY; y++) {
			for (x = 0; x < iNX; x++) {
				layer.getCellValueAsDouble(x, y);
			}
		}
		lTime2 = System.currentTimeMillis();
		System.out.println("Reading values:" + Long.toString(lTime2 - lTime));

		lTime = System.currentTimeMillis();
		layer = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, layer.getLayerGridExtent());
		lTime2 = System.currentTimeMillis();
		System.out.println("Layer creation:" + Long.toString(lTime2 - lTime));

		iNX = layer.getWindowGridExtent().getNX();
		iNY = layer.getWindowGridExtent().getNY();

		lTime = System.currentTimeMillis();
		for (y = 0; y < iNY; y++) {
			for (x = 0; x < iNX; x++) {
				layer.setCellValue(x, y, 1);
			}
		}
		lTime2 = System.currentTimeMillis();
		System.out.println("Writing values:" + Long.toString(lTime2 - lTime));

		return !m_Task.isCanceled();

	}


	@Override
	public void defineCharacteristics() {

		setName(Sextante.getText("Benchmarking"));
		setGroup(Sextante.getText("Development"));

		setUserCanDefineAnalysisExtent(false);

		try {
			m_Parameters.addInputRasterLayer(INPUT, "Input", true);
		}
		catch (final RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		}

	}

}

package org.sextante.raster;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

/**
 * 
 * @author giuseppe aruta (August 2020)
 * This tool is recompiled from it.betastudio.adbtoolbox.ageom.pitremover
 * of AdBToolbox software
 *
 */
public class MapPitsAlgorithm extends GeoAlgorithm {


	public static final String  DEM            = "DEM";
	public static final String  MINSLOPE       = "MINSLOPE";
	public static final String  RESULT         = "RESULT";


	private int                 m_iNX, m_iNY;

	private IRasterLayer        m_DEM          = null;

	private IRasterLayer        m_PreprocessedDEM;


	@Override
	public void defineCharacteristics() {



		setName("Map pits");
		setGroup(Sextante.getText("DEM processing"));

		super.setUserCanDefineAnalysisExtent(false);


		try {
			m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);

			addOutputRasterLayer(RESULT, Sextante.getText("Preprocessed"));
		}
		catch (final RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		}

	}



	@Override
	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {


		m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);


		final AnalysisExtent ge = new AnalysisExtent(m_DEM);
		m_PreprocessedDEM = getNewRasterLayer(RESULT, m_DEM.getName() + " [pit map]",
				IRasterLayer.RASTER_DATA_TYPE_DOUBLE, ge);

		int NOPITC = 1;
		int PITCOL = 8;
		int FLAT = 6;
		int m_shC[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
		int m_shR[] = { 1, 1, 0, -1, -1, -1, 0, 1 };
		int x;
		int y;
		m_iNX = ge.getNX();
		m_iNY = ge.getNY();
		double noData = m_DEM.getNoDataValue();
		double z = Math.abs(m_DEM.getMaxValue());
		int kernUpp = 0;
		int kernExt = 0;
		int kernFlat = 0;


		for (x = 0; (x < m_iNX) && setProgress(x, m_iNX); x++) {
			for (y = 0; y < m_iNY; y++) {
				z =  m_DEM.getCellValueAsDouble (x, y);
				if (z != noData) {
					kernUpp = 0;
					kernExt = 0;
					kernFlat = 0;

					for (int i = 0; i < 8; i++) {
						if ( m_DEM.getCellValueAsDouble(x + m_shC[i], y + m_shR[i]) == noData) {
							kernExt++;
						}
					}

					for (int i = 0; i < 8; i++) {
						if (m_DEM.getCellValueAsDouble(x + m_shC[i], y + m_shR[i])!= noData
								&&  m_DEM.getCellValueAsDouble (x, y) <= m_DEM.getCellValueAsDouble(x + m_shC[i], y + m_shR[i])) {
							kernUpp++;
						}
					}



					for (int i = 0; i < 8; i++) {
						if (m_DEM.getCellValueAsDouble(x + m_shC[i], y + m_shR[i])!= noData
								&&  m_DEM.getCellValueAsDouble (x, y) == m_DEM.getCellValueAsDouble(x + m_shC[i], y + m_shR[i])) {
							kernFlat++;
						}
					}






					if (kernUpp == (8 - kernExt)) {
						m_PreprocessedDEM.setCellValue(x,y,PITCOL);
					} else {
						if (kernFlat ==8) {
							m_PreprocessedDEM.setCellValue(x,y,FLAT);
						} else {
							m_PreprocessedDEM.setCellValue(x,y,NOPITC);}
					}
					//}
				} else {
					m_PreprocessedDEM.setCellValue(x,y,noData);
				}
			}}
		return  !m_Task.isCanceled();
	}



}

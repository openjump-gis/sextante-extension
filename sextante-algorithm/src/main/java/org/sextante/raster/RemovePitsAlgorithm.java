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
 * This tools are recompiled from WhiteBox GAT software
 * two algorithms to remove single pits (fill or breach)
 * (https://jblindsay.github.io/ghrg/Whitebox/)
 *
 */
public class RemovePitsAlgorithm extends GeoAlgorithm {


	public static final String  DEM            = "DEM";
	public static final String  MINSLOPE       = "MINSLOPE";
	public static final String  RESULT         = "RESULT";
	public static final String  METHOD        = "METHOD";

	int                         m_iMethod;

	private int                 m_iNX, m_iNY;

	private IRasterLayer        m_DEM          = null;

	private IRasterLayer        m_PreprocessedDEM;


	@Override
	public void defineCharacteristics() {

		final String[] sMethod = {"Fill single-cell pits",
		"Breach single-cell pits"};


		setName("Remove single-cell pits");
		setGroup(Sextante.getText("DEM processing"));
		super.setUserCanDefineAnalysisExtent(false);


		try {
			m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
			m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
			addOutputRasterLayer(RESULT, Sextante.getText("Preprocessed"));
		}
		catch (final RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		}

	}

	private boolean amIActive = false;

	@Override
	public boolean isActive() {
		return this.amIActive;
	}

	@Override
	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {


		m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
		m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);

		final AnalysisExtent ge = new AnalysisExtent(m_DEM);
		m_PreprocessedDEM = getNewRasterLayer(RESULT, m_DEM.getName() + " [remove pits]",
				IRasterLayer.RASTER_DATA_TYPE_DOUBLE, ge);



		switch (m_iMethod) {
		case 0:
		default:

			break;

		case 1:
			FillPits(ge);
			break;

		case 2:
			breachPits(ge);
			break;
		}
		return  !m_Task.isCanceled();
	}


	public boolean breachPits(AnalysisExtent ge) {

		int progress = 0;
		int[] dX = { 1, 1, 1, 0, -1, -1, -1, 0 };
		int[] dY = { -1, 0, 1, 1, 1, 0, -1, -1 };
		int[] dX2 = { 
				2, 2, 2, 2, 2, 1, 0, -1, -2, -2, 
				-2, -2, -2, -1, 0, 1 };
		int[] dY2 = { 
				-2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 
				0, -1, -2, -2, -2, -2 };
		int[] breachcell = { 
				0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 
				5, 5, 6, 6, 7, 0 };
		m_iNX = ge.getNX();
		m_iNY = ge.getNY();
		double noData = m_DEM.getNoDataValue();



		double z = Math.abs(m_DEM.getMaxValue());

		int x;
		int y;
		for (x = 0; (x < m_iNX) && setProgress(x, m_iNX); x++) {
			for (y = 0; y < m_iNY; y++) {
				z =  m_DEM.getCellValueAsDouble (x, y);
				m_PreprocessedDEM.setCellValue(x, y, z ); 
			} 
			progress = (int)(100.0F * y / (m_iNY - 1));
			setProgressText("Loop 1 of 2:" + progress);
		} 
		for (x = 0; (x < m_iNX) && setProgress(x, m_iNX); x++) {
			for (y = 0; y < m_iNY; y++) {

				z = m_DEM.getCellValueAsDouble(x, y);
				if (z != noData) {
					int n = 0;
					int i;
					for (i = 0; i < 8; i++) {
						double z2 = m_DEM.getCellValueAsDouble(x + dX[i], y + dY[i]);
						if (z2 < z)
							n++; 
					} 
					if (n == 0)
						for (i = 0; i < 16; i++) {
							double z2 = m_DEM.getCellValueAsDouble(x + dX2[i], y + dY2[i]);
							if (z2 < z && z2 != noData)

								m_PreprocessedDEM.setCellValue( x + dX[breachcell[i]],y + dY[breachcell[i]],(z + z2) / 2.0D);   	


						}  
				} 
			} 

			progress = (int)(100.0F * y / (m_iNY - 1));
			setProgressText("Loop 2 of 2:" + progress);
		} 


		return !m_Task.isCanceled();

	}



	public boolean FillPits(AnalysisExtent ge) {
		int x;
		int y;
		int[] dX = { 1, 1, 1, 0, -1, -1, -1, 0 };
		int[] dY = { -1, 0, 1, 1, 1, 0, -1, -1 };
		m_iNX = ge.getNX();
		m_iNY = ge.getNY();
		double noData = m_DEM.getNoDataValue();
		double aSmallValue;
		double z = Math.abs(m_DEM.getMaxValue());
		if (z <= 9.0D) {
			aSmallValue = 9.999999747378752E-6D;
		} else if (z <= 99.0D) {
			aSmallValue = 9.999999747378752E-5D;
		} else if (z <= 999.0D) {
			aSmallValue = 0.0010000000474974513D;
		} else if (z <= 9999.0D) {
			aSmallValue = 0.0010000000474974513D;
		} else if (z <= 99999.0D) {
			aSmallValue = 0.009999999776482582D;
		} else {
			aSmallValue = 1.0D;
		} 

		for (x = 0; (x < m_iNX) && setProgress(x, m_iNX); x++) {
			for (y = 0; y < m_iNY; y++) {
				z =  m_DEM.getCellValueAsDouble (x, y);
				if (z != noData) {
					boolean isPit = true;
					double lowestNeighbour = Double.POSITIVE_INFINITY;
					for (int i = 0; i < 8; i++) {
						double z2 = m_DEM.getCellValueAsDouble(x + dX[i], y + dY[i]);

						if (z2 != noData) {
							if (z2 < lowestNeighbour)
								lowestNeighbour = z2; 
							if (z2 < z)
								isPit = false; 
						} 
					} 
					if (isPit && lowestNeighbour < Double.POSITIVE_INFINITY) {
						m_PreprocessedDEM.setCellValue(x, y, lowestNeighbour + aSmallValue);
					} else {
						m_PreprocessedDEM.setCellValue(x, y, z);
					} 
				} 
			} 


		}    

		return !m_Task.isCanceled();

	}




}

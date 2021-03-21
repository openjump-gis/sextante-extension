package it.betastudio.adbtoolbox.algorithms.math;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JOptionPane;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;


/**
 *
 * @author Beta Studio
 */
public class PitRemover {

	public PitRemover(String grdDemFullFileName, IRasterLayer        m_DEM,

			double outX, double outY, int frameSize, double stepZ) {

		this.grdDemFullFileName = grdDemFullFileName;

		this.m_DEM = m_DEM;
		AnalysisExtent extent = m_DEM.getLayerGridExtent();
		this.nCols =  extent.getNX();
		this.nRows = extent.getNY();

		noOu = cOu == 0 && rOu == 0;
		double xllCorner = extent.getXMin();
		double yllCorner =  extent.getYMin();
		if (!noOu) {
			cOu = (int) ((outX - xllCorner) / extent.getCellSize()) + 1;
			rOu = (int) ((outY - yllCorner) / extent.getCellSize()) + 1;

			rOu = nRows - rOu + 1;
		}

		this.frameSize = frameSize;
		this.stepZ = stepZ;

		bby = new char[nCols + 2][nRows + 2];
		m_MAP.setNoDataValue(m_DEM.getNoDataValue());
		m_MAP.setWindowExtent(m_DEM);
		//	rasPit = new double[nCols + 2][nRows + 2];

		for (int r = 0; r < nRows + 2; r++) {
			for (int c = 0; c < nCols + 2; c++) {
				bby[c][r] = 'B';
				m_MAP.setCellValue(c, r, noData);
				//		rasPit[c][r] = noData;
			}
		}

		threePlaces = new DecimalFormat(threePlacesS, dfs);

	}

	public int process() {

		if (mapPits() != 0) {
			return 1;
		}

		report += "-,No,Elev,Col,Row,Ext,Iter";

		pitCountTot = 0;
		for (int i = 1; i <= 100; i++) {
			pitCountIter = 0;
			if (depit(i) != 0) {
				return 1;
			}
			pitCountTot = +pitCountIter;
			if (pitCountIter == 0) {
				break;
			}
		}

		return 0;

	}

	private int mapPits() {

		int kernUpp = 0;
		int kernExt = 0;

		try {
			for (int r = 1; r <= nRows; r++) {
				for (int c = 1; c <= nCols; c++) {

					if (c == cOu && r == rOu) {
						m_MAP.setCellValue(c, r, NOPITC);
						//	rasPit[c][r] = NOPITC;
						continue;
					}

					if ( m_DEM.getCellValueAsDouble (c, r) != noData) {
						kernUpp = 0;
						kernExt = 0;

						for (int i = 0; i < 8; i++) {
							if ( m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i])

									== noData) {
								kernExt++;
							}
						}

						if (noOu && kernExt >= 1) {
							m_MAP.setCellValue(c, r, NOPITC);
							//rasPit[c][r] = NOPITC;
							continue;
						}

						for (int i = 0; i < 8; i++) {
							if (m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i]) != noData
									&&  m_DEM.getCellValueAsDouble (c, r) <= m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i])) {
								kernUpp++;
							}
						}

						if (kernUpp == (8 - kernExt)) {
							m_MAP.setCellValue(c, r, PITCOL);
							//	rasPit[c][r] = PITCOL;
						} else {
							m_MAP.setCellValue(c, r, NOPITC);
							//	rasPit[c][r] = NOPITC;
						}

					} else {
						m_MAP.setCellValue(c, r, noData);
						//rasPit[c][r] = noData;
					}
				}
			}
			return 0;
		} catch (final  Exception e) {
			Sextante.addErrorToLog(e);

			return 1;
		}
	}

	private int depit(int iterCount) {

		try {
			int kernUpp = 0;
			int kernExt = 0;
			int kernTot = 0;
			double valMin = Double.MAX_VALUE;
			double valMax = 0;
			double valH = 0;
			double valHMin = 0;
			int ixSv = 0;
			int iySv = 0;
			int ixTg = 0;
			int iyTg = 0;
			int ix1Tg = 0;
			int iy1Tg = 0;
			int ix2Tg = 0;
			int iy2Tg = 0;

			int ic1 = 0;
			int ir1 = 0;
			int ic2 = 0;
			int ir2 = 0;

			int ixDist = 0;
			int iyDist = 0;

			boolean finto = false;

			final int[] dist = new int[3];
			final int[] iOrd = new int[3];

			double distance = 0;

			int lKiller = 0;

			int kerXMin = 0;
			int kerXMax = 0;
			int kerYMin = 0;
			int kerYMax = 0;

			final int[] iPerc = new int[frameSize * 20];

			double valDelta = 0;

			int ixK1 = 0;
			int ixK2 = 0;
			int iyK1 = 0;
			int iyK2 = 0;

			m_DEM.setCellValue(cOu, rOu, m_DEM.getCellValueAsDouble (cOu,rOu)- stepZ);


			for (int r = 1; r <= nRows; r++) {
				for (int c = 1; c <= nCols; c++) {

					// if(c==19 && r==20){
					// System.out.println("CIC");
					// }

					if (			m_DEM.getCellValueAsDouble(c,r) == noData) {
						continue;
					}
					if (c == cOu && r == rOu) {
						continue;
					}

					kernUpp = 0;
					kernExt = 0;

					for (int i = 0; i < 8; i++) {
						if (m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i]) == noData) {
							kernExt++;
						}
					}
					if (noOu && kernExt >= 1) {
						continue;
					}

					kernTot = 8 - kernExt;
					for (int i = 0; i < 8; i++) {
						if (m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i]) != noData
								&& m_DEM.getCellValueAsDouble (c , r) <= m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i])) {
							kernUpp++;
						}
					}

					valMin = Double.MAX_VALUE;

					if (kernUpp == kernTot) {
						for (int i = 0; i < 8; i++) {
							if (m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i]) != noData
									&& !(c + m_shC[i] == cOu && r + m_shR[i] == rOu)) {
								valMin = Math.min(valMin,
										m_DEM.getCellValueAsDouble (c + m_shC[i], r + m_shR[i]) );
							}
						}
						pitCountIter++;
						report += lineFeed + "pit," + pitCountIter + ","
								+ threePlaces.format(m_DEM.getCellValueAsDouble (c , r ) ) + "," + c
								+ "," + (nRows - r + 1) + "," + kernExt + ","
								+ iterCount;
						m_MAP.setCellValue(c,r, Math.max(RUNPIT,m_MAP.getCellValueAsDouble(c, r)));
						//	rasPit[c][r] = Math.max(RUNPIT, rasPit[c][r]);
						m_DEM.setCellValue(c, r, valMin - stepZ);

						valH = m_DEM.getCellValueAsDouble(c, r);

						report += lineFeed + "elevation,,"
								+ threePlaces.format(valH);

						// Check a solver in the kernel <IFRAME
						for (int k = 2; k <= frameSize; k++) {
							valMax = stepZ;
							for (int ix = c - k; ix <= c + k; ix++) {
								for (int iy = r - k; iy <= r + k; iy++) {
									if (ix > 0 && ix < nCols + 2 && iy > 0
											&& iy < nRows + 2) {
										if (m_DEM.getCellValueAsDouble(ix,iy) > valMax
												&& m_DEM.getCellValueAsDouble(ix,iy) < valH
												&& m_DEM.getCellValueAsDouble(ix,iy) != noData) {
											valMax = m_DEM.getCellValueAsDouble(ix,iy);
											ixSv = ix;
											iySv = iy;
										}
									}
								}
							}
							if (valMax > stepZ) {
								break;
							}
						}

						if (valMax <= stepZ) {
							kernel: for (int k = 1; k <= Math.max(nCols, nRows); k++) {
								for (int jr = r - k; jr <= r + k; jr++) {
									if (jr >= 0 && jr <= nRows + 1) {
										for (int jc = c - k; jc <= c + k; jc++) {
											if (jr >= 0 && jr <= nRows + 1) {
												if (m_DEM.getCellValueAsDouble(jc,jr)
														== noData) {

													m_DEM.setCellValue(jc, jr,  m_DEM.getCellValueAsDouble(c,r)
															- 1000. * stepZ);
													valMax = m_DEM.getCellValueAsDouble(jc,jr);
													ixSv = jc;
													iySv = jr;
													finto = true;
													break kernel;
												}
											}
										}
									}
								}
							}
						report += lineFeed
								+ ",ERROR - No solver found in the frame";
						}

						bby[ixSv][iySv] = 'X';


						m_MAP.setCellValue(ixSv,iySv, Math.max(SOLCOL,m_MAP.getCellValueAsDouble(ixSv, iySv)));


						//	rasPit[ixSv][iySv] = Math.max(SOLCOL,
						//			rasPit[ixSv][iySv]);

						report += lineFeed + "solver,,"
								+ threePlaces.format(m_DEM.getCellValueAsDouble(ixSv,iySv)) + ","
								+ ixSv + "," + (nRows - iySv + 1);

						ixTg = ixSv;
						iyTg = iySv;
						ic1 = c + 1;
						ic2 = c - 1;
						ir1 = r + 1;
						ir2 = r - 1;

						int chL = 0;

						for (chL = 1; chL <= frameSize * 20; chL++) {
							valHMin = Double.MAX_VALUE;
							for (int l = 0; l < 3; l++) {
								dist[l] = Integer.MAX_VALUE;
								iOrd[l] = 0;
							}

							for (int killer = 0; killer < 8; killer++) {
								ixDist = ixTg + m_shC[killer];
								iyDist = iyTg + m_shR[killer];
								if (ixDist == cOu && iyDist == rOu) {
									continue;
								}
								if (m_DEM.getCellValueAsDouble(ixDist,iyDist) == noData) {
									continue;
								}
								distance = Math.sqrt(Math.pow(c - ixDist, 2)
										+ Math.pow(r - iyDist, 2));

								if (distance < dist[0]) {
									iOrd[2] = iOrd[1];
									iOrd[1] = iOrd[0];
									iOrd[0] = killer;
									dist[2] = dist[1];
									dist[1] = dist[0];
									dist[0] = (int) Math.round(distance);
									continue;
								} else if (distance >= dist[0]
										&& distance < dist[1]) {
									iOrd[2] = iOrd[1];
									iOrd[1] = killer;
									dist[2] = dist[1];
									dist[1] = (int) Math.round(distance);
									continue;
								} else if (distance >= dist[1]
										&& distance < dist[2]) {
									iOrd[2] = killer;
									dist[2] = (int) Math.round(distance);
									continue;
								}
							}

							for (int killer = 0; killer < 8; killer++) {
								ixDist = ixTg + m_shC[killer];
								iyDist = iyTg + m_shR[killer];

								if (bby[ixDist][iyDist] == 'X') {
									continue;
								}
								if (Math.abs(ixDist - c) > frameSize
										|| Math.abs(iyDist - r) > frameSize) {
									continue;
								}
								if ((killer != iOrd[0]) && (killer != iOrd[1])
										&& (killer != iOrd[1])) {
									continue;
								}

								if (m_DEM.getCellValueAsDouble(ixDist,iyDist)< valHMin) {
									valHMin =m_DEM.getCellValueAsDouble(ixDist,iyDist);
									lKiller = killer;
								}
							}

							if (valHMin == Double.MAX_VALUE) {

								if (kernExt > 0) {

									m_DEM.setCellValue(c, r, m_DEM.getCellValueAsDouble(c, r) + 2 * stepZ);
									report += lineFeed + "Elevat.+++,,"
											+ threePlaces.format(m_DEM.getCellValueAsDouble(c, r));
									break;
								} else {

									kerXMin = Math.min(c, ixSv);
									kerXMax = Math.max(c, ixSv);
									kerYMin = Math.min(r, iySv);
									kerYMax = Math.max(r, iySv);

									String lineOut = ",";
									for (int lx = kerXMin - 2; lx <= kerXMax + 4; lx++) {
										lineOut += lx + ",";
									}

									reportErr += lineOut.substring(0,
											lineOut.lastIndexOf(","));

									for (int ly = kerYMax + 2; ly >= kerYMin - 4; ly--) {
										lineOut = lineFeed + ly + ",";
										for (int lx = kerXMin - 2; lx <= kerXMax + 4; lx++) {
											if (lx > 0 && lx < nCols + 2
													&& ly > 0 && ly < nRows + 2) {
												lineOut += threePlaces
														.format(m_DEM.getCellValueAsDouble(lx,ly))
														+ ",";
											} else {
												lineOut += threePlaces
														.format(noData) + ",";
											}
										}
										reportErr += lineOut.substring(0,
												lineOut.lastIndexOf(","));
									}

									report += lineFeed + "pit," + pitCountIter
											+ ","
											+ threePlaces.format(m_DEM.getCellValueAsDouble(c, r))
											+ "," + c + "," + (nRows - r + 1)
											+ "," + kernExt + "," + iterCount;
									report += lineFeed
											+ "ERROR - No solver along the threePlacespercorso.";

									final int ret = JOptionPane
											.showConfirmDialog(
													null,
													"Breaching complited. No solver"
															+ "found per the pit at column "
															+ c
															+ ", row "
															+ r
															+ "."
															+ lineFeed
															+ "Create report file?",
															plugInName,
															JOptionPane.YES_NO_OPTION);
									if (ret == JOptionPane.YES_OPTION) {
										writeReportErr();
									}

									return 1;
								}
							} else {
								/*
								 * ----------------------------------------------
								 * -------------- / condizioni di appartenenza
								 * di 1 cella al percorso di correzione: / 1)
								 * appartenere al bacino / 2) non appartenere
								 * già al percorso / 3) appartenere alla max
								 * finestra pit-solver: limiti imposti a IDELTX
								 * e IDELTY / 4+5) avere quota minima tra le tre
								 * celle piu' vicine al pit /
								 * --------------------
								 * ----------------------------------------
								 */
								iPerc[chL] = lKiller;
								ixTg = ixTg + m_shC[lKiller];
								iyTg = iyTg + m_shR[lKiller];
								bby[ixTg][iyTg] = 'X';
								m_MAP.setCellValue(ixTg,iyTg, Math.max(CHACOL,m_MAP.getCellValueAsDouble(ixTg, iyTg)));
								//	rasPit[ixTg][iyTg] = Math.max(CHACOL,
								//		rasPit[ixTg][iyTg]);
								if ((ixTg <= ic1 && ixTg >= ic2)
										&& (iyTg <= ir1 && iyTg >= ir2)) {
									break;
								}
							}
						}

						/*
						 * ------------------------------------------------------
						 * ------------- / scrive il percorso di correzione
						 * (i_percorso) / modifica le quote delle celle percorso
						 * incrementando di val_delta /
						 * --------------------------
						 * -----------------------------------------
						 */
						if (!(valHMin == Double.MAX_VALUE && kernExt > 0)) {
							valDelta = (m_DEM.getCellValueAsDouble(c, r)- 
									m_DEM.getCellValueAsDouble(ixSv, iySv)
									)
									/ (chL + 1);
							ix1Tg = ixSv;
							iy1Tg = iySv;
							for (int i = 1; i <= chL; i++) {
								ix2Tg = ix1Tg + m_shC[iPerc[i]];
								iy2Tg = iy1Tg + m_shR[iPerc[i]];
								m_DEM.setCellValue(ix2Tg, iy2Tg, m_DEM.getCellValueAsDouble(ix1Tg, iy1Tg)+ valDelta);


								report += lineFeed
										+ "channel,,"
										+ threePlaces
										.format(m_DEM.getCellValueAsDouble(ix2Tg, iy2Tg))
										+ "," + ix2Tg + ","
										+ (nRows - iy2Tg + 1);

								ix1Tg = ix2Tg;
								iy1Tg = iy2Tg;
							}
						}
						/*
						 * ------------------------------------------------ /
						 * reinizializza le parti di BBY modificate /
						 * ----------------------------------------------
						 */
						if (finto) {
							m_DEM.setCellValue(ixSv, iySv, noData);

							finto = false;
						}
						ixK1 = Math.min(ixSv, c);
						ixK2 = Math.max(ixSv, c);
						iyK1 = Math.min(iySv, r);
						iyK2 = Math.max(iySv, r);
						for (int iyl = iyK1; iyl <= iyK2; iyl++) {
							for (int ixl = ixK1; ixl <= ixK2; ixl++) {
								if (m_DEM.getCellValueAsDouble(ixl,iyl) != noData) {
									bby[ixl][iyl] = 'B';
								}
							}
						}
					}
				}
			}
			return 0;
		} catch (final Exception ex) {
			Sextante.addErrorToLog(ex);
			return 1;
		}
	}

	public void writeReport() {

		try {
			String reportFullFileName = grdDemFullFileName.substring(0,
					grdDemFullFileName.lastIndexOf(File.separator));
			reportFullFileName += File.separator + "Report.txt";

			BufferedWriter bw = new BufferedWriter(new FileWriter(
					reportFullFileName));
			bw.write(report);

			bw.close();
			bw = null;

		} catch (final Exception ex) {
			Sextante.addErrorToLog(ex);
		}

	}

	private void writeReportErr() {

		try {

			String reportErrFullFileName = grdDemFullFileName.substring(0,
					grdDemFullFileName.lastIndexOf(File.separator));
			reportErrFullFileName += File.separator + "ReportErr.txt";

			BufferedWriter bw = new BufferedWriter(new FileWriter(
					reportErrFullFileName));
			bw.write(reportErr);

			bw.close();
			bw = null;

		} catch (final Exception ex) {
			Sextante.addErrorToLog(ex);
		}

	}

	public IRasterLayer getDem() {
		return m_DEM;
	}

	public IRasterLayer getPitMap() {
		return m_MAP;
	}

	private final String plugInName = "Depittaggio DEM";

	private String grdDemFullFileName = null;


	private IRasterLayer        m_DEM          = null;

	private IRasterLayer        m_MAP;



	//	private double[][] rasPit = null;
	private char[][] bby = null;

	private int nCols = 0;
	private int nRows = 0;
	private final double noData = -9999;

	private boolean noOu = true;
	private int cOu = 0;
	private int rOu = 0;

	private final static int m_shC[] = { 0, -1, -1, -1, 0, 1, 1, 1 };
	private final static int m_shR[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

	private final static int NOPITC = 1;
	private final static int SOLCOL = 3;
	private final static int CHACOL = 5;
	private final static int RUNPIT = 6;
	private final static int PITCOL = 8;

	private double stepZ = 0.1;
	private int frameSize = 500;

	private int pitCountIter = 0;
	private int pitCountTot = 0;

	private String report = "";
	private String reportErr = "";

	private final String lineFeed = System.getProperty("line.separator");

	private final DecimalFormatSymbols dfs = new DecimalFormatSymbols(
			Locale.ENGLISH);
	private final String threePlacesS = "0.000";
	private DecimalFormat threePlaces = null;

}
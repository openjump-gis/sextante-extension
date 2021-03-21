package org.sextante.vector;




import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class MakeValidAlgorithm extends
GeoAlgorithm {


	public static String PRESERVE_GEOM_DIM         = Sextante.getText("MakeValid_preserve");
	public static String REMOVE_DUPLICATE_COORD    = Sextante.getText("Remove duplicate points");
	public static String DECOMPOSE_MULTI           = Sextante.getText("Explode multi geometries");
	public static final String INPUT_LAYER = "LAYERMAIN";

	public static final String OUTPUT_LAYER    = "RESULT";
	public static final String PRESERVE       = "PRESERVE";
	public static final String REMOVE       = "REMOVE";
	public static final String EXPLODE        = "EXPLODE";
	public static final String RESULT             = "RESULT";

	private IVectorLayer       m_Layer;
	private IVectorLayer       m_Output;


	@Override
	public void defineCharacteristics() {
		setName(Sextante.getText("MakeValid"));
		setGroup(Sextante.getText("Topology"));
		setUserCanDefineAnalysisExtent(false);


		try {
			//	m_Parameters.addString("MAKEVALID", Sextante.getText("MakeValid_tip"));
			m_Parameters.addInputVectorLayer(INPUT_LAYER, Sextante.getText("Main_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
					true);
			m_Parameters.addBoolean(PRESERVE, Sextante.getText("MakeValid_preserve"), true);
			m_Parameters.addBoolean(REMOVE, Sextante.getText("MakeValid_remove"), true);
			m_Parameters.addBoolean(EXPLODE, Sextante.getText("MakeValid_explode"), false);
			addOutputVectorLayer(OUTPUT_LAYER, Sextante.getText("Result"));
		} catch (final RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		}



	}
	@Override
	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
		m_Layer = m_Parameters.getParameterValueAsVectorLayer(INPUT_LAYER);
		m_Output = getNewVectorLayer(OUTPUT_LAYER, m_Layer.getName(), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, m_Layer.getFieldTypes(),
				m_Layer.getFieldNames());

		final boolean preserve = m_Parameters.getParameterValueAsBoolean("PRESERVE");
		final boolean remove = m_Parameters.getParameterValueAsBoolean("REMOVE");
		final boolean decomposeMulti = m_Parameters.getParameterValueAsBoolean("EXPLODE");
		MakeValidOp makeValidOp = new MakeValidOp();
		makeValidOp.setPreserveGeomDim(preserve);
		//makeValidOp.setPreserveCoordDim(preserveCoordDim);
		makeValidOp.setPreserveDuplicateCoord(remove);

		final IFeatureIterator iter = m_Layer.iterator();
		int i = 0;
		final int iShapeCount = m_Layer.getShapesCount();
		while (iter.hasNext() && setProgress(i, iShapeCount)) {
			int number= iShapeCount-i;
			setProgressText("Validating geometry: "+number);
			final IFeature feature = iter.next();
			final Geometry geom = feature.getGeometry();
			final Object[] values = feature.getRecord().getValues();

			if (decomposeMulti) {

				for (int j = 0; j < geom.getNumGeometries(); j++) {

					final Geometry subgeom = geom.getGeometryN(j);
					PreparedGeometry targetPrep
					= PreparedGeometryFactory.prepare(subgeom);
					
					final Geometry validGeom= makeValidOp.makeValid(subgeom);
					m_Output.addFeature(validGeom, values);
				}
			}

			else {
				Geometry fixedGeometry = makeValidOp.makeValid(feature.getGeometry());
				m_Output.addFeature(fixedGeometry, values);
			}


			i++;
		}


		iter.close();
		return !m_Task.isCanceled();
	}




}

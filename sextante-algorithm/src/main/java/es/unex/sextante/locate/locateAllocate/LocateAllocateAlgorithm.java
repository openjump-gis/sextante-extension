package es.unex.sextante.locate.locateAllocate;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.OutputVectorLayer;

public class LocateAllocateAlgorithm
         extends
            GeoAlgorithm {

   public static final int    EUCLIDEAN             = 0;

   public static final int    MINSUM                = 0;
   public static final int    CONSTRAINEDMINSUM     = 1;
   public static final int    MINSTDDEV             = 2;
   public static final int    MINIMAX               = 3;
   public static final int    MAXCOV                = 4;
   public static final int    MAXSUM                = 5;
   public static final int    MINCOV                = 6;

   public static final String MAXDIST               = "MAXDIST";
   public static final String METHOD                = "METHOD";
   public static final String NEWLOCATIONS          = "NEWLOCATIONS";
   public static final String CANDIDATES            = "CANDIDATES";
   public static final String OFFER                 = "OFFER";
   public static final String FIELDDEMAND           = "FIELDDEMAND";
   public static final String DEMAND                = "DEMAND";
   public static final String DIST                  = "DIST";
   public static final String SPIDER                = "SPIDER";
   public static final String RESULT                = "RESULT";
   public static final String MEAN_DIST             = "MEAN_DIST";
   public static final String MEAN_SQUARED_DISTANCE = "MEAN_SQUARED_DISTANCE";
   public static final String MIN_DISTANCE          = "MIN_DISTANCE";
   public static final String MAX_DISTANCE          = "MAX_DISTANCE";
   public static final String VARIANCE              = "VARIANCE";
   public static final String SUM_OF_DISTANCES      = "SUM_OF_DISTANCES";
   public static final String COEFF_OF_VARIATION    = "COEFF_OF_VARIATION";
   public static final String OBJ_FUNCTION_VALUE    = "OBJ_FUNCTION_VALUE";

   private static final int   ITERATIONS            = 100;

   private ArrayList          m_Demand;
   private ArrayList          m_Candidates;
   private ArrayList          m_Offer;
   private double             m_dDist[][];
   private double             m_dMaxDist;
   private double             m_dObjective;
   private double             m_dBestObjective;
   private int                m_iDistMethod;
   private int                m_iNewLocations;
   private int[]              m_iSolution;
   private int[]              m_iBestSolution;
   private int                m_iOffer;
   private int                m_iTotalOffer;
   private int                m_iMethod;
   private int                m_iDemandField;
   private IVectorLayer       m_DemandLayer;
   private IVectorLayer       m_OfferLayer;
   private IVectorLayer       m_CandidatesLayer;


   @Override
   public void defineCharacteristics() {

      final String sDistOptions[] = { Sextante.getText("Euclidean"), Sextante.getText("Manhattan") };
      final String sMethod[] = { Sextante.getText("Minimum_sum"), Sextante.getText("Minimum_sum_with_restrictions"),
               Sextante.getText("Minimum_standard_deviation"), Sextante.getText("Minimize_maximum_distance"),
               Sextante.getText("Maximum_coverage"), Sextante.getText("Maximum_sum"), Sextante.getText("Minimum_coverage") };

      setName(Sextante.getText("Location-allocation"));
      setGroup(Sextante.getText("Location-allocation"));
      setUserCanDefineAnalysisExtent(false);
      setIsDeterminatedProcess(false);
      try {
         m_Parameters.addInputVectorLayer(DEMAND, Sextante.getText("Demand_points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addTableField(FIELDDEMAND, Sextante.getText("Field"), DEMAND);
         m_Parameters.addInputVectorLayer(OFFER, Sextante.getText("Preexistent_resources"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, false);
         m_Parameters.addInputVectorLayer(CANDIDATES, Sextante.getText("Candidate_points"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addSelection(DIST, Sextante.getText("Type_of_distance"), sDistOptions);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(NEWLOCATIONS, Sextante.getText("Number_of_resources_to_allocate"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 1, 0, Integer.MAX_VALUE);
         m_Parameters.addNumericalValue(MAXDIST, Sextante.getText("Maximum_distance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         addOutputVectorLayer(SPIDER, Sextante.getText("Connections"), OutputVectorLayer.SHAPE_TYPE_LINE);
         addOutputVectorLayer(NEWLOCATIONS, Sextante.getText("Selected_points"), OutputVectorLayer.SHAPE_TYPE_POINT);
         addOutputText(RESULT, Sextante.getText("Statistics"));
         addOutputNumericalValue(MEAN_DIST, Sextante.getText("Mean_distance"));
         addOutputNumericalValue(MEAN_SQUARED_DISTANCE, Sextante.getText("Mean_squared_distance"));
         addOutputNumericalValue(MIN_DISTANCE, Sextante.getText("Min_distance"));
         addOutputNumericalValue(MAX_DISTANCE, Sextante.getText("Maximum_distance"));
         addOutputNumericalValue(VARIANCE, Sextante.getText("Variance"));
         addOutputNumericalValue(SUM_OF_DISTANCES, Sextante.getText("Sum_of_distances"));
         addOutputNumericalValue(COEFF_OF_VARIATION, Sextante.getText("Coefficient_of_variation"));

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_iDistMethod = m_Parameters.getParameterValueAsInt(DIST);
      m_DemandLayer = m_Parameters.getParameterValueAsVectorLayer(DEMAND);
      m_iDemandField = m_Parameters.getParameterValueAsInt(FIELDDEMAND);
      m_OfferLayer = m_Parameters.getParameterValueAsVectorLayer(OFFER);
      m_CandidatesLayer = m_Parameters.getParameterValueAsVectorLayer(CANDIDATES);
      m_iNewLocations = m_Parameters.getParameterValueAsInt(NEWLOCATIONS);
      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_dMaxDist = m_Parameters.getParameterValueAsDouble(MAXDIST);

      if (m_iNewLocations == 0) {
         return false;
      }

      extractPoints();
      calculateDistanceMatrix();

      m_dBestObjective = Double.MAX_VALUE;
      for (int i = 0; (i < ITERATIONS) && setProgress(i, ITERATIONS); i++) {
         setProgressText(Sextante.getText("Iteration") + " " + Integer.toString(i) + "/" + Integer.toString(ITERATIONS));
         runTeitzBart();
         if (m_dObjective < m_dBestObjective) {
            m_dBestObjective = m_dObjective;
            m_iBestSolution = m_iSolution;
         }
      }

      if (m_Task.isCanceled()) {
         return false;
      }
      else {
         if (m_iBestSolution != null) {
            createResults();
         }
         return true;
      }

   }


   private void extractPoints() throws GeoAlgorithmExecutionException {

      double dWeight;

      m_Demand = new ArrayList();
      m_Offer = new ArrayList();
      m_Candidates = new ArrayList();

      IFeatureIterator iter = m_DemandLayer.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Coordinate coord = feature.getGeometry().getCoordinate();
         try {
            dWeight = Double.parseDouble(feature.getRecord().getValue(m_iDemandField).toString());
         }
         catch (final Exception e) {
            dWeight = 1;
         }
         m_Demand.add(new DemandPoint(coord.x, coord.y, dWeight));
      }
      iter.close();

      iter = m_CandidatesLayer.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Coordinate coord = feature.getGeometry().getCoordinate();
         m_Candidates.add(coord);
      }
      iter.close();

      if (m_OfferLayer != null) {
         iter = m_OfferLayer.iterator();
         while (iter.hasNext()) {
            final IFeature feature = iter.next();
            final Coordinate coord = feature.getGeometry().getCoordinate();
            m_Offer.add(coord);
         }
         iter.close();
      }

      if ((m_Candidates.size() < m_iNewLocations) || (m_Demand.size() == 0)) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Invalid_or_insufficient_data"));
      }

   }


   private void calculateDistanceMatrix() {

      m_iOffer = m_Offer.size();
      m_iTotalOffer = m_Offer.size() + m_Candidates.size();
      final int iDemand = m_Demand.size();
      double x1, y1, x2, y2;
      DemandPoint dpt;
      Coordinate pt;

      m_dDist = new double[iDemand][m_iTotalOffer];

      for (int i = 0; i < m_iTotalOffer; i++) {
         if (i < m_iOffer) {
            pt = (Coordinate) m_Offer.get(i);
         }
         else {
            pt = (Coordinate) m_Candidates.get(i - m_iOffer);
         }
         x1 = pt.x;
         y1 = pt.y;
         for (int j = 0; j < iDemand; j++) {
            dpt = (DemandPoint) m_Demand.get(j);
            x2 = dpt.x;
            y2 = dpt.y;
            if (m_iDistMethod == EUCLIDEAN) {
               m_dDist[j][i] = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            }
            else {
               m_dDist[j][i] = Math.abs((x2 - x1) + (y2 - y1));
            }
         }

      }

   }


   private void runTeitzBart() {

      int i, j;
      int iAlternative = 0;
      int iOldValue;
      int iValueToChange = 0;
      int iRandCount, iCount;
      double dMin;
      boolean bHasImproved;
      boolean bIsSelected;
      m_iSolution = new int[m_iNewLocations];

      for (i = 0; i < m_iSolution.length; i++) {
         m_iSolution[i] = i;
      }

      do {
         bHasImproved = false;
         dMin = getObjectiveFunctionValue();
         iCount = 0;
         iRandCount = (int) Math.floor(Math.random() * (m_Candidates.size() - m_iSolution.length));
         for (i = 0; i < m_Candidates.size(); i++) {
            bIsSelected = false;
            for (j = 0; j < m_iSolution.length; j++) {
               if (m_iSolution[j] == i) {
                  bIsSelected = true;
                  break;
               }
            }
            if (!bIsSelected) {
               if (iRandCount == iCount) {
                  iAlternative = i;
                  break;
               }
               else {
                  iCount++;
               }
            }
         }
         for (i = 0; i < m_iSolution.length; i++) {
            iOldValue = m_iSolution[i];
            m_iSolution[i] = iAlternative;
            m_dObjective = getObjectiveFunctionValue();
            if (m_dObjective < dMin) {
               bHasImproved = true;
               dMin = m_dObjective;
               iValueToChange = i;
            }
            m_iSolution[i] = iOldValue;
         }
         if (bHasImproved) {
            m_iSolution[iValueToChange] = iAlternative;
         }
      }
      while (bHasImproved);

   }


   private double getObjectiveFunctionValue() {

      switch (m_iMethod) {
         case MINSUM:
         default:
            return getObjectiveFunctionValueMinSum();
         case CONSTRAINEDMINSUM:
            return getObjectiveFunctionValueConstrainedMinSum();
         case MINSTDDEV:
            return getObjectiveFunctionValueMinStdDev();
         case MAXSUM:
            return -getObjectiveFunctionValueMinSum();
         case MAXCOV:
            return getObjectiveFunctionValueMaxCov();
         case MINCOV:
            return -getObjectiveFunctionValueMaxCov();
         case MINIMAX:
            return getObjectiveFunctionValueMinMaxDistance();
      }

   }


   private double getObjectiveFunctionValueMinStdDev() {

      int i, j;
      int iPt;
      double dMin;
      double dSum = 0;
      double dVar = 0;


      for (i = 0; i < m_Demand.size(); i++) {
         dMin = Double.MAX_VALUE;
         for (j = 0; j < m_iOffer; j++) {
            if (m_dDist[i][j] < dMin) {
               dMin = m_dDist[i][j];
            }
         }
         for (j = 0; j < m_iSolution.length; j++) {
            iPt = m_iSolution[j];
            if (m_dDist[i][iPt + m_iOffer] < dMin) {
               dMin = m_dDist[i][iPt + m_iOffer];
            }
         }
         //TODO dpt = (DemandPoint) m_Demand.get(i); �NO CONSIDERAMOS PESOS????

         dSum += dMin;
         dVar += dMin * dMin;

      }

      dSum /= m_Demand.size();
      dVar = dVar / m_Demand.size() - dSum * dSum;

      return dVar;

   }


   private double getObjectiveFunctionValueConstrainedMinSum() {

      int i, j;
      int iPt;
      double dValue = 0;
      double dMin;
      DemandPoint dpt;

      for (i = 0; i < m_Demand.size(); i++) {
         dMin = Double.MAX_VALUE;
         for (j = 0; j < m_iOffer; j++) {
            if (m_dDist[i][j] < dMin) {
               dMin = m_dDist[i][j];
            }
         }
         for (j = 0; j < m_iSolution.length; j++) {
            iPt = m_iSolution[j];
            if (m_dDist[i][iPt + m_iOffer] < dMin) {
               dMin = m_dDist[i][iPt + m_iOffer];
            }
         }
         if (dMin < m_dMaxDist) {
            dpt = (DemandPoint) m_Demand.get(i);
            dValue += (dMin * dpt.weight);
         }
         else {
            dValue = Double.POSITIVE_INFINITY;
         }

      }

      return dValue;

   }


   private double getObjectiveFunctionValueMaxCov() {

      int i, j;
      int iPt;
      double dValue = 0;
      double dDemand;;
      DemandPoint dpt;

      for (i = 0; i < m_Demand.size(); i++) {
         dpt = (DemandPoint) m_Demand.get(i);
         dDemand = dpt.weight;
         for (j = 0; j < m_iOffer; j++) {
            if (m_dDist[i][j] < m_dMaxDist) {
               dDemand = 0;
               break;
            }
         }
         if (dDemand != 0) {
            for (j = 0; j < m_iSolution.length; j++) {
               iPt = m_iSolution[j];
               if (m_dDist[i][iPt + m_iOffer] < m_dMaxDist) {
                  dDemand = 0;
                  break;
               }
            }
         }

         dValue += dDemand;
      }

      return dValue;

   }


   private double getObjectiveFunctionValueMinSum() {

      int i, j;
      int iPt;
      double dValue = 0;
      double dMin;
      DemandPoint dpt;

      for (i = 0; i < m_Demand.size(); i++) {
         dMin = Double.MAX_VALUE;
         for (j = 0; j < m_iOffer; j++) {
            if (m_dDist[i][j] < dMin) {
               dMin = m_dDist[i][j];
            }
         }
         for (j = 0; j < m_iSolution.length; j++) {
            iPt = m_iSolution[j];
            if (m_dDist[i][iPt + m_iOffer] < dMin) {
               dMin = m_dDist[i][iPt + m_iOffer];
            }
         }
         dpt = (DemandPoint) m_Demand.get(i);
         dValue += (dMin * dpt.weight);
      }

      return dValue;

   }


   private double getObjectiveFunctionValueMinMaxDistance() {

      int i, j;
      int iPt;
      double dMin, dMax = Double.NEGATIVE_INFINITY;

      for (i = 0; i < m_Demand.size(); i++) {
         dMin = Double.MAX_VALUE;
         for (j = 0; j < m_iOffer; j++) {
            if (m_dDist[i][j] < dMin) {
               dMin = m_dDist[i][j];
            }
         }
         for (j = 0; j < m_iSolution.length; j++) {
            iPt = m_iSolution[j];
            if (m_dDist[i][iPt + m_iOffer] < dMin) {
               dMin = m_dDist[i][iPt + m_iOffer];
            }
         }
         if (dMax < dMin) {
            dMax = dMin;
         }
      }

      return dMax;

   }


   private void createResults() throws GeoAlgorithmExecutionException {

      int i, j;
      int iPt;
      int iID = 1;
      double dMin;
      double dDist;
      DemandPoint dpt;
      Coordinate pt = null;
      LineString line;
      final Coordinate coords[] = new Coordinate[2];
      final Object values[] = new Object[8];
      String sOfferName = null;
      final String[] sNames = { Sextante.getText("ID"), Sextante.getText("Demand_point"), Sextante.getText("X_demand"),
               Sextante.getText("Y_demand"), Sextante.getText("Resource"), Sextante.getText("X_resource"),
               Sextante.getText("Y_resource"), Sextante.getText("Distance") };
      final Class[] types = { Integer.class, String.class, Double.class, Double.class, String.class, Double.class, Double.class,
               Double.class };
      final SimpleStats stats = new SimpleStats();
      final IVectorLayer spiderWebLayer = getNewVectorLayer(SPIDER, Sextante.getText("Conexions"), IVectorLayer.SHAPE_TYPE_LINE,
               types, sNames);
      final IVectorLayer newPoints = getNewVectorLayer(NEWLOCATIONS, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_POINT,
               new Class[] { Integer.class }, new String[] { "ID" });
      final GeometryFactory gf = new GeometryFactory();
      for (i = 0; i < m_Demand.size(); i++) {
         dpt = (DemandPoint) m_Demand.get(i);
         dMin = Double.MAX_VALUE;
         for (j = 0; j < m_iOffer; j++) {
            if (m_dDist[i][j] < dMin) {
               dMin = m_dDist[i][j];
               pt = (Coordinate) m_Offer.get(j);
               sOfferName = Sextante.getText("Preexistent") + Integer.toString(j);
            }
         }
         for (j = 0; j < m_iBestSolution.length; j++) {
            iPt = m_iBestSolution[j];
            if (m_dDist[i][iPt + m_iOffer] < dMin) {
               dMin = m_dDist[i][iPt + m_iOffer];
               pt = (Coordinate) m_Candidates.get(iPt);
               sOfferName = Sextante.getText("Candidate") + Integer.toString(iPt);
            }
         }

         stats.addValue(dMin);
         coords[0] = pt;
         coords[1] = new Coordinate(dpt.x, dpt.y);
         line = gf.createLineString(coords);
         values[0] = new Integer(iID++);
         values[1] = Integer.toString(i);
         values[2] = new Double(dpt.x);
         values[3] = new Double(dpt.y);
         values[4] = sOfferName;
         values[5] = new Double(pt.x);
         values[6] = new Double(pt.y);
         dDist = Math.sqrt(Math.pow(pt.x - dpt.x, 2) + Math.pow(pt.y - dpt.y, 2));
         values[7] = new Double(dDist);
         spiderWebLayer.addFeature(line, values);

      }

      for (j = 0; j < m_iBestSolution.length; j++) {
         iPt = m_iBestSolution[j];
         final Coordinate solutionPt = (Coordinate) m_Candidates.get(iPt);
         newPoints.addFeature(gf.createPoint(solutionPt), new Object[] { new Integer(j + 1) });
      }


      final DecimalFormat df = new DecimalFormat("##.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Result"));
      doc.addHeader(Sextante.getText("Statistics_of_global_solution"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Objective_function") + ": " + df.format(Math.abs(m_dObjective)));
      doc.addListElement(Sextante.getText("Mean_distance") + ": " + df.format(stats.getMean()));
      doc.addListElement(Sextante.getText("Mean_squared_distance") + ": " + df.format(stats.getRMS()));
      doc.addListElement(Sextante.getText("Min_distance") + ": " + df.format(stats.getMin()));
      doc.addListElement(Sextante.getText("Maximum_distance") + ": " + df.format(stats.getMax()));
      doc.addListElement(Sextante.getText("Variance") + ": " + df.format(stats.getVariance()));
      doc.addListElement(Sextante.getText("Sum_of_distances") + ": " + df.format(stats.getSum()));
      doc.addListElement(Sextante.getText("Coefficient_of_variation") + ": " + df.format(stats.getCoeffOfVar()));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(RESULT, Sextante.getText("Statistics"), doc.getHTMLCode());

      addOutputNumericalValue(OBJ_FUNCTION_VALUE, m_dObjective);
      addOutputNumericalValue(MEAN_DIST, stats.getMean());
      addOutputNumericalValue(MEAN_SQUARED_DISTANCE, stats.getRMS());
      addOutputNumericalValue(MIN_DISTANCE, stats.getMin());
      addOutputNumericalValue(MAX_DISTANCE, stats.getMax());
      addOutputNumericalValue(VARIANCE, stats.getVariance());
      addOutputNumericalValue(SUM_OF_DISTANCES, stats.getSum());
      addOutputNumericalValue(COEFF_OF_VARIATION, stats.getMean());

   }

   private class DemandPoint {

      public double x;
      public double y;
      public double weight;


      DemandPoint(final double dX,
                  final double dY,
                  final double dWeight) {

         x = dX;
         y = dY;
         weight = dWeight;

      }
   }
}

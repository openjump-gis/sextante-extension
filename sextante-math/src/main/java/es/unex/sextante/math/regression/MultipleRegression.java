package es.unex.sextante.math.regression;

import java.util.ArrayList;

import Jama.Matrix;


/**
 * A class for performing multiple regressions
 * 
 * @author volaya
 * 
 */
public class MultipleRegression {

   private class MaxValues {

      public int    iMax;

      public double dRMax;

   }

   private final ArrayList m_Y = new ArrayList();

   private final ArrayList m_X[];

   private final double    m_dDCoeff[];

   private final double    m_dRCoeff[];

   private final int       m_iOrder[];


   public MultipleRegression(final int iVariables) {

      m_X = new ArrayList[iVariables];
      for (int i = 0; i < m_X.length; i++) {
         m_X[i] = new ArrayList();
      }
      m_dRCoeff = new double[iVariables + 1];
      m_dDCoeff = new double[iVariables + 1];
      m_iOrder = new int[iVariables + 1];

   }


   public void addValue(final double dX[],
                        final double dY) {

      if (dX.length == m_X.length) {
         m_Y.add(new Double(dY));
         for (int i = 0; i < dX.length; i++) {
            m_X[i].add(new Double(dX[i]));
         }
      }

   }


   public boolean calculate() {

      if ((m_Y.size() > m_iOrder.length - 1) && (m_iOrder.length > 0)) {
         getRegression();
         getCorrelation();
         return true;
      }

      return false;

   }


   private boolean eliminate(final double[] X,
                             final double[] Y) {

      final Regression regression = new Regression();

      if (regression.calculate(X, Y)) {
         for (int i = 0; i < Y.length; i++) {
            Y[i] -= regression.getConstant() + regression.getCoeff() * X[i];
         }

         return true;
      }

      return false;

   }


   public double getCoeff(int iVariable) {

      if ((++iVariable > 0) && (iVariable < m_dDCoeff.length)) {
         return (m_dRCoeff[iVariable]);
      }

      return 0.0;

   }


   public double getConstant() {

      if (m_dRCoeff.length > 1) {
         return (m_dRCoeff[0]);
      }

      return (0.0);
   }


   boolean getCorrelation() {

      int i, k, nVariables, nValues;
      double Values[][], r2_sum;
      final MaxValues maxValues = new MaxValues();

      nVariables = m_iOrder.length;
      nValues = m_Y.size();

      if ((nValues >= nVariables) && (nVariables > 1)) {
         Values = new double[nVariables][nValues];

         for (k = 0; k < nValues; k++) {
            Values[0][k] = ((Double) m_Y.get(k)).doubleValue();
         }

         for (i = 1; i < nVariables; i++) {
            for (k = 0; k < nValues; k++) {
               Values[i][k] = ((Double) m_X[i - 1].get(k)).doubleValue();
            }
         }

         m_iOrder[0] = -1;
         m_dDCoeff[0] = -1;

         for (i = 0, r2_sum = 0.0; i < nVariables - 1; i++) {
            getCorrelation(Values, Values[0], maxValues);

            r2_sum += (1.0 - r2_sum) * maxValues.dRMax;

            m_iOrder[maxValues.iMax] = i;
            m_dDCoeff[maxValues.iMax] = r2_sum;
         }

         return true;
      }

      return false;
   }


   boolean getCorrelation(final double[][] X,
                          final double[] Y,
                          final MaxValues maxValues) {

      int i, n;
      double XMax[];
      final Regression regression = new Regression();

      for (i = 1, n = 0, maxValues.iMax = -1, maxValues.dRMax = 0.0; i < X.length; i++) {
         if ((X[i] != null) && regression.calculate(X[i], Y)) {
            n++;
            if ((maxValues.iMax < 0) || (maxValues.dRMax < regression.getR2())) {
               maxValues.iMax = i;
               maxValues.dRMax = regression.getR2();
            }
         }
      }

      if (n > 1) {
         XMax = X[maxValues.iMax];
         X[maxValues.iMax] = null;

         for (i = 0; i < X.length; i++) {
            if (X[i] != null) {
               eliminate(XMax, X[i]);
            }
         }

         eliminate(XMax, Y);
      }

      return (maxValues.iMax >= 1);
   }


   public int getOrder(int iVariable) {

      if ((++iVariable > 0) && (iVariable < m_iOrder.length)) {
         return (m_iOrder[iVariable]);
      }

      return -1;
   }


   public int getOrdered(final int iOrder) {

      for (int i = 0; i < m_iOrder.length; i++) {
         if (iOrder == m_iOrder[i]) {
            return i - 1;
         }
      }

      return -1;
   }


   public double getR2(int iVariable) {

      if ((++iVariable > 0) && (iVariable < m_dDCoeff.length)) {
         return (m_dDCoeff[iVariable]);
      }

      return 0.0;
   }


   public double getR2Change(final int iVariable) {

      final int iOrder = getOrder(iVariable);

      if (iOrder > 0) {
         return (getR2(iVariable) - getR2(getOrdered(iOrder - 1)));
      }

      if (iOrder == 0) {
         return (getR2(iVariable));
      }

      return (0.0);
   }


   private boolean getRegression() {

      int i, j, k, nVariables, nValues;
      double sum, B[], P[][], X[][], Y[];

      nVariables = m_iOrder.length;
      nValues = m_Y.size();

      B = new double[nVariables];
      P = new double[nVariables][nVariables];

      Y = new double[nValues];
      X = new double[nVariables][nValues];

      for (k = 0; k < nValues; k++) {
         Y[k] = ((Double) m_Y.get(k)).doubleValue();
         X[0][k] = 1.0;
      }

      for (i = 1; i < nVariables; i++) {
         for (k = 0; k < nValues; k++) {
            X[i][k] = ((Double) m_X[i - 1].get(k)).doubleValue();
         }
      }

      for (i = 0; i < nVariables; i++) {
         for (k = 0, sum = 0.0; k < nValues; k++) {
            sum += X[i][k] * Y[k];
         }

         B[i] = sum;

         for (j = 0; j < nVariables; j++) {
            for (k = 0, sum = 0.0; k < nValues; k++) {
               sum += X[i][k] * X[j][k];
            }
            P[i][j] = sum;
         }
      }

      final Matrix m = new Matrix(P);
      Matrix inverse;
      try {
         inverse = m.inverse();
      }
      catch (final Exception e) {
         return false;
      }

      for (i = 0; i < nVariables; i++) {
         for (j = 0, sum = 0.0; j < nVariables; j++) {
            sum += inverse.get(i, j) * B[j];
         }
         m_dRCoeff[i] = sum;
      }

      return true;

   }

}

package es.unex.sextante.math.regression;

import java.text.DecimalFormat;
import java.util.ArrayList;

import Jama.Matrix;
import Jama.QRDecomposition;

/*import es.unex.sextante.libMath.Jama.Matrix;
import es.unex.sextante.libMath.Jama.QRDecomposition;*/


/**
 * Least squares fitting
 * 
 * @author volaya
 * 
 */
public class LeastSquaresFit {

   private final ArrayList m_X     = new ArrayList();

   private final ArrayList m_Y     = new ArrayList();

   private double[]        m_dCoeffs;

   private String          m_sExpression;

   private double          m_dYMin = Double.MAX_VALUE;

   private double          m_dXMax = Double.NEGATIVE_INFINITY;

   private double          m_dXMin = Double.MAX_VALUE;

   private double          m_dYMax = Double.NEGATIVE_INFINITY;


   public void addValue(final double dX,
                        final double dY) {

      setMinMaxX(dX);
      setMinMaxY(dY);

      m_X.add(new Double(dX));
      m_Y.add(new Double(dY));

   }


   public boolean calculate(final int iOrder) {

      double dX, dY;
      final int nk = iOrder + 1;

      final double[][] alpha = new double[nk][nk];
      final double[] beta = new double[nk];
      double term = 0;

      m_dCoeffs = new double[nk];

      final int iNumPoints = m_X.size();

      for (int k = 0; k < nk; k++) {

         for (int j = k; j < nk; j++) {
            term = 0.0;
            alpha[k][j] = 0.0;
            for (int i = 0; i < iNumPoints; i++) {

               double prod1 = 1.0;
               dX = ((Double) m_X.get(i)).doubleValue();
               if (k > 0) {
                  for (int m = 0; m < k; m++) {
                     prod1 *= dX;
                  }
               }
               double prod2 = 1.0;
               if (j > 0) {
                  for (int m = 0; m < j; m++) {
                     prod2 *= dX;
                  }
               }

               term = (prod1 * prod2);

               alpha[k][j] += term;
            }
            alpha[j][k] = alpha[k][j];
         }

         for (int i = 0; i < iNumPoints; i++) {
            dX = ((Double) m_X.get(i)).doubleValue();
            dY = ((Double) m_Y.get(i)).doubleValue();
            double prod1 = 1.0;
            if (k > 0) {
               for (int m = 0; m < k; m++) {
                  prod1 *= dX;
               }
            }
            term = (dY * prod1);

            beta[k] += term;
         }
      }

      final Matrix alpha_matrix = new Matrix(alpha);
      final QRDecomposition alpha_QRD = new QRDecomposition(alpha_matrix);
      final Matrix beta_matrix = new Matrix(beta, nk);
      Matrix param_matrix;
      try {
         param_matrix = alpha_QRD.solve(beta_matrix);
      }
      catch (final Exception e) {
         return false;
      }

      final DecimalFormat df = new DecimalFormat("####.#####");
      final StringBuffer sb = new StringBuffer("");
      for (int k = 0; k < nk; k++) {
         m_dCoeffs[k] = param_matrix.get(k, 0);
         if (k != 0) {
            sb.append(" + " + df.format(m_dCoeffs[k]) + "x^" + Integer.toString(k));
         }
         else {
            sb.append(df.format(m_dCoeffs[k]));
         }
      }

      m_sExpression = sb.toString();

      return true;

   }


   public String getExpression() {

      return m_sExpression;

   }


   public int getNumPoints() {

      return m_X.size();
   }


   public void getPoints(final double[] x,
                         final double[] y) {

      int i;

      for (i = 0; i < m_X.size(); i++) {
         x[i] = ((Double) m_X.get(i)).doubleValue();
         y[i] = ((Double) m_Y.get(i)).doubleValue();
      }

   }


   public double getXMax() {

      return m_dXMax;

   }


   public double getXMin() {

      return m_dXMin;

   }


   public double getY(final double x) {

      int i;
      double dRet = 0;

      for (i = 0; i < m_dCoeffs.length; i++) {
         dRet += m_dCoeffs[i] * Math.pow(x, i);
      }

      return dRet;

   }


   public double getYMax() {

      return m_dYMax;

   }


   public double getYMin() {

      return m_dYMin;

   }


   private void setMinMaxX(final double x) {

      if (x > m_dXMax) {
         m_dXMax = x;
      }
      if (x < m_dXMin) {
         m_dXMin = x;
      }

   }


   private void setMinMaxY(final double y) {

      if (y > m_dYMax) {
         m_dYMax = y;
      }
      if (y < m_dYMin) {
         m_dYMin = y;
      }

   }
}

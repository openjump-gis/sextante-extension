/*******************************************************************************
Regression.java
Copyright (C) Victor Olaya

Adapted from SAGA, System for Automated Geographical Analysis.
Copyrights (c) 2002-2005 by Olaf Conrad
Portions (c) 2002 by Andre Ringeler
Portions (c) 2005 by Victor Olaya

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/

package es.unex.sextante.math.regression;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Simple regression class
 *
 * @author volaya
 *
 */
public class Regression {

   public static final int     REGRESSION_Best_Fit = 0;

   public static final int     REGRESSION_Linear   = 1;              // Y = a + b * X

   public static final int     REGRESSION_Rez_X    = 2;              // Y = a + b / X

   public static final int     REGRESSION_Rez_Y    = 3;              // Y = a / (b - X)

   public static final int     REGRESSION_Pow      = 4;              // Y = a * X^b

   public static final int     REGRESSION_Exp      = 5;              // Y = a * e^(b * X)

   public static final int     REGRESSION_Log      = 6;              // Y = a + b * ln(X)

   private static final double ALMOST_ZERO         = 0.0001;

   private final ArrayList           m_X                 = new ArrayList();

   private final ArrayList           m_Y                 = new ArrayList();

   private double              m_dR;

   private double              m_dCoeff;

   private double              m_dConst;

   private double              m_dXMin, m_dXMax, m_dYMin, m_dYMax;

   private double              m_dXMean, m_dYMean;

   private double              m_dXVar, m_dYVar;

   private int                 m_iType;


   public Regression() {

   }


   private double _X_Transform(double x) {

      switch (m_iType) {
         default:
            return (x);

         case REGRESSION_Rez_X:
            if (x == 0.0) {
               x = ALMOST_ZERO;
            }
            return (1.0 / x);

         case REGRESSION_Pow:
         case REGRESSION_Log:
            if (x <= 0.0) {
               x = ALMOST_ZERO;
            }
            return (Math.log(x));
      }
   }


   private double _Y_Transform(double y) {

      switch (m_iType) {
         default:
            return (y);

         case REGRESSION_Rez_Y:
            if (y == 0.0) {
               y = ALMOST_ZERO;
            }
            return (1.0 / y);

         case REGRESSION_Pow:
         case REGRESSION_Exp:
            if (y <= 0.0) {
               y = ALMOST_ZERO;
            }
            return (Math.log(y));
      }
   }


   public void addValue(final double dX,
                        final double dY) {

      m_X.add(new Double(dX));
      m_Y.add(new Double(dY));

   }


   public boolean calculate() {

      return calculateLinear();

   }


   public boolean calculate(final double[] x,
                            final double[] y) {

      if (x.length == y.length) {
         m_X.clear();
         m_Y.clear();
         for (int i = 0; i < y.length; i++) {
            this.addValue(x[i], y[i]);
         }
         return calculate();
      }
      else {
         return false;
      }

   }


   public boolean calculate(final int iRegressionType) {

      double d;

      if (iRegressionType == REGRESSION_Best_Fit) {
         m_iType = getBestFitType();
      }
      else {
         m_iType = iRegressionType;
      }

      if (calculateLinear()) {

         switch (m_iType) {

            case REGRESSION_Linear:
            default:
               break;

            case REGRESSION_Rez_X:
               m_dXVar = 1.0 / m_dXVar;
               break;

            case REGRESSION_Rez_Y:
               d = m_dConst;
               m_dConst = 1.0 / m_dCoeff;
               m_dCoeff = d * m_dCoeff;
               m_dYVar = 1.0 / m_dYVar;
               break;

            case REGRESSION_Pow:
               m_dConst = Math.exp(m_dConst);
               m_dXVar = Math.exp(m_dXVar);
               m_dYVar = Math.exp(m_dYVar);
               break;

            case REGRESSION_Exp:
               m_dConst = Math.exp(m_dConst);
               m_dYVar = Math.exp(m_dYVar);
               break;

            case REGRESSION_Log:
               m_dXVar = Math.exp(m_dXVar);
               break;
         }

         if (m_iType != REGRESSION_Linear) {
            calculateMinMaxMean();
         }

         return true;
      }

      return false;
   }


   private boolean calculateLinear() {

      int i;
      double x, y, s_xx, s_xy, s_x, s_y, s_dx2, s_dy2, s_dxdy;

      if (m_X.size() > 1) {

         m_dXMean = m_dXMin = m_dXMax = _X_Transform(((Double) m_X.get(0)).doubleValue());
         m_dYMean = m_dYMin = m_dYMax = _Y_Transform(((Double) m_Y.get(0)).doubleValue());

         for (i = 1; i < m_X.size(); i++) {
            m_dXMean += (x = _X_Transform(((Double) m_X.get(i)).doubleValue()));
            m_dYMean += (y = _Y_Transform(((Double) m_Y.get(i)).doubleValue()));
            setMinMaxX(x);
            setMinMaxY(y);
         }

         m_dXMean /= m_X.size();
         m_dYMean /= m_X.size();

         if (m_dXMin < m_dXMax && m_dYMin < m_dYMax) {

            s_x = s_y = s_xx = s_xy = s_dx2 = s_dy2 = s_dxdy = 0.0;

            for (i = 0; i < m_X.size(); i++) {

               x = _X_Transform(((Double) m_X.get(i)).doubleValue());
               y = _Y_Transform(((Double) m_Y.get(i)).doubleValue());

               s_x += x;
               s_y += y;
               s_xx += x * x;
               s_xy += x * y;

               x -= m_dXMean;
               y -= m_dYMean;

               s_dx2 += x * x;
               s_dy2 += y * y;
               s_dxdy += x * y;
            }

            m_dXVar = s_dx2 / m_X.size();
            m_dYVar = s_dy2 / m_X.size();

            m_dCoeff = s_dxdy / s_dx2;
            if (m_X.size() * s_xx - s_x * s_x != 0) {
               m_dConst = (s_xx * s_y - s_x * s_xy) / (m_X.size() * s_xx - s_x * s_x);
            }
            else {
               m_dConst = 0;
            }
            m_dR = s_dxdy / Math.sqrt(s_dx2 * s_dy2);

            return true;
         }
      }

      return false;

   }


   private void calculateMinMaxMean() {

      int i;
      double x, y;

      if (m_X.size() > 1) {
         m_dXMean = m_dXMin = m_dXMax = _X_Transform(((Double) m_X.get(0)).doubleValue());
         m_dYMean = m_dYMin = m_dYMax = _Y_Transform(((Double) m_Y.get(0)).doubleValue());
         for (i = 1; i < m_X.size(); i++) {
            m_dXMean += (x = ((Double) m_X.get(i)).doubleValue());
            m_dYMean += (y = ((Double) m_Y.get(i)).doubleValue());
            setMinMaxX(x);
            setMinMaxY(y);
         }
         m_dXMean /= m_X.size();
         m_dYMean /= m_X.size();
      }

   }


   private int getBestFitType() {

      int i;
      int iType = 1;
      final int iTypes = 6;
      double m_dBestR = 0;

      m_dR = 0;

      for (i = 1; i < iTypes + 1; i++) {
         calculate(i);
         if (m_dR > m_dBestR) {
            iType = i;
            m_dBestR = m_dR;
         }
      }

      return iType;

   }


   public double getCoeff() {

      return m_dCoeff;

   }


   public double getConstant() {

      return m_dConst;

   }


   public String getExpression() {

      final DecimalFormat df = new DecimalFormat("####.####");

      switch (m_iType) {
         case REGRESSION_Linear:
            return " y = " + df.format(m_dConst) + " + " + df.format(m_dCoeff) + "x";

         case REGRESSION_Rez_X:
            return " y = " + df.format(m_dConst) + " + " + df.format(m_dCoeff) + "/x";

         case REGRESSION_Rez_Y:
            return " y = " + df.format(m_dConst) + " / (" + df.format(m_dCoeff) + "- x)";

         case REGRESSION_Pow:
            return " y = " + df.format(m_dConst) + "x^" + df.format(m_dCoeff);

         case REGRESSION_Exp:
            return " y = " + df.format(m_dConst) + " � e^(" + df.format(m_dCoeff) + "x)";

         case REGRESSION_Log:
            return " y = " + df.format(m_dConst) + " + " + df.format(m_dCoeff) + " � ln(x)";
      }

      return "";

   }


   public double getR() {

      return m_dR;

   }


   public double getR2() {

      return m_dR * m_dR;

   }


   public void getRestrictedSample(final double[] x,
                                   final double[] y,
                                   final int nPoints) {

      int i;
      int iIndex;

      for (i = 0; i < nPoints; i++) {
         iIndex = (int) (Math.random() * m_X.size());
         x[i] = ((Double) m_X.get(iIndex)).doubleValue();
         y[i] = ((Double) m_Y.get(iIndex)).doubleValue();
      }

   }


   public double getX(double y) {

      if (m_X.size() > 0.0) {
         switch (m_iType) {
            case REGRESSION_Linear: // Y = a + b * X -> X = (Y - a) / b
               if (m_dCoeff != 0.0) {
                  return ((m_dConst * y) / m_dCoeff);
               }

            case REGRESSION_Rez_X: // Y = a + b / X -> X = b / (Y - a)
               if ((y = y - m_dConst) != 0.0) {
                  return (m_dCoeff / y);
               }

            case REGRESSION_Rez_Y: // Y = a / (b - X) -> X = b - a / Y
               if (y != 0.0) {
                  return (m_dCoeff - m_dConst / y);
               }

            case REGRESSION_Pow: // Y = a * X^b -> X = (Y / a)^(1 / b)
               if (m_dConst != 0.0 && m_dCoeff != 0.0) {
                  return (Math.pow(y / m_dConst, 1.0 / m_dCoeff));
               }

            case REGRESSION_Exp: // Y = a * e^(b * X) -> X = ln(Y / a) / b
               if (m_dConst != 0.0 && (y = y / m_dConst) > 0.0 && m_dCoeff != 0.0) {
                  return (Math.log(y) / m_dCoeff);
               }

            case REGRESSION_Log: // Y = a + b * ln(X) -> X = e^((Y - a) / b)
               if (m_dCoeff != 0.0) {
                  return (Math.exp((y - m_dConst) / m_dCoeff));
               }
         }
      }

      return (Double.NEGATIVE_INFINITY );
   }


   public double getXMax() {

      return m_dXMax;

   }


   public double getXMin() {

      return m_dXMin;

   }


   public double getXVar() {

      return m_dXVar;

   }


   public double getY(double x) {

      if (m_X.size() > 0.0) {
         switch (m_iType) {
            case REGRESSION_Linear: // Y = a + b * X
               return (m_dConst + m_dCoeff * x);

            case REGRESSION_Rez_X: // Y = a + b / X
               if (x != 0.0) {
                  return (m_dConst + m_dCoeff / x);
               }

            case REGRESSION_Rez_Y: // Y = a / (b - X)
               if ((x = m_dCoeff - x) != 0.0) {
                  return (m_dConst / x);
               }

            case REGRESSION_Pow: // Y = a * X^b
               return (m_dConst * Math.pow(x, m_dCoeff));

            case REGRESSION_Exp: // Y = a e^(b * X)
               return (m_dConst * Math.exp(m_dCoeff * x));

            case REGRESSION_Log: // Y = a + b * ln(X)
               if (x > 0.0) {
                  return (m_dConst + m_dCoeff * Math.log(x));
               }
         }
      }

      return (Double.NEGATIVE_INFINITY );
   }


   public double getYMax() {

      return m_dYMax;

   }


   public double getYMin() {

      return m_dYMin;

   }


   public double getYVar() {

      return m_dYVar;

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

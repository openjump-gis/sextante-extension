/*************************************************************
 *
 * A reduced version of Michael Thomas Flanagan's Stat Class
 * with PDFs and CDFs
 *
 * http://www.ee.ucl.ac.uk/~mflanaga/java/Stat.html
 *
 **************************************************************/

package es.unex.sextante.math.pdf;

/**
 * Statistical functions with PDFs and CDFs. Based on Michael Thomas Flanagan's Stat Class
 *
 * @author volaya
 *
 */
public class PDF {

   // GAMMA FUNCTIONS
   // Lanczos Gamma Function approximation - N (number of coefficients -1)
   private static int         lgfN     = 6;

   // Lanczos Gamma Function approximation - Coefficients
   private static double[]    lgfCoeff = { 1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091,
            -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5 };

   // Lanczos Gamma Function approximation - small gamma
   private static double      lgfGamma = 5.0;

   // Maximum number of iterations allowed in Incomplete Gamma Function
   // calculations
   private static int         igfiter  = 1000;

   // Tolerance used in terminating series in Incomplete Gamma Function
   // calculations
   private static double      igfeps   = 1e-8;

   public static final double FPMIN    = 1e-300;


   // BETA DISTRIBUTIONS AND FUNCTIONS
   // beta distribution cdf
   public static double betaCDF(final double alpha,
                                final double beta,
                                final double limit) {

      return betaCDF(0.0D, 1.0D, alpha, beta, limit);
   }


   // beta distribution pdf
   public static double betaCDF(final double min,
                                final double max,
                                final double alpha,
                                final double beta,
                                final double limit) {

      if (alpha <= 0.0D) {
         throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
      }
      if (beta <= 0.0D) {
         throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
      }
      if (limit < min) {
         throw new IllegalArgumentException("limit, " + limit + ", must be greater than or equal to the minimum value, " + min);
      }
      if (limit > max) {
         throw new IllegalArgumentException("limit, " + limit + ", must be less than or equal to the maximum value, " + max);
      }
      return PDF.regularisedBetaFunction(alpha, beta, (limit - min) / (max - min));
   }


   // Beta function
   public static double betaFunction(final double z,
                                     final double w) {

      return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
   }


   // beta distribution pdf
   public static double betaPDF(final double alpha,
                                final double beta,
                                final double x) {

      return betaPDF(0.0D, 1.0D, alpha, beta, x);
   }


   // beta distribution pdf
   public static double betaPDF(final double min,
                                final double max,
                                final double alpha,
                                final double beta,
                                final double x) {

      if (alpha <= 0.0D) {
         throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
      }
      if (beta <= 0.0D) {
         throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
      }
      if (x < min) {
         throw new IllegalArgumentException("x, " + x + ", must be greater than or equal to the minimum value, " + min);
      }
      if (x > max) {
         throw new IllegalArgumentException("x, " + x + ", must be less than or equal to the maximum value, " + max);
      }
      final double pdf = Math.pow(x - min, alpha - 1) * Math.pow(max - x, beta - 1) / Math.pow(max - min, alpha + beta - 1);
      return pdf / PDF.betaFunction(alpha, beta);
   }


   // Returns a binomial mass probabilty function
   public static double binomial(final double p,
                                 final int n,
                                 final int k) {

      if (k < 0 || n < 0) {
         throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
      }
      if (k > n) {
         throw new IllegalArgumentException("\nk is greater than n");
      }
      return Math.floor(0.5D + Math.exp(PDF.logFactorial(n) - PDF.logFactorial(k) - PDF.logFactorial(n - k))) * Math.pow(p, k)
             * Math.pow(1.0D - p, n - k);
   }


   // Returns the binomial cumulative probabilty
   public static double binomialCDF(final double p,
                                    final int n,
                                    final int k) {

      if (p < 0.0D || p > 1.0D) {
         throw new IllegalArgumentException("\np must lie between 0 and 1");
      }
      if (k < 0 || n < 0) {
         throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
      }
      if (k > n) {
         throw new IllegalArgumentException("\nk is greater than n");
      }
      return PDF.regularisedBetaFunction(k, n - k + 1, p);
   }


   // Chi-Square Probability Density Function
   // nu = the degrees of freedom
   public static double chiSquare(double chiSquare,
                                  final int nu) {

      if (nu <= 0) {
         throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
      }
      final double dnu = (double) nu;
      return Math.pow(0.5D, dnu / 2.0D) * Math.pow(chiSquare, dnu / 2.0D - 1.0D) * Math.exp(-chiSquare / 2.0D)
             / PDF.gammaFunction(dnu / 2.0D);
   }


   // CHI SQUARE
   // Chi-Square Cumulative Distribution Function
   // probability that an observed chi-square value for a correct model should
   // be less than chiSquare
   // nu = the degrees of freedom
   public static double chiSquareCDF(final double chiSquare,
                                     final int nu) {

      if (nu <= 0) {
         throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
      }
      return PDF.regularisedGammaFunction((double) nu / 2.0D, chiSquare / 2.0D);
   }


   // Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) =
   // 1 - integral from zero to x of (exp(-t)t^(a-1))dt
   public static double complementaryRegularisedGammaFunction(final double a,
                                                              final double x) {

      if (a < 0.0D || x < 0.0D) {
         throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
      }
      double igf = 0.0D;

      if (x != 0.0D) {
         if (x == 1.0D / 0.0D) {
            igf = 1.0D;
         }
         else {
            if (x < a + 1.0D) {
               // Series representation
               igf = 1.0D - incompleteGammaSer(a, x);
            }
            else {
               // Continued fraction representation
               igf = 1.0D - incompleteGammaFract(a, x);
            }
         }
      }
      return igf;
   }


   // Incomplete fraction summation used in the method regularisedBetaFunction
   // modified Lentz's method
   public static double contFract(final double a,
                                  final double b,
                                  final double x) {

      final int maxit = 500;
      final double eps = 3.0e-7;
      final double aplusb = a + b;
      final double aplus1 = a + 1.0D;
      final double aminus1 = a - 1.0D;
      double c = 1.0D;
      double d = 1.0D - aplusb * x / aplus1;
      if (Math.abs(d) < PDF.FPMIN) {
         d = FPMIN;
      }
      d = 1.0D / d;
      double h = d;
      double aa = 0.0D;
      double del = 0.0D;
      int i = 1, i2 = 0;
      boolean test = true;
      while (test) {
         i2 = 2 * i;
         aa = i * (b - i) * x / ((aminus1 + i2) * (a + i2));
         d = 1.0D + aa * d;
         if (Math.abs(d) < PDF.FPMIN) {
            d = FPMIN;
         }
         c = 1.0D + aa / c;
         if (Math.abs(c) < PDF.FPMIN) {
            c = FPMIN;
         }
         d = 1.0D / d;
         h *= d * c;
         aa = -(a + i) * (aplusb + i) * x / ((a + i2) * (aplus1 + i2));
         d = 1.0D + aa * d;
         if (Math.abs(d) < PDF.FPMIN) {
            d = FPMIN;
         }
         c = 1.0D + aa / c;
         if (Math.abs(c) < PDF.FPMIN) {
            c = FPMIN;
         }
         d = 1.0D / d;
         del = d * c;
         h *= del;
         i++;
         if (Math.abs(del - 1.0D) < eps) {
            test = false;
         }
         if (i > maxit) {
            test = false;
         }
      }
      return h;

   }


   // Hyperbolic cosine of a double number
   public static double cosh(double a) {

      return 0.5D * (Math.exp(a) + Math.exp(-a));
   }


   // Error Function
   public static double erf(final double x) {

      double erf = 0.0D;
      if (x != 0.0) {
         if (x == 1.0D / 0.0D) {
            erf = 1.0D;
         }
         else {
            if (x >= 0) {
               erf = PDF.regularisedGammaFunction(0.5, x * x);
            }
            else {
               erf = -PDF.regularisedGammaFunction(0.5, x * x);
            }
         }
      }
      return erf;
   }


   // Exponential probability
   public static double exponential(final double mu,
                                    final double sigma,
                                    final double x) {

      double arg = (x - mu) / sigma;
      double y = 0.0D;
      if (arg >= 0.0D) {
         y = Math.exp(-arg) / sigma;
      }
      return y;
   }


   // Exponential cumulative distribution function
   // probability that a variate will assume a value less than the upperlimit
   public static double exponentialCDF(final double mu,
                                       final double sigma,
                                       final double upperlimit) {

      double arg = (upperlimit - mu) / sigma;
      double y = 0.0D;
      if (arg > 0.0D) {
         y = 1.0D - Math.exp(-arg);
      }
      return y;
   }


   // factorial of n
   // Argument is of type double but must be, numerically, an integer
   // factorial returned as double but is, numerically, should be an integer
   // numerical rounding may makes this an approximation after n = 21
   public static double factorial(final double n) {

      if (n < 0 || (n - Math.floor(n)) != 0) {
         throw new IllegalArgumentException(
                  "\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
      }
      double f = 1.0D;
      for (int i = 1; i <= n; i++) {
         f *= i;
      }
      return f;
   }


   // Gamma distribution - standard
   // probablity density function
   public static double gamma(final double gamma,
                              double x) {

      if (x < 0.0D) {
         throw new IllegalArgumentException("The variable, x, " + x + "must be equal to or greater than zero");
      }
      if (gamma <= 0.0D) {
         throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
      }
      return Math.pow(x, gamma - 1) * Math.exp(-x) / gammaFunction(gamma);
   }


   // Gamma distribution - standard
   // cumulative distribution function
   public static double gammaCDF(final double gamma,
                                 final double upperLimit) {

      if (upperLimit < 0.0D) {
         throw new IllegalArgumentException("The upper limit, " + upperLimit + "must be equal to or greater than zero");
      }
      if (gamma <= 0.0D) {
         throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
      }
      return regularisedGammaFunction(gamma, upperLimit);
   }


   // Gamma function
   // Lanczos approximation (6 terms)
   public static double gammaFunction(double x) {

      double xcopy = x;
      double first = x + lgfGamma + 0.5;
      double second = lgfCoeff[0];
      double fg = 0.0D;

      if (x >= 0.0) {
         if (x >= 1.0D && x - (int) x == 0.0D) {
            fg = PDF.factorial(x) / x;
         }
         else {
            first = Math.pow(first, x + 0.5) * Math.exp(-first);
            for (int i = 1; i <= lgfN; i++) {
               second += lgfCoeff[i] / ++xcopy;
            }
            fg = first * Math.sqrt(2.0 * Math.PI) * second / x;
         }
      }
      else {
         fg = -Math.PI / (x * PDF.gammaFunction(-x) * Math.sin(Math.PI * x));
      }
      return fg;
   }


   // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of
   // (exp(-t)t^(a-1))dt
   // Continued Fraction representation of the function - valid for x >= a + 1
   // This method follows the general procedure used in Numerical Recipes for
   // C,
   // The Art of Scientific Computing
   // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
   // Cambridge University Press, http://www.nr.com/
   public static double incompleteGammaFract(final double a,
                                             double x) {

      if (a < 0.0D || x < 0.0D) {
         throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
      }
      if (x < a + 1) {
         throw new IllegalArgumentException("\nx < a+1   Use Series Representation");
      }

      int i = 0;
      double ii = 0;
      double igf = 0.0D;
      boolean check = true;

      final double loggamma = PDF.logGamma(a);
      double numer = 0.0D;
      double incr = 0.0D;
      double denom = x - a + 1.0D;
      double first = 1.0D / denom;
      double term = 1.0D / FPMIN;
      double prod = first;

      while (check) {
         ++i;
         ii = (double) i;
         numer = -ii * (ii - a);
         denom += 2.0D;
         first = numer * first + denom;
         if (Math.abs(first) < PDF.FPMIN) {
            first = PDF.FPMIN;
         }
         term = denom + numer / term;
         if (Math.abs(term) < PDF.FPMIN) {
            term = PDF.FPMIN;
         }
         first = 1.0D / first;
         incr = first * term;
         prod *= incr;
         if (Math.abs(incr - 1.0D) < igfeps) {
            check = false;
         }
         if (i >= PDF.igfiter) {
            check = false;
         }
      }
      igf = 1.0D - Math.exp(-x + a * Math.log(x) - loggamma) * prod;
      return igf;
   }


   // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of
   // (exp(-t)t^(a-1))dt
   // Series representation of the function - valid for x < a + 1
   public static double incompleteGammaSer(double a,
                                           double x) {

      if (a < 0.0D || x < 0.0D) {
         throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
      }
      if (x >= a + 1) {
         throw new IllegalArgumentException("\nx >= a+1   use Continued Fraction Representation");
      }

      int i = 0;
      double igf = 0.0D;
      boolean check = true;

      final double acopy = a;
      double sum = 1.0 / a;
      double incr = sum;
      final double loggamma = PDF.logGamma(a);

      while (check) {
         ++i;
         ++a;
         incr *= x / a;
         sum += incr;
         if (Math.abs(incr) < Math.abs(sum) * PDF.igfeps) {
            igf = sum * Math.exp(-x + acopy * Math.log(x) - loggamma);
            check = false;
         }
         if (i >= PDF.igfiter) {
            check = false;
            igf = sum * Math.exp(-x + acopy * Math.log(x) - loggamma);
         }
      }
      return igf;
   }


   // log to base e of the factorial of n
   // Argument is of type double but must be, numerically, an integer
   // log[e](factorial) returned as double
   // numerical rounding may makes this an approximation
   public static double logFactorial(final double n) {

      if (n < 0 || (n - Math.floor(n)) != 0) {
         throw new IllegalArgumentException(
                  "\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
      }
      double f = 0.0D;
      double iCount = 2.0D;
      while (iCount <= n) {
         f += Math.log(iCount);
         iCount += 1.0D;
      }
      return f;
   }


   // log to base e of the factorial of n
   // log[e](factorial) returned as double
   // numerical rounding may makes this an approximation
   public static double logFactorial(final int n) {

      if (n < 0 || (n - (int) n) != 0) {
         throw new IllegalArgumentException(
                  "\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
      }
      double f = 0.0D;
      for (int i = 2; i <= n; i++) {
         f += Math.log(i);
      }
      return f;
   }


   // log to base e of the Gamma function
   // Lanczos approximation (6 terms)
   // Retained for backward compatibility
   public static double logGamma(final double x) {

      double xcopy = x;
      double fg = 0.0D;
      double first = x + lgfGamma + 0.5;
      double second = lgfCoeff[0];

      if (x >= 0.0) {
         if (x >= 1.0 && x - (int) x == 0.0) {
            fg = logFactorial(x) - Math.log(x);
         }
         else {
            first -= (x + 0.5) * Math.log(first);
            for (int i = 1; i <= lgfN; i++) {
               second += lgfCoeff[i] / ++xcopy;
            }
            fg = Math.log(Math.sqrt(2.0 * Math.PI) * second / x) - first;
         }
      }
      else {
         fg = Math.PI / (PDF.gammaFunction(1.0D - x) * Math.sin(Math.PI * x));

         if (fg != 1.0 / 0.0 && fg != -1.0 / 0.0) {
            if (fg < 0) {
               throw new IllegalArgumentException("\nThe gamma function is negative");
            }
            else {
               fg = Math.log(fg);
            }
         }
      }
      return fg;
   }


   // Logistic probability density function
   // mu = location parameter, beta = scale parameter
   public static double logistic(final double mu,
                                 final double beta,
                                 final double x) {

      return square(sech((x - mu) / (2.0D * beta))) / (4.0D * beta);
   }


   // Logistic cumulative distribution function
   // probability that a variate will assume a value less than the upperlimit
   // mu = location parameter, beta = scale parameter
   public static double logisticCDF(final double mu,
                                    final double beta,
                                    final double upperlimit) {

      return 0.5D * (1.0D + tanh((upperlimit - mu) / (2.0D * beta)));
   }


   // Lorentzian probability
   public static double lorentzian(final double mu,
                                   final double gamma,
                                   final double x) {

      final double arg = gamma / 2.0D;
      return (1.0D / Math.PI) * arg / (square(mu - x) + arg * arg);
   }


   // Lorentzian cumulative distribution function
   // probability that a variate will assume a value less than the upperlimit
   public static double lorentzianCDF(final double mu,
                                      final double gamma,
                                      final double upperlimit) {

      final double arg = (upperlimit - mu) / (gamma / 2.0D);
      return (1.0D / Math.PI) * (Math.atan(arg) + Math.PI / 2.0);
   }


   // Gaussian (normal) probability
   // mean = the mean, sd = standard deviation
   public static double normal(final double mean,
                               final double sd,
                               final double x) {

      return Math.exp(-square((x - mean) / sd) / 2.0) / (sd * Math.sqrt(2.0D * Math.PI));
   }


   // Gaussian (normal) cumulative distribution function
   // probability that a variate will assume a value less than the upperlimit
   // mean = the mean, sd = standard deviation
   public static double normalCDF(final double mean,
                                  final double sd,
                                  final double upperlimit) {

      final double arg = (upperlimit - mean) / (sd * Math.sqrt(2.0));
      return (1.0D + PDF.erf(arg)) / 2.0D;
   }


   // Poisson Probability Function
   // k is an integer greater than or equal to zero
   // mean = mean of the Poisson distribution
   public static double poisson(final int k,
                                double mean) {

      if (k < 0) {
         throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
      }
      return Math.pow(mean, k) * Math.exp(-mean) / PDF.factorial((double) k);
   }


   // Cumulative Poisson Probability Function
   // probability that a number of Poisson random events will occur between 0
   // and k (inclusive)
   // k is an integer greater than equal to 1
   // mean = mean of the Poisson distribution
   public static double poissonCDF(final int k,
                                   final double mean) {

      if (k < 1) {
         throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
      }
      return PDF.complementaryRegularisedGammaFunction((double) k, mean);
   }


   // Regularised Incomplete Beta function
   // Continued Fraction approximation (see Numerical recipies for details of
   // method)
   public static double regularisedBetaFunction(final double z,
                                                final double w,
                                                final double x) {

      if (x < 0.0D || x > 1.0D) {
         throw new IllegalArgumentException("Argument x, " + x + ", must be lie between 0 and 1 (inclusive)");
      }
      double ibeta = 0.0D;
      if (x == 0.0D) {
         ibeta = 0.0D;
      }
      else {
         if (x == 1.0D) {
            ibeta = 1.0D;
         }
         else {
            // Term before continued fraction
            ibeta = Math.exp(PDF.logGamma(z + w) - PDF.logGamma(z) - logGamma(w) + z * Math.log(x) + w * Math.log(1.0D - x));
            // Continued fraction
            if (x < (z + 1.0D) / (z + w + 2.0D)) {
               ibeta = ibeta * PDF.contFract(z, w, x) / z;
            }
            else {
               // Use symmetry relationship
               ibeta = 1.0D - ibeta * PDF.contFract(w, z, 1.0D - x) / w;
            }
         }
      }
      return ibeta;
   }


   // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of
   // (exp(-t)t^(a-1))dt
   public static double regularisedGammaFunction(final double a,
                                                 final double x) {

      if (a < 0.0D || x < 0.0D) {
         throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
      }
      double igf = 0.0D;

      if (x < a + 1.0D) {
         // Series representation
         igf = incompleteGammaSer(a, x);
      }
      else {
         // Continued fraction representation
         igf = incompleteGammaFract(a, x);
      }
      return igf;
   }


   // Hyperbolic secant of a double number
   public static double sech(final double a) {

      return 1.0D / cosh(a);
   }


   /* returns -1 if x < 0 else returns 1 */
   // double version
   public static double sign(final double x) {

      if (x < 0.0) {
         return -1.0;
      }
      else {
         return 1.0;
      }
   }


   // Hyperbolic sine of a double number
   public static double sinh(double a) {

      return 0.5D * (Math.exp(a) - Math.exp(-a));
   }


   // Square of a double number
   public static double square(final double a) {

      return a * a;
   }


   // Returns the Student's t probability density function
   public static double studentT(final double tValue,
                                 final int df) {

      final double ddf = (double) df;
      double dfterm = (ddf + 1.0D) / 2.0D;
      return ((PDF.gammaFunction(dfterm) / PDF.gammaFunction(ddf / 2)) / Math.sqrt(ddf * Math.PI))
             * Math.pow(1.0D + tValue * tValue / ddf, -dfterm);
   }


   // Returns the Student's t cumulative distribution function probability
   public static double studentTCDF(final double tValue,
                                    final int df) {

      final double ddf = (double) df;
      final double x = ddf / (ddf + tValue * tValue);
      return 0.5D * (1.0D + (PDF.regularisedBetaFunction(ddf / 2.0D, 0.5D, 1) - PDF.regularisedBetaFunction(ddf / 2.0D, 0.5D, x))
                            * PDF.sign(tValue));
   }


   // Hyperbolic tangent of a double number
   public static double tanh(final double a) {

      return sinh(a) / cosh(a);
   }


   // Weibull probability
   public static double weibull(final double mu,
                                final double sigma,
                                final double gamma,
                                final double x) {

      final double arg = (x - mu) / sigma;
      double y = 0.0D;
      if (arg >= 0.0D) {
         y = (gamma / sigma) * Math.pow(arg, gamma - 1.0D) * Math.exp(-Math.pow(arg, gamma));
      }
      return y;
   }


   // Weibull cumulative distribution function
   // probability that a variate will assume a value less than the upperlimit
   public static double weibullCDF(final double mu,
                                   final double sigma,
                                   final double gamma,
                                   final double upperlimit) {

      final double arg = (upperlimit - mu) / sigma;
      double y = 0.0D;
      if (arg > 0.0D) {
         y = 1.0D - Math.exp(-Math.pow(arg, gamma));
      }
      return y;
   }

}

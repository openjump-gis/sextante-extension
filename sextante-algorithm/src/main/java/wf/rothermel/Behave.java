/**
 * A modified version of the Behave class by Andreas Bachmann,
 * available at  http://www.geo.unizh.ch/gis/research/edmg/fire/unc.html
 *
 * @author: Andreas Bachman, Victor Olaya
 *
 *
 */
package wf.rothermel;

public class Behave {


   /**
    * INSTANCE VARS
    */

   public boolean isCalculated = false;
   public boolean canDerive    = true;

   // Rothermel's model input variables
   public int     fuelModel    = 0;          // fuel model nr
   public double  w0_d1        = 0.;         // Fuel loading [kg/m2]
   public double  w0_d2        = 0.;
   public double  w0_d3        = 0.;
   public double  w0_lh        = 0.;
   public double  w0_lw        = 0.;
   public double  sv_d1        = 0.;         // surface to volume ratio [1/m]
   public double  sv_d2        = 357.6115;
   public double  sv_d3        = 98.4252;
   public double  sv_lh        = 4921.2598;
   public double  sv_lw        = 4921.2598;
   public double  depth        = 0.;         // fuel bed depth [m]
   public double  rho_p        = 512.72341;  // particle density [kg/m3]
   public double  heat         = 18606.70194; // particle low heat content [kJ/kg]
   public double  s_t          = 5.5;        // total mineral content [%]
   public double  s_e          = 1.0;        // effective mineral content [%]
   public double  mx           = 0.;         // moisture of extinction, dead fuel [%]
   public double  m_d1         = 0.;         // fuel moisture [%]
   public double  m_d2         = 0.;
   public double  m_d3         = 0.;
   public double  m_lh         = 0.;
   public double  m_lw         = 0.;

   public double  wsp          = 0.;         // wind speed [m/s]
   public double  wdr          = 0.;         // wind dir [Degree],Northern wind = 0.0!
   public double  slp          = 0.;         // slope [Degree]
   public double  asp          = 0.;         // aspect [Degree] southfacing = 180 !

   // additional variables...
   public double  rho_b        = 0.;         // bulk density     [kg/m3]
   public double  beta         = 0.;         // packing ratio
   public double  beta_opt     = 0.;         // optimal packing ratio
   public double  beta_ratio   = 0.;         // ratio mean/optimal packing ratio
   public double  w_n          = 0.;         // net fuel loading
   public double  eta_s        = 0.;         // mineral damping coefficient
   public double  eta_M        = 0.;         // moisture damping coefficient
   public double  xi           = 0.;         // propagating flux ratio
   public double  A            = 0.;
   public double  gamma        = 0.;         // potential reaction velocity [1/s]
   public double  gamma_max    = 0.;         // maximum reaction velocity [1/s]
   public double  I_r          = 0.;         // reaction intensity [kW/m2]
   public double  phi_s        = 0.;         // slope factor
   public double  B, C, E = 0.;
   public double  phi_w        = 0.;         // wind factor
   public double  phi_t        = 0.;         // combined slope/wind factor
   public double  vx, vy, vl = 0.;           // Vector components
   public double  ecc          = 0;          // eccentricity

   public double  asp_r;                     // radians equivalent of asp
   public double  slp_r;                     // radians equivalent of slp
   public double  wdr_r;                     // radians equivalent of wdr
   public double  sdr_r;                     // radians equivalent of sdr
   public double  sin_asp;
   public double  cos_asp;
   public double  sin_wdr;
   public double  cos_wdr;
   public double  tan_slp;

   public double  al;
   public double  splitDeg, splitRad;
   public double  cos_splitRad, sin_splitRad;
   public double  alDeg, alRad;

   public double  sw_d1, sw_d2, sw_d3, sw_lh, sw_lw, sw_d, sw_l, sw_t = 0.;
   public double  s2w_d, s2w_l, s2w_t = 0.;
   public double  sw2_d, sw2_l, sw2_t = 0.;
   public double  swm_d, swm_l, swm_t = 0.;
   public double  sigma        = 0.;
   public double  w0           = 0.;
   public double  wn_d1, wn_d2, wn_d3, wn_lh, wn_lw, wn_d, wn_l;

   public double  eps_d1, eps_d2, eps_d3, eps_lh, eps_lw;
   public double  q_d1, q_d2, q_d3, q_lh, q_lw;
   public double  hskz;

   public double  hn_d1, hn_d2, hn_d3, hn_lh, hn_lw = 0.;
   public double  sumhd, sumhl, sumhdm = 0.;
   public double  W            = 0.;                                       // W' ratio of "fine" fuel loading
   public double  eta_Ml, eta_Md = 0.;                                     // damping coefficient
   public double  rm_d, rm_l = 0.;                                         // moisture ratio
   public double  Mf_dead      = 0.;                                       // Moisture content of dead fine fuel
   public double  Mx_live      = 0.;                                       // Moisture of extinction of living fuel
   public double  dead, live = 0.;

   // resulting variables
   public double  sdr          = 0.;                                       // spread direction           [degree]
   public double  efw          = 0.;                                       // effective wind speed       [m/s]
   public double  hsk          = 0.;                                       // heat sink term             [kJ/m3]
   public double  ros          = 0.;                                       // rate of spread             [m/s]
   public double  tau          = 0.;                                       // flame residence time       [s]
   public double  hpa          = 0.;                                       // heat release per unit area [kJ/m2]
   public double  fzd          = 0.;                                       // flame zone depth           [m]
   public double  fli          = 0.;                                       // fire line intensity        [kW/m]
   public double  fln          = 0.;                                       // flame length               [m]


   public boolean setFuelModel(final int model) {

      if (model != this.fuelModel) {
         this.fuelModel = model;
         switch (fuelModel) {
            case 1:
               setFuelModelValues(0.18, 0., 0., 0., 0., 0.3048, 11482.940, 12.);
               break;
            case 2:
               setFuelModelValues(0.486, 0.243, 0.122, 0.122, 0., 0.3048, 9842.0, 15.);
               break;
            case 3:
               setFuelModelValues(0.732, 0., 0., 0., 0., 0.762, 4921.2598, 25);
               break;
            case 4:
               setFuelModelValues(1.218, 0.975, 0.486, 1.218, 0., 1.8288, 6561.67979, 20);
               break;
            case 5:
               setFuelModelValues(0.243, 0.122, 0., 0., 0.122, 0.6096, 6561.680, 20);
               break;
            case 6:
               setFuelModelValues(0.365, 0.608, 0., 0., 0.486, 0.732, 5741.47, 25);
               break;
            case 7:
               setFuelModelValues(0.275, 0.645, 0.365, 0., 0.090, 0.762, 5741.47, 40);
               break;
            /*case 8:
            	setFuelmodelValues(0.365, 0.243, 0.6075, 0.0, 0.06096, *, 30);
            	break;
            case 9:
            	setFuelmodelValues(0.7046, 0.0996, 0.03645, 0.0, 0.06096, *, 25);
            	break;
            case 10:
            	setFuelmodelValues(0.729, 0.486, 1.215, 0.486, 0.3048, * 25);
            	break;
            case 11:
            	setFuelmodelValues(0.365, 1.093, 1.3365, 0.0, 0.3048, *, 15);
            	break;
            case 12:
            	setFuelmodelValues(0.972, 3.402, 4.0095, 0.0, 0.6992,*, 20);
            	break;
            case 13:
            	setFuelmodelValues(1.701, 5.589, 6.804, 0.0, 0.9144,*, 25);
            	break;*/
            default:
               return false;
         }
      }
      else if (model < 1 || model > 13) {
         return false;
      }

      return true;

   }


   private void setFuelModelValues(final double wd1,
                                   final double wd2,
                                   final double wd3,
                                   final double wlh,
                                   final double wlw,
                                   final double dep,
                                   final double sv,
                                   final double mox) {

      w0_d1 = wd1;
      w0_d2 = wd2;
      w0_d3 = wd3;
      w0_lh = wlh;
      w0_lw = wlw;
      depth = dep;
      sv_d1 = sv;
      mx = mox;
      w0 = w0_d1 + w0_d2 + w0_d3 + w0_lh + w0_lw;

   }


   public void setWindSpeed(final double speed) {
      this.wsp = speed;
   }


   public void setWindDir(final double dir) {
      this.wdr = dir;
   }


   public void setSlope(final double slope) {
      this.slp = slope;
   }


   public void setAspect(final double aspect) {
      this.asp = aspect;
   }


   public void setDeadMoisture1(final double moisture) {
      this.m_d1 = moisture;
   }


   public void setDeadMoisture10(final double moisture) {
      this.m_d2 = moisture;
   }


   public void setDeadMoisture100(final double moisture) {
      this.m_d3 = moisture;
   }


   public void setHerbMoisture(final double moisture) {
      this.m_lh = moisture;
   }


   public void setWoodyMoisture(final double moisture) {
      this.m_lw = moisture;
   }


   public static double getRosInDir(final double dRos,
                                    final double dMaxDirection,
                                    final double dDirection,
                                    final double dEcc) {

      double dDir;

      if ((dDir = Math.abs(dMaxDirection - dDirection)) > 180.) {
         dDir = 360. - dDir;
      }

      return (dRos * (1. - dEcc) / (1. - dEcc * Math.cos(Math.toRadians(dDir))));

   }


   public void calc() {
      calcRothermel();
   }


   /******************************************************************************************************************************
    * calcRothermel
    *
    * the main logic of rothermel wildfire behaviour calculation
    *
    *****************************************************************************************************************************/
   private void calcRothermel() {

      // reset flags
      isCalculated = false;
      canDerive = true;

      /* prepare fuel parameters */
      calcFuel();

      /* mineral damping coefficient:  eta_s */
      /*   Rothermel 1972: eq. (62)  */
      eta_s = 0.174 * Math.pow(s_e / 100, -0.19);

      /* moisture damping coefficient: eta_M */
      moistureDamping();

      /* reaction velocity: gamma */
      reactionVelocity();

      /* reaction intensity */
      I_r = gamma * heat * eta_s * eta_M;

      /* propagating flux ratio: xi
      	Rothermel 1972: eq. (42)
      	Formula:
      	with sigma[1/ft]:
      	  xi = exp[(0.792 + 0.681*            sqrt(sigma))*(beta + 0.1)] /
      	       (192 + 0.259*sigma)
      	with sigma[1/m] :
      	  xi = exp[(0.792 + 0.681*sqrt(.3048)*sqrt(sigma))*(beta + 0.1)] /
      	       (192 + 0.259*0.3048*sigma)
      */
      xi = Math.exp((0.792 + 0.37597 * Math.sqrt(sigma)) * (beta + 0.1)) / (192 + 0.0791 * sigma);

      /* heat sink: hsk */
      heatSink();

      /* forward rate of spread   */
      /*     no wind and no slope */
      ros = I_r * xi / hsk;

      /*     wind and slope       */
      combinedWindAndSlopeFactor(); // -> phi_t

      if (phi_t > 0.) {
         ros = I_r * xi * (1 + phi_t) / hsk; // (52), [m/s]
      }

      /** ******************************************************************** */
      /* additional fire behaviour results                                   */
      //
      /* flame residence time: tau               */
      /* Anderson 1969, in Albini (1976), p.91:  */
      /* tau = 384/ sigma   [min]                */
      tau = 384. * 60 / (sigma * 0.3048); // [s]

      /* heat release per unit area: hpa */
      hpa = I_r * tau;

      /* flame zone depth	*/
      fzd = ros * tau;

      /* fireline intensity */
      fli = I_r * fzd;

      /* flame length                                   */
      /* based on Byram (1959), in Albini (1976), p. 91 */
      fln = 0.0775 * Math.pow(fli, 0.46);

      /* it's over...*/
      isCalculated = true;
   }


   /******************************************************************************************************************************
    * calcFuel
    *
    * calculates: - characteristic surface-to-volume ration (sigma) - bulk densities (rho_b) - packing ratios (beta, beta_opt,
    * beta_ratio) - net fuel loadings (wn_..)
    *
    *
    * Exceptions are thrown if - w0 <= 0. no fuel specified - sw_t <= 0. surface-to-voume-ratios not properly specified - depth <=
    * 0. depth of fuel bed not properly specified
    *****************************************************************************************************************************/
   protected void calcFuel() {

      /* reset all values to 0. ***************************/
      sigma = 0.;
      rho_b = 0.;
      beta = 0.;
      beta_opt = 0.;
      beta_ratio = 0.;
      // sw_
      sw_d1 = 0.;
      sw_d2 = 0.;
      sw_d3 = 0.;
      sw_lh = 0.;
      sw_lw = 0.;
      s2w_d = 0.;
      s2w_l = 0.;
      s2w_t = 0.;
      sw2_d = 0.;
      sw2_l = 0.;
      sw2_t = 0.;
      swm_d = 0.;
      swm_l = 0.;
      //
      wn_d1 = 0.;
      wn_d2 = 0.;
      wn_d3 = 0.;
      wn_lh = 0.;
      wn_lw = 0.;
      wn_d = 0.;
      wn_l = 0.;
      /** ********************************************** */

      // auxiliary variables
      sw_d1 = sv_d1 * w0_d1;
      sw_d2 = sv_d2 * w0_d2;
      sw_d3 = sv_d3 * w0_d3;
      sw_lh = sv_lh * w0_lh;
      sw_lw = sv_lw * w0_lw;
      sw_d = sw_d1 + sw_d2 + sw_d3;
      sw_l = sw_lh + sw_lw;
      sw_t = sw_d + sw_l;
      //
      s2w_d = sw_d1 * sv_d1 + sw_d2 * sv_d2 + sw_d3 * sv_d3;
      s2w_l = sw_lh * sv_lh + sw_lw * sv_lw;
      s2w_t = s2w_d + s2w_l;
      //
      sw2_d = sw_d1 * w0_d1 + sw_d2 * w0_d2 + sw_d3 * w0_d3;
      sw2_l = sw_lh * w0_lh + sw_lw * w0_lw;
      sw2_t = sw2_d + sw2_l;
      //
      swm_d = sw_d1 * m_d1 + sw_d2 * m_d2 + sw_d3 * m_d3;
      swm_l = sw_lh * m_lh + sw_lw * m_lw;

      sigma = s2w_t / sw_t;

      /**
       * mean bulk density Rothermel 1972: eq. (74)
       */
      // see further down "beta"
      // rho_b should not be bigger than 0.5 of the particle density
      //
      rho_b = w0 / depth;
      /**
       * packing ratios
       */
      // mean packing ratio
      beta = rho_b / rho_p;
      // should be between 0. and 0.5?
      // in Rothermel 1972, p.18-19, values are between 0 and 0.12
      if ((beta > 1) || (beta < 0)) {
         System.out.println("Mean packing ration [beta] out of limits [0,1]: " + beta);
      }

      // optimal packing ratio
      beta_opt = 8.8578 * Math.pow(sigma, -0.8189);

      // ratio mean / optimal packing
      beta_ratio = beta / beta_opt;

      /**
       * Net fuel loading Rothermel 1972: eq. (60), adjusted by Albini 1976, p.88
       */
      wn_d1 = w0_d1 * (1 - s_t / 100);
      wn_d2 = w0_d2 * (1 - s_t / 100);
      wn_d3 = w0_d3 * (1 - s_t / 100);
      wn_lh = w0_lh * (1 - s_t / 100);
      wn_lw = w0_lw * (1 - s_t / 100);
      // Rothermel 1972: eq. (59)
      if (sw_d > 0) {
         wn_d = (1 - s_t / 100) * sw2_d / sw_d;
      }
      if (sw_l > 0) {
         wn_l = (1 - s_t / 100) * sw2_l / sw_l;
      }
   }


   //END calcFuel()

   /******************************************************************************************************************************
    * phi_s: slope factor
    *
    * called from combinedWindAndSlopeFactor()
    *****************************************************************************************************************************/
   protected void slopeFactor() {
      slp_r = Math.toRadians(slp);
      tan_slp = Math.tan(slp_r);
      phi_s = 5.275 * Math.pow(beta, -0.3) * Math.pow(tan_slp, 2);
   }


   /******************************************************************************************************************************
    * phi_w: wind factor
    *
    * called from combinedWindAndSlopeFactor()
    *
    * conversion: sigma [1/ft] = sigma[1/m] * 0.3048! original formulae in Rothermel 1972, eq. XXXXX B = 0.013298 *
    * Math.pow(sigma,0.54); C = 7.47 * Math.exp(-0.06919 * Math.pow(sigma,0.55)); E = 0.715 * Math.exp(0.0001094 * sigma);
    *
    *****************************************************************************************************************************/
   protected void windFactor() {
      B = 0.02526 * Math.pow(sigma * 0.3048, 0.54);
      C = 7.47 * Math.exp(-0.133 * Math.pow(sigma * 0.3048, 0.55));
      E = 0.715 * Math.exp(-0.000359 * 0.3048 * sigma);
      phi_w = C * Math.pow(3.281 * 60 * wsp, B) * Math.pow(beta_ratio, -E);
   }


   /******************************************************************************************************************************
    * phi combined wind and slope factor assumptions: wsp > 0. and/or slp > 0.
    *  -> phi_t
    *****************************************************************************************************************************/
   protected void combinedWindAndSlopeFactor() {
      // reset values
      phi_t = 0.;
      vl = 0.;

      // calculate the wind and slope factor
      slopeFactor(); // -> phi_s
      windFactor(); // -> phi_w

      // combine the two factors using a vector sum..
      // conversion of input values....
      asp_r = Math.toRadians(asp);
      wdr_r = Math.toRadians(wdr);

      // Flip Aspect
      // -> upslope direction is needed
      if (asp_r < Math.PI) {
         asp_r = asp_r + Math.PI;
      }
      else {
         asp_r = asp_r - Math.PI;
      }

      /*
       * Flip Wind Direction
       * standard meteorological definitions says
       *        winddirection == direction where the wind is blowing FROM
       * for the calculation we need
       *        winddirection == direction where the is blowing TO
       */
      if (wdr_r < Math.PI) {
         wdr_r = wdr_r + Math.PI;
      }
      else {
         wdr_r = wdr_r - Math.PI;
      }

      /* the following code according to fireLib.c
       * 1. normalize for upslope direction
       * 2. consider differing angle of wind by splitAngle
       */
      splitRad = Math.abs(wdr_r - asp_r) >= Math.PI ? wdr_r + asp_r - 2 * Math.PI : wdr_r - asp_r;
      cos_splitRad = Math.cos(splitRad);
      sin_splitRad = Math.sin(splitRad);
      vx = phi_s + phi_w * cos_splitRad;
      vy = phi_w * sin_splitRad;
      vl = Math.sqrt(vx * vx + vy * vy);
      //
      al = Math.asin(vy / vl);
      //
      if (vx >= 0.) {
         alRad = (vy >= 0.) ? asp_r + al : asp_r + al + 2 * Math.PI;
      }
      else {
         alRad = asp_r - al + Math.PI;
      }
      alDeg = Math.toDegrees(alRad);
      if (alDeg > 360) {
         alDeg -= 360.;
      }
      //
      sdr = alDeg;
      /***************************************************************************************************************************
       * effective windspeed actually this is only the inverse function of phi_w
       **************************************************************************************************************************/
      efw = (Math.pow(vl / (C * Math.pow(beta_ratio, -E)), 1 / B)) / 196.85;
      // rothermel 87: sets an upper limit on
      // the wind multiplication factor
      if (efw > 0.024 * I_r) {
         efw = Math.min(efw, 0.024 * I_r);
         phi_t = C * Math.pow(196.85 * efw, B) * Math.pow(beta_ratio, -E);
         // flag that derivations are not allowed!
         canDerive = false;
      }
      else {
         phi_t = vl;
      }
      final double lwRatio = 1. + 0.002840909 * efw;
      ecc = Math.sqrt(lwRatio * lwRatio - 1.0) / lwRatio;

   }


   /******************************************************************************************************************************
    * moistureDamping ********************************************************************* calculate the moisture damping
    * coefficients for dead and live fuel
    *
    * Exceptions thrown if mx <= 0.
    */
   protected void moistureDamping() {
      // reset variables...
      hn_d1 = 0.;
      hn_d2 = 0.;
      hn_d3 = 0.;
      hn_lh = 0.;
      hn_lw = 0.;
      sumhd = 0.;
      sumhl = 0.;
      sumhdm = 0.;
      W = 0.;
      Mf_dead = 0.;
      Mx_live = 0.;
      eta_Ml = 0.;
      eta_Md = 0.;
      eta_M = 0.;

      /**
       * moisture damping coefficient weighting factors for live moisture of extinction...
       *
       * Rothermel (1972): eq. (88) (mx)_living = 2.9W(1-(M_f)_dead/0.3) - 0.226 (min = 0.3)
       *  => Albini (1976): page 89! (mx)_living = 2.9W'(1-(M'_f)_dead/(mx)_dead) - 0.226 (min = mx)
       *
       * -------------------------------------------------------- Ratio of "fine fuel loadings, dead/live W' =
       * SUM(w0_d*exp(-138/sv_d*)/SUM(w0_l*exp(-500/sv_l*) 0.20482 = Multiplier for [pound/ft2] to [kg/m2] -452.76 = -138 / 0.3048
       * -1640.2 = -500 / 0.3048
       */
      if (sv_d1 > 0.) {
         hn_d1 = 0.20482 * w0_d1 * Math.exp(-452.76 / sv_d1);
      }
      if (sv_d2 > 0.) {
         hn_d2 = 0.20482 * w0_d2 * Math.exp(-452.76 / sv_d2);
      }
      if (sv_d3 > 0.) {
         hn_d3 = 0.20482 * w0_d3 * Math.exp(-452.76 / sv_d3);
      }
      if (sv_lh > 0.) {
         hn_lh = 0.20482 * w0_lh * Math.exp(-1640.42 / sv_lh);
      }
      if (sv_lw > 0.) {
         hn_lw = 0.20482 * w0_lw * Math.exp(-1640.42 / sv_lw);
      }

      // sum up...
      sumhd = hn_d1 + hn_d2 + hn_d3;
      sumhl = hn_lh + hn_lw;
      sumhdm = hn_d1 * m_d1 + hn_d2 * m_d2 + hn_d3 * m_d3;

      /*
        moisture damping for live fuel
       */
      // calc only if there is any live fuel available...
      // sw_l = sv_lh * w0_lh + sv_lw * w0_lw
      // sw_l > 0 ensures that sumhl > 0
      if (sw_l > 0.) {
         // W' ratio of "fine" fuel loading, dead/living
         W = sumhd / sumhl;

         // Moisture content of "fine" dead fuel
         if (sumhd > 0) {
            Mf_dead = sumhdm / sumhd;
         }

         // Moisture of extinction of living fuel
         Mx_live = (2.9 * W * (1 - Mf_dead / mx) - 0.226) * 100;

         /*
          * Check for Minimum of Mx_live
          *        Mx_live = Math.max(Mx_live,mx)
          *
          * if Mx_live is lower than mx, we have a problem with the
          * calculation of the error, as the function is no longer continuos
          *
          */
         if (Mx_live < mx) {
            canDerive = false;
            Mx_live = mx;
         }
         // dead moisture ratio
         rm_l = swm_l / (sw_l * Mx_live);
      }

      // moisture ratios
      // Rothermel (1972): eq. (65) & (66)
      if (sw_d > 0) {
         rm_d = swm_d / (sw_d * mx);
      }

      // moisture damping coefficient
      // Rothermel (1972): eq. (64)
      // damping coefficients range from 0 to 1 (Rothermel 1972, p.11!).
      // 0 means a moisture ratio rm_* greater than 1, i.e. the moisture
      //   content is higher than the moisture of extinction
      //
      eta_Md = 1 - 2.59 * (rm_d) + 5.11 * Math.pow(rm_d, 2) - 3.52 * Math.pow(rm_d, 3);
      eta_Ml = 1 - 2.59 * (rm_l) + 5.11 * Math.pow(rm_l, 2) - 3.52 * Math.pow(rm_l, 3);

      // check for eta_* lower than 0;
      if (eta_Md < 0) {
         eta_Md = 0.;
      }
      if (eta_Ml < 0) {
         eta_Ml = 0.;
      }

      //
      eta_M = wn_d * eta_Md + wn_l * eta_Ml;
   }


   /******************************************************************************************************************************
    * gamma': reaction velocity
    *
    *****************************************************************************************************************************/
   protected void reactionVelocity() {
      /**
       * exponent A: => Rothermel 1972 eq.(70), replaced by Albini (1976) (p.88) A = 133 * sigma**-0.7913 ;sigma[1/ft] = 133 *
       * 0.3048**-0.7913 * sigma**-0.7913 ;sigma[1/m] = 340.53 * sigma**-0.7913 ;sigma[1/m]
       */
      A = 340.53 * Math.pow(sigma, -0.7913);
      /**
       * maximum reaction velocity: => Rothermel 1972: (68), based on (36) conversion: gamma_max [min-1] = 60 gamma_max [s-1]
       * Formulae: gamma_max = sigma**1.5 / (495 + 0.0594* sigma**1.5) counter = sigma**1.5 ;sigma [1/ft] = 1 * Math.pow(0.3048,
       * 1.5) * sigma**1.5 ;sigma [1/m] = 0.16828 * sigma**1.5
       *
       * denominator = 495 + 0.0594 * sigma**1.5 ;sigma[1/ft] = 495*60 + 0.0594*60*0.16828 * sigma**1.5 ;sigma[1/m] = 29700 +
       * 0.5997 * sigma**1.5 ;sigma[1/m]
       */
      gamma_max = 0.16828 * Math.pow(sigma, 1.5) / (29700 + 0.5997 * Math.pow(sigma, 1.5));
      gamma = gamma_max * Math.pow(beta_ratio, A) * Math.exp(A * (1 - beta_ratio));
   }


   /******************************************************************************************************************************
    * heat sink term
    *
    * Rothermel (1972): eq. (77) + (78)
    *****************************************************************************************************************************/
   protected void heatSink() {
      /**
       * Effective heating number: epsilon = exp(-138 / sigma_ft) (14) = exp(-138 / (sigma_m * 0.3048)) conversion! = exp( -452.76 /
       * sigma)
       */

      if (sv_d1 > 0.0) {
         eps_d1 = Math.exp(-452.76 / sv_d1);
      }
      if (sv_d2 > 0.0) {
         eps_d2 = Math.exp(-452.76 / sv_d2);
      }
      if (sv_d3 > 0.0) {
         eps_d3 = Math.exp(-452.76 / sv_d3);
      }
      if (sv_lh > 0.0) {
         eps_lh = Math.exp(-452.76 / sv_lh);
      }
      if (sv_lw > 0.0) {
         eps_lw = Math.exp(-452.76 / sv_lw);
      }
      /**
       * Heat of Preignition: Q_ig, [Btu/lb] = 1.05506 kJ / 0.4535 kg = 2.3265 kJ/kg Q_ig = 250.0 + 1116 * M_f ; M_f [fraction] =
       * 581.5 + 2.3265 *(0.01 * M_f) ; M_f [%]
       */
      q_d1 = 581.5 + 25.957 * m_d1;
      q_d2 = 581.5 + 25.957 * m_d2;
      q_d3 = 581.5 + 25.957 * m_d3;
      q_lh = 581.5 + 25.957 * m_lh;
      q_lw = 581.5 + 25.957 * m_lw;

      /**
       * Heat Sink
       */
      hskz = sw_d1 * eps_d1 * q_d1 + sw_d2 * eps_d2 * q_d2 + sw_d3 * eps_d3 * q_d3 + sw_lh * eps_lh * q_lh + sw_lw * eps_lw
             * q_lw;
      hsk = rho_b * hskz / sw_t;
   }

}

package es.unex.sextante.geosocial.twitter;

import java.awt.geom.Point2D;
import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.Twitter.User;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class SearchTwitterAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT        = "RESULT";
   public static final String SEARCH_STRING = "SEARCH_STRING";
   public static final String INIT_DATE     = "INIT_DATE";
   public static final String END_DATE      = "END_DATE";
   public static final String POINT         = "POINT";
   public static final String SEARCH_RADIUS = "SEARCH_RADIUS";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Search_twitter"));
      setGroup(Sextante.getText("Geosocial"));

      try {
         m_Parameters.addString(SEARCH_STRING, Sextante.getText("Search_string"));
         m_Parameters.addString(INIT_DATE, Sextante.getText("init_date"));
         m_Parameters.addString(END_DATE, Sextante.getText("end_date"));
         m_Parameters.addPoint(POINT, Sextante.getText("Search_coordinate"));
         m_Parameters.addNumericalValue(SEARCH_RADIUS, Sextante.getText("Search_radius_km"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 50, 0, Double.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final String sSearch = m_Parameters.getParameterValueAsString(SEARCH_STRING);
      final String sInitDate = m_Parameters.getParameterValueAsString(INIT_DATE);
      final String sEndDate = m_Parameters.getParameterValueAsString(END_DATE);
      final Point2D pt = m_Parameters.getParameterValueAsPoint(POINT);
      final double dRadius = m_Parameters.getParameterValueAsDouble(SEARCH_RADIUS);

      final Class[] types = new Class[] { String.class, String.class };
      final String[] sFields = new String[] { "user", "message" };
      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Resultado"), IVectorLayer.SHAPE_TYPE_POINT, types,
               sFields);

      final Twitter twitter = new Twitter();
      twitter.setMaxResults(300);
      twitter.setSearchLocation(pt.getX(), pt.getY(), Double.toString(dRadius) + "km");
      try {
         final Date initDate = new Date(sInitDate);
         twitter.setSinceDate(initDate);
      }
      catch (final Exception e) {}
      try {
         final Date endDate = new Date(sEndDate);
         twitter.setUntilDate(endDate);
      }
      catch (final Exception e) {}

      setProgressText(Sextante.getText("retrieving_tweets"));
      final List<Twitter.Status> stati = new Twitter().search(sSearch, null, 90);

      setProgressText(Sextante.getText("processing_tweets"));
      final GeometryFactory gm = new GeometryFactory();
      for (int i = 0; i < stati.size() && setProgress(i, stati.size()); i++) {
         try {
            final Status status = stati.get(i);
            String sPosition = status.getGeo();
            if (sPosition == null) {
               final User user = twitter.show(status.getUser().getScreenName());
               sPosition = Geocoding.getCoordinates(user.getLocation());
            }
            final String sLatitude = sPosition.substring(0, sPosition.indexOf(","));
            final String sLongitude = sPosition.substring(sPosition.indexOf(",") + 1);
            final double dLatitude = Double.parseDouble(sLatitude);
            final double dLongitude = Double.parseDouble(sLongitude);
            final Point point = gm.createPoint(new Coordinate(dLongitude, dLatitude));
            final Object[] record = new Object[] { status.getUser().getScreenName(), status.getText() };
            output.addFeature(point, record);
         }
         catch (final Exception e) {//ignore problematic tweets}

         }
      }

      return !m_Task.isCanceled();

   }


}

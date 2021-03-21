/*******************************************************************************
SubAreaResult.java
Copyright (C) 2009 ETC-LUSI http://etc-lusi.eionet.europa.eu/
 *******************************************************************************/
package es.unex.sextante.gridCategorical.tabulateArea;

import java.util.Map;


/**
 * 
 * @author Cesar Martinez Izquierdo
 */
public class SubAreaResult {
   private TempTableReader      tmpReader   = null;
   private boolean              isSuccessul = false;
   private Map<Object, Integer> zones;


   public void setReader(final TempTableReader reader) {
      this.tmpReader = reader;
   }


   public TempTableReader getReader() {
      return tmpReader;
   }


   public boolean isSuccessful() {
      return isSuccessul;
   }


   public void setSuccessful(final boolean success) {
      isSuccessul = success;
   }


   public void setZones(final Map<Object, Integer> classes) {
      this.zones = classes;
   }


   public Map<Object, Integer> getZones() {
      return this.zones;
   }
}

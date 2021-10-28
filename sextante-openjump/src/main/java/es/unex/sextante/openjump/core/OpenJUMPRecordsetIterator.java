package es.unex.sextante.openjump.core;

import java.util.Iterator;

import com.vividsolutions.jump.feature.Feature;

import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IRecordsetIterator;

public class OpenJUMPRecordsetIterator implements IRecordsetIterator {

   private final Iterator<Feature> m_Iterator;


   public OpenJUMPRecordsetIterator(final Iterator<Feature> iterator) {

      m_Iterator = iterator;

   }


   public void close() {}


   public boolean hasNext() {

      return m_Iterator.hasNext();

   }


   public IRecord next() {

      return new OpenJUMPRecord(m_Iterator.next());

   }
}

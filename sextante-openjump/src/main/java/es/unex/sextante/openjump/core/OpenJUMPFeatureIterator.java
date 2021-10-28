package es.unex.sextante.openjump.core;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;
import es.unex.sextante.exceptions.IteratorException;

//TODO filters parameter is not honoured !
public class OpenJUMPFeatureIterator implements IFeatureIterator {

   private List<IVectorLayerFilter> m_Filters;
   private Iterator<Feature>        m_Iterator;


   public OpenJUMPFeatureIterator() {

   }


   public OpenJUMPFeatureIterator(final Layer layer,
                                  final List<IVectorLayerFilter> filters) {

      m_Filters = filters;
      m_Iterator = layer.getFeatureCollectionWrapper().iterator();

   }


   public boolean hasNext() {

      return m_Iterator.hasNext();

   }


   public IFeature next() throws IteratorException {

      try {
         //m_Iterator.next();
         final OpenJUMPFeature feature = new OpenJUMPFeature(m_Iterator.next());
         return feature;
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         throw new IteratorException();
      }

   }


   public void close() {}


}

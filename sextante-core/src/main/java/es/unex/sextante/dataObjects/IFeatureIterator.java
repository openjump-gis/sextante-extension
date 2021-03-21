package es.unex.sextante.dataObjects;

import es.unex.sextante.exceptions.IteratorException;

/**
 * Interface for feature iterators
 * 
 * @author volaya volaya@unex.es
 * 
 */
public interface IFeatureIterator {

   public boolean hasNext();


   public IFeature next() throws IteratorException;


   public void close();


   /**
    * returns the number of features that the iterator will return
    * 
    * @return the number of features that the iterator will return
    * 
    * public int getFeatureCount();
    * 
    * 
    * /** this method is here for performance reasons. Returns the minimum extent that covers all the features returned by the
    * iterator
    * 
    * @return Returns the minimum extent that covers all the features returned by the iterator
    * 
    * public Rectangle2D getExtent();/* }
    */

}

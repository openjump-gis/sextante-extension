package es.unex.sextante.dataObjects;

import es.unex.sextante.exceptions.IteratorException;

/**
 * Interface for iterators used to iterate a database or table
 *
 * @author volaya
 *
 */
public interface IRecordsetIterator {

   public boolean hasNext();


   public IRecord next() throws IteratorException;


   public void close();

}

package es.unex.sextante.core;

/**
 * This interface defines a filter to be applied to a set of GeoAlgorithms.
 *
 * @author volaya
 *
 */
public interface IGeoAlgorithmFilter {

   public boolean accept(GeoAlgorithm alg);

}

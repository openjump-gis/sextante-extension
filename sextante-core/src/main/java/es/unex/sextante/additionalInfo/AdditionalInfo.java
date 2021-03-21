package es.unex.sextante.additionalInfo;

/**
 * Additional information for a {@link es.unex.sextante.parameters.Parameter} in a {@link es.unex.sextante.core.OutputObjectsSet}
 * 
 * This information includes all the necessary elements to fully define the parameter, like a default value, a range of possible
 * values, or any other thing needed.
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public interface AdditionalInfo {

   public String getTextDescription();

}

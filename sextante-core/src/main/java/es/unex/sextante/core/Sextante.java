package es.unex.sextante.core;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;


public class Sextante {

	private static final SextanteLogHandler                       m_Logger = new SextanteLogHandler();

	private static HashMap<String, String>                        m_Text;
	private static HashMap<String, HashMap<String, GeoAlgorithm>> m_Algorithms;


	/**
	 * initializes the library, loading algorithms and properties. Algorithms and properties are loaded from files in the
	 * classpath.
	 */
	public static void initialize() {

	  // disabled, replaced by external search/add in SextanteToolboxPlugin [2022.01 ed]
		//AlgorithmsAndResources.addAlgorithmsAndPropertiesFromClasspath();

		m_Text = new HashMap<>();
		m_Algorithms = new HashMap<>();

		setLanguageStrings();

		loadLibraryAlgorithms();

	}


	/**
	 * Initializes the library loading algorithms and properties located inside the specified jar files
	 * 
	 * @param jars
	 *                URLs of jar files containing algorithms and properties.
	 */
	public static void initialize(final URL[] jars) {
		AlgorithmsAndResources.addAlgorithmsAndPropertiesFromURLs(jars);

		m_Text = new HashMap<>();
		m_Algorithms = new HashMap<>();

		setLanguageStrings();

		loadLibraryAlgorithms();

	}


	/**
	 * initializes the library, loading algorithms and properties.
	 * 
	 * @param sFolder
	 *                the folder where sextante jars are located. Algorithms and properties files are loaded from those jar files
	 *                prior to library initialization.
	 */
	public static void initialize(final String sFolder) {

		AlgorithmsAndResources.addAlgorithmsAndPropertiesFromFolder(sFolder);

		m_Text = new HashMap<>();
		m_Algorithms = new HashMap<>();

		setLanguageStrings();

		loadLibraryAlgorithms();

	}


	private static void setLanguageStrings() {

		final String[] files = AlgorithmsAndResources.getPropertiesFilenames();

		for (final String element : files) {
			try {
				final ResourceBundle labels = ResourceBundle.getBundle(element, Locale.getDefault());
				final Enumeration<String> bundleKeys = labels.getKeys();

				while (bundleKeys.hasMoreElements()) {
					final String key = bundleKeys.nextElement();
					final String value = labels.getString(key);
					m_Text.put(key, value);
				}
			}
			catch (final MissingResourceException e) {
				addErrorToLog(e);
			}
		}

	}


	/**
	 * Adds a group of algorithms coming from the same algorithm provider. It will replace the previous group of algorithms from
	 * that provider (if it exists) in the collection of currently available algorithms
	 * 
	 * @param sName
	 *                the name that identifies the provider of the algorithms to add
	 * @param algs
	 *                a map with algorithm command-line names as keys and geoalgorithms as values
	 */
	public static void addAlgorithmsFromProvider(final String sName,
			final HashMap<String, GeoAlgorithm> algs) {

		m_Algorithms.put(sName, algs);

	}


	/**
	 * Adds an external geoprocess. An external algorithm is a GeoAlgorithm which has been created out of this library.
	 * 
	 * @param sProviderGroup
	 *                the name of the provider group for the algorithm to add
	 * @param alg
	 *                the class of the algorithm to add
	 * @param text
	 *                HashMap which contains the properties
	 */
	public static void addGeoalgorithm(final String sProviderGroup,
			final Class<?> alg,
			final HashMap<String, String> text) {

		if (text != null) {
			m_Text.putAll(text);
		}

		if (alg != null) {
			Object obj = null;
			try {
				obj = alg.newInstance();
			}
			catch (final InstantiationException | IllegalAccessException e) {
				Sextante.addErrorToLog(e);
			}
			if ((obj instanceof GeoAlgorithm)) {
				HashMap<String, GeoAlgorithm> group = m_Algorithms.get(sProviderGroup);
				if (group == null) {
					group = new HashMap<>();
					m_Algorithms.put(sProviderGroup, group);
				}
				group.put(((GeoAlgorithm) obj).getCommandLineName(), (GeoAlgorithm) obj);
			}
		}

	}


	/**
	 * Adds an external geoprocess. An external algorithm is a GeoAlgorithm which has been created outside of this library.
	 * 
	 * @param sProviderGroup
	 *                the name of the provider group for the algorithm to add
	 * 
	 * @param alg
	 *                External algorithm
	 */
	public static void addGeoalgorithm(final String sProviderGroup,
			final GeoAlgorithm alg) {

		if (alg != null) {
			HashMap<String, GeoAlgorithm> group = m_Algorithms.get(sProviderGroup);
			if (group == null) {
				group = new HashMap<>();
				m_Algorithms.put(sProviderGroup, group);
			}
			group.put(alg.getCommandLineName(), alg);
		}

	}


	/**
	 * Adds external translations
	 * 
	 * @param text
	 *                HashMap which contains the properties
	 */
	public static void addTranslation(final HashMap<String, String> text) {

		if (text != null) {
			m_Text.putAll(text);
		}

	}


	/**
	 * logs an error or exception to the SEXTANTE logger
	 * 
	 * @param e
	 *                the throwable object to log
	 */
	public static void addErrorToLog(final Throwable e) {

		m_Logger.addError(e);

	}


	/**
	 * Adds an error message to the SEXTANTE logger
	 * 
	 * @param sError
	 *                the error message
	 */
	public static void addErrorToLog(final String sError) {

		m_Logger.addError(sError);

	}


	/**
	 * Adds a warning message to the SEXTANTE logger
	 * 
	 * @param sWarning
	 *                the warning message
	 */
	public static void addWarningToLog(final String sWarning) {

		m_Logger.addWarning(sWarning);

	}


	/**
	 * Adds an info message to the SEXTANTE logger
	 * 
	 * @param sInfo
	 *                the info message
	 */
	public static void addInfoToLog(final String sInfo) {

		m_Logger.addInfo(sInfo);

	}


	/**
	 * Returns the logger to log SEXTANTE issues
	 * 
	 * @return the logger to log SEXTANTE issues
	 */
	public static SextanteLogHandler getLogger() {

		return m_Logger;

	}


	/**
	 * Returns a map of all algorithms currently loaded. That includes algorithm in the library and all external algorithms added
	 * afterwards using the corresponding methods. The name of the provider of these algorithms ("SEXTANTE" for library algorithms)
	 * is used as key. Values are maps where command-line names (see {@link GeoAlgorithm#getCommandLineName()}) are used as keys
	 * and algorithms as values. The library must have been initialized before in order to contain library algorithms.
	 * 
	 * @return a map of all algorithms in the library.
	 */
	public static HashMap<String, HashMap<String, GeoAlgorithm>> getAlgorithms() {

		return m_Algorithms;

	}


	/**
	 * Returns a filtered map of all algorithms currently loaded. That includes algorithm in the library and all external
	 * algorithms added afterwards using the corresponding methods. The name of the provider of these algorithms ("SEXTANTE" for
	 * library algorithms) is used as key. Values are maps where command-line names (see {@link GeoAlgorithm#getCommandLineName()})
	 * are used as keys and algorithms as values. The library must have been initialized before in order to contain library
	 * algorithms.
	 * 
	 * @param filter
	 *                The filter to use
	 * @return a map of algorithms in the library filtered according to a given filter.
	 */
	public static HashMap<String, HashMap<String, GeoAlgorithm>> getAlgorithms(final IGeoAlgorithmFilter filter) {

		final HashMap<String, HashMap<String, GeoAlgorithm>> algs = new HashMap<>();

		final Set<String> set = m_Algorithms.keySet();
		final Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			final String key = iter.next();
			final HashMap<String, GeoAlgorithm> group = m_Algorithms.get(key);
			final Set<String> set2 = group.keySet();
			final Iterator<String> iter2 = set2.iterator();
			while (iter2.hasNext()) {
				final String key2 = iter.next();
				final GeoAlgorithm alg = group.get(key2);
				if (filter.accept(alg)) {
					HashMap<String, GeoAlgorithm> returnGroup = algs.get(key);
					if (returnGroup == null) {
						returnGroup = new HashMap<>();
						algs.put(key, returnGroup);
					}
					returnGroup.put(key2, alg);
				}
			}
		}

		return algs;


	}


	/**
	 * returns an algorithm based on its command line name. (see {@link GeoAlgorithm#getCommandLineName()})
	 * 
	 * @param sName
	 *                the command-line name
	 * @return an algorithm. null if there is no algorithm with that name
	 */
	public static GeoAlgorithm getAlgorithmFromCommandLineName(final String sName) {

		final Set<String> set = m_Algorithms.keySet();
		for (String key : set) {
			final GeoAlgorithm alg = m_Algorithms.get(key).get(sName);
			if (alg != null) {
				return alg;
			}
		}
		return null;

	}


	public static String getAlgorithmProviderName(final GeoAlgorithm alg) {

		final Set<String> set = m_Algorithms.keySet();
		for (String key : set) {
			final HashMap<String, GeoAlgorithm> algs = m_Algorithms.get(key);
			final String sName = alg.getCommandLineName();
			if (algs.containsKey(sName)) {
				return key;
			}
		}
		return "SEXTANTE";

	}


	/**
	 * Puts into the map of available algorithms those included in the SEXTANTE library and loaded from the corresponding jar files
	 */
	private static void loadLibraryAlgorithms() {

		final HashMap<String, GeoAlgorithm> algsMap = new HashMap<>();

		final String[] algs = AlgorithmsAndResources.getAlgorithmClassNames();

		for (final String element : algs) {
			try {
				final Class<?> clazz = Class.forName(element);
				final Object obj = clazz.newInstance();
				if (obj instanceof GeoAlgorithm) {
					final GeoAlgorithm alg = (GeoAlgorithm) obj;
					if (alg.getGroup() != null) { //to avoid loading base classes
						algsMap.put(alg.getCommandLineName(), alg);
					}
				}
			}
			catch (final Exception e) {}
		}

		m_Algorithms.put("SEXTANTE", algsMap);

	}


	/**
	 * Returns an internationalized string based on a key value (i.e the string in the current language associated with the key
	 * value) Use this method to support internationalization. Resource strings are loaded when the library is initialized, so the
	 * initialize() method has to be called to use the corresponding translations.
	 * 
	 * @param sKey
	 *                the key to search
	 * @return the corresponding string in the current language. If the key was not found, it returns that same key.
	 */
	public static String getText(final String sKey) {

		if (m_Text == null) {
			return sKey;
		}

		final String s = m_Text.get(sKey);
		if (s == null) {
			return sKey;
		}
		else {
			return s;
		}

	}


	/**
	 * Returns "true" if the current operating system is a variant of Windows.
	 * 
	 * @return "true", if we are running on Windows, "false" otherwise.
	 */
	public static boolean isWindows() {
		final String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}


	/**
	 * Returns "true" if the current operating system is Mac OS X.
	 * 
	 * @return "true", if we are running on Mac OS X, "false" otherwise.
	 */
	public static boolean isMacOSX() {
		final String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("mac"));
	}


	/**
	 * Returns "true" if the current operating system is some Unix flavor (including Linux). Does not return true for Mac OS X.
	 * 
	 * @return "true", if we are running on Unix/Linux, "false" otherwise.
	 */
	public static boolean isUnix() {
		final String os = System.getProperty("os.name").toLowerCase();
		return ((os.contains("nix")) || (os.contains("nux")));
	}


	/**
	 * Returns the number of algorithms available
	 * 
	 * @return the number of algorithms available
	 */
	public static int getAlgorithmsCount() {

		int iCount = 0;
		final Set<String> set = m_Algorithms.keySet();
		for (String key : set) {
			iCount += m_Algorithms.get(key).size();

		}
		return iCount;

	}


	public static String getVersionNumber() {

		return Sextante.getText("version_number");

	}


}

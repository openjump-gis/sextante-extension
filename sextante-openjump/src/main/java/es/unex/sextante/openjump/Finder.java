package es.unex.sextante.openjump;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

public class Finder {
  private static ScanResult scanResult = null;

  /**
   * initializes a Classgraph scan over the class loader urls limited to
   * - not jars (paths) containing a folder named starting with sextante e.g.
   * 'sextante-openjump/target/classes'
   * - jars anywhere starting with sextante e.g. 'target/libs/SextanteTest.jar'
   */
  public static void initClassGraphScan() {
    // use cached scan
    if (scanResult != null)
      return;

    scanResult = new ClassGraph()//.verbose()
      .filterClasspathElements(new ClassGraph.ClasspathElementFilter() {
        Pattern jarPattern = Pattern.compile(".*/sextante(?:[^/]*|.*(?<!\\.jar))$", Pattern.CASE_INSENSITIVE);
  
        @Override
        public boolean includeClasspathElement(String classpathElementPathStr) {
          if (!jarPattern.matcher(classpathElementPathStr).matches())
            return false;
          System.out.println(classpathElementPathStr);
          return true;
        }
      })
      .enableClassInfo().scan();
    
    //scanResult.getAllResources().getPaths().forEach(System.out::println);
  }

  /**
   * Find classes that implement (or have superclasses that implement) the give
   * interface (or one of its subinterfaces).
   * 
   * @param interfaceOrSuperClass (interface class )
   * @param namePattern           (null if not needed)
   * @return List of classes
   */
  public static <T> List<Class<T>> findClassesImplementingOrExtending(final Class<T> interfaceOrSuperClass,
      Pattern namePattern) {
    initClassGraphScan();
    // Pattern regex = Pattern.compile("", Pattern.CASE_INSENSITIVE);
    ClassInfoList classInfos = interfaceOrSuperClass.isInterface()
        ? scanResult.getClassesImplementing(interfaceOrSuperClass)
        : scanResult.getSubclasses(interfaceOrSuperClass);
    if (namePattern != null)
      classInfos = classInfos.filter(new ClassInfoList.ClassInfoFilter() {
        @Override
        public boolean accept(ClassInfo classInfo) {
          return namePattern.matcher(classInfo.getName()).matches();
        }
      });
    final List<Class<T>> foundClasses = new ArrayList<Class<T>>();
    for (ClassInfo classInfo : classInfos) {
      // TODO: should probably catch exceptions here?
      foundClasses.add((Class<T>) classInfo.loadClass(interfaceOrSuperClass, true));
    }
    return foundClasses;
  }

  public static List<String> findResourcesMatching(Pattern namePattern) {
    initClassGraphScan();
    return scanResult.getAllResources().filter(new ResourceList.ResourceFilter() {
      @Override
      public boolean accept(Resource resource) {
        String path = resource.getPath();
        return namePattern.matcher(path).matches();
      }
    }).getPaths();
  }

  public static List<String> findResourcesWithExtension(String extension) {
    initClassGraphScan();
    return scanResult.getResourcesWithExtension(extension).getPaths();
  }
}

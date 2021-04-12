package es.unex.sextante.openjump.language;

import com.vividsolutions.jump.I18N;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18NPlug {
  private static final ResourceBundle I18N_RESOURCE = ResourceBundle.getBundle(
      "es.unex.sextante.openjump.language.Sextante",
      new Locale(I18N.getLocale()));

  public static String getI18N(String key) {
    String out;
    try {
      out = I18N_RESOURCE.getString(key);
      if (out == null)
        throw new MissingResourceException(
            "Missing translation for key " + key, "", key);
    } catch (MissingResourceException ex) {
      String[] labelpath = key.split("\\.");
      ex.printStackTrace();
      out = labelpath[(labelpath.length - 1)];
    }
    return out;
  }
}

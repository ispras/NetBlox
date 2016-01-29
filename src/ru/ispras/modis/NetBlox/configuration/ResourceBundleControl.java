package ru.ispras.modis.NetBlox.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * The basic ResourceBundle.Control allows to use properties files only in ASCII encoding.
 * This class is created to allow the system to deal with a wider range of file formats.
 * 
 * @author ilya
 */
public class ResourceBundleControl extends ResourceBundle.Control {
	private Charset charset;


	public ResourceBundleControl(Charset charset)	{
		this.charset = charset;
	}

	public ResourceBundleControl(String charsetName)	{
		if (charsetName != null  &&  !charsetName.isEmpty())	{
			charset = Charset.forName(charsetName);
		}
		if (charset == null)	{
			charset = Charset.defaultCharset();
		}
	}


	/**
	 * See main description in parent class. What has been changed: the creation of <code>PropertyResourceBundle</code>.
	 * Now a new <code>InputStreamReader</code> is created for it that considers the global class variable <code>charset</code>.
	 */
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		String bundleName = toBundleName(baseName, locale);
		ResourceBundle bundle = null;
		if (format.equals("java.class")) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ResourceBundle> bundleClass =
					(Class<? extends ResourceBundle>) loader.loadClass(bundleName);

				// If the class isn't a ResourceBundle subclass, throw a ClassCastException.
				if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
					bundle = bundleClass.newInstance();
				} else {
					throw new ClassCastException(bundleClass.getName()+" cannot be cast to ResourceBundle");
				}
			} catch (ClassNotFoundException e) {
			}
		}
		else if (format.equals("java.properties")) {
			final String resourceName = toResourceName0(bundleName, "properties");
			if (resourceName == null) {
				return bundle;
			}
			final ClassLoader classLoader = loader;
			final boolean reloadFlag = reload;
			InputStream stream = null;
			try {
				stream = AccessController
						.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
							public InputStream run() throws IOException {
								InputStream is = null;
								if (reloadFlag) {
									URL url = classLoader.getResource(resourceName);
									if (url != null) {
										URLConnection connection = url.openConnection();
										if (connection != null) {
											// Disable caches to get fresh data for reloading.
											connection.setUseCaches(false);
											is = connection.getInputStream();
										}
									}
								}
								else {
									is = classLoader.getResourceAsStream(resourceName);
								}
								return is;
							}
						});
			} catch (PrivilegedActionException e) {
				throw (IOException) e.getException();
			}
			if (stream != null) {
				try {
					bundle = new PropertyResourceBundle(new InputStreamReader(stream, charset));
				} finally {
					stream.close();
				}
			}
		} else {
			throw new IllegalArgumentException("unknown format: " + format);
		}
		return bundle;
	}


	/**
	 * This method has been copied from the original class for the sake of visibility.
	 */
	private String toResourceName0(String bundleName, String suffix) {
        // application protocol check
        if (bundleName.contains("://")) {
            return null;
        } else {
            return toResourceName(bundleName, suffix);
        }
    }
}

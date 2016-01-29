package ru.ispras.modis.NetBlox.configuration;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import ru.ispras.modis.NetBlox.Activator;
import ru.ispras.modis.NetBlox.exceptions.PluginException;

public class LanguagesConfiguration {
	private static final SystemConfiguration systemConfig = SystemConfiguration.getInstance();

	public static final String DEFAULT_RELATIVE_LOCATION_OF_RESOURCES = "resources";
	private static final String LABELS_BUNDLE_NAME = "LabelsBundle";

	private final ResourceBundle labelsBundle;
	private static ResourceBundle netbloxLabelsBundle = null;


	public LanguagesConfiguration(URL resourcesURL)	{
		if (resourcesURL == null)	{
			labelsBundle = null;
		}
		else	{
			labelsBundle = prepareResourceBundle(resourcesURL);
		}
	}


	private static ResourceBundle prepareResourceBundle(URL resourcesURL)	{
		try {
			resourcesURL = FileLocator.resolve(resourcesURL);
		} catch (IOException e) {
			throw new PluginException("Failed to resolve URL to resources directory: "+e.getMessage());
		}

		URL[] urls = {resourcesURL};
		ClassLoader loader = new URLClassLoader(urls);

		Locale locale = new Locale(systemConfig.getLanguageCode());

		ResourceBundleControl resourceBundleControl = new ResourceBundleControl(systemConfig.getCharsetCode());

		ResourceBundle resourceBundle = ResourceBundle.getBundle(LABELS_BUNDLE_NAME, locale, loader, resourceBundleControl);
		return resourceBundle;
	}

	private static void initiateNetBloxLabelsBundle()	{
		if (netbloxLabelsBundle == null)	{
			Bundle netbloxBundle = Activator.getContext().getBundle();
			URL netbloxResources = netbloxBundle.getEntry(DEFAULT_RELATIVE_LOCATION_OF_RESOURCES+SystemConfiguration.FILES_SEPARATOR);
			netbloxLabelsBundle = prepareResourceBundle(netbloxResources);
		}
	}


	public String getLabel(String scenarioTag)	{
		String label;
		try	{
			if (labelsBundle == null)	{
				System.out.println("WARNING:\tNo labels bundle in the plug-in that implements "+scenarioTag);
				label = getNetBloxLabel(scenarioTag);
			}
			else	{
				try	{
					label = labelsBundle.getString(scenarioTag);
				}
				catch (MissingResourceException mre)	{
					System.out.println("WARNING:\tNo label for "+scenarioTag+" in its plug-in language resources bundle.");
					label = getNetBloxLabel(scenarioTag);
				}
			}
		}
		catch (MissingResourceException mre2)	{
			System.out.println("WARNING:\tNo language bundle with label for "+scenarioTag);
			label = scenarioTag;
		}
		return label;
	}

	public static String getNetBloxLabel(String key)	{
		initiateNetBloxLabelsBundle();
		return netbloxLabelsBundle.getString(key);
	}


	//================================================================================================================


	private static final Map<URL, LanguagesConfiguration> configurations = new HashMap<URL, LanguagesConfiguration>();

	public static LanguagesConfiguration getConfiguration(URL resourcesURL)	{
		LanguagesConfiguration configuration = configurations.get(resourcesURL);
		if (configuration == null)	{
			configuration = new LanguagesConfiguration(resourcesURL);
			configurations.put(resourcesURL, configuration);
		}
		return configuration;
	}
}

package ru.ispras.modis.NetBlox.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IGraphDescriptionParser;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IGraphMiningDescriptionParser;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IMeasureDescriptionParser;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IParserSupplier;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.ISupplementaryAlgorithmDescriptionParser;

public class ScenarioParserExtensionsRegistry {
	public class ScenarioNamedElementsParsers	{
		private static final String ATTRIBUTE_PARSE_IN_PLUGIN = "parseInPlugin";
		private static final String ATTRIBUTE_CLASS = "class";

		private Map<String, IConfigurationElement> elementsRegistry;	//key: element's name in scenario
		private Map<String, IParserSupplier> pluginParsersSuppliers;
		private Map<String, Object> inPluginParsersForElements;

		public ScenarioNamedElementsParsers()	{
			elementsRegistry = new HashMap<String, IConfigurationElement>();
			pluginParsersSuppliers = new HashMap<String, IParserSupplier>();
			inPluginParsersForElements = new HashMap<String, Object>();
		}

		public void addElementParserConfiguration(String elementName, IConfigurationElement configurationElement)	{
			elementsRegistry.put(elementName, configurationElement);
		}


		public boolean isToBeParsedInPlugin(String elementName)	{
			IConfigurationElement configurationElement = getConfigurationElement(elementName);
			String stringValue = configurationElement.getAttribute(ATTRIBUTE_PARSE_IN_PLUGIN);
			if (stringValue == null  ||  stringValue.isEmpty())	{
				return false;
			}
			return Boolean.parseBoolean(stringValue);
		}

		public IParserSupplier getParserSupplier(String elementName)	{
			IParserSupplier supplierCallback = pluginParsersSuppliers.get(elementName);

			if (supplierCallback == null)	{
				IConfigurationElement configurationElement = getConfigurationElement(elementName);
				try {
					Object callback = configurationElement.createExecutableExtension(ATTRIBUTE_CLASS);
					if (!(callback instanceof IParserSupplier))	{
						throw new PluginException("Callback class "+callback.getClass().getName()+" is not an IParserSupplier.");
						//XXX Tell more about which plug-in it is?
					}
					supplierCallback = (IParserSupplier) callback;
					pluginParsersSuppliers.put(elementName, supplierCallback);
				} catch (CoreException e) {
					throw new PluginException(e);
				}
			}

			return supplierCallback;
		}

		public Object getElementParser(String elementName)	{
			Object parserCallback = inPluginParsersForElements.get(elementName);

			if (parserCallback == null)	{
				IConfigurationElement configurationElement = getConfigurationElement(elementName);
				try {
					Object callback = configurationElement.createExecutableExtension(ATTRIBUTE_CLASS);
					if (!(callback instanceof IGraphDescriptionParser  ||
							callback instanceof ISupplementaryAlgorithmDescriptionParser  ||
							callback instanceof IGraphMiningDescriptionParser  ||
							callback instanceof IMeasureDescriptionParser))	{
						String errorMessage = "Callback class "+callback.getClass().getName()+
								" is not an instance of any of plug-in parsers interfaces.";
						throw new PluginException(errorMessage);	//XXX Tell more about which plug-in it is?
					}
					parserCallback = callback;
					inPluginParsersForElements.put(elementName, parserCallback);
				} catch (CoreException e) {
					throw new PluginException(e);
				}
			}

			return parserCallback;
		}


		private IConfigurationElement getConfigurationElement(String elementName)	{
			IConfigurationElement configurationElement = elementsRegistry.get(elementName);
			if (configurationElement == null)	{
				throw new PluginException("There're no plug-ins to parse the element named '"+elementName+"'.");
			}
			return configurationElement;
		}
	}



	private static final String EXTENSION_POINT_ID = "scenario.parsers";
	private static final String ATTRIBUTE_SECTION_TAG = "sectionTag";
	private static final String ATTRIBUTE_SCENARY_NAME = "scenaryName";

	private Map<String, ScenarioNamedElementsParsers> parsersRegistry;	//key - tag of a section in scenario.

	public ScenarioParserExtensionsRegistry()	{
		parsersRegistry = new HashMap<String, ScenarioNamedElementsParsers>();

		IExtensionRegistry extensionRegistry = RegistryFactory.getRegistry();	//The registry of extension points and extensions (plug-ins).
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EXTENSION_POINT_ID);
		IConfigurationElement[] extensionMembers = extensionPoint.getConfigurationElements();

		for (IConfigurationElement parserConfiguration : extensionMembers)	{
			String sectionTag = parserConfiguration.getAttribute(ATTRIBUTE_SECTION_TAG);
			String nameOfAlgorithmOrTypeInScenario = parserConfiguration.getAttribute(ATTRIBUTE_SCENARY_NAME);

			ScenarioNamedElementsParsers inSectionParsers = parsersRegistry.get(sectionTag);
			if (inSectionParsers == null)	{
				inSectionParsers = new ScenarioNamedElementsParsers();
				parsersRegistry.put(sectionTag, inSectionParsers);
			}

			inSectionParsers.addElementParserConfiguration(nameOfAlgorithmOrTypeInScenario, parserConfiguration);
		}
	}

	public ScenarioNamedElementsParsers getParsersForSection(String sectionTag)	{
		ScenarioNamedElementsParsers parsers = parsersRegistry.get(sectionTag);
		if (parsers == null)	{
			throw new PluginException("No parsers for the children of the section tag '"+sectionTag+"'.");
		}
		return parsers;
	}
}

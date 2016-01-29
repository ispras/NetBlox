package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;

import org.eclipse.core.runtime.IConfigurationElement;

import ru.ispras.modis.NetBlox.graphAlgorithms.GraphAlgorithmExtensionRegistry;

/**
 * A registry for all the extensions of characteristics.computers extension point.
 * 
 * @author ilya
 */
public class CharacteristicsExtensionRegistry extends GraphAlgorithmExtensionRegistry<CharacteristicComputer> {
	private static final String EXTENSION_POINT_ID = "characteristics.computers";

	private static final String ATTRIBUTE_CHARACTERISTIC_SCENARY_NAME = "characteristicNameInScenary";
	private static final String ATTRIBUTE_IS_SOURCE_IN_FILES = "isSourceInFiles";


	public CharacteristicsExtensionRegistry() {
		super(CharacteristicComputer.class, EXTENSION_POINT_ID, ATTRIBUTE_CHARACTERISTIC_SCENARY_NAME);
	}


	public CharacteristicComputer getCharacteristicComputer(String characteristicName)	{
		return getCallbackObject(characteristicName);
	}

	public boolean isSourcePassedInFiles(String algorithmName)	{
		IConfigurationElement minerConfiguration = getConfigurationElement(algorithmName);

		String stringValue = minerConfiguration.getAttribute(ATTRIBUTE_IS_SOURCE_IN_FILES);
		if (stringValue == null  ||  stringValue.isEmpty())	{
			return true;
		}
		return Boolean.parseBoolean(stringValue);
	}
}

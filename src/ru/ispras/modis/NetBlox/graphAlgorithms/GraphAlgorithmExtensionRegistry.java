package ru.ispras.modis.NetBlox.graphAlgorithms;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

import ru.ispras.modis.NetBlox.exceptions.PluginException;

public class GraphAlgorithmExtensionRegistry<CallbackType> {
	private Class<CallbackType> callbackType;

	private static final String ATTRIBUTE_CLASS = "class";

	private Map<String, IConfigurationElement> configurationsForAlgorithmsByName;
	private Map<String, CallbackType> algorithmsCallbacks;

	public GraphAlgorithmExtensionRegistry(Class<CallbackType> callbackType, String extensionPointID, String attribute_scenaryName)	{
		this.callbackType = callbackType;

		configurationsForAlgorithmsByName = new HashMap<String, IConfigurationElement>();
		algorithmsCallbacks = new HashMap<String, CallbackType>();

		IExtensionRegistry extensionRegistry = RegistryFactory.getRegistry();	//The registry of extension points and extensions (plug-ins).
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(extensionPointID);
		IConfigurationElement[] extensionMembers = extensionPoint.getConfigurationElements();

		for (IConfigurationElement minerConfiguration : extensionMembers)	{
			String algorithmName = minerConfiguration.getAttribute(attribute_scenaryName);
			configurationsForAlgorithmsByName.put(algorithmName, minerConfiguration);	//XXX Consider the case of different implementations.
		}
	}


	@SuppressWarnings("unchecked")
	protected CallbackType getCallbackObject(String algorithmName)	{
		CallbackType algorithmCallback = algorithmsCallbacks.get(algorithmName);
		if (algorithmCallback != null)	{
			return algorithmCallback;
		}

		IConfigurationElement configuration = getConfigurationElement(algorithmName);

		Object callback;
		try {
			callback = configuration.createExecutableExtension(ATTRIBUTE_CLASS);
			if (!( callbackType.isAssignableFrom(callback.getClass()) ))	{
				throw new PluginException("Callback class "+callback.getClass().getName()+" is not a "+callbackType);
				//XXX Tell more about which plug-in it is?
			}
			algorithmCallback = (CallbackType) callback;
			algorithmsCallbacks.put(algorithmName, algorithmCallback);
		} catch (CoreException e) {
			throw new PluginException(e);
		}

		return algorithmCallback;
	}

	protected IConfigurationElement getConfigurationElement(String algorithmName)	{
		IConfigurationElement configuration = configurationsForAlgorithmsByName.get(algorithmName);
		if (configuration == null)	{
			throw new PluginException("There're no plug-ins that implement "+algorithmName+" (see your scenario file).");
		}
		return configuration;
	}
}

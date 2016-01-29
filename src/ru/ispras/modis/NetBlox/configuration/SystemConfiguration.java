package ru.ispras.modis.NetBlox.configuration;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class SystemConfiguration {
	private static final String CONFIG_DEFAULT_FILE_NAME = "netblox.properties.xml";

	private final XMLConfiguration config;
	private static SystemConfiguration configurationInstance = null;


	public SystemConfiguration() throws ConfigurationException	{
		this(CONFIG_DEFAULT_FILE_NAME);
	}

	public SystemConfiguration(String configFileName) throws ConfigurationException	{
		config = new XMLConfiguration();
		config.load(new File(configFileName));
	}


	public static SystemConfiguration getInstance()	{
		if (configurationInstance == null)	{
			try {
				configurationInstance = new SystemConfiguration();
			} catch (ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		return configurationInstance;
	}

	public static void initiate(String configFileName)	{
		try {
			configurationInstance = new SystemConfiguration(configFileName);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	//===========================================================================================

	public static final String FILES_SEPARATOR = System.getProperty("file.separator");

	//===========================================================================================

	private static final String KEY_JVM_XMS;
	private static final String KEY_JVM_XMX;
	static	{
		String keyExternal = "forExternalLaunches.";
		KEY_JVM_XMS = keyExternal+"jvm_xms";
		KEY_JVM_XMX = keyExternal+"jvm_xmx";
	}

	private static final String KEY_GRAPH_FILES_ROOT = "graphFilesRoot";
	private static final String KEY_TEMP_FOLDER = "tempFolder";
	private static final String KEY_LOGS_FOLDER = "logsFolder";

	private static final String KEY_LANGUAGE = "language";
	private static final String KEY_CHARSET = "charset";

	public String getGraphFilesRoot()	{
		return config.getString(KEY_GRAPH_FILES_ROOT);
	}

	public String getTempFolder()	{
		String tempFolderPath = config.getString(KEY_TEMP_FOLDER);

		File tempFolder = new File(tempFolderPath);
		if (!tempFolder.exists())	{
			tempFolder.mkdirs();
		}

		return tempFolderPath;
	}
	public String getLogsFolder()	{
		String logsFolderPath = config.getString(KEY_LOGS_FOLDER);

		File logsFolder = new File(logsFolderPath);
		if (!logsFolder.exists())	{
			logsFolder.mkdirs();
		}

		return logsFolderPath;
	}

	public String getJVM_XMS_valueString()	{
		return config.getString(KEY_JVM_XMS);
	}
	public String getJVM_XMX_valueString()	{
		return config.getString(KEY_JVM_XMX);
	}


	public String getLanguageCode()	{
		return config.getString(KEY_LANGUAGE);
	}
	public String getCharsetCode()	{
		return config.getString(KEY_CHARSET);
	}
}

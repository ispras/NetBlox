package ru.ispras.modis.NetBlox.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.exceptions.ExternalException;
import ru.ispras.modis.NetBlox.exceptions.PluginException;

/**
 * A class launches external computations (applications) and waits for them to execute the task.
 * If the external application fails then its error log is saved in NetBlox logs directory.
 * 
 * @author ilya
 */
public class ExternalApplicationProvider {
	protected static final SystemConfiguration configuration = SystemConfiguration.getInstance();

	protected static final String FILES_SEPARATOR = SystemConfiguration.FILES_SEPARATOR;

	protected static final String XMS;
	protected static final String XMX;
	static	{
		XMS = "-Xms"+configuration.getJVM_XMS_valueString();
		XMX = "-Xmx"+configuration.getJVM_XMX_valueString();
	}


	protected static String getAppsForPluginRoot(Bundle pluginBundle, String relativePathInsidePlugin)	{
		/*String bundleLocation = pluginBundle.getLocation();
		System.out.println("\t"+bundleLocation+"\t- bundle location");
		try {
			//System.out.println("\t"+FileLocator.resolve(new URL(bundleLocation))+"\t- resolved bundle location"); - MalformedURLException
			System.out.println("\t"+FileLocator.getBundleFile(pluginBundle)+"\t- bundle file");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		URL minersRootURL = pluginBundle.getEntry(relativePathInsidePlugin+FILES_SEPARATOR);
		try {
			System.out.println("\t"+minersRootURL);
			minersRootURL = FileLocator.resolve(minersRootURL);
			System.out.println("\t-> "+minersRootURL);
		} catch (IOException e) {
			throw new PluginException("Couldn't initiate the "+pluginBundle.getSymbolicName()+" plug-in: "+e.getMessage());
		}

		String appsForPluginRoot = minersRootURL.getPath();
		return appsForPluginRoot;*/
		try {
			File bundleFile = FileLocator.getBundleFile(pluginBundle);
			//System.out.println("\t"+bundleFile.toString());
			if (!bundleFile.isDirectory())	{
				bundleFile = bundleFile.getParentFile();
				//System.out.println("\tparent: "+bundleFile.toString());
			}
			//System.out.println("\t"+pluginBundle.getLocation());

			String appsForPluginRoot = bundleFile.toString() + FILES_SEPARATOR + relativePathInsidePlugin + FILES_SEPARATOR;
			return appsForPluginRoot;
		} catch (IOException e) {
			throw new PluginException(e);
		}
	}


	protected static void runExternal(List<String> command, String launchDirectoryPathString, OutputStream outputStream) throws ExternalException	{
		File launchDirectory = new File(launchDirectoryPathString);
		runExternal(command, launchDirectory, outputStream);
	}

	/**
	 * Runs the external application specified by <code>command</code>.
	 * @param command	- the list containing the operating system program (the one that can be launched from command string) and its arguments.
	 * 					See java ProcessBuilder.
	 * @param launchDirectory	- the directory in which the command is to be launched.
	 * @param outputStream		- the stream to which the output of the launched application is to be redirected (can be null).
	 * @throws ExternalException 
	 */
	protected static void runExternal(List<String> command, File launchDirectory, OutputStream outputStream) throws ExternalException	{
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(launchDirectory);

		try {
			Process process = processBuilder.start();

			// It's necessary to redirect stdout and stderr of the process before process.waitFor() because otherwise there's a chance that
			// process output will overfill the pipe buffer and the process will hang.
			String stderrFileOutputName = getTempFolderPathString() + SystemConfiguration.FILES_SEPARATOR + "externalApp.stderr";
			FileOutputStream stderrFileOutputStream = new FileOutputStream(stderrFileOutputName);
			StreamsRedirector errorsRedirector = new StreamsRedirector(process.getErrorStream(), stderrFileOutputStream);
			StreamsRedirector outputRedirector = new StreamsRedirector(process.getInputStream(), outputStream);
			//XXX flush() and close() outputStream here instead of StreamsRedirector?

			outputRedirector.start();
			errorsRedirector.start();

			int exitCode = process.waitFor();
			if (exitCode != 0)	{
				stderrFileOutputStream.flush();	//Is necessary.
				stderrFileOutputStream.close();

				String errorLogStoragePathname = saveErrorLog(command, stderrFileOutputName);

				StringBuilder errorMessageBuilder = new StringBuilder("The external application (");
				for (String commandPiece : command)	{
					errorMessageBuilder.append(commandPiece).append(" ");
				}
				errorMessageBuilder.append(") has failed; see file '").append(errorLogStoragePathname).append("' for log.");
				throw new ExternalException(errorMessageBuilder.toString());
			}
		} catch (IOException e) {	//from	processBuilder.start()
			//e.printStackTrace();
			throw new ExternalException(e);
		} catch (InterruptedException e) {	//from	process.waitFor()
			//e.printStackTrace();
			throw new ExternalException(e);
		}
	}


	private static String saveErrorLog(List<String> command, String stderrFileOutputPath) throws IOException	{
		StringBuilder saveErrorLogPathBuilder = new StringBuilder(configuration.getLogsFolder()).
				append(SystemConfiguration.FILES_SEPARATOR).append("err_");

		long currentTime = System.currentTimeMillis();
		Date date = new Date(currentTime);
		String dateString = date.toString().replaceAll(":", "-");
		saveErrorLogPathBuilder.append(dateString).append(".log");

		String errorLogStoragePathname = saveErrorLogPathBuilder.toString();

		Path sourcePath = Paths.get(stderrFileOutputPath);
		Path targetPath = Paths.get(errorLogStoragePathname);
		Files.move(sourcePath, targetPath);

		return errorLogStoragePathname;
	}

	//private abstract static String printProcessStream(InputStream stream);


	/**
	 * If the <code>parameter</code> is not null, add it with the corresponding key to the list
	 * of parameters of command that will be later executed in <code>runExternal(...)</code>.
	 * @param command	- the list containing the operating system program (the one that can be launched from command string) and its arguments.
	 * 					See java ProcessBuilder.
	 * @param key		- a key for the <code>parameter</code>.
	 * @param parameter	- the parameter to be added.
	 */
	protected static void addNotNullParameter(List<String> command, String key, Object parameter)	{
		if (parameter != null)	{
			command.add(key);
			command.add(parameter.toString());
		}
	}


	protected static String getTempFolderPathString()	{
		return configuration.getTempFolder();
	}


	/**
	 * Allows to delete some files (in folder) that are nor required any more.
	 * @param pathString
	 */
	protected static void deleteRecursively(String pathString)	{
		File toBeDeleted = new File(pathString);
		if (!toBeDeleted.exists())	{
			return;
		}

		if (toBeDeleted.isDirectory())	{
			for (String fileInDirectory : toBeDeleted.list())	{
				deleteRecursively(pathString+FILES_SEPARATOR+fileInDirectory);
			}
		}

		toBeDeleted.delete();
	}
}

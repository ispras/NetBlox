package ru.ispras.modis.NetBlox.utils;

import ru.ispras.modis.NetBlox.exceptions.PluginException;

/**
 * Basic class for passing the results of plug-ins work to NetBlox.
 * 
 * @author ilya
 */
public abstract class ResultsFromPlugins {
	public enum ResultsProvisionFormat	{
		FILE_PATH_STRING, INTERNAL, LIST_OF_STRINGS, STREAM
	}

	private ResultsProvisionFormat resultsFormat;

	public ResultsFromPlugins(ResultsProvisionFormat format)	{
		this.resultsFormat = format;
	}

	public ResultsProvisionFormat getProvisionFormat()	{
		return resultsFormat;
	}


	protected void tellAboutUnimplementedMethod(ResultsProvisionFormat requiredFormat)	{
		if (resultsFormat.equals(requiredFormat))	{
			throwUnimplementedException();
		}
		else	{
			throwMismatchException();
		}
	}
	private void throwMismatchException()	{
		throw new PluginException("Mismatching results provision format ("+getProvisionFormat()+") and the function call.");
	}
	private void throwUnimplementedException()	{
		throw new UnsupportedOperationException("The method hasn't been implemented in plug-in.");
	}
}

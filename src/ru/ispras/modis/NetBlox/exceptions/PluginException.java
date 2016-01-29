package ru.ispras.modis.NetBlox.exceptions;

public class PluginException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PluginException(String message)	{
		super(message);
	}

	public PluginException(Exception e)	{
		super(e);
	}
}

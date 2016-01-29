package ru.ispras.modis.NetBlox.exceptions;

public class ExternalException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1487648904980483978L;

	public ExternalException(String message)	{
		super(message);
	}

	public ExternalException(Exception e)	{
		super(e);
	}
}

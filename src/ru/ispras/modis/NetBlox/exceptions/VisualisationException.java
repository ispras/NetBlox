package ru.ispras.modis.NetBlox.exceptions;

public class VisualisationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4259250983489181058L;

	public VisualisationException(String message)	{
		super(message);
	}

	public VisualisationException(Exception e)	{
		super(e);
	}
}

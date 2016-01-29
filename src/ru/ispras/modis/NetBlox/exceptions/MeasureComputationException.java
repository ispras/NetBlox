package ru.ispras.modis.NetBlox.exceptions;

public class MeasureComputationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7634210473325061644L;

	public MeasureComputationException(Exception e)	{
		super(e);
	}

	public MeasureComputationException(String message)	{
		super(message);
	}
}

package ru.ispras.modis.NetBlox.exceptions;

public class ResultsPresentationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5220480519269148490L;

	public ResultsPresentationException(String message)	{
		super(message);
	}

	public ResultsPresentationException(Exception e)	{
		super(e);
	}
}

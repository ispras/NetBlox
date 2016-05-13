package ru.ispras.modis.NetBlox.exceptions;

public class SourceGraphException extends Exception {
	public SourceGraphException(String message) {
		super(message);
	}

	public SourceGraphException(Exception e)	{
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}

package ru.ispras.modis.NetBlox.exceptions;

public class GraphMiningException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GraphMiningException(String message) {
		super(message);
	}

	public GraphMiningException(Exception e)	{
		super(e);
	}
}

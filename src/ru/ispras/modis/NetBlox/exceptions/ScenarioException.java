package ru.ispras.modis.NetBlox.exceptions;

public class ScenarioException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ScenarioException(String message) {
		super(message);
	}

	public ScenarioException(Exception e)	{
		super(e);
	}
}

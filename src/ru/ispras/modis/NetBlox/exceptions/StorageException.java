package ru.ispras.modis.NetBlox.exceptions;

public class StorageException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 141178092399597546L;

	public StorageException(Exception e)	{
		super(e);
	}

	public StorageException(String message)	{
		super(message);
	}
}

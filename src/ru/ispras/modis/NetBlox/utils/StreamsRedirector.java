package ru.ispras.modis.NetBlox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A class for redirecting some input stream. If the output stream is specified then
 * the input is redirected to it, otherwise to void.
 * 
 * @author ilya
 *
 */
public class StreamsRedirector extends Thread {
	InputStream inputStream;
	OutputStream outputStream = null;

	public StreamsRedirector(InputStream inputStream)	{
		this.inputStream = inputStream;
	}

	public StreamsRedirector(InputStream inputStream, OutputStream outputStream)	{
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}


	@Override
	public void run()	{
		PrintWriter printWriter = null;
		if (outputStream != null)	{
			printWriter = new PrintWriter(outputStream);
		}

		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line = null;
		try {
			while ((line=bufferedReader.readLine()) != null)	{
				if (printWriter != null)	{
					printWriter.println(line);
				}
			}
			if (printWriter != null)	{
				printWriter.flush();
			}

			if (outputStream != null)	{
				outputStream.flush();
				outputStream.close();
			}
		} catch (IOException e) {	// from bufferedReader.readLine()
			e.printStackTrace();
			//TODO Throw an exception to outer space? The system must know it has failed to get the output of the external process?
		}
	}
}

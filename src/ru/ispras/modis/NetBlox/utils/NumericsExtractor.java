package ru.ispras.modis.NetBlox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts numeric values in different ways from provided streams, lines.
 * 
 * @author ilya
 */
public class NumericsExtractor {
	private static final String WHITESPACE_CHARACTER_REGEX = "\\s";

	private static final String FLOAT_REGEX = "\\d*[\\.,]?\\d+([eE][-\\+]?\\d+)?";
	private static final Pattern FLOAT_PATTERN = Pattern.compile(FLOAT_REGEX);
	private static final Pattern FLOAT_AFTER_WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_CHARACTER_REGEX+FLOAT_REGEX);


	/**
	 * Extracts a single Float value from an InputStream.
	 * @param stream
	 * @return
	 * @throws IOException 
	 */
	public static Float extractSingleFloat(InputStream stream) throws IOException	{
		Float result = null;

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream), 1);

        String line = bufferedReader.readLine();
        if (line != null)	{
        	result = extractFloat(line);
        }

		stream.close();
		bufferedReader.close();

        return result;
	}


	/**
	 * Tries to extract a single float value from the line. If there's more than just one number in the line then tries
	 * to get the first float number that is preceded by a whitespace.
	 * @param line	- contains the number that we want to extract.
	 * @return
	 */
	public static Float extractFloat(String line)	{
		String floatInString = null;

		Matcher matcher = FLOAT_PATTERN.matcher(line);
		if (matcher.matches())	{
			floatInString = matcher.group();
		}

		if (floatInString == null)	{	//That means the line consisted of more than one number.
			matcher = FLOAT_AFTER_WHITESPACE_PATTERN.matcher(line);
			if (matcher.find())	{
				floatInString = matcher.group().trim();
			}
		}

		return Float.parseFloat(floatInString);
	}
}

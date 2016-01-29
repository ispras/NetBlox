package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;

/**
 * Some utilities and common constants for the parser.
 * 
 * @author ilya
 */
public class Utils {
	public static final String DELIMITER = ";";


	private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");

	public static boolean isWord(String string)	{
		Matcher matcher = WORD_PATTERN.matcher(string);
		return matcher.matches();
	}

	public static void checkWhetherIsWordInScenario(String string, String name, String surroundName)	{
		if (!isWord(string))	{
			StringBuilder messageBuilder = new StringBuilder("'").
					append(name).append("' in '").append(surroundName).
					append("' must be a word (consist of word characters: [a-zA-Z_0-9]).");
			throw new ScenarioException(messageBuilder.toString());
		}
	}


	public static String getId(Attributes attributes, String tagName, String idAttributeName) {
		String idString = attributes.getValue(idAttributeName);
		if (idString == null)	{
			return AlgorithmDescription.NO_ID;
		}
		else	{
			checkWhetherIsWordInScenario(idString, idAttributeName, tagName);
			return idString;
		}
	}
}

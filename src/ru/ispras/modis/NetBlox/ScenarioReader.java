package ru.ispras.modis.NetBlox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.ScenarioParser;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

/**
 * The class that reads a scenario file and launches the parsing process.
 * 
 * @author ilya
 */
public class ScenarioReader {
	private final static ScenarioParser parser = new ScenarioParser();


	public static Collection<ScenarioTask> read(String pathToScenario)	{
		InputStream scenarioInputStream;
		try {
			scenarioInputStream = new FileInputStream(pathToScenario);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		Collection<ScenarioTask> tasksCollection = new LinkedList<ScenarioTask>();
		try {
			runParser(scenarioInputStream, tasksCollection);
		} catch (ScenarioException e) {
			e.printStackTrace();
			return null;
		}	

		return tasksCollection;
	}


	private static void runParser(InputStream inputStream, Collection<ScenarioTask> resultStorage)	{
		SAXParserFactory saxfactory = SAXParserFactory.newInstance();
		saxfactory.setNamespaceAware(true);

		parser.setStorage(resultStorage);
		try {
			saxfactory.newSAXParser().parse(inputStream, parser);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ScenarioException(e);
		}
	}
}

package ru.ispras.modis.NetBlox.parser;

import java.io.InputStream;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IMeasureDescriptionParser;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.DescriptionMeasure;

public class MeasuresSectionParser extends ScenarioSectionParser {
	private static final String TAG_MEASURE = "measure";

	private static final String ATTRIBUTE_NAME = "name";


	public MeasuresSectionParser(ScenarioParser scenarioParser, ScenarioParserExtensionsRegistry extensionsRegistry, String scenarioSectionTag) {
		super(extensionsRegistry, scenarioSectionTag);

		defaultSectionElementProcessor = new MeasureTagProcessor(scenarioParser);
	}


	/**
	 * Called when child element is found.
	 */
	@Override
	public XMLElementProcessor startElement(String tagName, Attributes attributes) {
		if (!tagName.equalsIgnoreCase(TAG_MEASURE))	{
			throw new ScenarioException("Measures section must consist of <measure/>s.");
		}

		return startElement(tagName, attributes, ATTRIBUTE_NAME);
	}


	@Override
	protected void checkCallbackSuitsSection(Object callback) {
		if (!(callback instanceof IMeasureDescriptionParser))	{
			String errorMessage = "Measure description parser must implement "+IMeasureDescriptionParser.class.getName()+", while "
					+callback.getClass().getName()+" does not.";	//XXX Give more information about plug-in?
			throw new PluginException(errorMessage);
		}
	}


	@Override
	protected void putToTask(AlgorithmDescription parsedDescription, String currentAlgorithmName) {
		if (! (parsedDescription instanceof DescriptionMeasure))	{
			String errorMessage = "The parser of measures section elements for "+currentAlgorithmName+
					" measure (characteristic) has put its result to a wrong container (not "+DescriptionMeasure.class.getName()+" descendant).";
			throw new PluginException(errorMessage);
		}

		getTaskStorage().addMeasureDescription((DescriptionMeasure) parsedDescription);
	}



	private class MeasureTagProcessor extends SectionElementProcessor	{
		private IMeasureDescriptionParser descriptionParserCallback;

		public MeasureTagProcessor(ScenarioParser overallScenarioParser) {
			super(overallScenarioParser);
		}

		@Override
		public void setExtensionCallback(Object callback) {
			descriptionParserCallback = (IMeasureDescriptionParser) callback;
		}

		/**
		 * Called finally when the element is closed.
		 */
		@Override
		public void closeElement()	{
			InputStream contentStream = closeAndGetContent();

			DescriptionMeasure measureDescription = descriptionParserCallback.parseMeasureDescription(contentStream);
			setAlgorithmDescription(measureDescription);	//No IDs for measures, at least for now.
		}
	}
}

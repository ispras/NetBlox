package ru.ispras.modis.NetBlox.parser;

import java.util.Collection;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementWithChildrenProcessor;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

/**
 * The class to parse a single task from the scenario xml-file.
 * 
 * @author ilya
 */
public class TaskParser extends XMLElementWithChildrenProcessor {
	private static final String TAG_GRAPHS = "graphs";
	//private static final String TAG_PRELIMINARY = "preliminary";
	private static final String TAG_GRAPH_MINING = "graphMining";
	private static final String TAG_MEASURES = "measures";
	private static final String TAG_CHART = "chart";
	private static final String TAG_PLOT = "plot";
	private static final String TAG_VISUALISATION = "visualisation";

	private final GraphsSectionParser graphsProcessor;
	//TODO private final PreliminarySectionParser preliminarySectionProcessor;
	private final GraphMiningSectionParser graphMiningSectionProcessor;
	private MeasuresSectionParser measuresProcessor;
	private ChartParser chartProcessor;
	private PlotParser plotProcessor;
	private VisualisationSectionParser visualisationSectionProcessor;

	private static final String ATTRIBUTE_GOAL = "goal";
	private static final String ATTRIBUTE_GOAL_VALUE_NONE = "none";
	private static final String ATTRIBUTE_GOAL_VALUE_MINING = "mining";
	private static final String ATTRIBUTE_GOAL_VALUE_PERFORMANCE = "performance";
	private static final String ATTRIBUTE_GOAL_VALUE_MEASURES = "measures";
	private static final String ATTRIBUTE_GOAL_VALUE_GRAPH_VISUALISATION = "graphVisualisation";

	private Collection<ScenarioTask> tasksStorage;

	private ScenarioTask currentTask;


	public TaskParser(ScenarioParser overallScenarioParser, ScenarioParserExtensionsRegistry extensionsRegistry)	{
		super();

		//The following sections can be present in the scenario tasks of all types.
		addTaggedParser(TAG_GRAPHS, graphsProcessor = new GraphsSectionParser(
				overallScenarioParser, extensionsRegistry, TAG_GRAPHS));
		//TODO addTaggedParser(TAG_PRELIMINARY, preliminarySectionProcessor = new PreliminarySectionParser());
		addTaggedParser(TAG_GRAPH_MINING, graphMiningSectionProcessor = new GraphMiningSectionParser(
				overallScenarioParser, extensionsRegistry, TAG_GRAPH_MINING));

		addTaggedParser(TAG_MEASURES, measuresProcessor = new MeasuresSectionParser(
				overallScenarioParser, extensionsRegistry, TAG_MEASURES));

		addTaggedParser(TAG_CHART, chartProcessor = new ChartParser());
		addTaggedParser(TAG_PLOT, plotProcessor = new PlotParser());

		addTaggedParser(TAG_VISUALISATION, visualisationSectionProcessor = new VisualisationSectionParser());
	}


	public void setStorage(Collection<ScenarioTask> tasksStorage)	{
		this.tasksStorage = tasksStorage;
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		ScenarioTask.Goal goal = extractGoal(attributes);
		currentTask = new ScenarioTask(goal);

		graphsProcessor.setStorage(currentTask);
		//TODO preliminarySectionProcessor.setStorage(currentTask);
		graphMiningSectionProcessor.setStorage(currentTask);

		measuresProcessor.setStorage(currentTask);

		chartProcessor.setStorage(currentTask);
		plotProcessor.setStorage(currentTask);

		visualisationSectionProcessor.setStorage(currentTask);
	}

	@Override
	public void closeElement()	{
		super.closeElement();

		tasksStorage.add(currentTask);
	}


	private ScenarioTask.Goal extractGoal(Attributes attributes)	{
		ScenarioTask.Goal goal = null;

		String goalInText = attributes.getValue(ATTRIBUTE_GOAL);
		if (goalInText == null  ||  goalInText.isEmpty())	{
			throw new ScenarioException("Scenario goal must be specified.");
		}

		if (goalInText.equalsIgnoreCase(ATTRIBUTE_GOAL_VALUE_NONE))	{
			goal = ScenarioTask.Goal.NONE;
		}
		else if (goalInText.equalsIgnoreCase(ATTRIBUTE_GOAL_VALUE_MINING))	{
			goal = ScenarioTask.Goal.MINING;
		}
		else if (goalInText.equalsIgnoreCase(ATTRIBUTE_GOAL_VALUE_PERFORMANCE))	{
			goal = ScenarioTask.Goal.PERFORMANCE;
		}
		else if (goalInText.equalsIgnoreCase(ATTRIBUTE_GOAL_VALUE_MEASURES))	{
			goal = ScenarioTask.Goal.MEASURES;
		}
		else if (goalInText.equalsIgnoreCase(ATTRIBUTE_GOAL_VALUE_GRAPH_VISUALISATION))	{
			goal = ScenarioTask.Goal.GRAPH_VISUALISATION;
		}
		else	{
			throw new ScenarioException("Unknown scenario goal: "+goalInText);
		}

		return goal;
	}
}

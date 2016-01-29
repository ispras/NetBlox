package ru.ispras.modis.NetBlox.scenario;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ru.ispras.modis.NetBlox.scenario.performanceStats.PerformanceStatisticDescription;
import ru.ispras.modis.NetBlox.utils.Pair;

/**
 * All necessary information to describe one task from the testing scenario.
 * 
 * @author ilya
 */
public class ScenarioTask {
	public static final int NO_SUPPLEMENTARY_COMPUTATIONS = -1;

	public static enum Goal {
		NONE, MINING, PERFORMANCE, MEASURES, GRAPH_VISUALISATION
	}


	private Goal goal = Goal.MINING;
	private boolean isPresentGraphMiningSection = false;

	private Collection<AlgorithmDescription> graphs = null;
	private Map<String, AlgorithmDescription> preliminaryComputations = null;	//Map<algorithmID: String, description: DescriptionPreliminaryAlgorithm>
	private Collection<AlgorithmDescription> graphMiningAlgorithms = null;

	private Collection<AlgorithmDescription> measures = null;
	private Set<DescriptionMeasure> uniqueMeasuresByAlgorithm = null;	//XXX Is it necessary?

	private Collection<DescriptionDataArrangement> charts = null;
	private Collection<DescriptionDataArrangement> plots = null;

	//key - ID; value.1 - values of variation range, value.2 - URL for language resources from corresponding plug-in.
	private Map<String, Pair<RangeOfValues<?>, URL>> rangesOfValuesForVariations = null;


	private Collection<GraphVisualisationDescription> graphVisualisationDescriptions;


	public ScenarioTask(Goal goal)	{
		this.goal = goal;

		graphs = new LinkedList<AlgorithmDescription>();
		preliminaryComputations = new HashMap<String, AlgorithmDescription>();
		graphMiningAlgorithms = new LinkedList<AlgorithmDescription>();

		rangesOfValuesForVariations = new HashMap<String, Pair<RangeOfValues<?>, URL>>();

		if (goal == Goal.PERFORMANCE)	{
			DescriptionMeasure performanceMeasureDescription = new PerformanceStatisticDescription();
			uniqueMeasuresByAlgorithm = new HashSet<DescriptionMeasure>(1);
			uniqueMeasuresByAlgorithm.add(performanceMeasureDescription);	//Will be used in plot drawing process.
		}
	}


	public Goal getGoal()	{
		return goal;
	}

	public Iterator<ParametersSet> getGraphsIterator()	{
		return new ParametersSetsIterator(graphs);
	}

	public Iterator<ParametersSet> getGraphMinersIterator()	{
		return new ParametersSetsIterator(graphMiningAlgorithms);
	}

	public Iterator<ParametersSet> getMeasuresIterator()	{
		return new ParametersSetsIterator(measures);
	}


	public Collection<DescriptionDataArrangement> getChartsDescriptions()	{
		return Collections.unmodifiableCollection(charts);
	}

	public Collection<DescriptionDataArrangement> getPlotsDescriptions()	{
		return Collections.unmodifiableCollection(plots);
	}


	public boolean doRunGraphMining()	{
		return isPresentGraphMiningSection;
	}

	public boolean doNeedArrangedData()	{
		return (doNeedCharts() || doNeedPlots())  &&  (goal==Goal.PERFORMANCE || goal==Goal.MEASURES);
		//Either plots or charts are necessary for PERFORMANCE and MEASURES tasks -
		//to represent *performance*, *quality*, *graph_characteristic* or *communities_characteristic* statistics. 
	}

	public boolean doNeedCharts()	{
		return (charts != null)  &&  !charts.isEmpty();
	}

	public boolean doNeedPlots()	{
		return (plots != null)  &&  !plots.isEmpty();
	}


	public void addGraphTypeDescription(DescriptionGraphsOneType graphTypeDescription)	{
		graphs.add(graphTypeDescription);

		addVariations(graphTypeDescription.getAllVariations(), graphTypeDescription.getLanguageResourcesURL());
	}

	/*TODO public void addPreliminaryComputationAlgorithm(DescriptionPreliminaryAlgorithm algorithm, String algorithmId)	{
		preliminaryComputations.put(algorithmId, algorithm);
	}*/

	public void addGraphMiningAlgorithmDescription(DescriptionGraphMiningAlgorithm miningAlgorithmDescription)	{
		graphMiningAlgorithms.add(miningAlgorithmDescription);
		isPresentGraphMiningSection = true;

		miningAlgorithmDescription.setScenarioTask(this);

		addVariations(miningAlgorithmDescription.getAllVariations(), miningAlgorithmDescription.getLanguageResourcesURL());
	}

	public void addMeasureDescription(DescriptionMeasure measureDescription)	{
		if (measures == null)	{
			measures = new LinkedList<AlgorithmDescription>();
			uniqueMeasuresByAlgorithm = new HashSet<DescriptionMeasure>();
		}

		measures.add(measureDescription);
		uniqueMeasuresByAlgorithm.add(measureDescription);
	}

	public void addChartDescription(DescriptionDataArrangement chartDescription)	{
		if (charts == null)	{
			charts = new LinkedList<DescriptionDataArrangement>();
		}

		charts.add(chartDescription);
	}

	public void addPlotDescription(DescriptionDataArrangement plotDescription)	{
		if (plots == null)	{
			plots = new LinkedList<DescriptionDataArrangement>();
		}

		plots.add(plotDescription);
	}

	public void addGraphVisualisationDescription(GraphVisualisationDescription graphVisualisationDescription)	{
		if (graphVisualisationDescriptions == null)	{
			graphVisualisationDescriptions = new LinkedList<GraphVisualisationDescription>();
		}

		graphVisualisationDescriptions.add(graphVisualisationDescription);
	}


	public void addVariations(Collection<RangeOfValues<?>> variations, URL languageResourcesURL)	{
		for (RangeOfValues<?> variation : variations)	{
			if (variation == null)	{
				continue;
			}

			String variationId = variation.getRangeId();
			if (!variationId.equals(RangeOfValues.NO_RANGE_ID))	{
				Pair<RangeOfValues<?>, URL> variationAndLanguageResources = new Pair<RangeOfValues<?>, URL>(variation, languageResourcesURL);
				rangesOfValuesForVariations.put(variationId, variationAndLanguageResources);
			}
		}
	}


	public RangeOfValues<?> getVariationValues(String varId)	{
		return rangesOfValuesForVariations.get(varId).get1st();
	}
	public URL getVariationLanguageResourcesURL(String varId)	{
		return rangesOfValuesForVariations.get(varId).get2nd();
	}

	public Set<DescriptionMeasure> getUniqueByAlgorithmMeasuresDescriptions()	{
		return Collections.unmodifiableSet(uniqueMeasuresByAlgorithm);
	}

	public Collection<GraphVisualisationDescription> getGraphVisualisationDescriptions()	{
		return Collections.unmodifiableCollection(graphVisualisationDescriptions);
	}


	public DescriptionPreliminaryAlgorithm getPreliminaryComputationAlgorithmDescription(String setID)	{
		return (DescriptionPreliminaryAlgorithm) preliminaryComputations.get(setID);	//TODO Check.
	}
}

package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic.Type;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.AxesScale;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.PlotStyle;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.DescriptionMeasure;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

/**
 * Each instance of this class keeps several multidimensional arrays that contain values of one same measure type (algorithm).
 * A <code>SingleTypeBigChart</code> object corresponds to a plot with several lines (each represented by a multidimensional
 * array) or to a big chart composed of several lesser charts, each characterised by a single set of fixed varying parameters
 * (again one multidimensional array - one lesser chart).
 * 
 * @author ilya
 */
public class SingleTypeBigChart implements Iterable<MultiDimensionalArray>	{
	protected static final SystemConfiguration configuration = SystemConfiguration.getInstance();

	private DescriptionMeasure measureThatInterestsUs;	//Its values are aggregated in this Big Chart.
	private List<MultiDimensionalArray> lesserChartsForFixedParametersSets;

	private DescriptionDataArrangement dataArrangementDescription;


	public SingleTypeBigChart(ScenarioTask scenarioTask, DescriptionMeasure measure, DescriptionDataArrangement dataArrangementDescription)	{
		measureThatInterestsUs = measure;
		this.dataArrangementDescription = dataArrangementDescription;

		Collection<LabeledSetOfValues> fixedValuesSets = dataArrangementDescription.getFixedValuesSetsCollection();
		CoordinateVector<String> variationIDsForDimensionsList = dataArrangementDescription.getVariationIDsForDimensionsList();
		lesserChartsForFixedParametersSets = new ArrayList<MultiDimensionalArray>(fixedValuesSets.size());
		for (LabeledSetOfValues fixedValuesSet : fixedValuesSets)	{
			lesserChartsForFixedParametersSets.add(new MultiDimensionalArray(scenarioTask, variationIDsForDimensionsList, fixedValuesSet));
		}
	}


	public boolean isRelevant(MeasureParametersSet measureParametersSet)	{
		return measureThatInterestsUs.doesBelong(measureParametersSet);
	}

	public String getMeasureValuesName()	{
		return measureThatInterestsUs.getValuesName();
	}

	public String get1stDimensionName()	{
		if (getIndividualValueType() == Type.FUNCTION)	{
			return measureThatInterestsUs.getFunctionArgumentName();
		}
		else	{
			String dimensionLabel = lesserChartsForFixedParametersSets.get(0).getDimensionLabel(1);
			return dimensionLabel;
		}
	}

	public String getChartName()	{
		StringBuilder nameBuilder = new StringBuilder(measureThatInterestsUs.getNameInScenario());

		MultiDimensionalArray anArray = lesserChartsForFixedParametersSets.get(0);
		for (int i=1 ; i<=anArray.getNumberOfDimensions() ; i++)	{
			nameBuilder.append("_").append(anArray.getDimensionTag(i));
		}
		nameBuilder.append('_').append(dataArrangementDescription.getName());

		return nameBuilder.toString();
	}


	public NumericCharacteristic.Type getIndividualValueType()	{
		Iterator<MultiDimensionalArray> lesserChartsIterator = lesserChartsForFixedParametersSets.iterator();
		if (!lesserChartsIterator.hasNext())	{
			return null;
		}

		return lesserChartsIterator.next().getContainedValuesType();
	}

	public PlotStyle getPlotStyle()	{
		return dataArrangementDescription.getPlotStyle();
	}

	public AxesScale getAxesScale()	{
		return dataArrangementDescription.getAxesScale();
	}

	public StatisticsAggregation getStatisticsAggregationType()	{
		return dataArrangementDescription.getStatisticsAggregationType();
	}

	public int getPlotWidth()	{
		return dataArrangementDescription.getPlotWidth();
	}

	public int getPlotHeight()	{
		return dataArrangementDescription.getPlotHeight();
	}

	public boolean toShowGraphsData()	{
		return dataArrangementDescription.toShowGraphsData();
	}
	public boolean showLegend()	{
		return dataArrangementDescription.showLegend();
	}
	public boolean plotIncludesZero()	{
		return dataArrangementDescription.plotIncludesZero();
	}


	public Set<GraphOnDriveHandler> getParticipatingGraphsHandlers()	{
		SortedSet<GraphOnDriveHandler> handlers = new TreeSet<GraphOnDriveHandler>(new GraphOnDriveHandler.PositionInRowComparator());

		for (MultiDimensionalArray lesserChart : lesserChartsForFixedParametersSets)	{
			handlers.addAll(lesserChart.getParticipatingGraphsHandlers());
		}

		return handlers;
	}

	/**
	 * Says whether any of the lines of this chart uses averaging along a variation.
	 */
	public boolean doAveraging()	{
		Collection<LabeledSetOfValues> linesDescriptions = dataArrangementDescription.getFixedValuesSetsCollection();
		for (LabeledSetOfValues lineDescription : linesDescriptions)	{
			if (lineDescription.doAverageAlongVariation())	{
				return true;
			}
		}
		return false;
	}


	public int getNumberOfLines()	{
		return lesserChartsForFixedParametersSets.size();
	}

	@Override
	public Iterator<MultiDimensionalArray> iterator() {
		return lesserChartsForFixedParametersSets.iterator();
	}
}
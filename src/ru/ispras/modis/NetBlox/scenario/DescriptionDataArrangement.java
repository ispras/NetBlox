package ru.ispras.modis.NetBlox.scenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.modis.NetBlox.dataStructures.internalMechs.CoordinateVector;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.LabeledSetOfValues;

/**
 * Contains the description of data arrangement for charts and plots, parsed from scenario.
 * <p>
 * <code>collectionOfFixedValuesSets</code> contains several sets of fixed values for different
 * parameters presented by a range of values (instead of single values); these parameters are
 * specified by their variation IDs.
 * 
 * @author ilya
 */
public class DescriptionDataArrangement {
	public enum PlotStyle {LINE, BAR, STEP, SCATTER, HISTOGRAM};
	public enum AxesScale {SIMPLE, X_LOG10, Y_LOG10, XY_LOG10};
	public enum StatisticsAggregation {NONE, DISTRIBUTION, BIG_INTERVALS, CUMULATIVE_AVERAGE};


	private CoordinateVector<String> variationIdsForDimensions;
	private List<LabeledSetOfValues> listOfFixedValuesSets;

	private String arrangementName;

	private PlotStyle plotStyle = PlotStyle.LINE;
	private AxesScale axesScale = AxesScale.SIMPLE;
	private StatisticsAggregation statisticsAggregation = StatisticsAggregation.DISTRIBUTION;
	private boolean showGraphsData = false;	// whether to add subtitles with graphs data
	private boolean showLegend = true;	// whether to draw legend for the plot
	private boolean plotIncludesZero = true;

	private int plotWidth = 800;
	private int plotHeight = 600;


	public DescriptionDataArrangement(String name)	{
		arrangementName = name;

		variationIdsForDimensions = new CoordinateVector<String>(1);
		listOfFixedValuesSets = new ArrayList<LabeledSetOfValues>(2);
	}


	public void addDimension(int number, String variationId)	{
		variationIdsForDimensions.set(number, variationId);
	}


	public void addFixedValuesSet(String label, Map<String, String> valuesForIds, String graphId, String gcdId, Collection<String> averageByVariationsIds)	{
		if (label == null)	{
			label = "label"+listOfFixedValuesSets.size();
		}

		listOfFixedValuesSets.add(new LabeledSetOfValues(label, valuesForIds, graphId, gcdId, averageByVariationsIds));
	}


	public CoordinateVector<String> getVariationIDsForDimensionsList()	{
		return variationIdsForDimensions;
	}

	public Collection<LabeledSetOfValues> getFixedValuesSetsCollection()	{
		return Collections.unmodifiableCollection(listOfFixedValuesSets);
	}


	public String getName()	{
		return arrangementName;
	}


	public void setPlotStyle(PlotStyle plotType)	{
		this.plotStyle = plotType;
	}

	public PlotStyle getPlotStyle()	{
		return plotStyle;
	}


	public void setAxesScale(AxesScale axesScale)	{
		this.axesScale = axesScale;
	}

	public AxesScale getAxesScale()	{
		return axesScale;
	}


	public void setAggregationType(StatisticsAggregation aggregationType)	{
		statisticsAggregation = aggregationType;
	}

	public StatisticsAggregation getStatisticsAggregationType()	{
		return statisticsAggregation;
	}


	public void setShowGraphsData(boolean b)	{
		showGraphsData = b;
	}

	public boolean toShowGraphsData()	{
		return showGraphsData;
	}

	public void setShowLegend(boolean b)	{
		showLegend = b;
	}

	public boolean showLegend()	{
		return showLegend;
	}

	public void plotIncludesZero(boolean b)	{
		plotIncludesZero = b;
	}

	public boolean plotIncludesZero()	{
		return plotIncludesZero;
	}


	public void setWidth(int width)	{
		plotWidth = width;
	}

	public void setHeight(int height)	{
		plotHeight = height;
	}


	public int getPlotWidth()	{
		return plotWidth;
	}

	public int getPlotHeight()	{
		return plotHeight;
	}
}

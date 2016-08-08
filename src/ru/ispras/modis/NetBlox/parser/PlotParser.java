package ru.ispras.modis.NetBlox.parser;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.basicParsersAndUtils.Utils;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.AxesScale;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.BoxAndWhiskersStyle;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.PlotStyle;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

public class PlotParser extends DataArrangementParser {
	class XAxisProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();

			String xVariationID = getText();
			if (xVariationID == null  ||  xVariationID.isEmpty())	{
				throw new ScenarioException("The variation ID specified by <x/> must be unempty.");
			}
			Utils.checkWhetherIsWordInScenario(xVariationID, TAG_X, arrangementTag);

			currentArrangementDescription.addDimension(1, xVariationID);
		}
	}

	class WidthProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			currentArrangementDescription.setWidth(Integer.parseInt(getText()));
		}
	}

	class HeightProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			currentArrangementDescription.setHeight(Integer.parseInt(getText()));
		}
	}

	class ValuesScalingProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			currentArrangementDescription.setValuesScaling(Float.parseFloat(getText()));
		}
	}


	private static final String TAG_X = "x";
	private static final String TAG_LINE = "line";
	private static final String TAG_WIDTH = "width";
	private static final String TAG_HEIGHT = "height";
	private static final String TAG_VALUE_SCALING_COEFFICIENT = "valuesScalingCoef";

	private final FixedVaryingParametersSetParser lineDescriptionProcessor;

	private static final String ATTRIBUTE_PLOT_STYLE = "style";
	private static final String ATTRIBUTE_PLOT_STYLE_LINE = "line";
	private static final String ATTRIBUTE_PLOT_STYLE_BAR = "bar";
	private static final String ATTRIBUTE_PLOT_STYLE_STEP = "step";
	private static final String ATTRIBUTE_PLOT_STYLE_SCATTER = "scatter";
	private static final String ATTRIBUTE_PLOT_STYLE_HIST = "hist";

	private static final String ATTRIBUTE_PLOT_AXES_SCALE = "scale";
	private static final String ATTRIBUTE_PLOT_AXES_SCALE_XLOG10 = "xlog10";
	private static final String ATTRIBUTE_PLOT_AXES_SCALE_YLOG10 = "ylog10";
	private static final String ATTRIBUTE_PLOT_AXES_SCALE_LOG10 = "log10";

	private static final String ATTRIBUTE_AGGREGATION = "aggregation";
	private static final String ATTRIBUTE_AGGREGATION_CUMULATIVE_AVERAGE = "cumulativeAverage";
	private static final String ATTRIBUTE_AGGREGATION_BIG_INTERVALS = "bigIntervals";	//XXX Unimplemented really?
	private static final String ATTRIBUTE_AGGREGATION_NONE = "none";

	private static final String ATTRIBUTE_SHOW_GRAPHS_DATA = "showGraphsData";
	private static final String ATTRIBUTE_SHOW_LEGEND = "showLegend";
	private static final String ATTRIBUTE_PLOT_INCLUDES_ZERO = "plotIncludesZero";

	private static final String ATTRIBUTE_BOX_AND_WHISKERS_STYLE = "boxAndWhiskers";	// min_max, noBox
	private static final String ATTRIBUTE_BOX_AND_WHISKERS_NO_BOX = "no_box";


	public PlotParser()	{
		addTaggedParser(TAG_X, new XAxisProcessor());
		addTaggedParser(TAG_LINE, lineDescriptionProcessor = new FixedVaryingParametersSetParser());
		addTaggedParser(TAG_WIDTH, new WidthProcessor());
		addTaggedParser(TAG_HEIGHT, new HeightProcessor());
		addTaggedParser(TAG_VALUE_SCALING_COEFFICIENT, new ValuesScalingProcessor());
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		String plotStyle = attributes.getValue(ATTRIBUTE_PLOT_STYLE);
		if (plotStyle != null)	{
			if (plotStyle.equalsIgnoreCase(ATTRIBUTE_PLOT_STYLE_LINE))	{
				currentArrangementDescription.setPlotStyle(PlotStyle.LINE);
			}
			else if (plotStyle.equalsIgnoreCase(ATTRIBUTE_PLOT_STYLE_BAR))	{
				currentArrangementDescription.setPlotStyle(PlotStyle.BAR);
			}
			else if (plotStyle.equalsIgnoreCase(ATTRIBUTE_PLOT_STYLE_STEP))	{
				currentArrangementDescription.setPlotStyle(PlotStyle.STEP);
			}
			else if (plotStyle.equalsIgnoreCase(ATTRIBUTE_PLOT_STYLE_SCATTER))	{
				currentArrangementDescription.setPlotStyle(PlotStyle.SCATTER);
			}
			else if (plotStyle.equalsIgnoreCase(ATTRIBUTE_PLOT_STYLE_HIST))	{
				currentArrangementDescription.setPlotStyle(PlotStyle.HISTOGRAM);
			}
		}

		String plotAxesScale = attributes.getValue(ATTRIBUTE_PLOT_AXES_SCALE);
		if (plotAxesScale != null)	{
			if (plotAxesScale.equalsIgnoreCase(ATTRIBUTE_PLOT_AXES_SCALE_XLOG10))	{
				currentArrangementDescription.setAxesScale(AxesScale.X_LOG10);
			}
			else if (plotAxesScale.equalsIgnoreCase(ATTRIBUTE_PLOT_AXES_SCALE_YLOG10))	{
				currentArrangementDescription.setAxesScale(AxesScale.Y_LOG10);
			}
			else if (plotAxesScale.equalsIgnoreCase(ATTRIBUTE_PLOT_AXES_SCALE_LOG10))	{
				currentArrangementDescription.setAxesScale(AxesScale.XY_LOG10);
			}
		}

		String plotStatisticAggregationTypeString = attributes.getValue(ATTRIBUTE_AGGREGATION);
		if (plotStatisticAggregationTypeString != null)	{
			if (plotStatisticAggregationTypeString.equalsIgnoreCase(ATTRIBUTE_AGGREGATION_CUMULATIVE_AVERAGE))	{
				currentArrangementDescription.setAggregationType(StatisticsAggregation.CUMULATIVE_AVERAGE);
			}
			else if (plotStatisticAggregationTypeString.equalsIgnoreCase(ATTRIBUTE_AGGREGATION_BIG_INTERVALS))	{
				currentArrangementDescription.setAggregationType(StatisticsAggregation.BIG_INTERVALS);
			}
			else if (plotStatisticAggregationTypeString.equalsIgnoreCase(ATTRIBUTE_AGGREGATION_NONE))	{
				currentArrangementDescription.setAggregationType(StatisticsAggregation.NONE);
			}
		}

		String booleanDataString = attributes.getValue(ATTRIBUTE_SHOW_GRAPHS_DATA);
		if (booleanDataString != null  &&  !booleanDataString.isEmpty())	{
			currentArrangementDescription.setShowGraphsData(Boolean.parseBoolean(booleanDataString));
		}

		booleanDataString = attributes.getValue(ATTRIBUTE_SHOW_LEGEND);
		if (booleanDataString != null  &&  !booleanDataString.isEmpty())	{
			currentArrangementDescription.setShowLegend(Boolean.parseBoolean(booleanDataString));
		}

		booleanDataString = attributes.getValue(ATTRIBUTE_PLOT_INCLUDES_ZERO);
		if (booleanDataString != null  &&  !booleanDataString.isEmpty())	{
			currentArrangementDescription.plotIncludesZero(Boolean.parseBoolean(booleanDataString));
		}

		String boxAndWhiskersStyle = attributes.getValue(ATTRIBUTE_BOX_AND_WHISKERS_STYLE);
		if (boxAndWhiskersStyle != null  &&  !boxAndWhiskersStyle.isEmpty())	{
			if (boxAndWhiskersStyle.equalsIgnoreCase(ATTRIBUTE_BOX_AND_WHISKERS_NO_BOX))	{
				currentArrangementDescription.setBoxAndWhiskersStyle(BoxAndWhiskersStyle.NO_BOX);
			}
		}

		currentArrangementDescription.addDimension(1, RangeOfValues.NO_RANGE_ID);	//Set default value for the 1st dimension ID, in case it won't be specified.	

		lineDescriptionProcessor.setStorage(currentArrangementDescription);
	}


	@Override
	public void closeElement()	{
		super.closeElement();
		taskStorage.addPlotDescription(currentArrangementDescription);
	}
}

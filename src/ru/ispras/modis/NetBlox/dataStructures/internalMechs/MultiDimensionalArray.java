package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.DataArrangementException;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;
import ru.ispras.modis.NetBlox.scenario.ValueFromRange;

public class MultiDimensionalArray implements Cloneable {
	public class DataCell	{
		private NumericCharacteristic valueCarried;
		private GraphParametersSet graphParameters = null;


		public DataCell(NumericCharacteristic value, GraphParametersSet graphParameters)	{
			this.valueCarried = value;
			this.graphParameters = graphParameters;
		}


		public NumericCharacteristic getCarriedValue()	{
			return valueCarried;
		}

		public GraphParametersSet getGraphParameters()	{
			return graphParameters;
		}
	}



	private ScenarioTask scenarioTask;

	private CoordinateVector<String> variationsIDsForDimensionsList;
	private LabeledSetOfValues fixedParametersLabeledSet;

	private SortedSet<GraphOnDriveHandler> participatingGraphsHandlers;


	private SnakeKeyMap<CoordinateVector<Object>, Object, DataCell> dataContainer;
	//1st key: coordinates; 2nd key: values of variations along which we will be averaging computed values.

	public static final CoordinateVector<Object> DEFAULT_SINGLE_COORDINATE_VALUE = new CoordinateVector<Object>(1);
	public static final int FIRST_DIMENSION = 1;



	public MultiDimensionalArray(ScenarioTask scenarioTask, CoordinateVector<String> variationIDsForDimensionsList, LabeledSetOfValues fixedParameters)	{
		this.scenarioTask = scenarioTask;
		this.variationsIDsForDimensionsList = variationIDsForDimensionsList;
		this.fixedParametersLabeledSet = fixedParameters;

		dataContainer = new SnakeKeyMap<CoordinateVector<Object>, Object, DataCell>();
		participatingGraphsHandlers = new TreeSet<GraphOnDriveHandler>(new GraphOnDriveHandler.PositionInRowComparator());
	}


	/**
	 * Put <code>value</code> into the multidimensional array. Coordinates are defined by algorithms parameters with which the value has been computed.
	 * @param value
	 * @param fixedParametersGraph
	 * @param analysedDataIdentifier
	 * @param fixedParametersMeasure
	 * @throws DataArrangementException
	 */
	public void putValue(NumericCharacteristic value, GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet fixedParametersMeasure) throws DataArrangementException	{
		GraphParametersSet fixedParametersGraph = graphHandler.getGraphParameters();

		CoordinateVector<Object> coordinates = prepareCoordinates(fixedParametersGraph, analysedDataIdentifier, fixedParametersMeasure);

		List<Object> valuesOfVariationsOverWhichAverageCharacteristics = prepareValuesOfVariationsOverWhichToAverageCharacteristics(
				fixedParametersGraph, analysedDataIdentifier, fixedParametersMeasure);

		//Put the value to the prepared positions.
		DataCell dataCell = new DataCell(value, fixedParametersGraph);
		if (coordinates.areAllCoordinatesSpecified())	{
			dataContainer.put(coordinates, valuesOfVariationsOverWhichAverageCharacteristics, dataCell);
		}
		else	{
			putFixedValueToWholeRangeOfCoordinates(coordinates, valuesOfVariationsOverWhichAverageCharacteristics, dataCell);
		}

		//Add the handler for the original graph to the set of handlers for those that will be part of the "line".
		participatingGraphsHandlers.add(graphHandler);
	}


	private List<Object> prepareValuesOfVariationsOverWhichToAverageCharacteristics(GraphParametersSet fixedParametersGraph,
			AnalysedDataIdentifier analysedDataIdentifier, MeasureParametersSet fixedParametersMeasure) throws DataArrangementException	{
		if (!fixedParametersLabeledSet.doAverageAlongVariation())	{
			return null;
		}

		Collection<String> variationsIds = fixedParametersLabeledSet.getAverageByVariationsIds();
		List<Object> valuesOfVariationsOverWhichAverageCharacteristics = new ArrayList<Object>(variationsIds.size());

		for (String averageByVariationId : variationsIds)	{
			if (analysedDataIdentifier == null)	{	//We're dealing just graph characteristics, without any graph mining done.
				valuesOfVariationsOverWhichAverageCharacteristics.add(getCoordinateValue(averageByVariationId,
						fixedParametersGraph, fixedParametersMeasure));
			}
			else if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.MINED)	{
				valuesOfVariationsOverWhichAverageCharacteristics.add(getCoordinateValue(averageByVariationId,
						fixedParametersGraph, analysedDataIdentifier.getMiningParameters(), fixedParametersMeasure));
			}
			else if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.EXTERNAL)	{
				valuesOfVariationsOverWhichAverageCharacteristics.add(getCoordinateValue(averageByVariationId,
						fixedParametersGraph, analysedDataIdentifier.getExternalFilepathAsInScenario(), fixedParametersMeasure));
			}
		}

		return valuesOfVariationsOverWhichAverageCharacteristics;
	}


	private CoordinateVector<Object> prepareCoordinates(GraphParametersSet fixedParametersGraph, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet fixedParametersMeasure) throws DataArrangementException	{
		if (variationsIDsForDimensionsList.getCoordinate(FIRST_DIMENSION).equals(RangeOfValues.NO_RANGE_ID))	{
			return DEFAULT_SINGLE_COORDINATE_VALUE;
		}

		CoordinateVector<Object> coordinates = new CoordinateVector<Object>(getNumberOfDimensions());
		int dimension = 0;
		for (String variationId : variationsIDsForDimensionsList)	{
			dimension++;

			Object coordinateValue = null;
			if (analysedDataIdentifier == null)	{
				coordinateValue = getCoordinateValue(variationId, fixedParametersGraph, fixedParametersMeasure);
			}
			else if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.MINED)	{
				coordinateValue = getCoordinateValue(variationId,
						fixedParametersGraph, analysedDataIdentifier.getMiningParameters(), fixedParametersMeasure);
			}
			else if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.EXTERNAL)	{
				coordinateValue = getCoordinateValue(variationId,
						fixedParametersGraph, analysedDataIdentifier.getExternalFilepathAsInScenario(), fixedParametersMeasure);
			}

			if (coordinateValue != null)	{
				coordinates.set(dimension, coordinateValue);
			}
			else	{
				coordinates.setUnspecifiedCoordinate(dimension, variationId);
			}
		}

		return coordinates;
	}

	private Object getCoordinateValue(String variationId, GraphParametersSet fixedParametersGraph, MeasureParametersSet fixedParametersMeasure)
			throws DataArrangementException	{
		Object coordinateValue;
		if ((coordinateValue = getCoordinateValue(variationId, fixedParametersGraph)) != null)	{
			return coordinateValue;
		}

		if ((coordinateValue = getCoordinateValue(variationId, fixedParametersMeasure)) != null)	{
			return coordinateValue;
		}

		StringBuilder exceptionMessage = new StringBuilder("The variation with ID '").append(variationId).
				append("' could not be found in any of proposed parameter sets:\n graph=").append(fixedParametersGraph.toString()).
				append(", measure=[").append(fixedParametersMeasure.getCharacteristicNameInScenario()).append("]");
				//XXX Add data about the implementor? Or some other data to identify more specificly?
		throw(new DataArrangementException(exceptionMessage.toString()));
	}

	private Object getCoordinateValue(String variationId, GraphParametersSet fixedParametersGraph, ExtendedMiningParameters extendedMiningParameters,
			MeasureParametersSet fixedParametersMeasure) throws DataArrangementException	{
		Object coordinateValue;
		if ((coordinateValue = getCoordinateValue(variationId, fixedParametersGraph)) != null)	{
			return coordinateValue;
		}

		ValueFromRange<String> externalForMiningFilename = extendedMiningParameters.getRelativeExternalFilename();
		if (externalForMiningFilename != null)	{
			if (variationId.equals(externalForMiningFilename.getRangeId()))	{
				return externalForMiningFilename.getValue();
			}
		}

		GraphMiningParametersSet graphMiningParameters = extendedMiningParameters.getMiningParameters();
		if (graphMiningParameters.useSupplementaryData())	{
			for (ParametersSet supplementaryAlgorithmParameters : graphMiningParameters.getPreliminaryCalculationsParametersSets())	{
				if ((coordinateValue = getCoordinateValue(variationId, supplementaryAlgorithmParameters)) != null)	{
					return coordinateValue;
				}
			}
		}

		if ((coordinateValue = getCoordinateValue(variationId, graphMiningParameters)) != null)	{
			return coordinateValue;
		}

		if ((coordinateValue = getCoordinateValue(variationId, fixedParametersMeasure)) != null)	{
			return coordinateValue;
		}

		if (graphMiningParameters.hasVariationBeenFixedConstant(variationId))	{
			return null;
		}

		StringBuilder exceptionMessage = new StringBuilder("The variation with ID '").append(variationId).
				append("' could not be found in any of proposed parameter sets:\n graph=").append(fixedParametersGraph.toString()).
				append(", miner=[").append(graphMiningParameters.getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues()).
				append("], measure=[").append(fixedParametersMeasure.getCharacteristicNameInScenario()).append("]");
				//XXX Add data about the implementor? Or some other data to identify more specificly?
		throw(new DataArrangementException(exceptionMessage.toString()));
	}

	private Object getCoordinateValue(String variationId, GraphParametersSet fixedParametersGraph, String externalSetOfGroupsOfNodesFileName,
			MeasureParametersSet fixedParametersMeasure) throws DataArrangementException	{
		Object coordinateValue;
		if ((coordinateValue = getCoordinateValue(variationId, fixedParametersGraph)) != null)	{
			return coordinateValue;
		}

		RangeOfValues<String> providedCoversFilesNames = fixedParametersGraph.getProvidedForCharacterizationExternalCoversFilenames();
		if (variationId.equals(providedCoversFilesNames.getRangeId()))	{
			return externalSetOfGroupsOfNodesFileName;
		}

		if ((coordinateValue = getCoordinateValue(variationId, fixedParametersMeasure)) != null)	{
			return coordinateValue;
		}

		StringBuilder exceptionMessage = new StringBuilder("The variation with ID '").append(variationId).
				append("' could not be found in any of proposed parameter sets:\n graph=").append(fixedParametersGraph.toString()).
				append(", external cover file name=").append(externalSetOfGroupsOfNodesFileName).
				append(", measure=[").append(fixedParametersMeasure.getCharacteristicNameInScenario()).append("]");
				//XXX Add data about the implementor? Or some other data to identify more specificly?
		throw(new DataArrangementException(exceptionMessage.toString()));
	}

	private Object getCoordinateValue(String variationId, ParametersSet fixedParametersSet)	{
		Object result = null;
		if ((fixedParametersSet != null)  &&  fixedParametersSet.hasParametersFromSomeRange())	{
			result = fixedParametersSet.getValueForVariationId(variationId);
		}

		return result;
	}


	private void putFixedValueToWholeRangeOfCoordinates(CoordinateVector<Object> coordinates, Object valueOfVariationOverWhichAverage, DataCell dataCell)	{
		Collection<Integer> dimensionsWithUnfixedValues = coordinates.getDimensionsWithUnspecifiedValues();
		Iterator<Integer> dimensionsIterator = dimensionsWithUnfixedValues.iterator();
		putValueToDimensions(dimensionsIterator, coordinates, valueOfVariationOverWhichAverage, dataCell);
	}

	private void putValueToDimensions(Iterator<Integer> dimensionsIterator, CoordinateVector<Object> coordinates, Object valueOfVariationOverWhichAverage,
			DataCell dataCell)	{
		if (dimensionsIterator.hasNext())	{
			Integer dimension = dimensionsIterator.next();
			String variationId = coordinates.getUnspecifiedCoordinateVariationId(dimension);
			RangeOfValues<?> rangeOfCoordinateValues = scenarioTask.getVariationValues(variationId);
			for (Object coordinateValue : rangeOfCoordinateValues)	{
				coordinates.set(dimension, coordinateValue);
				putValueToDimensions(dimensionsIterator, coordinates, valueOfVariationOverWhichAverage, dataCell);
			}
		}
		else	{
			CoordinateVector<Object> effectiveCoordinates = coordinates.clone();	//We use a clone here so that the coordinates would be independent objects in each case.
			dataContainer.put(effectiveCoordinates, valueOfVariationOverWhichAverage, dataCell);
		}
	}


	/**
	 * Get from the multidimensional array the <code>value</code> with specified <code>coordinates</code>.
	 * @param coordinates
	 * @return
	 * @throws ResultsPresentationException 
	 */
	public DataCell getDataCell(CoordinateVector<Object> coordinates) throws ResultsPresentationException	{
		Collection<DataCell> toBeAveraged = getMultipleValues(coordinates);
		int numberOfElements = toBeAveraged.size();

		Iterator<DataCell> dataCellsIterator = toBeAveraged.iterator();
		DataCell dataCell = dataCellsIterator.next();
		if (dataCell == null)	{	//#4761. Bring information about failures of plug-ins to plots.
			return dataCell;
		}

		NumericCharacteristic result = dataCell.getCarriedValue();
		if (result != null)	{
			while (dataCellsIterator.hasNext())	{
				dataCell = dataCellsIterator.next();
				if (dataCell == null)	{	//#4761. Bring information about failures of plug-ins to plots.
					System.out.println("WARNING:\tAt least one of plug-ins with methods has fallen down.");
					return dataCell;
				}

				NumericCharacteristic otherCharacteristic = dataCell.getCarriedValue();
				if (otherCharacteristic != null)	{
					result.add(otherCharacteristic);	//XXX Is supported only for SINGLE_VALUE now.
				}
				else	{
					System.out.println("WARNING:\tAt least one of characteristic values in a row to be averaged was not computed "
							+"(one of methods in the pipeline got no results).");
					result = null;	//#4689. Plot 'absence of data'.
					numberOfElements = 1;
					break;
				}
			}
			if (numberOfElements > 1)	{
				result.divideBy(numberOfElements);
			}
		}

		return new DataCell(result, dataCell.getGraphParameters());
	}

	public Collection<DataCell> getMultipleValues(CoordinateVector<Object> coordinates) throws ResultsPresentationException	{
		Map<Object, DataCell> mappedMultipleValues = dataContainer.getTails(coordinates);
		if (mappedMultipleValues == null)	{
			System.out.println(
					"WARNING:\tAn algorithm mentioned in scenario data arrangement description has failed and there's no data (results) for it. "+
					"Pay attention to plots.");
			Collection<DataCell> nullResult = new ArrayList<DataCell>(1);
			nullResult.add(null);
			return nullResult;	//#4761. Bring information about failures of plug-ins to plots.
		}
		return mappedMultipleValues.values();
	}

	/**
	 * Checks whether there's data in this MultiDimensionalArray that is supposed to be averaged according to scenario description.
	 */
	public boolean hasDataToBeAveraged() throws ResultsPresentationException	{
		Collection<CoordinateVector<Object>> allCoordinates = dataContainer.getHeads();
		if (allCoordinates.isEmpty())	{
			return false;
		}
		CoordinateVector<Object> firstCoordinate = allCoordinates.iterator().next();

		Map<Object, DataCell> mappedMultipleValues = dataContainer.getTails(firstCoordinate);
		if (mappedMultipleValues == null)	{
			System.out.println(
					"WARNING:\tAn algorithm mentioned in scenario data arrangement description has failed and there's no data (results) for it. "+
					"Pay attention to plots.");
			return false;
		}

		if (mappedMultipleValues.size() > 1)	{
			for (DataCell dataCell : mappedMultipleValues.values())	{
				//#4761. If one of the required plug-ins has failed or found no results then behave as if all in the line did the same.
				if (dataCell == null  ||  dataCell.getCarriedValue() == null)	{
					return false;
				}
			}
			return true;
		}
		return false;
	}


	public LabeledSetOfValues getSpecifiedFixedParameters()	{
		return fixedParametersLabeledSet;
	}

	public String getLabel()	{
		return fixedParametersLabeledSet.getLabel();
	}

	public NumericCharacteristic.Type getContainedValuesType()	{
		Iterator<CoordinateVector<Object>> coordinatesIterator = dataContainer.getHeads().iterator();
		while (coordinatesIterator.hasNext())	{
			CoordinateVector<Object> coordinateVector = coordinatesIterator.next();
			Iterator<DataCell> valuesIterator = dataContainer.getTails(coordinateVector).values().iterator();
			while (valuesIterator.hasNext())	{
				NumericCharacteristic characteristic = valuesIterator.next().getCarriedValue();
				if (characteristic != null)	{
					return characteristic.getType();
				}
			}
		}

		return null;
	}


	public int getNumberOfDimensions()	{
		return variationsIDsForDimensionsList.getNumberOfDimensions();
	}

	public String getVariationIdForDimension(int dimension)	{
		return variationsIDsForDimensionsList.getCoordinate(dimension);
	}

	public String getDimensionTag(int dimension)	{
		String dimensionVariationId = getVariationIdForDimension(dimension);

		String dimensionTag = "no"+dimension+"dimension";
		if (!dimensionVariationId.equals(RangeOfValues.NO_RANGE_ID))	{
			RangeOfValues<?> variationRange = scenarioTask.getVariationValues(dimensionVariationId);
			dimensionTag = variationRange.getRangeTag();
		}
		return dimensionTag;
	}

	public String getDimensionLabel(int dimension)	{
		String dimensionVariationId = getVariationIdForDimension(dimension);
		String dimensionTag = getDimensionTag(dimension);

		String label;
		if (!dimensionVariationId.equals(RangeOfValues.NO_RANGE_ID))	{
			URL languageResourcesURL = scenarioTask.getVariationLanguageResourcesURL(dimensionVariationId);
			LanguagesConfiguration languageConfiguration = LanguagesConfiguration.getConfiguration(languageResourcesURL);
			label = languageConfiguration.getLabel(dimensionTag);
		}
		else	{
			label = LanguagesConfiguration.getNetBloxLabel(dimensionTag);
		}
		return label;
	}


	public Set<GraphOnDriveHandler> getParticipatingGraphsHandlers()	{
		return participatingGraphsHandlers;
	}
}

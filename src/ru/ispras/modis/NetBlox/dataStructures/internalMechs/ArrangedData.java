package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.DataArrangementException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement;
import ru.ispras.modis.NetBlox.scenario.DescriptionMeasure;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;
import ru.ispras.modis.NetBlox.scenario.ValueFromRange;

public class ArrangedData {
	private final ScenarioTask scenarioTask;

	private List<SingleTypeBigChart> charts = null;
	private List<SingleTypeBigChart> plots = null;


	public ArrangedData(ScenarioTask task)	{
		scenarioTask = task;

		//FUTURE_WORK: If there're charts or plots required for output, check the correspondence of dimensions. If they don't match the varied parameters, throw error message.

		if (scenarioTask.doNeedCharts())	{
			charts = prepareChartsStructure(scenarioTask.getChartsDescriptions());
		}

		if (scenarioTask.doNeedPlots())	{
			plots = prepareChartsStructure(scenarioTask.getPlotsDescriptions());
		}
	}

	private List<SingleTypeBigChart> prepareChartsStructure(Collection<DescriptionDataArrangement> descriptions)	{
		List<SingleTypeBigChart> result = new LinkedList<SingleTypeBigChart>();
		for (DescriptionMeasure measureDescription : scenarioTask.getUniqueByAlgorithmMeasuresDescriptions())	{
			for (DescriptionDataArrangement arrangementDescription : descriptions)	{
				result.add(new SingleTypeBigChart(scenarioTask, measureDescription, arrangementDescription));
			}
		}

		return result;
	}


	/**
	 * Put the results of computations for fixed parameters into the corresponding cell of the structure.
	 * @param graphHandler				- a handler to the graph for which the computations have been performed.
	 * @param analysedDataIdentifier	- describes the groups of nodes that cover the graph, for which the statistic has been computed.
	 * @param fixedParametersMeasure	- describes which measures (statistics) have been computed.
	 * @param value		- the computed value.
	 * @throws DataArrangementException 
	 */
	public void putComputedStatistic(GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet fixedParametersMeasure, NumericCharacteristic value) throws DataArrangementException	{
		if (scenarioTask.doNeedCharts())	{
			putValueToStructure(charts, value, graphHandler, analysedDataIdentifier, fixedParametersMeasure);
		}

		if (scenarioTask.doNeedPlots())	{
			putValueToStructure(plots, value, graphHandler, analysedDataIdentifier, fixedParametersMeasure);
		}
	}


	/**
	 * A line of a plot (or choice of values for a chart) is specified by concrete fixed values of varying parameters; they a represented by <code>variedValuesFixedForLine</code>.
	 * The method checks whether the set of all parameters fixed for a computation run (for a value of a measure or statistic) fits into the constrains for this line.
	 * @param variedValuesFixedForLine
	 * @param fixedParametersGraph
	 * @param analysedDataIdentifier
	 * @param fixedParametersMeasure
	 * @return
	 */
	private boolean doesValueBelongToLine(LabeledSetOfValues parametersPrespecifiedForChart, GraphParametersSet fixedParametersGraph,
			AnalysedDataIdentifier analysedDataIdentifier, MeasureParametersSet fixedParametersMeasure)	{
		if (!parametersPrespecifiedForChart.doesGraphDescriptionFit(fixedParametersGraph) ||
				!doesCoverFit(parametersPrespecifiedForChart, analysedDataIdentifier))	{
			return false;
		}

		Map<String, String> variedValuesFixedForLine = parametersPrespecifiedForChart.getValuesForIds();
		for (Map.Entry<String, String> variationIdValuePair : variedValuesFixedForLine.entrySet())	{
			if (!doParameterValuesFit(variationIdValuePair, fixedParametersGraph))	{
				return false;
			}

			if (!doParameterValuesForAnalisedDataFit(variationIdValuePair, fixedParametersGraph, analysedDataIdentifier))	{
				return false;
			}

			if (!doParameterValuesFit(variationIdValuePair, fixedParametersMeasure))	{
				return false;
			}
		}

		return true;
	}

	private boolean doesCoverFit(LabeledSetOfValues parametersPrespecifiedForChart, AnalysedDataIdentifier analysedDataIdentifier)	{
		boolean result = true;

		if (analysedDataIdentifier != null)	{
			if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.MINED)	{
				result = parametersPrespecifiedForChart.doesGraphMinerDescriptionFit(analysedDataIdentifier.getMiningParameters().getMiningParameters());
			}
			else if (parametersPrespecifiedForChart.isGraphMinerDescriptionSpecified())	{
				result = false;
			}
		}

		return result;
	}

	private boolean doParameterValuesForAnalisedDataFit(Map.Entry<String, String> variationIdValuePair,
			GraphParametersSet fixedParametersGraph, AnalysedDataIdentifier analysedDataIdentifier)	{
		if (analysedDataIdentifier == null)	{
			return true;
		}

		if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.MINED)	{
			ExtendedMiningParameters extendedMiningParameters = analysedDataIdentifier.getMiningParameters();

			ValueFromRange<String> externalForMiningRelativeFilename = extendedMiningParameters.getRelativeExternalFilename();
			if (externalForMiningRelativeFilename != null)	{
				if (!doParameterValuesFit(variationIdValuePair, externalForMiningRelativeFilename.getValue(),
						externalForMiningRelativeFilename.getRangeId()))	{
					return false;
				}
			}

			GraphMiningParametersSet graphMiningParameters = extendedMiningParameters.getMiningParameters();
			if (graphMiningParameters.useSupplementaryData())	{
				for (ParametersSet supplementaryAlgorithmParameters : graphMiningParameters.getPreliminaryCalculationsParametersSets())	{
					if (!doParameterValuesFit(variationIdValuePair, supplementaryAlgorithmParameters))	{
						return false;
					}
				}
			}

			if (!doParameterValuesFit(variationIdValuePair, graphMiningParameters))	{
				return false;
			}
		}
		else if (analysedDataIdentifier.type() == AnalysedDataIdentifier.Type.EXTERNAL)	{
			if (!doParameterValuesFit(variationIdValuePair, analysedDataIdentifier.getExternalFilepathAsInScenario(),
					fixedParametersGraph.getProvidedForCharacterizationExternalCoversFilenames().getRangeId()))	{
				return false;
			}
		}

		return true;
	}

	private boolean doParameterValuesFit(Map.Entry<String, String> variationIdValuePair, ParametersSet fixedParameters)	{
		boolean result = true;
		if (fixedParameters != null  &&  fixedParameters.hasParametersFromSomeRange())	{
			Object fixedValue = fixedParameters.getValueForVariationId(variationIdValuePair.getKey());
			if (fixedValue == null)	{
				return true;
			}

			String requiredValue = variationIdValuePair.getValue();

			if (!requiredValue.equals(fixedValue.toString()))	{
				result = false;
			}
		}

		return result;
	}

	private boolean doParameterValuesFit(Map.Entry<String, String> variationIdValuePair, String externalSetOfGroupsOfNodesFileName,
			String externalSetsOfGroupsVariationID)	{
		boolean result = true;

		String checkedVariationId = variationIdValuePair.getKey();
		if (checkedVariationId.equals(externalSetsOfGroupsVariationID))	{
			String requiredValue = variationIdValuePair.getValue();
			if (!requiredValue.equals(externalSetOfGroupsOfNodesFileName))	{
				result = false;
			}
		}

		return result;
	}


	private void putValueToStructure(List<SingleTypeBigChart> structureGroups, NumericCharacteristic value,
			GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier, MeasureParametersSet fixedParametersMeasure)
					throws DataArrangementException	{
		for (SingleTypeBigChart setOfArraysForValues : structureGroups)	{
			if (!setOfArraysForValues.isRelevant(fixedParametersMeasure))	{
				continue;
			}

			for (MultiDimensionalArray arrayForValues : setOfArraysForValues)	{
				LabeledSetOfValues parametersPrespecifiedForChart = arrayForValues.getSpecifiedFixedParameters();
				if (!doesValueBelongToLine(parametersPrespecifiedForChart, graphHandler.getGraphParameters(),
						analysedDataIdentifier, fixedParametersMeasure))	{
					continue;
				}

				try	{
					arrayForValues.putValue(value, graphHandler, analysedDataIdentifier, fixedParametersMeasure);
				} catch (DataArrangementException e) {
					throw new DataArrangementException("Failed to put value into arrangement structure: "+e.getMessage());
				}
			}
		}
	}


	public List<SingleTypeBigChart> getArrangedForCharts()	{
		return Collections.unmodifiableList(charts);
	}

	public List<SingleTypeBigChart> getArrangedForPlots()	{
		return Collections.unmodifiableList(plots);
	}
}

package ru.ispras.modis.NetBlox.dataStructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Container for characteristic statistics and quality measures.
 * Can be represented as a single (float) value, a set of float values, a distribution of values or a function.
 * 
 * TODO Extract an interface with a limited set of methods that will be available to plug-ins?
 * TODO Or how about making a descendant/container class that will have the methods that are of no need inside plug-ins? Or dividing the methods somehow else?
 * 
 * XXX Check whether all methods are really used in the final version of NetBlox.
 * 
 * @author ilya
 */
public class NumericCharacteristic {
	public enum Type {	SINGLE_VALUE, LIST_OF_VALUES, DISTRIBUTION, FUNCTION	}

	public class Distribution	{
		private Map<Number, Integer> numbersOfOccurences;
		private int numberOfAddedValues;

		public Distribution()	{
			numbersOfOccurences = new HashMap<Number, Integer>();
			numberOfAddedValues = 0;
		}

		public void addValue(Number value)	{
			Integer numberOfOccurences;
			if (numbersOfOccurences.containsKey(value))	{
				numberOfOccurences = numbersOfOccurences.get(value) + 1;
			}
			else	{
				numberOfOccurences = 1;
			}
			numbersOfOccurences.put(value, numberOfOccurences);
			numberOfAddedValues++;
		}

		public void addValue(Number value, int numberOfOccurences)	{
			int totalNumberOfOccurences = numberOfOccurences;
			if (numbersOfOccurences.containsKey(value))	{
				totalNumberOfOccurences += numbersOfOccurences.get(value);
			}
			numbersOfOccurences.put(value, totalNumberOfOccurences);
			numberOfAddedValues += numberOfOccurences;
		}


		public Set<Number> getValues()	{
			return numbersOfOccurences.keySet();
		}

		public double getProbability(Number value)	{
			if (!numbersOfOccurences.containsKey(value))	{
				return 0;
			}
			double numberOfOccurences = numbersOfOccurences.get(value);
			return numberOfOccurences / numberOfAddedValues;
		}

		public Integer getNumberOfOccurences(Number value)	{
			if (!numbersOfOccurences.containsKey(value))	{
				return 0;
			}
			return numbersOfOccurences.get(value);
		}

		public int getTotalNumberOfOccurences()	{
			return numberOfAddedValues;
		}

		public int getNumberOfDifferentValues()	{
			return numbersOfOccurences.size();
		}
	}


	private Type type;
	private float singleValue;
	private Distribution distribution = null;
	private List<Double> listOfValues = null;
	private Map<Double, Double> function = null;

	public NumericCharacteristic(Type type)	{
		this.type = type;
		if (type == Type.DISTRIBUTION)	{
			distribution = new Distribution();
		}
		else if (type == Type.LIST_OF_VALUES)	{
			listOfValues = new LinkedList<Double>();			//distribution = new Distribution();
		}
		else if (type == Type.FUNCTION)	{
			function = new HashMap<Double, Double>();
		}
	}

	public NumericCharacteristic(Type type, float value)	{
		if (type != Type.SINGLE_VALUE)	{
			//XXX Error. Throw exception.
		}
		this.type = type;
		singleValue = value;
	}

	public NumericCharacteristic(Type type, List<Double> values)	{
		if (type != Type.LIST_OF_VALUES)	{
			//XXX Error. Throw exception.
		}
		this.type = type;
		listOfValues = values;
	}


	public void putValue(float value)	{
		if (type != Type.SINGLE_VALUE)	{
			//XXX Error. Throw exception.
		}
		singleValue = value;
	}

	public void addValue(Double value)	{
		if (type != Type.LIST_OF_VALUES)	{
			//XXX Error. Throw exception.
		}
		listOfValues.add(value);		//distribution.addValue(value);
	}

	public void addToDistribution(Integer value)	{
		if (type != Type.DISTRIBUTION)	{
			//XXX Error. Throw exception.
		}
		distribution.addValue(value);
	}

	public void addToDistribution(Integer value, int numberOfOccurences)	{
		if (type != Type.DISTRIBUTION)	{
			//XXX Error. Throw exception.
		}
		distribution.addValue(value, numberOfOccurences);
	}

	public void putToFunction(Double parameter, Double value)	{
		if (type != Type.FUNCTION)	{
			//XXX Error. Throw exception.
		}
		function.put(parameter, value);
	}


	public Type getType()	{
		return type;
	}

	public float getValue()	{
		if (type != Type.SINGLE_VALUE)	{
			//XXX Error. Throw exception.
		}
		return singleValue;
	}

	public List<Double> getValues()	{
		if (type != Type.LIST_OF_VALUES)	{
			//XXX Error. Throw exception.
		}
		return Collections.unmodifiableList(listOfValues);
	}

	public Distribution getDistribution()	{
		if (type == Type.SINGLE_VALUE)	{
			//XXX Error. Throw exception.
		}
		else if (type == Type.LIST_OF_VALUES)	{
			distribution = makeDistributionOutOfListOfValues();
		}

		return distribution;
	}

	public Map<Double, Double> getFunction()	{
		if (type != Type.FUNCTION)	{
			//XXX Error. Throw exception.
		}
		return function;
	}


	private Distribution makeDistributionOutOfListOfValues()	{
		Distribution distribution = new Distribution();

		int numberOfIntervals = (int) Math.floor(Math.sqrt(listOfValues.size()));
		//int numberOfIntervals = (int) Math.floor(Math.pow(listOfValues.size(), 1.0/3));
		//int numberOfIntervals = (int) Math.floor(Math.log(listOfValues.size()));

		double minValue = Collections.min(listOfValues);
		double maxValue = Collections.max(listOfValues)*1.00001;
		double intervalLength = (maxValue - minValue) / numberOfIntervals;
		//double halfInterval = 0.5 * intervalLength;

		LinkedList<Double> localList = new LinkedList<Double>(listOfValues);
		double intervalMinValue = minValue;
		for (int i=1 ; i<=numberOfIntervals ; i++)	{
			double intervalMaxValue = intervalMinValue + intervalLength;
			//double intervalMidPoint = intervalMinValue + halfInterval;

			ListIterator<Double> localValuesIterator = localList.listIterator();
			while (localValuesIterator.hasNext())	{
				double candidate = localValuesIterator.next();
				if (intervalMinValue <= candidate  &&  candidate <= intervalMaxValue)	{
					//distribution.addValue(intervalMidPoint);
					distribution.addValue(intervalMinValue);
					localValuesIterator.remove();
				}
			}

			intervalMinValue = intervalMaxValue;
		}

		return distribution;
	}


	public float getAverage()	{
		switch (type)	{
		case DISTRIBUTION:
			float totalValuesSum = 0;
			for (Map.Entry<Number, Integer> valueOccurencesPair : distribution.numbersOfOccurences.entrySet())	{
				Number value = valueOccurencesPair.getKey();
				Integer numberOfOccurences = valueOccurencesPair.getValue();
				totalValuesSum += value.doubleValue()*numberOfOccurences;
			}
			return totalValuesSum / distribution.numberOfAddedValues;
		case LIST_OF_VALUES:
			float result = 0;
			for (Double value : listOfValues)	{
				result += value;
			}
			result /= listOfValues.size();
			return result;
		}

		return singleValue;
	}

	public double getMedian()	{
		switch (type)	{
		case DISTRIBUTION:
			SortedMap<Number, Integer> localMap = new TreeMap<Number, Integer>(distribution.numbersOfOccurences);
			int halfQuantity = distribution.numberOfAddedValues / 2;
			int numberChecked = 0;
			for (Map.Entry<Number, Integer> distributionEntry : localMap.entrySet())	{
				numberChecked += distributionEntry.getValue();
				if (numberChecked >= halfQuantity)	{
					return distributionEntry.getKey().doubleValue();
				}
			}
			break;

		case LIST_OF_VALUES:
			List<Double> localValues = new ArrayList<Double>(listOfValues);
			Collections.sort(localValues);
			int medianIndex = localValues.size() / 2;
			return localValues.get(medianIndex);
		}

		return singleValue;
	}

	public int getSampleSize()	{
		int result = 1;
		switch (type)	{
		case DISTRIBUTION:
			result = distribution.numberOfAddedValues;
			break;
		case LIST_OF_VALUES:
			result = listOfValues.size();
		}
		return result;
	}

	public float getStandardDeviation()	{
		float result = 0;
		float average = getAverage();
		float difference = 0;

		switch (type)	{
		case DISTRIBUTION:
			float totalDifferencesSum = 0;
			for (Map.Entry<Number, Integer> valueOccurencesPair : distribution.numbersOfOccurences.entrySet())	{
				Number value = valueOccurencesPair.getKey();
				Integer numberOfOccurences = valueOccurencesPair.getValue();
				difference = value.floatValue() - average;
				totalDifferencesSum += difference*difference * numberOfOccurences;
			}
			result = totalDifferencesSum / distribution.numberOfAddedValues;
			break;
		case LIST_OF_VALUES:
			for (Double value : listOfValues)	{
				difference = (float) (value - average);
				result += difference*difference;
			}
			result /= listOfValues.size();
			break;
		}

		return (float) Math.sqrt(result);
	}


	public void add(NumericCharacteristic other)	{
		if (type != other.type)	{
			//TODO Error! Throw exception.
		}

		switch (type)	{
		case SINGLE_VALUE:
			singleValue += other.singleValue;
			break;
		case LIST_OF_VALUES:
			//TODO Implement summation of lists of characteristic values.
			throw new UnsupportedOperationException("Do not support summation of lists of characteristic values.");
		case DISTRIBUTION:
			//TODO Implement summation of distributions.
			throw new UnsupportedOperationException("Do not support summation of distributions.");
		case FUNCTION:
			//TODO Implement summation of functions.
			throw new UnsupportedOperationException("Do not support summation of functions.");
		}
	}

	public void divideBy(Number divisor)	{
		switch (type)	{
		case SINGLE_VALUE:
			singleValue /= divisor.floatValue();
			break;
		case LIST_OF_VALUES:
			//TODO Implement division of lists of characteristic values by divisor.
			throw new UnsupportedOperationException("Do not support division of lists of characteristic values.");
		case DISTRIBUTION:
			//TODO Implement division of distributions by divisor.
			throw new UnsupportedOperationException("Do not support division of distributions.");
		case FUNCTION:
			//TODO Implement division of function values by divisor.
			throw new UnsupportedOperationException("Do not support division of functions.");
		}
	}
}

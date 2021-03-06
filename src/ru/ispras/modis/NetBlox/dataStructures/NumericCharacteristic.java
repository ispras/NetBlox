package ru.ispras.modis.NetBlox.dataStructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.modis.NetBlox.utils.NumbersComparator;

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
		private SortedMap<Number, Integer> numbersOfOccurences;
		private int numberOfAddedValues;

		public Distribution()	{
			numbersOfOccurences = new TreeMap<Number, Integer>(new NumbersComparator());
			numberOfAddedValues = 0;
		}

		public Distribution(Distribution toCopy)	{
			numbersOfOccurences = new TreeMap<Number, Integer>(toCopy.numbersOfOccurences);
			numberOfAddedValues = toCopy.numberOfAddedValues;
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

	private Float distributionScalingCoefficient = null;


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

	public NumericCharacteristic(NumericCharacteristic toCopy)	{
		type = toCopy.type;
		switch (type)	{
		case SINGLE_VALUE:
			singleValue = toCopy.singleValue;
			break;
		case LIST_OF_VALUES:
			listOfValues = new ArrayList<Double>(toCopy.listOfValues);
			break;
		case DISTRIBUTION:
			distribution = new Distribution(toCopy.distribution);
			break;
		case FUNCTION:
			function = new HashMap<Double, Double>(toCopy.function);
			break;
		}
		//distributionScalingCoefficient-? Unnecessary.
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

	public void addToDistribution(Number value)	{
		if (type != Type.DISTRIBUTION)	{
			//XXX Error. Throw exception.
		}
		distribution.addValue(value);
	}

	public void addToDistribution(Number value, int numberOfOccurences)	{
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
			if (distribution == null)	{
				distribution = makeDistributionOutOfListOfValues();
			}
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

		Iterator<Double> iterator = listOfValues.iterator();
		Double minValue = iterator.next();	//Collections.min(listOfValues);
		while (minValue.equals(Double.NaN) || minValue.equals(Double.NEGATIVE_INFINITY) || minValue.equals(Double.POSITIVE_INFINITY))	{
			distribution.addValue(minValue);
			if (iterator.hasNext())	{
				minValue = iterator.next();
			}
			else	{
				return distribution;
			}
		}
		Double maxValue = minValue;			//Collections.max(listOfValues)*1.00001;
		while (iterator.hasNext())	{
			Double value = iterator.next();
			if (value.equals(Double.NaN) || value.equals(Double.NEGATIVE_INFINITY) || value.equals(Double.POSITIVE_INFINITY))	{
				distribution.addValue(value);
			}
			else	{
				minValue = Math.min(minValue, value);
				maxValue = Math.max(maxValue, value);
			}
		}
		double intervalLength = (maxValue*1.0001 - minValue) / numberOfIntervals;
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
					//distribution.addValue(intervalMaxValue);
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
			//XXX Error! Throw exception.
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
			List<Double> newList = new ArrayList<Double>(listOfValues.size());
			for (Double value : listOfValues)	{
				newList.add(value / divisor.doubleValue());
			}
			listOfValues = newList;
			break;
		case DISTRIBUTION:
			//All numbers of occurrences are to be multiplied, so the overall distribution doesn't change.
			//So no need to do the multiplications in 1st place.
			break;
		case FUNCTION:
			for (Map.Entry<Double, Double> argumentAndValue : function.entrySet())	{
				Double value = argumentAndValue.getValue() / divisor.doubleValue();
				argumentAndValue.setValue(value);
			}
			break;
		}
	}

	public void multiplyBy(Number multiplier)	{
		switch (type)	{
		case SINGLE_VALUE:
			singleValue *= multiplier.floatValue();
			break;
		case LIST_OF_VALUES:
			List<Double> newList = new ArrayList<Double>(listOfValues.size());
			for (Double value : listOfValues)	{
				newList.add(value * multiplier.doubleValue());
			}
			listOfValues = newList;
			break;
		case DISTRIBUTION:
			setDistributionScalingCoefficient(multiplier.floatValue());
			break;
		case FUNCTION:
			for (Map.Entry<Double, Double> argumentAndValue : function.entrySet())	{
				Double value = argumentAndValue.getValue() * multiplier.doubleValue();
				argumentAndValue.setValue(value);
			}
			break;
		}
	}

	public void setDistributionScalingCoefficient(Float coefficient)	{
		distributionScalingCoefficient = coefficient;
	}
	public Float getDistributionScalingCoefficient()	{
		return distributionScalingCoefficient;
	}
}

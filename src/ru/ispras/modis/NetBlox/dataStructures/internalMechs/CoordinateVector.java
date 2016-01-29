package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A coordinate vector with values for coordinates.
 * @author ilya
 *
 * @param <T> - the type of values of coordinates.
 */
public class CoordinateVector<T> implements Cloneable, Iterable<T>	{
	private ArrayList<T> coordinates;
	private Map<Integer, String> unspecifiedCoordinates = null;	//Map<dimesion, variationId>

	public CoordinateVector(int numberOfDimensions)	{
		coordinates = new ArrayList<T>(numberOfDimensions);
		for (int i=0 ; i<numberOfDimensions ; i++)	{
			coordinates.add(null);
		}
	}


	public void set(int dimension, T coordinateValue)	{
		int currentSize = coordinates.size();
		if (dimension > currentSize)	{
			coordinates.ensureCapacity(dimension);
			for (int i=currentSize ; i<dimension ; i++)	{
				coordinates.add(null);
			}
		}

		coordinates.set(getArrayPositionForDimension(dimension), coordinateValue);
	}

	/**
	 * Sets that a coordinate for <code>dimension</code> is unspecified, but belongs to variation <code>variationId</code>.
	 * @param dimension
	 * @param variationId
	 */
	public void setUnspecifiedCoordinate(Integer dimension, String variationId)	{
		if (unspecifiedCoordinates == null)	{
			unspecifiedCoordinates = new HashMap<Integer, String>();
		}
		unspecifiedCoordinates.put(dimension, variationId);
	}


	public int getNumberOfDimensions()	{
		return coordinates.size();
	}

	public T getCoordinate(int dimension)	{
		return coordinates.get(getArrayPositionForDimension(dimension));
	}


	public boolean areAllCoordinatesSpecified()	{
		return (unspecifiedCoordinates == null)  ||  unspecifiedCoordinates.isEmpty();
	}

	public Collection<Integer> getDimensionsWithUnspecifiedValues()	{
		return unspecifiedCoordinates.keySet();
	}

	public String getUnspecifiedCoordinateVariationId(Integer dimension)	{
		return unspecifiedCoordinates.get(dimension);
	}


	private int getArrayPositionForDimension(int dimension)	{
		return (dimension - 1);
	}


	@Override
	public boolean equals(Object obj)	{
		if (obj instanceof CoordinateVector)	{
			ArrayList<?> coordinates2 = ((CoordinateVector<?>)obj).coordinates;
			if (coordinates.size() != coordinates2.size())	{
				return false;
			}

			for (int i=0 ; i < coordinates.size() ; i++)	{
				if (!coordinates.get(i).equals(coordinates2.get(i)))	{
					return false;
				}
			}
		}
		else	{
			return super.equals(obj);
		}

		return true;
	}

	@Override
	public int hashCode()	{
		return coordinates.hashCode();
	}


	@Override
	public CoordinateVector<T> clone()	{
		CoordinateVector<T> clone = new CoordinateVector<T>(coordinates.size());

		int dimension = 0;
		for (T coordinate : coordinates)	{
			dimension++;
			clone.set(dimension, coordinate);	//XXX Is it fine to put right the values themselves?
		}

		//XXX Do we need to clone unspecifiedCoordinates as well? Currently I can't see what for.

		return clone;
	}

	@Override
	public Iterator<T> iterator() {
		return coordinates.iterator();
	}
}
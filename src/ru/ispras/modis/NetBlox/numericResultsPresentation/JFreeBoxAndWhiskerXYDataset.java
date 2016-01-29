package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jfree.data.Range;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;

import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SnakeKeyMap;

/**
 * An implementation of <code>BoxAndWhiskerXYDataset</code> interface that is used
 * by corresponding plots drawers of JFreeChart library. This dataset can contain
 * data for multiple series.
 * 
 * @author ilya
 */
public class JFreeBoxAndWhiskerXYDataset extends DefaultBoxAndWhiskerXYDataset {
	private static final long serialVersionUID = -5394790346486074701L;

	private SnakeKeyMap<Comparable<?>, Number, BoxAndWhiskerItem> data;
	//1st key: series key; 2nd key: x-axis coordinate.
	private List<Comparable<?>> seriesKeysList;

	private Number minimumRangeValue = null;
	private Number maximumRangeValue = null;
	private Range rangeBounds = null;


	public JFreeBoxAndWhiskerXYDataset() {
		super(null);

		data = new SnakeKeyMap<Comparable<?>, Number, BoxAndWhiskerItem>();
		seriesKeysList = new ArrayList<Comparable<?>>();
	}


	public void add(Comparable<?> seriesKey, Number position, BoxAndWhiskerItem item)	{
		data.put(seriesKey, position, item);
		if (!seriesKeysList.contains(seriesKey))	{
			seriesKeysList.add(seriesKey);
		}

		Number localMinimum = item.getMinRegularValue();
		Number localMaximum = item.getMaxRegularValue();

		boolean refreshRange = false;
		if ((minimumRangeValue == null)  ||  (localMinimum.doubleValue() < minimumRangeValue.doubleValue()))	{
			minimumRangeValue = localMinimum;
			refreshRange = true;
		}
		if ((maximumRangeValue == null)  ||  (localMaximum.doubleValue() > maximumRangeValue.doubleValue()))	{
			maximumRangeValue = localMaximum;
			refreshRange = true;
		}

		if (refreshRange)	{
			rangeBounds = new Range(minimumRangeValue.doubleValue(), maximumRangeValue.doubleValue());
		}

		fireDatasetChanged();
	}


	@Override
	public int getSeriesCount()	{
		return seriesKeysList.size();
	}

	@Override
	public int getItemCount(int series)	{
		return data.getTails(getSeriesKey(series)).size();
	}

	@Override
	public Comparable<?> getSeriesKey(int index)	{
		if (index >= getSeriesCount())	{
			//XXX Throw an exception?
			return null;
		}
		return seriesKeysList.get(index);
	}


	/**
	 * Return an item from within the dataset.
	 *
	 * @param series  the series index (ignored, since this dataset contains only one series).
	 * @param itemIndex  the item within the series (zero-based index)
	 * @return The item.
	 */
	public BoxAndWhiskerItem getItem(int seriesIndex, int itemIndex)	{
		Comparable<?> seriesKey = getSeriesKey(seriesIndex);
		Number coordinate = getX(seriesIndex, itemIndex);
		return data.get(seriesKey, coordinate);
	}

	/**
	 * Returns the x-value for one item in a series.
	 *
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The x-value.
	 */
	@Override
	public Number getX(int seriesIndex, int itemIndex) {
		Map<Number, BoxAndWhiskerItem> series = data.getTails(getSeriesKey(seriesIndex));
		ArrayList<Number> coordinates = new ArrayList<Number>(series.keySet());
		Collections.sort(coordinates, new NumbersComparator());

		return coordinates.get(itemIndex);
	}

	/**
	 * Returns the y-value for one item in a series.<p>
     * This method (from the XYDataset interface) is mapped to the getMeanValue() method.
     *
     * @param seriesIndex  the series (zero-based index).
     * @param itemIndex  the item (zero-based index).
     * @return The y-value.
     */
	@Override
	public Number getY(int seriesIndex, int itemIndex) {
		return getMeanValue(seriesIndex, itemIndex);
	}


	/**
	 * Returns the mean for the specified series and item.
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The mean for the specified series and item.
	 */
	@Override
	public Number getMeanValue(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getMean();
		return result;
	}

	/**
	 * Returns the median-value for the specified series and item.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The median-value for the specified series and item.
	 */
	@Override
	public Number getMedianValue(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getMedian();
		return result;
	}

	/**
	 * Returns the Q1 median-value for the specified series and item.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The Q1 median-value for the specified series and item.
	 */
	@Override
	public Number getQ1Value(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getQ1();
		return result;
	}

	/**
	 * Returns the Q3 median-value for the specified series and item.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The Q3 median-value for the specified series and item.
	 */
	@Override
	public Number getQ3Value(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getQ3();
		return result;
	}

	/**
	 * Returns the min-value for the specified series and item.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The min-value for the specified series and item.
	 */
	@Override
	public Number getMinRegularValue(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getMinRegularValue();
		return result;
	}

	/**
	 * Returns the max-value for the specified series and item.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The max-value for the specified series and item.
	 */
	@Override
	public Number getMaxRegularValue(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getMaxRegularValue();
		return result;
	}


	/**
	 * Returns the minimum value which is not a farout.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return A <code>Number</code> representing the minimum non-farout value.
	 */
	@Override
	public Number getMinOutlier(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getMinOutlier();
		return result;
	}

	/**
	 * Returns the maximum value which is not a farout.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return A <code>Number</code> representing the maximum non-farout value.
	 */
	@Override
	public Number getMaxOutlier(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		Number result = (item==null) ? null : item.getMaxOutlier();
		return result;
	}

	/**
	 * Returns a list of outliers for the specified series and item.
	 * 
	 * @param seriesIndex  the series (zero-based index).
	 * @param itemIndex  the item (zero-based index).
	 * @return The list of outliers for the specified series and item (possibly <code>null</code>).
	 */
	@Override
	public List<?> getOutliers(int seriesIndex, int itemIndex)	{
		BoxAndWhiskerItem item = getItem(seriesIndex, itemIndex);
		List<?> result = (item==null) ? null : item.getOutliers();
		return result;
	}


	/**
	 * Returns the minimum y-value in the dataset.
	 *
	 * @param includeInterval  a flag that determines whether or not the y-interval is taken into account.
	 * @return The minimum value.
	 */
	@Override
	public double getRangeLowerBound(boolean includeInterval) {
        return (minimumRangeValue==null) ? Double.NaN : minimumRangeValue.doubleValue();
    }

	/**
	 * Returns the maximum y-value in the dataset.
	 *
	 * @param includeInterval  a flag that determines whether or not the y-interval is taken into account.
	 * @return The maximum value.
	 */
	@Override
	public double getRangeUpperBound(boolean includeInterval) {
        return (maximumRangeValue==null) ? Double.NaN : maximumRangeValue.doubleValue();
    }

	/**
	 * Returns the range of the values in this dataset's range.
	 * @param includeInterval  a flag that determines whether or not the y-interval is taken into account.
	 * @return The range.
	 */
	@Override
	public Range getRangeBounds(boolean includeInterval) {
        return rangeBounds;
    }


	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof JFreeBoxAndWhiskerXYDataset)) {
			return false;
		}

		JFreeBoxAndWhiskerXYDataset other = (JFreeBoxAndWhiskerXYDataset) obj;
		if (!this.data.equals(other.data))	{
			return false;
		}
		if (!this.seriesKeysList.equals(other.seriesKeysList))	{
			return false;
		}

		if (!this.minimumRangeValue.equals(other.minimumRangeValue))	{
			return false;
		}
		if (!this.maximumRangeValue.equals(other.maximumRangeValue))	{
			return false;
		}
		if (!this.rangeBounds.equals(other.rangeBounds))	{
			return false;
		}

		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		JFreeBoxAndWhiskerXYDataset clone = (JFreeBoxAndWhiskerXYDataset) super.clone();
		clone.data = new SnakeKeyMap<Comparable<?>, Number, BoxAndWhiskerItem>();
		clone.seriesKeysList = new ArrayList<Comparable<?>>();

		for (Comparable<?> seriesHead : data.getHeads())	{
			for (Map.Entry<Number, BoxAndWhiskerItem> seriesValuePair : data.getTails(seriesHead).entrySet())	{
				clone.add(seriesHead, seriesValuePair.getKey(), seriesValuePair.getValue());
			}
		}

        return clone;
    }
}



class NumbersComparator implements Comparator<Number>	{
	@Override
	public int compare(Number n1, Number n2)	{
		if (n1.doubleValue() < n2.doubleValue())	{
			return -1;
		}
		else if (n1.doubleValue() > n2.doubleValue())	{
			return 1;
		}
		return 0;
	}
}

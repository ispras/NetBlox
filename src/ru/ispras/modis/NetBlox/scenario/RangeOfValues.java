package ru.ispras.modis.NetBlox.scenario;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RangeOfValues <T> implements Iterable<T> {
	public static final String NO_RANGE_ID = "__NO_RANGE";	//null;	//-1;

	private String rangeId = NO_RANGE_ID;
	private String rangeTag;

	private List<T> values = null;


	public RangeOfValues(String rangeId, String rangeTag)	{
		this.rangeId = rangeId;
		this.rangeTag = rangeTag;

		values = new LinkedList<T>();
	}

	public RangeOfValues(String rangeId, String rangeTag, List<T> values)	{
		this.rangeId = rangeId;
		this.rangeTag = rangeTag;

		this.values = values;
	}


	public void addValue(T value)	{
		values.add(value);
	}


	public boolean isEmpty()	{
		return values == null  ||  values.isEmpty();
	}

	public String getRangeId()	{
		return rangeId;
	}

	public String getRangeTag()	{
		return rangeTag;
	}

	public int size()	{
		return (values==null) ? 0 : values.size();
	}


	/*public List<T> getAllValues()	{
		return Collections.unmodifiableList(values);
	}*/

	@Override
	public Iterator<T> iterator() {
		return values.iterator();
	}
}

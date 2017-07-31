package ru.ispras.modis.NetBlox.utils;

import java.util.Comparator;

public class NumbersComparator implements Comparator<Number> {

	@Override
	public int compare(Number n1, Number n2) {
		if (n1 instanceof Double  ||  n2 instanceof Double)	{
			Double d1 = n1.doubleValue();
			Double d2 = n2.doubleValue();
			return d1.compareTo(d2);
		}
		else if (n1 instanceof Float  ||  n2 instanceof Float)	{
			Float f1 = n1.floatValue();
			Float f2 = n2.floatValue();
			return f1.compareTo(f2);
		}
		else	{
			Integer i1 = n1.intValue();
			Integer i2 = n2.intValue();
			return i1.compareTo(i2);
		}
	}

}

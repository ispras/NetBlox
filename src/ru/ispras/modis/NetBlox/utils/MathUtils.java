package ru.ispras.modis.NetBlox.utils;

public class MathUtils {
	private static final double DEFAULT_PRECISION_COEFFICIENT = 0.0001;

	public static boolean approximatelyEquals(Double d1, Double d2, double precisionCoefficient)	{
		if (d1 == null  &&  d2 == null)	{
			return true;
		}
		else if (d1 == null  ||  d2 == null)	{
			return false;
		}

		return Math.abs(d1-d2 ) <= precisionCoefficient;
	}

	public static boolean approximatelyEquals(Double d1, Double d2)	{
		return approximatelyEquals(d1, d2, DEFAULT_PRECISION_COEFFICIENT);
	}
}

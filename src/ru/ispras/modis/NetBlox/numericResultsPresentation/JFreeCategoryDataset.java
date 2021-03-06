package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.data.category.DefaultCategoryDataset;

import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.AxesScale;

public class JFreeCategoryDataset extends DefaultCategoryDataset {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AxesScale axesScale;

	private List<Comparable<?>> columnKeys = null;


	public JFreeCategoryDataset(AxesScale axesScale)	{
		this.axesScale = axesScale;
	}


	public void addCorrectValue(Number value, Comparable<?> rowKey, Comparable<?> columnKey) throws ResultsPresentationException	{
		switch (axesScale)	{
		case XY_LOG10:
		case Y_LOG10:
			if (value == null)	{
				break;
			}
			if (value.doubleValue() < -LogarithmicAxis.SMALL_LOG_VALUE)	{
				throw new ResultsPresentationException("Negative numbers cannot be put to logarithmic scale.");
			}
			else if (value.doubleValue() < LogarithmicAxis.SMALL_LOG_VALUE)	{
				value = LogarithmicAxis.SMALL_LOG_VALUE;
			}
			break;
		}

		super.addValue(value, rowKey, columnKey);
	}


	@SuppressWarnings("unchecked")
	@Override
    public List<Comparable<?>> getColumnKeys() {
		List<?> columnKeysAsIs = super.getColumnKeys();

		@SuppressWarnings("rawtypes")
		List columnKeys = new ArrayList<>(columnKeysAsIs);

		Collections.sort(columnKeys, new CategoriesComparator());
        return columnKeys;
    }

	@Override
    public Comparable<?> getColumnKey(int column) {
		if (columnKeys == null)	{
			columnKeys = this.getColumnKeys();
		}
		return columnKeys.get(column);
    }


	@Override
    public Number getValue(int row, int column) {
		Comparable<?> columnKey = getColumnKey(column);
		int jfreeInnerColumnIndex = super.getColumnIndex(columnKey);
		return super.getValue(row, jfreeInnerColumnIndex);
	}



	private class CategoriesComparator implements Comparator<Object>	{
		private NumbersComparator numbersComparator = new NumbersComparator();

		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof String  ||  o2 instanceof String)	{
				String s1 = o1.toString();
				String s2 = o2.toString();
				return s1.compareTo(s2);
			}
			if (!(o1 instanceof Number  &&  o2 instanceof Number))	{
				return 0;
			}

			Number n1 = (Number) o1;
			Number n2 = (Number) o2;
			return numbersComparator.compare(n1, n2);
		}
		
	}
}

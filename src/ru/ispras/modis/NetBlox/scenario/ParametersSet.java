package ru.ispras.modis.NetBlox.scenario;

import java.util.LinkedList;
import java.util.List;

import ru.ispras.modis.NetBlox.utils.Pair;


/**
 * Keeps a set of fixed parameters from a subsection of <task/> in scenario.
 * 
 * @author ilya
 */
public abstract class ParametersSet {
	/**
	 * Some parameters in their description in scenario were represented not by single values but by ranges of values.
	 * This method tells whether a parameter set includes values for some of such parameters.
	 * @return
	 */
	public abstract boolean hasParametersFromSomeRange();

	/**
	 * Get the fixed value from a variation (range of values for parameter in scenario task description) specified by <code>id</code>.
	 * @param id
	 * @return a pair: left is the type of the value required, right is the value itself.	//XXX Introduce pairs here?
	 */
	public abstract Object getValueForVariationId(String id);


	/**
	 * Get the parameters specified in this set (by scenario task) as pairs of unique keys and values.
	 * @return	list of pairs of unique keys and values (the order matters) or <code>null</code> if there're no specified parameters.
	 */
	public abstract List<Pair<String, String>> getSpecifiedParametersAsPairsOfUniqueKeysAndValues();

	/**
	 * The same as <code>getSpecifiedParametersAsPairsOfUniqueKeysAndValues()</code> except that pairs
	 * for parameters are now assembled into groups.
	 * @return	list of groups of pairs of unique keys and values (the order matters) or <code>null</code> if there're no specified parameters.
	 */
	public List<List<Pair<String, String>>> getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues()	{
		List<List<Pair<String, String>>> result = appendNonNullSublist(null,
				getSpecifiedParametersAsPairsOfUniqueKeysAndValues());
		return result;
	}


	/**
	 * If the <code>parameter</code> is not null then append a key-value pair for it to the existing list.
	 * If the <code>existingList</code> is null then create a new list. Return the resulting list.
	 * @param existingList	- the existing list of key-values for parameters.
	 * @param parameter		- the parameter from which we want to take the key-value pair and put it to list.
	 * @param shortKeyForParameter	- the unique key for the <code>parameter</code> that will be used for the pair.
	 * @return	the resulting list with the added key-value pair if the <code>parameter</code> was not null. Can be null itself.
	 */
	protected List<Pair<String, String>> appendNonNullParameter(List<Pair<String, String>> existingList,
			ValueFromRange<?> parameter, String shortKeyForParameter)	{
		List<Pair<String, String>> result = existingList;
		if (parameter != null)	{
			if (result == null)	{
				result = new LinkedList<Pair<String, String>>();
			}

			result.add(new Pair<String, String>(shortKeyForParameter, parameter.getValue().toString()));
		}
		return result;
	}

	/**
	 * If the <code>sublist</code> is not null then add it to the existing list.
	 * If <code>existingList</code> is null then create it. Return the resulting list.
	 * @param existingList
	 * @param sublist
	 * @return	the resulting list with added <code>sublist</code> if the latter was not null. Can be null itself.
	 */
	protected List<List<Pair<String, String>>> appendNonNullSublist(List<List<Pair<String, String>>> existingList, List<Pair<String, String>> sublist)	{
		List<List<Pair<String, String>>> result = existingList;
		if (sublist != null)	{
			if (result == null)	{
				result = new LinkedList<List<Pair<String, String>>>();
			}
			result.add(sublist);
		}
		return result;
	}


	/**
	 * Clone the value from range if it is present.
	 * @param cloned
	 * @return
	 */
	protected <CT> ValueFromRange<CT> clone(ValueFromRange<CT> cloned)	{
		return (cloned==null) ? null : cloned.clone();
	}
}
package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * This is special map class for cases when key has two parts.
 * 
 * <p>
 * This implementation is based on HashMap so all key types should have correct
 * hashCode and equals overrides.
 * 
 * @author Yaroslav Nedumov
 * 
 * @param <KH>
 *            - type of key head
 * @param <KT>
 *            - type of key tail
 * @param <V>
 *            - type of map value
 */
public class SnakeKeyMap<KH, KT, V> implements Serializable {
	private static final long serialVersionUID = -3395592019378118666L;

	private HashMap<KH, HashMap<KT, V>> storage = new HashMap<KH, HashMap<KT, V>>();

	/**
	 * This is straightforward analog of {@link Map#containsKey(Object)}.
	 */
	public boolean containsKey(KH keyHead, KT keyTail) {
		return storage.containsKey(keyHead)
				&& storage.get(keyHead).containsKey(keyTail);
	}

	/**
	 * This is straightforward analog of {@link Map#get(Object)}.
	 */
	public V get(KH keyHead, KT keyTail) {
		if (containsKey(keyHead, keyTail)) {
			return storage.get(keyHead).get(keyTail);
		} else {
			return null;
		}
	}

	/**
	 * This is straightforward analog of {@link Map#put(Object, Object)}.
	 */
	public V put(KH keyHead, KT keyTail, V value) {
		if (storage.containsKey(keyHead)) {
			return storage.get(keyHead).put(keyTail, value);
		} else {
			HashMap<KT, V> subMap = new HashMap<KT, V>();
			subMap.put(keyTail, value);
			storage.put(keyHead, subMap);
			return null;
		}
	}

	/**
	 * Returns map with all possible key tails and their corresponding values.
	 * 
	 * @param keyHead
	 *            the key whose associated value is to be returned
	 * @return map from all stored key head tails and their values
	 */
	public Map<KT, V> getTails(KH keyHead) {
		return storage.get(keyHead);
	}

	/**
	 * Returns new collection contains values for all key heads and tails.
	 * Returned collection does not backed by the map (as {@link Map#values()}
	 * does).
	 * 
	 * @return collection contains values for all key heads and tails.
	 */
	public Collection<V> getAllValues() {
		Collection<V> result = new ArrayList<V>();
		for (Entry<KH, HashMap<KT, V>> entry : storage.entrySet()) {
			result.addAll(entry.getValue().values());
		}
		return result;
	}

	/**
	 * Returns all key heads stored in the map. Returned collection is backed by
	 * the map.
	 * 
	 * @return collection of all key heads stored in the map.
	 */
	public Collection<KH> getHeads() {
		return storage.keySet();
	}

	public void remove(KH keyHead) {
		storage.remove(keyHead);
	}

	@Override
	public String toString() {
		Collection<String> elements = new ArrayList<String>();
		for (Entry<KH, HashMap<KT, V>> head2tail : storage.entrySet()) {
			for (Entry<KT, V> tail2value : head2tail.getValue().entrySet()) {
				StringBuilder elementSb = new StringBuilder();
				elementSb.append(head2tail.getKey()).append("=")
						.append(tail2value.getKey()).append("=")
						.append(tail2value.getValue());
				elements.add(elementSb.toString());
			}
		}
		StringBuilder sb = new StringBuilder("{").append(StringUtils.join(
				elements, ", "));
		sb.append("}");
		return sb.toString();
	}
}
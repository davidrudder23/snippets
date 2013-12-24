package utils;

import java.util.Comparator;
import java.util.Map;

import play.Logger;

public class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {

	Map<K, V> map;

	public ValueComparator(Map<K, V> base) {
		this.map = base;
	}

	public int compare(K o1, K o2) {
		Integer v1 = (Integer)map.get(o1);
		Integer v2 = (Integer)map.get(o2);
		
		if (v2.compareTo(v1)==0) return ((String)o2).compareTo((String)o1);
		return (v2.compareTo(v1));
	}
}

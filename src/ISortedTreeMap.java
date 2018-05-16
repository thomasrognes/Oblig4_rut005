/**
 * Interface for SortedTreeMap.
 */

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public interface ISortedTreeMap<K extends Comparable<? super K>, V> {

    /**
     * Finds the minimum value in the map, if no value is found, returns null instead.
     * @return minimum value
     */
    Entry<K, V> min();

    /**
     * Finds the maximum value in the map, if no value is found returns null instead.
     * @return maximum value
     */
    Entry<K, V> max();

    /**
     * Inserts the specified value with the specified key as a new entry into the map.
     * If the value is already present, return the previous value, else null.
     * @param key The key to be inserted
     * @param value The value to be inserted
     * @return Previous value
     */
    V add(K key, V value);

    /**
     * Inserts the specified entry into the map. If the key is already a part of the map,
     * return the previous value, else null.
     * @param entry The new entry to be inserted into the map
     * @return Previous value
     */
    V add(Entry<K, V> entry);

    /**
     * Replaces the value for key in the map as long as it is already present. If they key
     * is not present, the method throws an exception.
     * @param key The key for which the value is replaced
     * @param value The new value
     * @throws NoSuchElementException When key is not in map
     */
    void replace(K key, V value) throws NoSuchElementException;

    /**
     * Applies a function to the value at key and replaces that value. Throws an exception
     * if the key is not present in the map.
     * @param key The key for which we are replacing the value
     * @param f The function to apply to the value
     * @throws NoSuchElementException When key is not in map
     */
    void replace(K key, BiFunction<K, V, V> f) throws NoSuchElementException;

    /**
     * Removes the entry for key in the map. Throws an exception if the key is not present
     * in the map.
     * @param key The key for the entry to remove
     * @return The removed value
     * @throws NoSuchElementException When key is not in map.
     */
    V remove(Object key) throws NoSuchElementException;

    /**
     * Retrieves the value for the key in the map.
     * @param key The key for the value to retrieve
     * @return The value for the key
     * @throws NoSuchElementException When key is not in map
     */
    V getValue(Object key) throws NoSuchElementException;

    /**
     *  Checks if a key is in the map.
     * @param key The key to check
     * @return true if the key is in the map, false otherwise
     */
    boolean containsKey(K key);

    /**
     * Checks if a value is in the map
     * @param value the value to look for
     * @return True if the value is present, false otherwise
     */
    boolean containsValue(V value);

    /**
     * Finds all the keys in the map and returns them in order.
     * @return keys in order
     */
    Iterable<K> keys();

    /**
     * Finds the values in order of the keys.
     * @return values in order of the keys
     */
    Iterable<V> values();

    /**
     * Finds all entries in the map in order of the keys.
     * @return All entries in order of the keys
     */
    Iterable<Entry<K, V>> entries();

    /**
     * Finds the entry for the key, if the key is not in the map returns the next
     * highest entry if such an entry exists
     * @param key The key to find
     * @return The entry for the key or the next highest
     */
    Entry<K, V> higherOrEqualEntry(K key);

    /**
     * Finds the entry for the key, if the key is not in the map, returns the next
     * lower entry if such an entry exists
     * @param key The key to find
     * @return The entry for the key or the next lower
     */
    Entry<K, V> lowerOrEqualEntry(K key);

    /**
     * Adds all entries in the other map into the current map. If a key is present
     * in both maps, the key in the other map takes precedent.
     * @param other The map to add to the current map.
     */
    void merge(ISortedTreeMap<K, V> other);

    /**
     * Removes any entry for which the predicate holds true. The predicate can
     * trigger on both the key and value of each entry.
     * @param p The predicate that tests which entries should be kept.
     */
    void removeIf(BiPredicate<K, V> p);

    /**
     * Checks if the map is empty
     * @return True if the map is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the number of entries in the map
     * @return Number of entries
     */
    int size();

    /**
     *  Clears the map of entries.
     */
    void clear();
}

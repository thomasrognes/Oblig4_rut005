/**
 * Obligatorisk oppgave 4 for Thomas Sebastian Rognes (Rut005)
 */


import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;



public class SortedTreeMap<K extends Comparable<? super K>, V> implements ISortedTreeMap<K,V> {
    private Entry<K, V> nil = new Entry<K, V>();
    private Entry<K, V> root = nil;
    private int size;
    private Comparator<K> comparator;

    public SortedTreeMap(Comparator<K> kComparator) {
        root = nil;
        size = 0;
        comparator = kComparator;
    }

    public SortedTreeMap() {
        root.leftChild = nil;
        root.rightChild = nil;
        root.parent = nil;
        size = 0;
    }

    /**
     * Finds the minimum key in the map, if no key is found, returns null instead.
     *
     * @return minimum key
     */
    public Entry<K, V> min(){
        if (isNil(root)) {
            return null;
        }
        Entry<K,V> minValue = root;
        while (!isNil(minValue.leftChild)){
            minValue = minValue.leftChild;
        }
        return minValue;
    }

    /**
     * Finds the minimum key in the map from a node, returns null if no key is found.
     * @param node to search from
     * @return minimum key
     */
    public Entry<K, V> min(Entry<K,V> node) {
        Entry<K, V> minKey = node;
        if (node.leftChild == null) {
            return minKey;
        }
        while (node.leftChild != nil) {
            minKey = node.leftChild;
            node = node.leftChild;
        }
        return minKey;
    }

    /**
     * Finds the maximum key in the map, if no key is found returns null instead.
     *
     * @return maximum key
     */
    public Entry<K, V> max() {

        if (isNil(root)) {
            return null;
        }
        Entry<K,V> maxValue = root;
        while (!isNil(maxValue.rightChild)) {
            maxValue = maxValue.getRightChild();
        }
        return maxValue;
    }

    /**
     * Inserts the specified value with the specified key as a new entry into the map.
     * If the value is already present, return the previous value, else null.
     *
     * @param key   The key to be inserted
     * @param value The value to be inserted
     * @return Previous value
     */
    @Override
    public V add(K key, V value) {

        Entry<K, V> newEntry = new Entry<K, V>(key, value);
        V returnValue;
        Entry<K, V> futureParent = nil;
        Entry<K, V> current = root;

        while (!isNil(current)) {
                futureParent = current;
                if (newEntry.key.compareTo(current.key) > 0) {
                    current = current.rightChild;
                }
                else if (newEntry.key.compareTo(current.key) < 0) {
                    current = current.leftChild;
                }
                else if (newEntry.key.compareTo(current.key) == 0) {
                    returnValue = current.value;
                    current.value = newEntry.value;
                    return returnValue;
                }
        }

        newEntry.parent = futureParent;

        if (isNil(futureParent)) {
            root = newEntry;
        }
        else if (newEntry.key.compareTo(futureParent.key) < 0) {
            futureParent.leftChild = newEntry;
        }
        else {
            futureParent.rightChild = newEntry;
        }

        newEntry.leftChild = nil;
        newEntry.rightChild = nil;

        size++;
        return null;
    }

    /**
     * Inserts the specified entry into the map. If the key is already a part of the map,
     * return the previous value, else null.
     *
     * @param entry The new entry to be inserted into the map
     * @return Previous value
     */
    public V add(Entry<K, V> entry) {
        return add(entry.key, entry.value);
    }

    /**
     * Replaces the value for key in the map as long as it is already present. If they key
     * is not present, the method throws an exception.
     *
     * @param key   The key for which the value is replaced
     * @param value The new value
     * @throws NoSuchElementException When key is not in map
     */
    public void replace(K key, V value) throws NoSuchElementException {
        if (!isInTree(key)) {
            throw new NoSuchElementException("The key is not in the tree");
        }
        else {
            add(key,value);
        }
    }

    /**
     * Applies a function to the value at key and replaces that value. Throws an exception
     * if the key is not present in the map.
     *
     * @param key The key for which we are replacing the value
     * @param f   The function to apply to the value
     * @throws NoSuchElementException When key is not in map
     */
    public void replace(K key, BiFunction<K, V, V> f) throws NoSuchElementException {
        Entry<K,V> valueToReplace = findNode(key);
        if (!isInTree(key)){
            throw new NoSuchElementException("The key is not in the tree");
        }
        else {
            valueToReplace.value = f.apply(valueToReplace.key, valueToReplace.value);
            add(valueToReplace.key, valueToReplace.value);
        }
    }

    /**
     * Removes the entry for key in the map. Throws an exception if the key is not present
     * in the map.
     *
     * @param key The key for the entry to remove
     * @return The removed value
     * @throws NoSuchElementException When key is not in map.
     */
    public V remove(Object key) throws NoSuchElementException {
        V valueToRemove = getValue(key);
        if (containsKey((K) key)) {
            root = entryToRemove((K) key, root);
            size--;
            return valueToRemove;
        }
        else {
            throw new NoSuchElementException("The key is not in the tree..");
        }
    }

    public Entry<K, V> entryToRemove(K key, Entry<K,V> entry){
        if (entry == nil) {
            return null;
        }

        int compare = key.compareTo(entry.key);

        if (compare < 0) {
            entry.leftChild = (entryToRemove(key, entry.leftChild));
        }
        else if (compare > 0) {
            entry.rightChild = entryToRemove(key, entry.rightChild);
        }
        else {
            if (entry.leftChild == nil) {
                return entry.rightChild;
            } else if (entry.rightChild == nil) {
                return entry.leftChild;
            }

            entry.key = min(entry.rightChild).key;
            entry.value = min(entry.rightChild).value;
            entry.rightChild = (entryToRemove(entry.key, entry.rightChild));
        }
        return entry;
    }

    /**
     * Retrieves the value for the key in the map.
     *
     * @param key The key for the value to retrieve
     * @return The value for the key
     * @throws NoSuchElementException When key is not in map
     */
    public V getValue(Object key) throws NoSuchElementException {
        if (!containsKey((K) key)) {
            throw new NoSuchElementException("The key is not in the tree");
        }
        return findNode((K) key).value;
    }

    /**
     * Checks if a key is in the map.
     *
     * @param key The key to check
     * @return true if the key is in the map, false otherwise
     */
    public boolean containsKey(K key) {
        if (isInTree(key)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a value is in the map
     *
     * @param value the value to look for
     * @return True if the value is present, false otherwise
     */
    public boolean containsValue(V value) {
        ArrayList<K> keysInTree = new ArrayList<K>();
        keysInTree = findKeysByEntry(root, keysInTree);

        for (K key : keysInTree) {
            if (findNode(key).value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds all the keys in the map and returns them in order.
     *
     * @return keys in order
     */
    public Iterable<K> keys() {
        ArrayList<K> keys = new ArrayList<K>();
        keys = findKeysByEntry(root, keys);
        Collections.sort(keys);
        return keys;
    }

    public ArrayList<K> findKeysByEntry(Entry<K,V> current, ArrayList<K> key){
        if (current != nil) {
            findKeysByEntry(current.getLeftChild(), key);
            key.add(current.key);
            findKeysByEntry(current.getRightChild(), key);
        }
        return key;
    }

    /**
     * Finds the values in order of the keys.
     *
     * @return values in order of the keys
     */
    public Iterable<V> values() {
        ArrayList<V> values = new ArrayList<V>();
        ArrayList<K> keys = new ArrayList<K>();

        keys = findKeysByEntry(root, keys);

        for (K key : keys) {
            values.add(findNode(key).value);
        }

        return values;
    }

    /**
     * Finds all entries in the map in order of the keys.
     *
     * @return All entries in order of the keys
     */
    public Iterable<Entry<K, V>> entries() {
        ArrayList<Entry<K,V>> orderedEntries = new ArrayList<>();
        ArrayList<K> keys = new ArrayList<>();

        keys = findKeysByEntry(root, keys);

        for (K key : keys) {
            orderedEntries.add(findNode(key));
        }

        return orderedEntries;
    }

    /**
     * Finds the entry for the key, if the key is not in the map returns the next
     * highest entry if such an entry exists
     *
     * @param key The key to find
     * @return The entry for the key or the next highest
     */
    public Entry<K, V> higherOrEqualEntry(K key) {
        ArrayList<K> keys = new ArrayList<>();

        keys = findKeysByEntry(root, keys);

        for (K key1 : keys) {
            if (key1 == key) {
                return findNode(key1);
            }
            else if ((Integer)key1 > (Integer) key){
                return findNode(key1);
            }
        }
        return null;
    }

    /**
     * Finds the entry for the key, if the key is not in the map, returns the next
     * lower entry if such an entry exists
     *
     * @param key The key to find
     * @return The entry for the key or the next lower
     */
    public Entry<K, V> lowerOrEqualEntry(K key) {
        ArrayList<K> keys = new ArrayList<>();

        keys = findKeysByEntry(root, keys);
        Collections.reverse(keys);

        for (K key1 : keys) {
            if (key1 == key){
                return findNode(key1);
            }
            else if ((Integer) key1 < (Integer) key) {
                return findNode(key1);
            }
        }
        return null;
    }

    /**
     * Adds all entries in the other map into the current map. If a key is present
     * in both maps, the key in the other map takes precedent.
     *
     * @param other The map to add to the current map.
     */
    public void merge(ISortedTreeMap<K, V> other) {
        SortedTreeMap<K,V> otherTree = (SortedTreeMap<K, V>) other;

        for (Entry<K,V> node : otherTree.entries()) {
            if (node != null) {
                add(node.key, node.value);
            }
        }
    }

    /**
     * Removes any entry for which the predicate holds true. The predicate can
     * trigger on both the key and value of each entry.
     *
     * @param p The predicate that tests which entries should be kept.
     */
    public void removeIf(BiPredicate<K, V> p) {
    ArrayList<Entry<K,V>> entries = new ArrayList<>();
    entries = (ArrayList<Entry<K,V>>) entries();

    for (Entry<K,V> entry : entries) {
        if (p.test(entry.key, entry.value)){
            remove(entry.key);
        }
    }
    }

    /**
     * Checks if the map is empty
     *
     * @return True if the map is empty, false otherwise.
     */
    public boolean isEmpty() {
        if (size == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the number of entries in the map
     *
     * @return Number of entries
     */
    public int size() {
        return size;
    }

    /**
     * Clears the map of entries.
     */
    public void clear() {
        root = nil;
        size = 0;
    }

    /**
     * Getter for root noden.
     * @return root noden.
     */
    public Entry<K, V> getRoot() {
        return root;
    }

    /**
     * Setter for root noden
     * @param root som skal settes.
     */
    public void setRoot(Entry<K,V> root) {
        this.root = root;
    }

    /**
     * Checks if the entry is a nil-node
     * @param node
     * @return true if the entry is a nil-node, false otherwise.
     */
    public boolean isNil (Entry<K, V> node) {
        return node == nil;
    }

    /**
     * Checks if the key is in the tree.
     * @param nodeToFind
     * @return true if the key is in the tree, false otherwise.
     */
    public boolean isInTree(K nodeToFind) {
        Entry<K, V> current = root;

        while (!isNil(current)) {
            // Checks if the key is greater than the current key.
            if (nodeToFind.compareTo(current.key) > 0) {
                current = current.rightChild;
            }
            // Checks if the key is lower than the current key.
            else if (nodeToFind.compareTo(current.key) < 0) {
                current = current.leftChild;
            }
            // Checks if key is in the tree.
            else if (nodeToFind.compareTo(current.key) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the entry to the key.
     * @param key
     * @return entry to the key.
     */
    public Entry<K, V> findNode(K key) {
        Entry<K,V> nodeToReturn;
        Entry<K, V> current = root;

        while (!isNil(current)) {
            // Checks if the key is greater than the current key.
            if (key.compareTo(current.key) > 0) {
                current = current.rightChild;
            }
            // Checks if the key is lower than the current key.
            else if (key.compareTo(current.key) < 0) {
                current = current.leftChild;
            }
            // Checks if key is in the tree.
            else if (key.compareTo(current.key) == 0) {
                nodeToReturn = current;
                return nodeToReturn;
            }
        }
        return null;
    }
}


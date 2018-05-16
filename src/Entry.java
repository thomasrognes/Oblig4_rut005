/**
 * Obligatorisk oppgave 4 for Thomas Sebastian Rognes (Rut005)
 */

public class Entry<K, V> {


    public K key;
    public V value;
    public Entry<K,V> leftChild;
    public Entry<K,V> rightChild;
    public Entry<K,V> parent;



    public Entry() {
        this.leftChild = null;
        this.rightChild = null;
        this.parent = null;
    }

    public Entry(K key, V value) {
        this();
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Entry) {
            Entry other = (Entry)o;
            return this.value.equals(other.value) && this.value.equals(other.value);
        }
        return false;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public Entry<K, V> getLeftChild() {
        return leftChild;
    }

    public Entry<K, V> getRightChild() {
        return rightChild;
    }

    public Entry<K, V> getParent() {
        return parent;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void setLeftChild(Entry<K, V> leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(Entry<K, V> rightChild) {
        this.rightChild = rightChild;
    }

    public void setParent(Entry<K, V> parent) {
        this.parent = parent;
    }
}

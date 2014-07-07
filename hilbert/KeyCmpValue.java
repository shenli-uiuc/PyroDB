public class KeyCmpValue<K, V> implements Comparable<KeyCmpValue<K, V> >{
  private K key;
  private V value;

  public KeyCmpValue(K Key, V value) {
    this.key = key;
    this.value = value;
  }

  public K geKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public int compareTo(KeyCmpValue<K, V> that) {
    return ((Comparable<V>)this.value)
            .compareTo(that.getValue());
  }
}

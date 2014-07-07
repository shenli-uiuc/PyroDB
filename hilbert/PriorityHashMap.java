import java.util.PriorityQueue;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.Map;

/**
 * A LinkedHashMap that supports top() to get the k-v pair
 * of the largest value.
 *
 * values need to implement Comparable
 *
 * TODO: implement Priority class whose internal priority can be modified
 * TODO: synchronize among multiple threads
 */
public class PriorityHashMap<K, V> {

  public static final float DEFAULT_LOAD_FACTOR = 0.75F;

  private int capacity;
  private int maxCapacity;
  private float loadFactor;

  private PriorityQueue<KeyCmpValue<K, V> > pq = null;
  private LruHashMap<K, KeyCmpValue<K, V> > lru = null;

  class LruHashMap<K, KV> extends LinkedHashMap<K, KV> {

    public LruHashMap() {
      super(maxCapacity, loadFactor, true);
    }

    @Override
    protected boolean 
    removeEldestEntry(final Map.Entry<K, KV> eldest) {
      if (super.size() > capacity) {
        if (!pq.remove(eldest.getValue())) {
          throw new IllegalStateException("Element (" + eldest.getKey() 
              + ", " + eldest.getValue() + ") not present in PriorityQueue");
        }
        return true;
      }
      return false;
    }
  }

  /**
   * capacity is the maximum enetries this data structure can hold.
   * Its capacity will be set as capacity/DEFAULT_LOAD_FACTOR;
   */
  public PriorityHashMap(int capacity) {
    this((int)Math.ceil(capacity / DEFAULT_LOAD_FACTOR) + 1, 
         DEFAULT_LOAD_FACTOR);
  }

  public PriorityHashMap(int maxCapacity, float loadFactor) { 
    this.maxCapacity = maxCapacity;
    this.capacity = (int)Math.ceil(maxCapacity * loadFactor);
    this.loadFactor = loadFactor;

    this.pq = 
      new PriorityQueue<KeyCmpValue<K, V> >(this.capacity);
    this.lru = new LruHashMap<K, KeyCmpValue<K, V> >();
  }

  /**
   * TODO: resource pooling objects?
   */
  public void put(K key, V value) {
    // add the element into LRU, which will evict the eldest
    // element in both lru and pq if necessary
    KeyCmpValue<K, V> kv= new KeyCmpValue<K, V>(key, value);
    this.lru.put(key, kv);

    // add the element into pq
    this.pq.add(kv);
  }

  public V get(K key) {
    KeyCmpValue<K, V> kv = this.lru.get(key);
    if (null == kv) {
      return null;
    } else {
      return kv.getValue();
    }
  }

  public KeyCmpValue<K, V> peek() {
    return this.pq.peek();
  }

  public KeyCmpValue<K, V> poll() {
    // TODO: also remove the entry in LinkedHashMap when materialize this kv
    return null;
  }

}

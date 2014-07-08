import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<A, B> extends LinkedHashMap<A, B> {
  public final static float DEFAULT_LOAD_FACTOR = 0.75F;
  private final int maxEntries;

  public LruCache(final int maxEntries) {
    super((int)Math.ceil(maxEntries / DEFAULT_LOAD_FACTOR) + 1, 
          DEFAULT_LOAD_FACTOR, true);
    this.maxEntries = maxEntries;
  }

  @Override
  protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
    return super.size() > maxEntries;
  }
}

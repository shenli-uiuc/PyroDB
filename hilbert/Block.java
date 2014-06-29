import java.util.ArrayList;

public class Block {
  // a list of (key, key-value-long) pairs
  public ArrayList<Pair<Long, Long> > data = null;
 
  public long blockSize = 0;

  public Block() {
    data = new ArrayList<Pair<Long, Long> > ();
    blockSize = 0;
  }

  public long write(long key, long kvLen) {
    data.add(new Pair<Long, Long>(key, kvLen));
    blockSize += kvLen;
    return kvLen;
  }

  /**
   *  @return the number of keys hit in the scan query
   */
  public long scan(long startKey, long endKey) {
    long effectSize = 0;
    Pair<Long, Long> tmp = null;
    for (int i = 0 ; i < data.size(); ++i) {
      tmp = data.get(i);
      if (tmp.first > endKey)
        return 0;
      if (tmp.first >= startKey)
        effectSize += tmp.second;
    }
    return effectSize;
  }
}

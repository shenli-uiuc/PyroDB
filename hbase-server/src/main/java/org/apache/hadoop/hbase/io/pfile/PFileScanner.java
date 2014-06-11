package org.apache.hadoop.hbase.io.pfile;

import java.io.Exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.KeyValue.KVComparator;
import org.apache.hadoop.hbase.Cell;

import org.apache.hadoop.hbase.io.hfile.HFileReaderV2;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFile.Writer;
import org.apache.hadoop.hbase.io.hfile.FixedFileTrailer;
import org.apache.hadoop.hbase.io.FSInputStreamWrapper;
import org.apache.hadoop.hbase.fs.HFileSystem;
import org.apache.hadoop.hbase.util.Bytes;

/*
 * Any component above this layer sees only HFileBlock. Skiplists and 
 * multi-block-reading functionality are made transparent to higher level
 * components.
 *
 * It seems that I do not need to implement PFileBlockReader, as all seek
 * are doen in HFileReaderV2.blockSeek(Cell Key, boolean seekBefore).
 *
 * So, rewrite that should be suffice for get. 
 *
 * TODO: study how the reader works for scan, do I need to rewrite the scan
 * portion?
 */

public class PFileScanner extends HFileReaderV2.ScannerV2 {
  private static final Log LOG = LogFactory.getLog(PFileScanner.class);

  public static final KEY_LEN_SIZE = Bytes.SIZEOF_INT;
  public static final MAX_INT = 2147483647;

  public PFileScanner(HFileReaderV2 r, boolean cacheBlocks,
      final boolean pread, final boolean isCompaction) {
    super(r, cacheBlocks, pread, isCompaction);
    this.reader = r;
  }

  /*
   * TODO: when called, is the blockBuffer.position() placed at the 
   * beginning of kv or pkv? HAS TO CONFIRM THIS!
   *
   * TODO: prev pointer is added, recalculate offsets
   */
  @Override
  protected int blockSeek(Cell key, boolean seekBefore) {
    int klen, vlen, skipKLen;
    int kvOffset, skipKvOffset;
    byte pNum, tmpPNum, skipPNum;
    int ptr, skipPrevPtr;
    long memstoreTS = 0;
    int memstoreTSLen = 0;
    int lastKeyValueSize = -1;
    int curOffset, skipOffset, ptrOffset, skipPrevOffset;

    // the target key is within the range of the pointers of the 
    // current entry
    boolean found;

    // helps search in the skiplist
    int maxOffset = this.MAX_INT;

    //TODO: read klen and vlen
    KeyValue.KeyOnlyKeyValue keyOnlyKv = new KeyValue.KeyOnlyKeyValue();
    KeyValue.KeyOnlyKeyValue skipKeyOnlyKv = new KeyValue.KeyOnlyKeyValue();

    /*
     * Invariant: the current key under do-while has to be smaller than the 
     * target key. The loop over the current skiplist entry will return if
     * found an exact match, otherwise set the current key to the largest key
     * that is smaller than the target key in its skiplist pointers.
     */
    do {
      //blockBuffer position will be reset before reading the skiplist entries
      blockBuffer.mark();
      pNum = blockBuffer.get();

      blockBuffer.reset();
      // offset to the beginning of the current pkv
      curOffset = blockBuffer.position() + blockBuffer.arrayOffset();
      // offset to the largest pointer
      ptrOffset = curOffset + PKeyValue.POINTER_NUM_SIZE +
                   (pNum - 1) * PKeyValue.POINTER_SIZE;

      found = false;
      // check pointers of the current entry
      while (ptrOffset > CurOffset) {
        ptr = blockBuffer.getInt(ptrOffset);
        // offset to the beginning of the pkv indicated by the pointer
        skipOffset = curOffset + ptr;
        if (skipOffset >= maxOffset) {
          ptrOffset -= PKeyValue.POINTER_SIZE;
          continue;
        }
        // ptr num of that pkv
        skipPNum = blockBuffer.get(skipOffset);
        // offset to the beginning of kv of that pkv
        skipKvOffset = skipOffset + PKeyValue.POINTER_NUM_SIZE +
                       skipPNum * PKeyValue.POINTER_SIZE +
                       PKeyValue.POINTER_SIZE; // for prev pointer
        // key length of that kv
        skipKLen = blockBuffer.getInt(skipKvOffset);
        skipKeyOnlyKv.setKey(blockBuffset.array(), 
                             skipKvOffset + KEY_VALEN_LEN_SIZE, skipKLen);

        comp = reader.getComparator().compareOnlyKeyPortion(key, 
                                                            skipKeyOnlyKv);
        //TODO: deal with reader.shouldIncludeMemstoreTS() on both readers
        //and writers.
        if (0 == comp) {
          //Found exact match
          //TODO: readKeyValueLen() rewrites currKeyLen/currValueLen
          //be careful when call readKeyValueLen()
          currKeyLen = skipKLen;
          currValueLen = blockBuffer.getInt(skipKvOffset + KEY_LEN_SIZE);

          if (seekBefore) {
            skipPrevOffset = skipKvOffset - PKeyValue.POINTER_SIZE;
            skipPrevPtr = blockBuffer.getInt(skipPrevOffset);
            if (skipPrevPtr <= 0) {
              KeyValue kv = KeyValueUtil.ensureKeyValue(key);
              throw new IllegalStateException(
                  "blockSeek with seekBefore at the first key of the block: "
                  + "key = " + Bytes.toStringBinary(kv.getKey(), 
                                                    kv.getKeyOffset(), 
                                                    kv.getKeyLength())
                  + ", blockOffset = " + block.getOffset() + ", onDiskSize = "
                  + block.getOnDiskSizeWithHeader());
            }
            // The writer currently do not write memstoreTS field,
            // hence we do not call readKeyValueLen(); But currKeyLen and 
            // currValueLen have to be set.

            // use the prev ptr to reset the position.
            blockBuffer.position(blockBuffser.position() + ptr - skipPrevPtr);
            return 1;
          }

          blockBuffer.position(blockBuffer.position() + ptr);
          return 0;
        } else if (comp < 0) {
          // larger than the target key, try the next smaller pointer
          ptrOffset -= PKeyValue.POINTER_SIZE;    
          maxOffset = skipOffset;
        } else {
          // found the largest key that is smaller than the target key, break
          blockBuffer.position(blockBuffer.position() + ptr);
          found = true;
          break;
        }
      }

      if (!found) {
        // all pointers point to larger keys, and the curren tkey is smaller 
        // than the target key.
      
        // check next pkv
        skipOffset = curOffset + PKeyValue.POINTER_NUM_SIZE +
                      (pNum + 1) * PKeyValue.POINTER_SIZE + 
                      KEY_VALUE_LEN_SIZE + klen + vlen;
        skipPNum = blockBuffer.get(skipOffset);
        skipKvOffset = skipOffset + PKeyValue.POINTER_NUM_SIZE + 
                       (skipPNum + 1) * PKeyValue.POINTER_SIZE;
        skipKLen = blockBuffer.getInt(skipKvOffset);
        skipKeyOnlyKv.setKey(blockBuffset.array(), 
                             skipKvOffset + KEY_VALEN_LEN_SIZE, skipKLen);
        comp = reader.getComparator().compareOnlyKeyPortion(key, 
                                                            skipKeyOnlyKv);
        if (0 == comp)
      }



      //TODO: finish the skiplist logic, 
      lastKeyValueSize = klen + vlen + memstoreTSLen + KEY_VALUE_LEN_SIZE;
      //TODO: has to skip the skiplist entry also
      blockBuffer.position(blockBuffer.position() + lastKeyValueSize);
      // TODO: the while condition is no longer right for skiplist cases
    } while (blockBuffer.remaining() > 0);

    // HFileReaderV2 says below seeks to the last key we successfully read,
    // when this is the last key/value pair in the file.
    // TODO: consider if we still need this for skiplist case
    blockBuffer.position(blockBuffer.position() - lastKeyValueSize);
    //TODO: same
    readKeyValueLen();
    return 1;
  }

  private int handleExactKey(int kvOffset, int klen) {
  }
}

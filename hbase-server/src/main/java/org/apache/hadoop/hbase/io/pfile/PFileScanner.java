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
    byte pNum, skipPNum;
    int pointer, lastPointer;
    long memstoreTS = 0;
    int memstoreTSLen = 0;
    int lastKeyValueSize = -1;
    int curPos, skipPos;

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

      // read klen, vlen, and key
      kvOffset = blockBuffer.arrayOffset() + blockBuffer.position() + 
                     pNum * PKeyValue.POINTER_SIZE;
      klen = blockBuffer.getInt(kvOffset);
      vlen = blockBuffer.getInt(kvOffset + Bytes.SIZEOF_INT);
      keyOnlyKv.setKey(blockBuffer.array(), 
                       kvOffset + KEY_VALUE_LEN_SIZE, klen);

      int comp = reader.getComparator().compareOnlyKeyPortion(key, keyOnlyKv);

      if (0 == comp) {
        if (seekBefore) {
          if (lastKeyValueSize < 0) {
            KeyValue kv = KeyValueUtil.ensureKeyValue(key);
            throw new IllegalStateException(
                "Shen Li: blockSeek with seekBefore " +
                "at the first key of the block: key = " +
                Bytes.toStringBinary(kv.getKey(), kv.getKeyOffset(), 
                                     kv.getKeyLength()) +
                ", blockOffset = " + block.getOffset() + ", onDiskSize = " +
                block.getOnDiskSizeWithHeader());
          }
          //TODO: the blockBuffer.position() is not the same with the one 
          //in HFileReaderV2
          blockBuffer.position(blockBuffer.position() - lastKeyValueSize);
          //TODO: don't know what is this...
          readKeyValueLen();
          return 1;
        }
        currKeyLen = klen;
        currValueLen = vlen;
        if (this.reader.shouldIncludeMemstoreTS()) {
          currMemstoreTS = memstoreTS;
          currMemstoreTSLen = memstoreTSLen;
        }
      } else if (comp < 0) {
        if (lastKeyValueSize > 0) {
          //TODO: check position is placed correctly
          blockBuffer.position(blockBuffer.position() - lastKeyValueSize);
        }
        //TODO: same as above
        // This method calls readMvccVersion to read mvcc version, which
        // sets currMemstoreTs and currMemstoreTSLen.
        // Caution: have to make sure that the position of the blockBuffer
        // is at the end of the skiplist entry
        readKeyValueLen();
        if (-1 == lastKeyValueSize && 0 == blockBuffer.position() &&
            this.reader.trailer.getMinorVersion() >= 
              MINOR_VERSION_WITH_FAKED_KEY) {
          return HConstants.INDEX_KEY_MAGIC;
        }
        return 1;
      }

      blockBuffer.reset();
      curPos = blockBuffer.position();

      for (int i = 0 ; i < pNum; ++i) {
        pointer = blockBuffer.getInt();
        skipPos = blockBuffer.arrayOffset() + 
                       blockBuffer.position() + pointer;
        skipPNum = blockBuffer.get(skipPos);
        skipKvOffset = skipPos + PKeyValue.POINTER_NUM_SIZE +
                       skipPNum * PKeyValue.POINTER_SIZE;
        skipKLen = blockBuffer.getInt(skipKvOffset);
        skipKeyOnlyKv.setKey(blockBuffset.array(), 
                             skipKvOffset + KEY_VALEN_LEN_SIZE, skipKLen);

        comp = reader.getComparator().compareOnlyKeyPortion(key, 
                                                            skipKeyOnlyKv);
        //TODO: deal with reader.shouldIncludeMemstoreTS() on both readers
        //and writers.
        if (0 == comp) {
          //TODO: readKeyValueLen() rewrites currKeyLen/currValueLen
          //be careful when call readKeyValueLen()
          currKeyLen = skipKLen;
          currValueLen = blockBuffer.getInt(skipKvOffset + KEY_LEN_SIZE);

          if (seekBefore) {
            if (lastKeyValueSize < 0) {
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
            return 1;
          }
          blockBuffer.position(blockBuffser.position() + pointer);

        }
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
}

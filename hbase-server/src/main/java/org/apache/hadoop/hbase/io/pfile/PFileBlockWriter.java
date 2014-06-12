package org.apache.hadoop.hbase.io.pfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.hbase.io.hfile.HFileBlock;
import org.apache.hadoop.hbase.io.hfile.HFileDataBlockEncoder;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.BlockType;
import org.apache.hadoop.hbase.KeyValue;

import org.apache.hadoop.hbase.io.pfile.PFileDataBlockEncoder;

/*
 * TODO TODO: it seems much easier to just append the skiplist to the end
 * of the block
 */

public class PFileBlockWriter extends HFileBlock.Writer {
  private static final Log LOG = LogFactory.getLog(PFileBlockWriter.class);

  private static final byte BYTE_NEGTIVE_ONE = -1;
  private static final int ARRAY_INIT_SIZE = 512;
  private static final int [] trailingZeroMap = new int [] {
    32, 0, 1, 26, 2, 23, 27, 0, 3, 16, 24, 30, 28, 11, 0, 13, 4,
    7, 17, 0, 25, 22, 31, 15, 29, 10, 12, 6, 0, 21, 14, 9, 5,
    20, 8, 19, 18
  };
  private static final byte MAX_POINTER_NUM = 127;

  private int [] offsets = null;
  private byte [] ptrNum = null;
  private List<KeyValue> kvs = null;
  private PFileDataBlockEncoder pDataBlockEncoder = null;

  //TODO
  public PFileBlockWriter(HFileDataBlockEncoder dataBlockEncoder, 
                          HFileContext fileContext) {
    super(dataBlockEncoder, fileContext);
    this.offsets = new int[ARRAY_INIT_SIZE];
    this.ptrNum = new byte[ARRAY_INIT_SIZE];
    this.kvs = new ArrayList<KeyValue>();
    // this encoder encodes the pointer array by offer apis to encode int 
    // and byte
    this.pDataBlockEncoder = PNoOpDataBlockEncoder.INSTANCE;
    LOG.info("Shen Li: in PFileBlockWriter constructor");
    //TODO
  }

  /*
   * clear the kvs whenever write a new block
   */
  public DataOutputStream startWriting(BlockType newBlockType) 
      throws IOException {
    this.kvs.clear();
    return super.startWriting(newBlockType);  
  }

  /*
   * append the pkv to cache and update the skiplist, pkv entries cannot be
   * written into the data stream yet, as the number skiplist pointers on some
   * entry may change latter.
   */
  @Override
  public void write(KeyValue kv) throws IOException {
    LOG.info("Shen Li: in PFileBlockWriter.write, key length" + 
             kv.getLength());
    expectState(State.WRITING);
    this.unencodedDataSizeWritten += kv.getLength();
    this.unencodedDataSizeWritten += PKeyValue.POINTER_NUM_SIZE;
    int kvsSize = this.kvs.size();
    int nOfZeros = kvsSize > 0 ? numberOfTrailingZeros(kvsSize) : 0;
    // the number of bytes written in order to update ancestors forwarding 
    // pointers, plus the prev pointer
    this.unencodedDataSizeWritten += (PKeyValue.POINTER_SIZE * (nOfZeros + 1));
    LOG.info("Shen Li: pointers info, " + nOfZeros + ", " + 
             PKeyValue.POINTER_NUM_SIZE + ", " + PKeyValue.POINTER_SIZE);
    this.kvs.add(kv);
    LOG.info("Shen Li: in PFileBlockWriter.write before loop");
    int idx = 0;
    for (int i = 1 ; i <= nOfZeros; ++i) {
      idx = kvsSize - (1 << i);
      if (this.ptrNum[idx] >= MAX_POINTER_NUM) {
        LOG.info("Shen Li: in PFileBlockWriter.write throw exception" + 
            this.ptrNum[idx] + ", " + MAX_POINTER_NUM);
        throw new IllegalStateException("Shen Li: Too many pointers when" +
            "inserting kv number " + this.kvs.size());
      }
      ++this.ptrNum[idx];
    }
    LOG.info("Shen Li: after loop");
    if (this.kvs.size() > this.ptrNum.length) 
      this.ptrNum = Arrays.copyOf(this.ptrNum, this.ptrNum.length * 2);
    // no pointer points to the next entry
    this.ptrNum[this.kvs.size() - 1] = 0;
    LOG.info("Shen Li: PFileBlockWriter.write finish");
  }

  /*
   * The algorithm comes from 
   * http://graphics.stanford.edu/~seander/bithacks.html#ZerosOnRightModLookup
   * which is more efficient than Integer.numberOfTrailingZeros()
   */
  private static int numberOfTrailingZeros(int v) {
    return trailingZeroMap[(v & -v) % 37];
  }

  /*
   * write all pkv entries into the stream.
   */
  @Override
  protected void finishBlock() throws IOException {
    LOG.info("Shen Li: in PFileBlockWriter.finishBlock()");
    //TODO: below only writes pkvs
    if (this.offsets.length < this.ptrNum.length) {
      // doubling array size
      this.offsets = new int [this.ptrNum.length];
    }

    int curOffset = 0;
    int nOfKvs = this.kvs.size();
    int maxPtrNum = 0;
    int j = 1, i = 0;

    // initializting offsets array
    for (i = 0; i < nOfKvs; ++i) {
      this.offsets[i] = curOffset;
      curOffset += this.kvs.get(i).getLength();
      curOffset += PKeyValue.POINTER_NUM_SIZE;
      curOffset += ((this.ptrNum[i] + 1) * PKeyValue.POINTER_SIZE);
    }

    // write PKeyValues
    for (i = 0; i < nOfKvs; ++i) {
      // write pointer number
      // TODO: implement PFileDataBlockEncoder to account encodeLong, the second
      // parameter indicate the number of lower bytes to encode from the int
      // variable.
      
      // The ptrNum of the last pkv equals -1;
      if (i + 1 >= nOfKvs) {
        this.pDataBlockEncoder.encodeByte(BYTE_NEGTIVE_ONE,
          dataBlockEncodingCtx, this.userDataStream);
      } else {
        this.pDataBlockEncoder.encodeByte(this.ptrNum[i], 
          dataBlockEncodingCtx, this.userDataStream);    
      }

      // write pointers, this.ptrNum array is 
      j = 2;
      while (i + j < nOfKvs && this.ptrNum[i] > 0) {
        this.pDataBlockEncoder.encodeInt(
            this.offsets[i + j] - this.offsets[i], 
            dataBlockEncodingCtx, this.userDataStream);
        j <<= 1;
        --(this.ptrNum[i]);
      }
      if (0 != this.ptrNum[i]) {
        LOG.info("Shen Li: ptrNum of entry " + i +
                 " should be 0, the real value is " + this.ptrNum[i]);
        throw new IllegalStateException("Shen Li: ptrNum of entry " + i + 
            " should be 0, the real value is " + this.ptrNum[i]);
      }

      // TODO: remove this and implement the logic for seekBefore if 
      // evaluations show large space overhead
      // relative offset to the previous
      if (0 == i) {
        this.pDataBlockEncoder.encodeInt(0, 
            dataBlockEncodingCtx, this.userDataStream);
      } else {
        this.pDataBlockEncoder.encodeInt(this.offsets[i-1] - this.offsets[i],
            dataBlockEncodingCtx, this.userDataStream);
      }

      // write KeyValue
      this.pDataBlockEncoder.encode(this.kvs.get(i), dataBlockEncodingCtx, 
          this.userDataStream);
    }

    super.finishBlock();
  }

  /*
   * called by the PFileWritter to check whether it is time to close the 
   * current block
   */
  @Override
  public int blockSizeWritten() {
    if (state != State.WRITING) return 0;
    return this.unencodedDataSizeWritten;
  }

}

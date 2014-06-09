package org.apache.hadoop.hbase.io.pfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.apache.hadoop.hbase.io.hfile.HFileBlock;

/*
 * TODO TODO: it seems much easier to just append the skiplist to the end
 * of the block
 */

public static class PFileBlockWriter extends HFileBlock.Writer {
  private static final int ARRAY_INIT_SIZE = 512;
  private static final int ARRAY_INIT_SIZE = 512;
  private static final int [] trailingZeroMap = new int [] {
    32, 0, 1, 26, 2, 23, 27, 0, 3, 16, 24, 30, 28, 11, 0, 13, 4,
    7, 17, 0, 25, 22, 31, 15, 29, 10, 12, 6, 0, 21, 14, 9, 5,
    20, 8, 19, 18
  };

  private int [] ptrNum = null;
  private List<KeyValue> kvs = null;

  //TODO
  public PFileBlockWrtier(PFileDataBlockEncoder pDataBlockEncoder, 
                          PFileContext pFileContext) {
    super(pDataBlockEncoder.getHFileDataBlockEncoder(), 
          pFileContext.getHFileContext);
    this.offsets = new int[ARRAY_INIT_SIZE];
    this.ptrNum = new int[ARRAY_INIT_SIZE];
    this.kvs = new ArrayList<KeyValue>();
    //TODO
  }

  /*
   * append the pkv to cache and update the skiplist, pkv entries cannot be
   * written into the data stream yet, as the number skiplist pointers on some
   * entry may change latter.
   */
  @Override
  public void write(KeyValue kv) throws IOException {
    expectState(State.WRITING);
    this.unencodedDataSizeWritten += kv.getLength();
    this.unencodedDataSizewritten += PKeyValue.POINTER_NUM_SIZE;
    int kvsSize = this.kvs.size();
    int nOfZeros = kvsSize > 0 ? numberOfTrailingZeros(kvsSize) : 0;
    // the number of bytes written in order to update ancestors forwarding 
    // pointers
    this.unencodedDataSizeWritten += (PKeyValue.POINTER_SIZE * nOfZeros);
    this.kvs.add(kv);
    int idx = 0;
    for (int i = 1 ; i <= nOfZeros; ++i) {
      idx = kvsSize - (1 << i);
      ++this.ptrNum[idx];
    }
    if (kvsSize > this.ptrNum.length) 
      this.ptrNum = Arrays.copyOf(this.ptrNum, this.ptrNum.length * 2);
    this.ptrNum[kvsSize - 1] = 0;
  }

  /*
   * The algorithm comes from 
   * http://graphics.stanford.edu/~seander/bithacks.html#ZerosOnRightModLookup
   * which is more efficient than Integer.numberOfTrailingZeros()
   */
  private static int numberOfTrailingZero(int v) {
    return this.trailingZeroMap[(v & -v) % 37];
  }

  /*
   * write all pkv entries into the stream.
   */
  @Override
  public void writeHeaderAndData(FSDataOutputStream out) throws IOException {
    //TODO: below only writes pkvs
    //calculate offset
    if (this.offsets.size() < this.kvs.size()) {
      // doubling array size
      this.offsets = new int [this.offsets.size() * 2];
    }

    int curOffset = 0;
    int nOfKvs = this.kvs.size();
    int lgstIndex = nOfKvs - 1;
    int intLog2 = 0;
    // > 1 as 1 = 1 << 0;
    while (lgstIndex > 1) {
      lgstIndex >>= 1;
      ++intLog2;
    }

    int ptrNumLeft = 0;
    int ptrNumRight = 0;
    lgstIndex = this.kvs.size() - 1;
    for (int i = 0; i <= lgstIndex; ++i) {
      // calculate number of pointers for this pkv
      ptrNumLeft = numberOfTrailingZero(i);
      if (((1 << intLog2) & (lgstIndex - i)) <= 0)
        --intLog2;
      ptrNumRight = intLog2;
      numOfPtr = Math.min(ptrNumLeft, ptrNumRight);
      curOffset += 
    }
  }

  /*
   * called by the PFileWritter to check whether it is time to close the 
   * current block
   */
  @Override
  public int blockSizeWritten() {
    if (state != State.WRITTING) return 0;
    return this.unencodedDataSizeWritten;
  }

}

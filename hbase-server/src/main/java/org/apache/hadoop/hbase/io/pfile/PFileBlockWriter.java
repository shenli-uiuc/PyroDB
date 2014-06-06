package org.apache.hadoop.hbase.io.pfile;

import org.apache.hadoop.hbase.io.hfile.HFileBlock;

/*
 * TODO TODO: it seems much easier to just append the skiplist to the end
 * of the block
 */

public static class PFileBlockWriter extends HFileBlock.Writer {
  //TODO
  public PFileBlockWrtier(PFileDataBlockEncoder pDataBlockEncoder, 
                          PFileContext pFileContext) {
    super(pDataBlockEncoder.getHFileDataBlockEncoder(), 
          pFileContext.getHFileContext);
    //TODO
  }

  /*
   * append the pkv to cache and update the skiplist, pkv entries cannot be
   * written into the data stream yet, as the number skiplist pointers on some
   * entry may change latter.
   */
  @Override
  public void write(PKeyValue pkv) throws IOException {
    expectState(State.WRITING);
    this.unencodedDataSizeWritten += 
  }

  /*
   * write all pkv entries into the stream.
   */
  @Override
  public void writeHeaderAndData(FSDataOutputStream out) throws IOException {
    //TODO
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

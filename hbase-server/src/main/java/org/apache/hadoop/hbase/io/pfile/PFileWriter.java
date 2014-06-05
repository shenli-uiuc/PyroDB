package org.apache.hadoop.hbase.io.pfile;


import org.apache.hadoop.hbase.io.HFileWriterV2;

/*
 * PFile format:
 * Entries are oganized into a skiplist. Each entry contains 6 fields:
 *    1. pointerNum:  the number of forward pointers
 *    2. keyLen: same
 *    3. ValueLen: same
 *    4. pointers: forward pointers of this skiplist node
 *    5. key: same
 *    6. value: same
 * TODO: check if we could inherit from HFileWriterV2
 * TODO: should I remove the header of each block?
 * TODO: 1. need to implement another HFileBlock.Writer. Note that either
 *          PFileWriter or the block writer has to hold the entire block before
 *          hand it to the next layer, as the skiplist needs to be generated 
 *          on demand. (it is better the block writer holds it.)
 *       2. we also need a new encoder
 */

public class PFileWriter extends  HFileWriterV2{

  /*
   * flush all PKeyValue entries into file system. Write index
   */
  @Override
  private void finishBlock() throws IOException{
  }

}

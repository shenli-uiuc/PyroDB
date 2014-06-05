package org.apache.hadoop.hbase;

import org.apache.hadoop.hbase.KeyValue;

/*
 * A thin wrapper over KeyValue to allow convenient operations on forward 
 * pointers. May need to implement Cell, HeapSize, and Cloneable later.
 *
 * Fields:
 *  1. pointerNum
 *  2. pointers
 *  3. KeyValue
 */
public class PKeyValue {

  private KeyValue kv = null;

  /*
   * number of skiplist forward pointers and pointer list.
   * pointers stores the number of BYTEs we should jump. It does not store a 
   * copy of the key, as the key can be accessed from the pointer destination.
   */
  private byte pointerNum = 0;
  private List<Integer> pointers = null;

  /*
   * convert a byte array into a PKeyValue object
   */
  public PKeyValue(final byte [] bytes) {
    this(bytes, 0);
  }

  /*
   * construct a PKeyValue from byte array starting from offset.
   */
  public PKeyValue(final byte [] bytes, final int offset) {
    this(bytes, offset, getLength(bytes, offset));
  }

  /*
   * @param bytes byte array
   * @param offset offset to the start of the PKeyValue
   * @param length length of the PKeyValue
   */
  public PKeyValue(final byte [] bytes, final int offset, final int length) {
    //TODO
  }
}

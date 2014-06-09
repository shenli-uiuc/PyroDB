package org.apache.hadoop.hbase;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;

/*
 * A thin wrapper over KeyValue to allow convenient operations on forward 
 * pointers. May need to implement Cell, HeapSize, and Cloneable later.
 *
 * Fields:
 *  1. pointerNum
 *  2. pointers
 *  3. KeyValue
 *
 * To keep PKeyValue light-weight, pointerNum and pointers are not materialized
 * until needed.
 */
public class PKeyValue {
  public static final int POINTER_NUM_SIZE = Bytes.SIZEOF_BYTE;
  public static final int POINTER_SIZE = Bytes.SIZEOF_INT;

  private byte [] bytes = null;
  private int offset = 0;
  private int length = 0;

  private KeyValue kv = null;

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
    int pHeaderLength = getPHeaderLength(bytes, offset);
    this(bytes, offset, pHeaderLength, 
         KeyValue.getLength(bytes, offset + pHeaderLength));
  }

  /*
   * @param bytes byte array
   * @param offset offset to the start of the PKeyValue
   * @param length length of the PKeyValue
   */
  public PKeyValue(final byte [] bytes, final int offset, final int length) {
    this.bytes = bytes;
    this.offset = offset;
    this.length = length;
    int pHeaderLength = getPHeaderLength();
    this.kv = new KeyValue(bytes, offset + pHeaderLength, length - pHeaderLength);
  }

  public PKeyValue(final byte [] bytes, final int offset, 
                   final int pHeaderLength, final int kvLength) {
    this.bytes = bytes;
    this.offset = offset;
    this.length = pHeaderLength + kvLength;
    this.kv = new KeyValue(bytes, offset + pHeaderLength, kvLength);
  }

  /*
   * only on write
   */
  public PKeyValue(final KeyValue kv) {
    this.kv = kv;
  }

  public int getPointerNum(){
    return this.bytes[0] & 0xFF;
  }

  public static int getPointerNum(byte [] bytes, int offset){
    return bytes[offset] & 0xFF;
  }

  /*
   * on read
   */
  public int getPHeaderLength(){
    return this.length - this.kv.getLength();
  }

  public static int getPHeaderLength(byte [] bytes, int offset){
    return POINTER_NUM_SIZE + getPointerNum(bytes, offset) * POINTER_SIZE;
  }

  public int getLength(){
    return length;
  }

  public static int getLength(byte [] bytes, int offset) {
    int pHeaderSize = POINTER_NUM_SIZE + getPointerNum(bytes, offset) * POINTER_SIZE;
    return pHeaderSize + KeyValue.getLength(bytes, offset + pHeaderSize);
  }

  public int getPointer(int pIndex) {
    return Bytes.toInt(bytes, 
                       offset + POINTER_NUM_SIZE + pIndex * POINTER_SIZE);
  }



}

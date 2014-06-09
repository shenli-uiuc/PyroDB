package org.apache.hadoop.hbase.io.pfile;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.hbase.io.hfile.HFileDataBlockEncoder;
import org.apache.hadoop.hbase.io.encoding.HFileBlockEncodingContext;

/*
 * For now, PBlock encoding/decoding programs do not need to be in 
 * hbase-common, as the PKeyValue format is only visible on the fs layer.
 */

public interface PFileDataBlockEncoder extends HFileDataBlockEncoder {

  public int encodeByte(byte v,
                        HFileBlockEncodingContext encodingCtx,
                        DataOutputStream out) throws IOException;

  public int encodeInt(int v,
                       HFileBlockEncodingContext encodingCtx,
                       DataOutputStream out) throws IOException;
}

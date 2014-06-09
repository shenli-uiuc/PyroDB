package org.apache.hadoop.hbase.io.pfile;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.hbase.io.hfile.NoOpDataBlockEncoder;
import org.apache.hadoop.hbase.io.encoding.HFileBlockEncodingContext;
import org.apache.hadoop.hbase.util.Bytes;


public class PNoOpDataBlockEncoder extends NoOpDataBlockEncoder
    implements PFileDataBlockEncoder {

  public static final PNoOpDataBlockEncoder INSTANCE = 
    new PNoOpDataBlockEncoder();

  private PNoOpDataBlockEncoder() {
  }

  public int encodeByte(byte v, HFileBlockEncodingContext ctx, 
                        DataOutputStream out) throws IOException {
    out.writeByte(v);
    return Bytes.SIZEOF_BYTE;
  }

  public int encodeInt(int v, HFileBlockEncodingContext ctx,
                       DataOutputStream out) throws IOException {
    out.writeInt(v);
    return Bytes.SIZEOF_INT;
  }
}

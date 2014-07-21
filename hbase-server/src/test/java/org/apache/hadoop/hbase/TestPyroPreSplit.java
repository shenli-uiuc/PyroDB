package org.apache.hadoop.hbase;

import java.nio.ByteBuffer;
import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;

public class TestPyroPreSplit extends Configured {

  public TestPyroPreSplit(final Configuration conf) {
    super(conf);
  }

  public static void checkTable(HBaseAdmin admin, HTableDescriptor desc,
                         byte[] startKey, byte[] endKey, 
                         int regionNum, int replicaNum) 
  throws IOException {
    TableName tableName = desc.getTableName();
    if (admin.tableExists(tableName)) {
      admin.disableTable(tableName);
      admin.deleteTable(tableName);
    }

    admin.createTable(desc, startKey, endKey, regionNum, replicaNum);

  }

  public static byte[] long2Bytes(long v) {
    return ByteBuffer.allocate(8).putLong(v).array();
  }

  public static void main(String args[]) {
    HBaseAdmin admin = null;

    try {
      TestPyroPreSplit test = 
        new TestPyroPreSplit(HBaseConfiguration.create());
      admin = new HBaseAdmin(test.getConf());
      HTableDescriptor desc = new HTableDescriptor(TableName.valueOf("PyroTestTable"));
      HColumnDescriptor family = new HColumnDescriptor("FAMILY_1");
      desc.addFamily(family);
      byte [] startKey = long2Bytes(100000000);
      byte [] endKey   = long2Bytes(200000000);
      int regionNum = Integer.parseInt(args[0]);
      int replicaNum = Integer.parseInt(args[1]);
      checkTable(admin, desc, startKey, endKey, regionNum, replicaNum);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }
}

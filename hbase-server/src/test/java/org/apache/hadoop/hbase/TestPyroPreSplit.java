package org.apache.hadoop.hbase;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.List;

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
      List<HRegionInfo> rInfos = admin.getTableRegions(desc.getTableName());
      for (HRegionInfo info : rInfos) {
        System.out.println(info.getRegionNameAsString());
      }
      int prevRegionNum = admin.getTableRegions(desc.getTableName()).size();
      boolean shouldSplit = Boolean.parseBoolean(args[2]);
      if (shouldSplit) {
        admin.split(rInfos.get(1).getRegionName(), true);
     
        int cnt = 0;
        while (prevRegionNum >= 
            admin.getTableRegions(desc.getTableName()).size()) {
          ++cnt;
          Thread.sleep(Long.parseLong(args[3]));
          System.out.println("\n============" + cnt + "===============\n");
        }
        rInfos = admin.getTableRegions(desc.getTableName());
        for (HRegionInfo info : rInfos) {
          System.out.println(info.getRegionNameAsString());
        }
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }
}

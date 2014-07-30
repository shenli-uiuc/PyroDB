package org.apache.hadoop.hbase;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.ResultScanner;

public class PyroClient extends Configured {

  protected static byte[] FIXED_VALUE = null;

  protected String tableName = null;

  protected long startKeyLong = 0;
  protected long endKeyLong = 0;
  protected int keyLen = 0;
  protected int valueLen = 0;
  protected int maxScanRange = 0;

  protected long expLen = 0;
  protected long writeThreadNum = 0;
  protected long writeThreadSleep = 0;
  protected long readThreadNum = 0;
  protected long readThreadSleep = 0;

  protected Random rand = null;

  protected String prefix = null;

  public PyroClient(final Configuration conf) {
    super(conf);
    rand = new Random();
  }

  public static byte[] long2Bytes(long v) {
    return ByteBuffer.allocate(8).putLong(v).array();
  }

  public static byte[] getRandomRow(Random rand, 
      int keyLen, long startKey, long endKey) {
    long number = rand.nextLong() % (endKey - startKey) + startKey;
    return format(keyLen, number);
  }

  public static byte[] format(int keyLen, long number) {
    byte [] b = new byte[keyLen];
    long d = Math.abs(number);
    for (int i = b.length - 1; i >= 0; i--) {
      b[i] = (byte)((d % 10) + '0');
      d /= 10;
    }
    return b;
  }

  public static byte[] generateData(Random rand, int valueLen) {
    synchronized(FIXED_VALUE) {
      if (null == FIXED_VALUE) {
        FIXED_VALUE = new byte[valueLen];
        for (int i = 0 ; i < valueLen; ++i) {
          FIXED_VALUE[i] = '3';
        }
      }
    }
    return FIXED_VALUE;
  }

  public WriteThread createWriteThread(long sleepLen) throws IOException {
    return new WriteThread(sleepLen);
  }

  public ReadThread createReadThread(long sleepLen, String logFileName) 
  throws IOException {
    return new ReadThread(sleepLen, logFileName);
  }

  class WriteThread extends Thread {
    private HConnection connection;
    private HTableInterface table;
    private long sleepLen = 0;

    public WriteThread(long sleepLen) throws IOException {
      this.sleepLen = sleepLen;
      this.connection = HConnectionManager.createConnection(getConf());
      this.table = connection.getTable(tableName);
    }

    void testRow() {
      try {
        byte [] row = getRandomRow(rand, keyLen, startKeyLong, endKeyLong);
        byte [] value = generateData(rand, valueLen);
        Put put = new Put(row);
        put.add(PyroAdmin.FAMILY_NAME, PyroAdmin.QUALIFIER_NAME, value);
        table.put(put);
      } catch (IOException ex) {
        System.out.println("Exception in WriteThread.testRow:" 
                           + ex.getMessage());
        ex.printStackTrace();
      }
    }

    public void run() {
      try {
      long endTime = System.currentTimeMillis() + expLen;
      while (true) {
        testRow();  
        if (endTime < System.currentTimeMillis()) {
          break;
        }
        Thread.sleep(sleepLen);
      }
      } catch (InterruptedException ex) {
        System.out.println("Exception: " + ex.getMessage());
        ex.printStackTrace();
      }
    }

  }

  class ReadThread extends Thread {
    private HConnection connection;
    private HTableInterface table;
    private long sleepLen = 0;
    private String logFileName = null;
    private PrintWriter writer = null;

    public ReadThread(long sleepLen, String logFileName) 
    throws IOException {
      this.sleepLen = sleepLen;
      this.logFileName = logFileName;
      try {
      this.connection = HConnectionManager.createConnection(getConf());
      this.table = connection.getTable(tableName);
      this.writer = new PrintWriter(logFileName, "UTF-8");
      } catch (IOException ex) {
        System.out.println("Exception in ReadThread: " + ex.getMessage());
        ex.printStackTrace();
      }
    }

    void testRow() {
      try {
        long randStartKey = 
          rand.nextLong() % (endKeyLong - startKeyLong) + startKeyLong;
        int scanRangeLen = rand.nextInt(maxScanRange);
        byte [] startRow = format(keyLen, randStartKey);
        byte [] endRow = format(keyLen, randStartKey + scanRangeLen);
        Scan scan = new Scan(startRow, endRow);

        scan.addColumn(PyroAdmin.FAMILY_NAME, PyroAdmin.QUALIFIER_NAME);
        long startTime = System.currentTimeMillis();
        ResultScanner s = this.table.getScanner(scan);
        int count = 0;
        while (s.next() != null) {
          count++;
        }
        s.close();
        long endTime = System.currentTimeMillis();
        this.writer.println((endTime - startTime));

      } catch (IOException ex) {
        System.out.println("Exception in ReadThread.testRow(): " 
                           + ex.getMessage());
        ex.printStackTrace();
      }
    }

    public void run() {
      try {
      long endTime = System.currentTimeMillis() + expLen;
      while (true) {
        testRow();  
        if (endTime < System.currentTimeMillis()) {
          break;
        }
        Thread.sleep(sleepLen);
      }
      this.writer.close();
      } catch (InterruptedException ex) {
        System.out.println("Exception: " + ex.getMessage());
        ex.printStackTrace();
      }
    }
  }

  public static void main(String args[]) {
    try {
      PyroClient client = 
        new PyroClient(HBaseConfiguration.create());

      // 0 1000000 10 5000 5 60000 2 10 3 5 /home/shen/hbase-logs/
      client.tableName = args[0];

      client.startKeyLong = Long.parseLong(args[1]);
      client.endKeyLong = Long.parseLong(args[2]);
      client.keyLen = Integer.parseInt(args[3]);
      client.valueLen = Integer.parseInt(args[4]);

      client.maxScanRange = Integer.parseInt(args[5]);

      client.expLen = Long.parseLong(args[6]);
      client.writeThreadNum = Long.parseLong(args[7]);
      client.writeThreadSleep = Long.parseLong(args[8]);
      client.readThreadNum = Long.parseLong(args[9]);
      client.readThreadSleep = Long.parseLong(args[10]);

      client.prefix = args[11];

      ArrayList<Thread> threads = new ArrayList<Thread>();
      for (int i = 0; i < client.writeThreadNum; ++i) {
        WriteThread t = client.createWriteThread(client.writeThreadSleep);
        t.setDaemon(true);
        t.start();
        threads.add(t);
      }

      long curTime = System.currentTimeMillis();
      for (int i = 0; i < client.readThreadNum; ++i) {
        ReadThread t = client.createReadThread(client.readThreadSleep, 
                                  client.prefix + "/" + "readDelay_" 
                                  + curTime + "_" + i 
                                  + ".log");
        t.setDaemon(true);
        t.start();
        threads.add(t);
      }

      for (Thread t : threads) {
        t.join();
      }

    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }
}

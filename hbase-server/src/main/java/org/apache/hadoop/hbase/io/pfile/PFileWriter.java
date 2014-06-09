package org.apache.hadoop.hbase.io.pfile;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.KeyValue.KVComparator;

import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFileWriterV2;

/*
 * PFile format:
 *    header followed by a series of PKeyValue entries
 * TODO: check if we could inherit from HFileWriterV2
 * TODO: should I remove the header of each block?
 * TODO: 1. need to implement another HFileBlock.Writer. Note that either
 *          PFileWriter or the block writer has to hold the entire block before
 *          hand it to the next layer, as the skiplist needs to be generated 
 *          on demand. (it is better the block writer holds it.)
 *       2. we also need a new encoder
 */

public class PFileWriter extends  HFileWriterV2{

  public PFileWriter(Configuration conf, CacheConfig cacheConf, FileSystem fs,
                     Path path, FSDataOutputStream ostream, 
                     final KVComparator comparator,
                     final HFileContext fileContext) throws IOException {
    super(conf, cacheConf, fs, path, ostream, comparator, fileContext);

  }

}

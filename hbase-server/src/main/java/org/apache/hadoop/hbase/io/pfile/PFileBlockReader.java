package org.apache.hadoop.hbase.io.pfile;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataInput;

import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.io.hfile.HFileBlock;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.BlockType;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.FSInputStreamWrapper;
import org.apache.hadoop.hbase.fs.HFileSystem;

public class PFileBlockReader extends HFileBlock.FSReaderV2 {
  
  public PFileBlockReader(FSDataInputStreamWrapper stream,
                          long fileSize, HFileSystem fs,
                          Path path, HFileContext fileContext) 
      throws IOException {
    super(stream, fileSzie, fs, path, fileContext);
  }


}

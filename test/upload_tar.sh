rm -rf ./hbase-0.99.0-SNAPSHOT
rm -rf ./hbase-0.99.0-shen
tar -zxvf ../hbase-assembly/target/hbase-0.99.0-SNAPSHOT-bin.tar.gz
mv ./hbase-0.99.0-SNAPSHOT ./hbase-0.99.0-shen
cp ./conf-dist/* ./hbase-0.99.0-shen/conf
cp -r ./scripts ./hbase-0.99.0-shen/
tar -zcvf hbase-0.99.0-shen.tar.gz ./hbase-0.99.0-shen
scp ./hbase-0.99.0-shen.tar.gz shenli3@tareka05.cs.uiuc.edu:/scratch/shenli3/software

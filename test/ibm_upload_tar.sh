rm -rf ./hbase-0.99.0-SNAPSHOT
rm -rf ./hbase-0.99.0-shen
tar -zxvf ../hbase-assembly/target/hbase-0.99.0-SNAPSHOT-bin.tar.gz
mv ./hbase-0.99.0-SNAPSHOT ./hbase-0.99.0-shen
cp ./conf-ibm/* ./hbase-0.99.0-shen/conf
cp -r ./scripts-ibm ./hbase-0.99.0-shen/scripts
tar -zcvf hbase-0.99.0-shen.tar.gz ./hbase-0.99.0-shen
scp ./hbase-0.99.0-shen.tar.gz spatiotemporal@icusrv95.watson.ibm.com:/home/spatiotemporal/tar

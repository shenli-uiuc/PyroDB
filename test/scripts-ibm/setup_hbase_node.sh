mkdir -p /home/spatiotemporal/srcs
mkdir -p /home/spatiotemporal/software
rm /home/spatiotemporal/srcs/hbase-0.99.0-shen.tar.gz
rm -rf /home/spatiotemporal/software/hbase-0.99.0-shen
rm -rf /home/spatiotemporal/zookeeper
mkdir -p /home/spatiotemporal/zookeeper
scp  spatiotemporal@icusrv95.watson.ibm.com:/home/spatiotemporal/tar/hbase-0.99.0-shen.tar.gz /home/spatiotemporal/srcs/
tar zxf /home/spatiotemporal/srcs/hbase-0.99.0-shen.tar.gz -C /home/spatiotemporal/software/

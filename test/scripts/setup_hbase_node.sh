mkdir -p /srv/scratch/shenli3/srcs
mkdir -p /srv/scratch/shenli3/software
rm /srv/scratch/shenli3/srcs/hbase-0.99.0-shen.tar.gz
rm -rf /srv/scratch/shenli3/software/hbase-0.99.0-shen
rm -rf /srv/scratch/shenli3/zookeeper
mkdir -p /srv/scratch/shenli3/zookeeper
cp /home/shenli3/software/src/hbase-0.99.0-shen.tar.gz /srv/scratch/shenli3/srcs/
tar zxf /srv/scratch/shenli3/srcs/hbase-0.99.0-shen.tar.gz -C /srv/scratch/shenli3/software/

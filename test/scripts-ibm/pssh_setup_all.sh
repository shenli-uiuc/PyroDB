# execute on tareka05
rm /home/tarek/shenli3/software/src/hbase-0.99.0-shen.tar.gz
cp ../../hbase-0.99.0-shen.tar.gz /home/tarek/shenli3/software/src/
mkdir -p /home/tarek/shenli3/script/pssh/hbase/msgerr
mkdir -p /home/tarek/shenli3/script/pssh/hbase/msgout
cp ./setup_hbase_node.sh /home/tarek/shenli3/script/pssh/hbase/
pssh -h ../conf/regionservers -e /home/tarek/shenli3/script/pssh/hbase/msgerr -o /home/tarek/shenli3/script/pssh/hbase/msgout /home/shenli3/script/pssh/hbase/setup_hbase_node.sh
pssh -h ../conf/master -e /home/tarek/shenli3/script/pssh/hbase/msgerr -o /home/tarek/shenli3/script/pssh/hbase/msgout /home/shenli3/script/pssh/hbase/setup_hbase_node.sh
pssh -h ../conf/zookeepers -e /home/tarek/shenli3/script/pssh/hbase/msgerr -o /home/tarek/shenli3/script/pssh/hbase/msgout /home/shenli3/script/pssh/hbase/setup_hbase_node.sh

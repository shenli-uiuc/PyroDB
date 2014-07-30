echo "/home/shenli3/script/pssh/hadoop/reboot.sh $1"
cp ./reboot.sh /home/tarek/shenli3/script/pssh/hadoop/reboot.sh
pssh -h ../etc/hadoop/slaves -e /home/tarek/shenli3/script/pssh/hadoop/msgerr -o /home/tarek/shenli3/script/pssh/hadoop/msgout /home/shenli3/script/pssh/hadoop/reboot.sh $1
pssh -h ../etc/hadoop/master -e /home/tarek/shenli3/script/pssh/hadoop/msgerr -o /home/tarek/shenli3/script/pssh/hadoop/msgout /home/shenli3/script/pssh/hadoop/reboot.sh $1

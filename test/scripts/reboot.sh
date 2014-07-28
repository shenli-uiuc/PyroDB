echo "$1"
echo -e "$1" | ssh -t -t `hostname` sudo -S reboot


#!/bin/sh

sudo /usr/sbin/iptables -D INPUT -s 172.16.1.0/24 -p icmp --icmp-type 8 -j DROP
sudo /usr/sbin/iptables -D OUTPUT -s 172.16.1.0/24 -p icmp --icmp-type 8 -j ACCEPT

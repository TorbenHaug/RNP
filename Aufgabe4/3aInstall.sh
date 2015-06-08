 
#!/bin/sh
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -j DROP
sudo /usr/sbin/iptables -I OUTPUT -s 172.16.1.0/24 -j REJECT
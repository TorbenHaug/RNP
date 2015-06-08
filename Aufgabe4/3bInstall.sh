
#!/bin/sh 

# drop all ports of IP 172.16.1.0/24
sudo /usr/sbin/iptables -I OUTPUT -s 172.16.1.0/24 -j REJECT
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -j DROP

# accept only port 51000 of IP 172.16.1.0/24
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -p tcp --dport 51000 -j ACCEPT
sudo /usr/sbin/iptables -I OUTPUT -s 172.16.1.0/24 -p tcp --dport 51000 -j ACCEPT
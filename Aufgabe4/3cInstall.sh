 
#!/bin/sh

# block tcp only
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -p tcp -j DROP

sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -p tcp -state --state ESTABLISHED -j ACCEPT
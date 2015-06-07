
#!/bin/sh

# drops every incoming icmp packages from IP 172.16.1.0
sudo iptables -I INPUT -s 172.16.1.0/24 -p icmp --icmp-type 8 -j DROP

# allows outgoing icmp packages from IP 172.16.1.0
sudo iptables -I OUTPUT -s 172.16.1.0/24 -p icmp --icmp-type 8 -j ACCEPT

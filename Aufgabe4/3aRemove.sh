 
#!/bin/sh
sudo iptables -D INPUT -s 172.16.1.0/24 -j DROP
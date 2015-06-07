 
#!/bin/sh
sudo iptables -I INPUT -s 172.16.1.0/24 -j DROP
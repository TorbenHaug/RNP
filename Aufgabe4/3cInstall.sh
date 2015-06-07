 
#!/bin/sh

# block tcp only
sudo iptables -I INPUT -s 172.16.1.0/24 -p tcp -j DROP
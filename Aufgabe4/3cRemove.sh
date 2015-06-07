 
#!/bin/sh

# unblock tcp only
sudo iptables -D INPUT -s 172.16.1.0/24 -p tcp -j DROP
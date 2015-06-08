
#!/bin/sh

sudo /usr/sbin/iptables -D OUTPUT -p tcp --dport http -j REJECT --reject-with tcp-reset

sudo /usr/sbin/iptables -D OUTPUT -p tcp -d www.dmi.dk --dport http -j ACCEPT


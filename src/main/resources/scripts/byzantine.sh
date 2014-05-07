#!/bin/bash
START_IP=10.0.0.9
END_IP=10.0.0.10
sudo iptables -A OUTPUT -m iprange --dst-range $START_IP-$END_IP -j NFQUEUE --queue-num 0
nohup sudo -b /tmp/byzantine &

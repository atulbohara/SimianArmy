#!/bin/bash
START_IP=10.0.0.3
END_IP=10.0.0.5
iptables -A OUTPUT -m iprange --dst-range $START_IP-$END_IP -j NFQUEUE --queue-num 0
nohup /tmp/byzantine_monkey &

#!/bin/bash
# Script for NullRoute Chaos Monkey

sudo ip route add blackhole 10.0.0.0/8

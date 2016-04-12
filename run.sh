#!/usr/bin/env bash

set -e

# Goes into the folder
cd ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/

# If running on a Raspberry Pi, uncomment the line below
#export DISPLAY=:0

# If running on a linux system without a graphical display (e.g. a fresh Digital Ocean droplet, or an Amazon Web Services VPS)
# Install vnc first, then uncomment the line below
#export DISPLAY=localhost:1

# Runs the fucking java file
java -jar Automator.jar

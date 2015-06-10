#!/bin/bash -e

# Goes into the folder
cd ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/

# Exports the display for headless (setup vnc first), then uncomment the line below
#export DISPLAY=localhost:1

# Runs the fucking java file
java -jar Automator.jar

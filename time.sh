#!/bin/bash -e

#write out current crontab
crontab -l > tempCron

# Runs the Java applet every day at midnight
echo "01 00 * * * java -jar ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/Automator.jar" >> tempCron

#install new cron file
crontab tempCron
rm tempCron

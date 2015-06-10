#!/bin/bash -e

# Write out current crontab
crontab -l > tempCron

# Runs the Java applet every day at midnight
if [[ "$OSTYPE" == "linux-gnu" ]];
        then
                echo "01 00 * * * /bin/bash /home/$(whoami)/projects/NYU-Bobst-Library-Reservation-Automator-Java/run.sh" >> tempCron;
                echo "Automator app added to linux scheduler";
elif [[ "$OSTYPE" == "darwin"* ]];
        then
                echo "01 00 * * * /bin/bash /Users/$(whoami)/projects/NYU-Bobst-Library-Reservation-Automator-Java/run.sh" >> tempCron;
                echo "Automator app added to OSX scheduler";
fi

# Adds a newline character
echo  >> tempCron;

# Installs new cron file
crontab tempCron
rm tempCron


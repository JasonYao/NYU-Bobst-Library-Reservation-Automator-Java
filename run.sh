#!/bin/bash -e

# Goes into the folder
cd ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/

# If running on a Raspberry Pi, uncomment the line below
#export DISPLAY=:0

# Checks for directory existance
if [! -d "lib"];
then
	echo "Creating library directory"
	mkdir lib
fi

# Checks for the chromium binary
if [-f "lib/chromedriver"];
then
	# Driver is already downloaded
else
	# Driver has not been downloaded yet
	echo "Downloading the chrome driver"
	if ["$(uname -s)" == "Darwin"];
	then
		wget https://chromedriver.storage.googleapis.com/2.21/chromedriver_mac32.zip -O chromedriver.zip
	else
		wget https://chromedriver.storage.googleapis.com/2.21/chromedriver_linux64.zip -O chromedriver.zip
	fi
	unzip chromedriver.zip -d lib
	rm chromedriver.zip
fi

# If running on a linux system without a graphical display (e.g. a fresh Digital Ocean droplet, or an Amazon Web Services VPS)
# Install vnc first, then uncomment the line below
#export DISPLAY=localhost:1

# Runs the fucking java file
java -jar Automator.jar

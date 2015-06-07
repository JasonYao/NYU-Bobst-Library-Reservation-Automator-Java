# NYU-Bobst-Library-Reservation-Automator-Java

## Description
An automator written in Python to automatically book the room you want, can be paired with cron to run daily

## Usage
[For Unix systems including OSX]:
`mkdir ~/projects` -> Creates a directory out of the way to house the app

`cd ~/projects` -> This will bring you into the just created overarching directory

`git clone https://github.com/JasonYao/NYU-Bobst-Library-Reservation-Automator-Java.git` -> Downloads the app

`cd NYU-Bobst-Library-Reservation-Automator-Java` -> Brings you into the app directory

`cp settings.example settings` -> Moves the example settings file to the one you'll actually use

`nano settings` -> Edit the settings file using the nano editor, it's pretty self-explanatory. When done with editing, use `CTRL` + `x`, and then `y` and `ENTER` to save the settings file.

Download your use logins file by exporting from the google docs, and then move it into the `~/projects/NYU-Bobst-Library-Reservation-Automator-Java` directory.

`java -jar Automator.jar` -> This will run the Java applet, assuming you have java installed on your computer.

NOTE: This java was built and compiled and tested using Java 1.8.0_25, and so should work for Java 8.x, if you have errors and have an older version of Java, please update first and try again.

![Very dogee](https://raw.github.com/JasonYao/NYU-Bobst-Library-Reservation-Automator-Java/master/img/dogee.jpg)

### Automating for every day
TODO show instructions for automatically setting up cron jobs to run the java applet every 24 hours.

## The Explaination
NYU's Library system makes it annoying for students because each student is limited to one booking every 24 hours.
This script is meant to help alleviate the issue, by having it done automatically for you.

More notably, there is a quirk of the current system that can help us with these bookings.

1.) The fact that for sanitation purposes, the library system does not actually get rid
	of duplicates, i.e. `JasonYao@nyu.edu` and `JasonYao+1@nyu.edu` are counted as separate
	when dealing with emails. What this means: we can exploit this so that the number of
	friends required to book a room sequentially are lower, enabling us to simply use 1
	person and their duplicate email for each 2-hour block.

2.) The fact that we can utilize web drivers such as with [Selenium](https://selenium-python.readthedocs.org) that can
	help us with automation when dealing with web elements.

Utilizing these two axioms, this simplifies the amount of work required to be much less than we'd normally need.

We first will create a new User class per person, with a .name, .password and .email attribute. Each User will be made to login, and book the room
that was specified in the [settings](settings.py) file.

In the settings file, we will thus create a list of Users, and simply automate the task of logging in, selecting the time/room,
filling out all forms required.

This script follows an optimized approach, booking rooms in the order of an arbitrary 'best' time that I have set up beforehand.

## Licensing
This software is released under the GNU GPL 2.0 License as described in the [License file](LICENSE).

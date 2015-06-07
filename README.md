# NYU-Bobst-Library-Reservation-Automator-Java

## Description
An automator written in Python to automatically book the room you want, can be paired with cron to run daily

## Usage
`./run` -> This will compile and run the program for you. If you'd like to manually do this, you can do so below.

### The 'hard' way
`javac Automator`

`java Automator`

![Very dogee](https://raw.github.com/JasonYao/NYU-Bobst-Library-Reservation-Automator-Java/master/img/dogee.jpg)

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

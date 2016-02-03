# NYU Bobst Library Reservation Automator
By Jason Yao - current version: v2.1.2

## Description
This program's purpose is to automatically book a room at New York University's Bobst Library every day.

NYU's Bobst library system currently limits the amount of reservations possible to a single booking of a study room every 24 hours per user. 
This places a great limitation on the students at the university, especially since there was no automated way (until now) to book these study rooms.

The recommended users for this program are **student groups** - while individual students may use this program to automatically book a room
for 2 hours each day, your mileage increases dramatically the more users in your group that you have.

## Setup (For unix systems including OSX)
Open up your terminal, and start copying the code below

0.) Creates a directory out of the way to house the app, and go into the directory

```sh
mkdir ~/projects
cd ~/projects
```

1.) Downloads the application into a new directory

```sh
git clone https://github.com/JasonYao/NYU-Bobst-Library-Reservation-Automator-Java.git
cd NYU-Bobst-Library-Reservation-Automator-Java
```

2.) Copies the example [settings](settings.example) and [userLogins.csv](userLogins.csv.example) files
(Note: you can call the .csv file anything you want, as long as you change the name in the settings file)

```sh
cp settings.example settings
cp userLogins.csv.example userLogins.csv
```

3.) Edit the newly created [settings](settings.example) and [userLogins.csv](userLogins.csv.example) files (they're pretty self explanatory)

NOTES:

- If you're running this program on a Raspberry Pi, edit the [run.sh](run.sh) file, and uncomment the line `export DISPLAY=:0`.

- If you're running this program on linux that doesn't have a graphical display (e.g. a fresh VPS on Digital Ocean or AWS), 
edit the [run.sh](run.sh) file, and uncomment the line `export DISPLAY=localhost:1` after installing `vnc`.

## Dependencies
Make sure that there's some version of the java 8.x or higher installed on your machine, as this program has been built, compiled, 
and tested with java 1.8.0_25. 

If you have errors, and have an older version of java installed, please update to the latest version, and try again.
This is the only depency required to run this program.

## Usage
Usage is the same as on OSX as it is on linux. If you're on windows, then you'll have to look up instructions on how to run a java .jar file, since I'm not going to bother.

### Running the program automatically each day
The following [script](time.sh) will use `cron` in order to run the program every day at 0001.
NOTE: Only run this command once, as it will tell the `cron` daemon to run the `./run.sh` command every day at 0001.

```sh
./time.sh
```

![Very dogee](/img/dogee.jpg)

### Running the program manually (for testing, or the masochistic)
This will run the java application, assuming that dependencies are met as stated above.

```sh
java -jar Automator.jar
```

OR

```sh
./run.sh
```

## Display issues
Due to the graphical nature of this program, a graphical display of some kind is required. I highly recommend to just run this program off of a raspberry pi,
since the stock version of it can run this program just fine, compared to running it on a VPS on services like [Digital Ocean](https://www.digitalocean.com/) 
or [AWS](https://aws.amazon.com/), since `vnc` timeout issues occur.

### For unix systems that do have a graphical display
Shit just works, no need to do anything.

### For unix systems that do **NOT** have a graphical display
You'll need to install `vnc`. I'm not going to bother showing how, so just following 
[Digital Ocean's tutorial](https://www.digitalocean.com/tutorials/how-to-install-and-configure-vnc-on-ubuntu-14-04), and you should be good.

## Contributing & self-generation of binaries
If you'd like to generate your own `.jar` binary (you should in general to make sure binaries you download are what they say they are),
or if you'd like to help contribute to this repository, then feel free to setup your development environment via the instructions 
in the [CONTRIBUTING.md](CONTRIBUTING.md).

## Licensing
This software is released under the GNU GPL 2.0 License, a copy of which may be found [here](LICENSE).

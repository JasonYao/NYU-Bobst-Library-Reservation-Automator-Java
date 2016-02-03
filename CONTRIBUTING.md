# Contributing & self-generation of binaries
This document is for people interested in either contributing back to this repository (yay!),
or for those who wish to generate their own `.jar` binary (good on you for being paranoid) on the internet.

## Contributing
There is no code of conduct here. Don't be an ass, but do be critical, of the current code in the repository, 
to your own code that you're submitting for pulls, or to the code that others may write.

This is **not** a safe space. The only thing that matters is your code, so if it's subpar and glaringly so, 
don't be surprised if I or anybody else calls you out on it before rejecting your pull request with our notes. 
If this doesn't sit well with you, feel free to fork this repo and create your own project with your own rules.

## Development environment setup
There's a few things to do to setup our development environment.

0.) Download the [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html), by clicking the link,
and then clicking the `download` button under the `JDK` header.

![Downloading the JDK](/img/jdk.png)

1.) Follow the download steps in the [readme](README.md)

2.) Download the [selenium](http://www.seleniumhq.org/download/) java libraries

![Downloading the selenium libraries](/img/selenium.png)

3.) Move the download selenium libraries to a `lib` directory inside the project folder

The following commands assume that the files are in your `downloads` directory. If not, modify the code below to suite your needs
```sh
mkdir ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/lib
unzip ~/Downloads/selenium-java-YOUR_VERSION_NUMBER_HERE.zip -d ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/lib
mv ~/Downloads/selenium-server-standalone-YOUR_VERSION_NUMBER_HERE.jar ~/projects/NYU-Bobst-Library-Reservation-Automator-Java/lib
```

4.) Add your `lib` directory to your java BUILDPATH
This one is IDE dependent. The following image shows how to do so with [JetBrain's IntelliJ IDEA](https://www.jetbrains.com/idea/).

![Add the libraries](/img/dev/dev_1.png)

![Add the libraries](/img/dev/dev_2.png)

![Add the libraries](/img/dev/dev_3.png)

![Add the libraries](/img/dev/dev_4.png)

5.) Setup your artifact (`.jar`)build settings

![Setup artifact build settings](/img/dev/dev_5.png)

![Setup artifact build settings](/img/dev/dev_6.png)

![Setup artifact build settings](/img/dev/dev_7.png)

6.) Add the libraries you just added to the artifact build path

![Add the libraries](/img/dev/dev_8.png)

![Add the libraries](/img/dev/dev_9.png)

7.) Test your artifact (`.jar`) generation by building the file

![Build the artifact](/img/dev/dev_10.png)

![Build the artifact](/img/dev/dev_11.png)

If everything worked, then your `.jar` file that was generated should be at 
`~/projects/NYU-Bobst-Library-Reservation-Automator-Java/out/artifacts/NYU_Bobst_Library_Reservation_Automator_Java_jar/NYU-Bobst-Library-Reservation-Automator-Java.jar`

Whenever you're done making changes, and would like to test it out, simple regenerate your artifact.

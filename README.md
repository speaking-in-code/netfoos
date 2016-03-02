# Netfoos Tournament Director Automation #

### What is this repository for? ###

These tools automate some tournament director tasks, such as

1. Getting a table fo results for the most recent tournament.
1. Updating local points based on recent results.
1. Updating local base points based on IFP changes.

### How do I get set up? ###

Maven (https://maven.apache.org) is used for software development. Install
it if you don't have it already.

Next, create a file ~/.netfoosrc. This holds the username and password for
your netfoos account. Example:

        username: zx4387
        password: deadbar

Compile, test, and install the software:

        mvn install

To use the software to get tournament results:

        ./netfoos-localpoints

# Netfoos Tournament Director Automation #

### What is this repository for? ###

These tools automate some tournament director tasks, such as

1. Getting a table of results for the most recent tournament.
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

Compile, and install the software, no tests:

        mvn install -Dmaven.test.skip=true

To use the software to get tournament results:

        ./netfoos-localpoints

### Warning ###

Netfoos doesn't have an API, so this software uses HTML scraping (in some cases
with a running Chrome browser) to pull results.

Many of the tests are running against the real netfoos.com web site. When
you're modifying the code and tests, check carefully that you're not sending
too much traffic (e.g. an infinite loop) to their site.

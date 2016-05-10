# BrowserStack Local for Java

[![Build Status](https://travis-ci.org/browserstack/browserstack-local-java.svg?branch=master)](https://travis-ci.org/browserstack/browserstack-local-java)

Launches [BrowserStack Local](https://www.browserstack.com/local-testing) tunnels enabling access to local web servers and file folders from [BrowserStack](https://www.browserstack.com).


## Usage

``` java
BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher("<browserstack-accesskey>")
    .setLocalIdentifier("ci-build-1")      // Unique ID distinguishing multiple simultaneous local testing connections
    .setForce(true)                        // Kill other running BrowserStackLocal instances before launch
    .setOnlyAutomate(true)                 // Disable Live Testing and Screenshots, just test Automate
    .setForceLocal(true)                   // Route all traffic via local machine
    .setOnly("localhost,3000,0")           // Restrict Local Testing access to specified local servers and/or folders
    .setProxy("proxy.example.com", 3128, "username", "password")  // (optional) Network proxy used to access www.browserstack.com
    .start();
```

```
// Terminate BrowserStack Local
browserstackLocal.stop();
```

Apart from the key, all other BrowserStack Local modifiers are optional. For the full list of modifiers, refer [BrowserStack Local modifiers](https://www.browserstack.com/local-testing#modifiers). For examples, refer below -

## Contribute

### Compile Instructions

To compile the package, `mvn package`.

To run the test suite run, `mvn test`.

### Reporting bugs

You can submit bug reports either in the Github issue tracker.

Before submitting an issue please check if there is already an existing issue. If there is, please add any additional information give it a "+1" in the comments.

When submitting an issue please describe the issue clearly, including how to reproduce the bug, which situations it appears in, what you expect to happen, what actually happens, and what platform (operating system and version) you are using.

### Pull Requests

We love pull requests! We are very happy to work with you to get your changes merged in, however, please keep the following in mind.

* Adhere to the coding conventions you see in the surrounding code.
* Include tests, and make sure all tests pass.
* Before submitting a pull-request, clean up the git history by going over your commits and squashing together minor changes and fixes into the corresponding commits. You can do this using the interactive rebase command.

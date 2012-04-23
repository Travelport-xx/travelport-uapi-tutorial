# Getting Started With The TravelPort Universal API

### Unit 1, Lesson 1

### WSDL

Although these lessons are using Java, the lessons' concepts apply to pretty much any programming language.  The _interface_ to the Universal API (uAPI) is defined with [WSDL](http://en.wikipedia.org/wiki/Web_Services_Description_Language) or Web Services Definition Language, pronounced "whiz-dul".  This means that any programming language that knows how to "use web services" can access the APIs and get useful things done.  In practice, this means that you need a _generator_ that can take a file "foo.wsdl" and spit out "foo_client.java", or whatever, in your favorite language.  Different generators have slightly different behaviors (of course, WSDL is a "standard!"); we'll be using [Apache's CXF](http://cxf.apache.org/) to work with the uAPI in Java.  ("CXF" is not really an acronym in this case but is related to the project's origin.)

### Downloading the tutorial code

You can download the tutorial code using github to clone the repository. You can use your favorite [git tool](https://git.wiki.kernel.org/articles/i/n/t/Interfaces,_frontends,_and_tools.html) or just use the command line like this:

```bash
> mkdir learn-tport
> cd learn-tport
> git clone git@github.com:iansmith/travelport-uapi-tutorial.git

```


This last command will create the directory `travelport-uapi-tutorial` and within that directory you will find the a few files that we will discuss later, plus the directories `src` and `wsdl`.  If you explore the `wsdl` directory, you'll see many WSDL files as well as a number of [XML schemas](http://en.wikipedia.org/wiki/XSD), as `xsd` files.  All of these files have been supplied by TravelPort and can be downloaded as version 2.1.0.1 of the uAPI from [their developer site](http://developer.travelport.com) as well.

### Apache CXF

Apache's CXF is the critical "glue" for connecting TravelPort's web services to our tutorial code.  (There are plenty of other choices for interacting with web services in Java, but CXF is just a very popular one.)  You'll need to [download and install](http://cxf.apache.org/download.html) the Apache CXF code; this tutorial expects you to be using at least version 2.6.0.  If you are using [Eclipse](http://www.eclipse.org) like most Java developers, you can install the CXF directly into Eclipse by going to "Preferences > WebServices".  (If you don't have this preference option, you'll need to install the [Web Tools Platform](http://www.eclipse.org/webtools/) or WTP for Eclipse.)  The preferences screen where you add your installation of CXF should look something like this (with the preference choice highlight by the red box).  

<img src="images/preferences.png"/>

If you are not using eclipse, you'll need to be sure that the CXF jar files are [in your classpath](http://docs.oracle.com/javase/tutorial/essential/environment/paths.html) and that you can run the command 'cxf' from the command line.  Be aware that there many java libraries that are "subsumed" by CXF, so you'll end up with about 75 libraries in your classpath!

### Generating A Java Version Of The API

We need to generate java code from the WSDL and XSD files.  Let's start by generating the Java code for the "System" service.  In eclipse, you can do this by selecting the WSDL file `System.wsdl` in the directory `wsdl/system_v8_0` and then using the context-menu in eclipse to choose "Generate Client", as is shown here:

<img src="images/generate-client-menu.png"/>

When you select that option in eclipse, you'll be presented with a sequence of three dialog boxes connected by hitting the `Next` button.  The vast majority of these options are really not of much interest to us for this tutorial, but to show you exactly the options to choose you can verify your configuration against this set as these screenshots:

<img src="images/generate-client-dialog1.png"/>

<img src="images/generate-client-dialog2.png"/>

<img src="images/generate-client-dialog3.png"/>

#### Command Line Generation Of A Java Client

If you are using the command line, you can do manually what eclipse does behind the scenes based on the values given in these dialogs:  

```
````
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

### Apache CXF

Apache's CXF is the critical "glue" for connecting TravelPort's web services to our tutorial code.  (There are plenty of other choices using web services in Java, CXF is just a very popular one.)  You'll need to [download and install](http://cxf.apache.org/download.html) the Apache CXF code; this tutorial expects you to be using at least version 2.6.0.  If you are using [Eclipse](http://www.eclipse.org) like most Java developers, you can install the CXF directly into Eclipse by going to "Preferences > WebServices".  (If you don't have this preference option, you'll need to install the [Web Tools Platform](http://www.eclipse.org/webtools/) or WTP for Eclipse.)


### Generating A Java Version Of The API

This last command will create the directory `travelport-uapi-tutorial` and within that directory you will find the a few files that we will discuss later, plus the directories `src` and `wsdl`.  If you explore the `wsdl` directory, you'll see many WSDL files as well as a number of [XML schemas](http://en.wikipedia.org/wiki/XSD), as `xsd` files.  We need to generate java code from the WSDL files.  
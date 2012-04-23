# Getting Started With The TravelPort Universal API

## Unit 1, Lesson 1

### Objective Of Unit 1

After working through the three lessons of Unit 1, you should be able to work with the TravelPort Universal API to make requests for services and understand the responses.  You will become familiar with "web services" if you are not already.

### WSDL

Although these lessons are using Java, the lessons' concepts apply to pretty much any programming language.  The _interface_ to the Universal API (uAPI) is defined with [WSDL](http://en.wikipedia.org/wiki/Web_Services_Description_Language) or Web Services Definition Language, pronounced "whiz-dul".  This means that any programming language that knows how to "use web services" can access the APIs and get useful things done.  In practice, this means that you need a _generator_ that can take a file "foo.wsdl" and spit out "foo_client.java", or whatever, in your favorite language.  Different generators have slightly different behaviors (of course, WSDL is a "standard!"); we'll be using [Apache's CXF](http://cxf.apache.org/) to work with the uAPI in Java.  ("CXF" is not an acronym, but is related to the project's origin.)

### Downloading the tutorial code

You can download the tutorial code using github to clone the repository. You can use your favorite [git tool](https://git.wiki.kernel.org/articles/i/n/t/Interfaces,_frontends,_and_tools.html) or just use the command line like this:

```bash
> mkdir learn-tport
> cd learn-tport
> git clone git@github.com:iansmith/travelport-uapi-tutorial.git

```


This last command will create the directory `travelport-uapi-tutorial` and within that directory you will find the a few files that we will discuss later, plus the directories `src` and `wsdl`.  If you explore the `wsdl` directory, you'll see many WSDL files as well as a number of [XML schemas](http://en.wikipedia.org/wiki/XSD), as `xsd` files.  All of these files have been supplied by TravelPort and can be downloaded as version 2.1.0.1 of the uAPI from [their developer site](http://developer.travelport.com) as well.

### Apache CXF

Apache's CXF is the critical "glue" for connecting TravelPort's web services to our tutorial code.  (There are plenty of other choices for interacting with web services in Java, but CXF is just a very popular one.)  You'll need to [download and install](http://cxf.apache.org/download.html) the Apache CXF code; this tutorial expects you to be using at least version 2.6.0.  If you are using [Eclipse](http://www.eclipse.org) like most Java developers, you can install the CXF directly into Eclipse by going to "Preferences > WebServices".  (If you don't have this preference option, you'll need to install the [Web Tools Platform](http://www.eclipse.org/webtools/), or WTP, for Eclipse.)  The preferences screen where you add your installation of CXF should look something like this (with the preference choice highlight by the red box).  

<br/>
<img src="images/preferences.png"/>
<br/>

If you are not using eclipse, you'll need to be sure that the CXF jar files are [in your classpath](http://docs.oracle.com/javase/tutorial/essential/environment/paths.html) and that you can run the command 'cxf' from the command line.  Be aware that there many java libraries that are "subsumed" by CXF, so you'll end up with about 75 libraries in your classpath!

### Generating A Java Version Of The API

We need to generate java code from the WSDL and XSD files supplied by Travelport's uAPI.  Let's start by generating the Java code for the "System" service.  In eclipse, you can do this by selecting the WSDL file `System.wsdl` in the directory `wsdl/system_v8_0` and then using the context-menu in eclipse to choose "Generate Client", as is shown here:

<br/>
<img src="images/generate-client-menu.png"/>
<br/>

When you select that option in eclipse, you'll be presented with a sequence of three dialog boxes connected by hitting the `Next` button.  The vast majority of these options are really not of much interest to us for this tutorial, but to show you exactly the options to choose you can verify your configuration against this set as these screenshots:

<br/>
<img src="images/generate-client-dialog1.png"/>
<br/>
<img src="images/generate-client-dialog2.png"/>
<br/>
<img src="images/generate-client-dialog3.png"/>
<br/>

On the first screenshot in this sequence, the red box indicates a slider.  This slider can be moved to another position, non error position, if you are the type of person who doesn't like having "errors" on your screen.  The last dialog box in the sequence shows that there are number of checkboxes selected; these control various options in CXF's generation of the Java code.  The lower set of checkboxes that are checked (such as -Xts and -Xts:multiline) are not crucial to having the tutorial work properly, but do make debugging easier.

#### Command Line Generation Of A Java Client

If you are using the command line, you can do manually what eclipse does behind the scenes based on the values given in these dialogs. Below is a very complex command line generated by eclipse that you can start from, although you will need to adjust the paths to your local machine. 

```
wsdl2java -client -d /Users/iansmith/tport-workspace/uapijava/.cxftmp/src -classdir /Users/iansmith/tport-workspace/uapijava/build/classes -p http://www.travelport.com/service/system_v8_0=com.travelport.service.system_v8_0 -impl -validate -exsh true -dns true -dex true -autoNameResolution -xjc-Xts,-Xts:style:multiline,-Xlocator,-mark-generated -wsdlLocation http://localhost:8080/kestrel/ExternalCacheAccessService?wsdl -verbose -fe jaxws -db jaxb -wv 1.1 file:/Users/iansmith/tport-workspace/uapijava/wsdl/system_v8_0/System.wsdl
```

### The Java Client For "System"

If you use the WDT viewer for a WSDL file, you will see this image when you open `wsdl/system_v8_0/System.wsdl`:

<br/>
<img src="images/SystemService.png"/>
<br/>

(There are tabs at the bottom of the main editor view to control whether you view this file in "design view" or as a normal source code file.)  This shows you that there are two "Services" exposed by `System` WSDL: `ExternalCacheAccessService` and `SystemService`.  Often we will get a sloppy with the nomenclature and refer to the "system service" as any object that is accessible from objects _generated from the System.wsdl file_.  In this case, the real, concrete class `SystemService` is derived from `System.wsdl` so there is less confusion, but this can become more confusing with WSDL files (like `Air.wsdl`) that expose many "services" as a by-product of generating the code for the "Air service." Whew!
 
We will be working only with `SystemService` for the rest of this lesson.  You can see the java code that has been generated for this service in your project's src folder the class `com.travelport.service.system_v8_0.SystemService`.  Although you are welcome to read and explore the source, the good news is that you can *safely ignore* all the implementation details about this service.  This the beauty of WSDL!

Referring back to the diagram for `SystemService` above, you'll see that there are three "ports" exposed by the `SystemService`: `SystemInfoPortType`, `SystemPingPortType`, and the `SystemTimePortType`.  In the code for this lesson, we'll run a simple ping request through the `SystemPingPortType`.

### The Programming Model

The pattern used by the uAPI design is to expose a "port" which has a single method called, sadly, "service." Usually we refer to the port object just by its name without the prefix or suffix such as "the ping port."  (All the ports exposed from the file `System.wsdl` start with "System" and end with "PortType.")  The ping port's only method is "service," as you can see from the diagram; far more interesting is the fact the diagram shows you the input and output parameters are of type `PingReq` and `PingRsp` for the request and response respectively.  You can find the source code for these classes in the src Java folder with a name like `com.travelport.schema.system_v8_0.PingReq` or you can explore them with design view of `System.wsdl`.  Again, the details of the implementation are not important to you, you can just _use_ these facilities as part of the uAPI.

We now have the logical pieces necessary to understand how to use functionality exposed by the uAPI.  Let's thing about this sequence of actions concretely with the ping port:

1. Create an object of type `PingReq`
2. Fill in the necessary fields of the 'PingReq` using its "setter" methods
3. Create an instance of the `SystemService`
4. Access the `SystemService` object to get an object of type `SystemPingPortType`
5. Call the method `service` on the `SystemPingPortType` instance, passing the `PingReq` object created in step 1
6. Examine the results of our request by looking at the `PingRsp` object using its "getter" methods

With very few exceptions, all the features and functions of the uAPI follow this pattern of "build the request parameters and use the port object to get the results."

### Getting a copy of the tutorial code






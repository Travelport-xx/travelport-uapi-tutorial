#Getting Started With The TravelPort Universal API

###Unit 1, Lesson 1

### WSDL

Although these lessons are using Java the lessons concepts apply to pretty much any programming language.  The _interface_ to the Universal API (uAPI) is defined with WSDL or Web Services Definition Language, pronounced "whiz-dul".  This means that any programming language that knows how to "use web services" can access the APIs and get useful things done.  In practice, this means that you need a _generator_ that can take a file "foo.wsdl" and spit out "foo_client.java" or whatever your favorite language.  Different generators have slightly different behaviors (of course, WSDL is a "standard!"); we'll be using [Apache's CXF](http://cxf.apache.org/) to work with the uAPI in Java.  ("CXF" is not really an acronym in this case.)


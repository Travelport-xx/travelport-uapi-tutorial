#Getting Started With The TravelPort Universal API

###Unit 1, Lesson 1

##Set-up, understanding the libraries, and Ping!


Downloading the code

So, you want to write an application that uses the Travelport Universal API, the uApi?
To paraphrase Dennis Richie and Brian Kernighan, “the only way to learn a new API is to write programs with it.”

Let’s get started building the equivalent of “Hello, world.” using the uApi.
```bash
$ curl http://developer.travelport.com/XXX_TBD_.zip
$ unzip XXX_TBD_.zip
```

The commands above download the zip file for all the lessons in this series, and unpacks that zip into the current directory. You don’t need to use these commands specifically if you prefer to use other ways to download and unpack web content.

After you have unpacked the zip file, you should go into the freshly-created directory `lesson1/src/lntport`. (In these lessons we use the unix-style forward slashes as the directory separator; on windows you should use backslash, like this \\.)

Inside lntport (“learn travelport!”) you’ll see the directories home, transport, and util as well as an empty file __init__.py.
home contains models and views for use later when we combine the uApi with Facebook.
transport Contains code to connect to and exchange XML documents with the uApi.
util Contains a copy of the Facebook App and miscellaneous utility code.

Python needed for the lessons, not for uApi!

If you noticed the .py on the end of the __init__.py file mentioned above, you will have realized that the examples in these lessons will be written in Python. Although you will need to install a python interpreter (http://python.org) that is version 2.7 (not version 3.0) to run the code in these lessons, none of the concepts in these lessons are specific to Python.

The uApi is agnostic about what programming language you develop in.

We’ve used Python for this tutorial because there is a convenient way to interact with Facebook via Python and because it makes the examples quite brief.
Credentials

If you don’t have them already, you’ll need to request a set of 30-day trial credentials for using the uAPI. You can request these on http://www.travelportdeveloperdirectory.com/forms/request-test-access.php. However, this web page cannot be navigated to via the menus on http://developer.travelport.com/app/developer-network/resource-centre-uapi unless you have already created an account on the website http://developer.travelport.com. When you request test credentials, you should choose “Galileo” as your preferred GDS.
After you have filled out the form, in a few minutes you’ll receive some email from “webmaster@travelport.com” with the relevant information.
Starting up

Let’s create a python file for this lesson and call it lesson1.py:
##
## boilerplate to allow you to run "special" code only in the case where this
## is the entry point of a program... "pass" in the python function that does
## nothing.
##
if __name__ == '__main__':
    pass

##
## This imports the name "travelport_proxy" into our code so we can access 
## functions on that object.  This code is in
## src/lntport/transport/travelport_proxy.py if you are curious.
##
import transport.travelport_proxy

This is as good a time as any to work out your PYTHONPATH. If you are using a development environment, such as Eclipse, you need to add configuration settings so that python can find the source code inside your project. You should add the src directory of the project to the set of places searched. If you are using the command-line version of python (usually just called “python”), you will need to set the environment variable PYTHONPATH to the full directory path of the src directory in this project.

You will probably still have some imports that cannot be resolved in the project as a whole. To make these go away, follow the instructions for installing “Django 1.3” (https://www.djangoproject.com/) and adding it to your PYTHONPATH. You’ll also need to install “lxml” (http://lxml.de/installation.html). The former is a toolkit for processing web requests–something we will need to handle facebook integration later on–and the latter is a lightning-fast xml processor…. that we’ll use today!
Credentials in the code

Although it’s better to not put credentials directly in the code, especially if you use a source code control system, we are going to break the rule here in the name of simplicity:
##
## Per user configuration.  You typically receive this information in your 
## welcome message when you sign up for test credentials with 
## developer.travelport.com  (requires login to website)
##
host = 'emea.universal-api.travelport.com'
url = 'https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/'
username='Universal API/uAPI0000000000-00000000'
password = 'XXX'
gds_provider = '1P'
target_branch = 'P100000'

Obviously, you should supply the appropriate credentials that you received in email as well as adjusting the host and url to the appropriate values for your location. Getting these latter two values correct improves the performance of the uApi, since it avoids the need to send bits across oceans!
See the uApi documentation for “Accessing Universal API Services” for a complete list of URLs that includes Americas, Asia-Pacific, and the test and production URLs.
Services

Be careful that you terminate the url with a slash because internally the code we have supplied will append the name of the “service” to that URL when it actually makes a request over the network.

A service (or, more precisely “service endpoint”) in the terms of the uApi is a URL that starts as above plus has the suffix for the service that implements the call. For example, https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirService is the needed service URL when searching for air fares.
Ping

The uApi’s System service has a method called ping that simply tests that the connectivity has been correctly established between your computer and the Travelport system. Here is the code you need to test your connectivity:
##
## Create the proxy that turns python objects into travelport API calls
## and vice versa.
##
proxy = transport.travelport_proxy.TravelportProxy(url,host,username,password,\
                                                    target_branch,gds_provider)


## make a ping request
pingResult = proxy.ping('this is a test of the emergency ping system')
if pingResult == None:
    print "Ping was successful!"
else:
    print "Ping error:"+pingResult

The Role Of The Proxy, Marshalling/Unmarshalling XML

The proxy object created in this bit of code is “bound” to the credentials you have supplied. The proxy’s primary function is turn method calls on itself into XML documents that are sent to the uAPI in the appropriate format. The proxy does the reverse for returned results from the API. You can see this in action with the call proxy.ping() where we supply a sample string–you can use anything for this. The proxy will create the necessary XML for a ping request wrapped around the supplied “payload” (see the ping documentation on http://developer.travelport.com for more on the parts of the ping request). When the result is returned from uAPI, the proxy checks that the correct payload was “pinged back” and it returns None (a python constant similar to null in other languages) on success. If there was an error, the proxy returns an error string to explain the problem.
It should be clear from this example that the proxy hides the complexity of the XML input and output to and from uApi in method call arguments and return values. This “hiding” can be handled in any programming language; most modern languages have the ability to marshal (“pack”) and unmarshal (“unpack”) XML documents to and from a set of variables, a dictionary or a struct as is appropriate for the language.
Running

You should now be able to run the whole program from your development environment (the “Run” menu in Eclipse, for example) or the command line. From the command line, you can invoke the python interpreter on this source code like this:
$ python lesson1.py

and you should see the output:
Ping was successful!

If you see something starting with “Ping Error” you have a problem with your connectivity–usually it’s a problem with the credentials when ping fails.

In the next lesson, we will discuss how to make more interesting request, such as searching for airfares.
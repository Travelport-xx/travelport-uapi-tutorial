---
layout: page
title: "Building a travel app for Facebook"
tagline:
description :
---
{% include JB/setup %}

## Unit 3, Lesson 7

### Objective Of Lesson 7

In Unit 3 we'll be building a Facebook app that uses the Travelport uAPI to search for travel.

Since it is embedded in Facebook, the travel the application will be using social features such as allow the user to go and visit friends on their birthday.

With that as the goal in the back of your mind, you should be aware that this lesson is neither about Facebook nor Travelport's uAPI!  Facebook's "API" must be integrated into an application such as the one we are developing, with the application running on its own server --- and the server must be on the public internet.  Most of the readers of this tutorial are probably behind firewalls, or do not have the ability to launch new internet-visible, server processes from their work or home machine.  

We need to get a Java program running on the public internet that can _receive_ web requests or be a "server" in internet-speak.  Until this point, we have used Java and WSDL as a _client_ of the Travelport servers that implement the the uAPI.

When completed, Facebook will be making calls as client of our server program.  Although our server is running on the internet _somewhere_, users who interact with our application will "see it" as being seamlessly inside Facebook. Our application will have a URL that begins with `http://apps.facebook.com`, for example.

### The plan

Because this lesson has a number of fairly complex moving parts, it's important to keep in mind the overall design we have in mind:

* We'll write our code in Java to handle web requests coming from Facebook.  Our responses to these web requests determine, if indirectly, what a Facebook user sees when using the application inside Facebook.

* The [Facebook API](http://developers.facebook.com) is not defined using WSDL like the uAPI.  The Facebook API uses a different mechanism called [REST](http://en.wikipedia.org/wiki/REST).  We'll be using the [Play Framework](http://www.playframework.org/) to do the heavy-lifting at the Web and REST levels.

* Since Facebook applications need to be on the public internet, we are going to use an internet service called [Heroku](http://www.heroku.com) to host our Java application.  Heroku is free when the usage of your application is light, so you should be able to the do the tutorial without incurring any costs.
>>> Do we need this? Anyone who wants to set up their FB app will already have a server somewhere, or at least a plan.

* Facebook makes use of a protocol called [OAuth](http://en.wikipedia.org/wiki/OAuth) to allow the user to control what information our application can receive about said user.  [This protocol](http://oauth.net/) depends crucially on cryptography. Thus, we'll need to make use of some of the [deeper layers](http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html) of the Java platform.  These can be ignored by most tutorial readers because these tend to "just work or they don't"; they are intended to not be "debuggable."

### Notes on protocols

For the truly interested in "how it works:"

* After generating the client code using a WSDL to Java compiler from Apache's CXF project, our uAPI code in lessons 1 to 6 used the [SOAP](http://en.wikipedia.org/wiki/SOAP) encoding on top of [XML](http://en.wikipedia.org/wiki/XML) being sent over [HTTP](http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol), the standard protocol for the web.  Since the URLs in use by Travelport are secured using [HTTPS](http://en.wikipedia.org/wiki/HTTPS), eavesdropping is prevented; there is a modicum of security from forgery in that a special header is included at the HTTP-level that includes our Travelport username and password.

* For this lesson and the following two, our code is using [Play Framework](http://en.wikipedia.org/wiki/Play_Framework) to interpret requests and responses at the HTTP level (same as above) as needed to interact with Facebook.  REST depends only the HTTP level and is a set of conventions about how to _use_ the HTTP protocol, not a protocol itself.  Play will be responsible for handling the "mechanics" of the REST/HTTP protocol, but it is our responsibility to implement the "semantics" of what we want to achieve in Java.  Facebook's use of OAUTH means that the user must give consent before their data is accessed and must be informed what information is to be released to our application.  Further, no other application may "impersonate" our application and receive data that was intended for our application nor can other parties eavesdrop on our communication as Facebook also communicates with others via HTTPS.

By way of comparison, both applications use HTTP as the means of transmitting a set of bytes from one point to another and use URLs as the way to identify with whom one wants to communicate.  The uAPI strategy of using WSDL, SOAP, and XML (as well as explicit versioning which we didn't mention before) is much safer than the relatively "loose" standard of REST which enforces nothing--it's just a set of conventions.   The advantage of REST is the comparative simplicity and ease of implementation compared to the relatively "heavyweight" process of using a WSDL-based approach.  Both systems offer reasonable protection from eavesdropping third-parties because they are built on top of HTTPS;  Facebook is probably marginally more secure as it prevents impersonation since it requires that messages be "signed" in a way that cannot be forged (although the true risk of this certainly very small).



### Play

The Play Framework is a popular Java f

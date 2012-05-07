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

Since the app is to be embedded in Facebook, the application merge social features with travel with results such as allowing the user to go and visit a friend on his or her birthday.

With that as the goal in the back of your mind, you should be aware that this lesson is neither about Facebook nor Travelport's uAPI!  Facebook's "API" must be integrated into an application such as the one we are developing, with the application running on its own server --- and the server must be on the public internet.  Most of the readers of this tutorial are probably behind firewalls, or do not have the ability to launch new, internet-visible, server processes from their work or home machine.  

We need to get a Java program running on the public internet that can _receive_ web requests or be a "server" in internet-speak.  Until this point in the tutorial, we have used Java plus WSDL as a _client_ of the Travelport _servers_ that implement the the uAPI.

When completed, Facebook will be making calls as client of our server program.  Although our server is running on the internet _somewhere_, users who interact with our application will "see it" as being seamlessly inside Facebook. Our application will have a URL that begins with `http://apps.facebook.com`, for example.  Simultaneously, our application will be a client of the uAPI, as we have done in previous lessons.

### The plan

Because this lesson has a number of fairly complex moving parts, it's important to keep in mind the overall design we have in mind:

We'll write our code in Java to handle web requests coming from Facebook.  Our responses to these web requests determine, if indirectly, what a Facebook user sees when using the application inside Facebook.

The [Facebook API](http://developers.facebook.com) is not defined using WSDL like the uAPI.  The Facebook API uses a different mechanism called [REST](http://en.wikipedia.org/wiki/REST).  We'll be using the [Play Framework](http://www.playframework.org/) to do the heavy-lifting at the Web and REST levels.

Since Facebook applications need to be on the public internet, we are going to use an internet service called [Heroku](http://www.heroku.com) to host our Java application.  Heroku is free when the usage of your application is light, so you should be able to the do the tutorial without incurring any costs.

Facebook makes use of a protocol called [OAuth](http://en.wikipedia.org/wiki/OAuth) to allow the user to control what information our application can receive about said user.  [This protocol](http://oauth.net/) depends crucially on cryptography. Thus, we'll need to make use of some of the [deeper layers](http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html) of the Java platform.  These can be ignored by most tutorial readers because these tend to "just work or they don't"; they are intended to _not_ be "debuggable."

### Notes on protocols

For the truly interested in "how it works:"

After generating the client code using a WSDL to Java compiler from Apache's CXF project, our uAPI code in lessons 1 to 6 used the [SOAP](http://en.wikipedia.org/wiki/SOAP) encoding on top of [XML](http://en.wikipedia.org/wiki/XML) being sent over [HTTP](http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol), the standard protocol for the web.  Since the URLs in use by Travelport are secured using [HTTPS](http://en.wikipedia.org/wiki/HTTPS), eavesdropping is prevented; there is a modicum of security from forgery in that a special header is included at the HTTP-level that includes our Travelport username and password.

For this lesson and the following two, our code is using [Play Framework](http://en.wikipedia.org/wiki/Play_Framework) to interpret requests and responses at the HTTP level (same as above) as needed to interact with Facebook.  REST depends only the HTTP level and is a set of conventions about how to _use_ the HTTP protocol, not a protocol itself.  Play will be responsible for handling the "mechanics" of the REST/HTTP protocol, but it is our responsibility to implement the "semantics" of what we want to achieve in Java.  Facebook's use of OAUTH means that the user must give consent before their data is accessed and must be informed what information is to be released to our application.  Further, no other application may "impersonate" our application and receive data that was intended for our application nor can other parties eavesdrop on our communication as Facebook also communicates with others via HTTPS.

By way of comparison, both applications use HTTP as the means of transmitting a set of bytes from one point to another and use URLs as the way to identify with whom one wants to communicate.  The uAPI strategy of using WSDL, SOAP, and XML (as well as explicit versioning which we didn't mention before) is much safer than the relatively "loose" standard of REST which enforces nothing--it's just a set of conventions.   The advantage of REST is the comparative simplicity and ease of implementation compared to the relatively "heavyweight" process of using a WSDL-based approach.  Both systems offer reasonable protection from eavesdropping third-parties because they are built on top of HTTPS;  Facebook is probably marginally more secure as it prevents impersonation since it requires that messages be "signed" in a way that cannot be forged (although the true risk of this type of attack is certainly very small).

### Play

The Play Framework is a popular Java framework for developing web applications. In fact, Play itself is written in [Scala](http://en.wikipedia.org/wiki/Scala_programming_language) but because Scala is compatible with Java at the binary level, it works for Java as well.  Let's start by getting play installed:  [Download play version 2.0](http://www.playframework.org/download) and follow the installation instructions.  When you have done this successfully, you should be able to the command play from the command line like this:

{% highlight console %}

$ play help
       _            _ 
 _ __ | | __ _ _  _| |
| '_ \| |/ _' | || |_|
|  __/|_|\____|\__ (_)
|_|            |__/ 
             
play! 2.0, http://www.playframework.org

Welcome to Play 2.0!

These commands are available:
-----------------------------
license            Display licensing informations.
new [directory]    Create a new Play application in the specified directory.

You can also browse the complete documentation at http://www.playframework.org.

{% endhighlight  %}

Just to be fully sure play is installed properly, let's go ahead and create a bogus project called _deleteme_ :


{% highlight console %}

$ play new unit3
 _ __ | | __ _ _  _| |
| '_ \| |/ _' | || |_|
|  __/|_|\____|\__ (_)
|_|            |__/ 
             
play! 2.0, http://www.playframework.org

The new application will be created in /Users/iansmith/tmp.unit3/unit3

What is the application name? 
> deleteme

Which template do you want to use for this new application? 

  1 - Create a simple Scala application
  2 - Create a simple Java application
  3 - Create an empty project

> 2

OK, application deleteme is created.

Have fun!

{% endhighlight %}

#### An Aside About Git

[Git](http://en.wikipedia.org/wiki/Git_software) is a distributed version control system, originally developed by [Linus Torvalds](http://en.wikipedia.org/wiki/Linus_Torvalds) for the Linux kernel.  We are assuming that most developers are familiar with git or at least will be able to "translate" the simple git commands in this lesson, typically just "git commit" and "git push", into some other version control system they are familiar with.  To learn about git, the best tool is probably the online [git community book](http://git-scm.com/book).

However, you will need a copy of git installed on your machine because it is used to trigger the build procedure on a remote machines.  Git will be installed by the next step in this lesson.  Like most version control systems git has a mechanism to trigger another program when a "commit" is submitted by a developer.  As we shall see, in git's case this ability is exploited by Heroku...

### Heroku

[Heroku](http://www.heroku.com) is an application hosting service, but at the _programming language_ level, not at the container level (like web application servers) nor at the operating system level.  Heroku itself is built using [Amazon's EC2](http://aws.amazon.com/ec2/) which provides the operating system level "hosting" (although EC2 is invisible to us in this unit).  Heroku can be thought, quite rightly, of as a layer on top of EC2.

#### Sign-Up

You'll need an [create an account on heroku](https://api.heroku.com/signup) if you don't have one already; these can be created without charge. You'll need to provide your email address during this process; from here on we'll use the notation `youraddress@example.com` to indicate a place where you should supply your email address.

#### Heroku Toolbelt

In terms of your local workstation, the first step in heroku development is to install the [Heroku Toolbelt](https://toolbelt.herokuapp.com/) as appropriate for your operating system.  This installs the necessary tools so you can interact with heroku from the command line.

>>>> Does this really install git also?


#### Match Your Credentials To Your Email

Then you need to set up your heroku credentials, including an [SSL](http://en.wikipedia.org/wiki/Secure_Socket_Layer) [private key](http://en.wikipedia.org/wiki/Private_key).  If you are not familiar with this process, just allow heroku to create a new key for you and you can ignore this issue in the future.

{% highlight console %}

$ heroku login
Enter your Heroku credentials.
Email: youraddress@example.com
Password:
Could not find an existing public key.
Would you like to generate one? [Yn]
Generating new SSH public key.
Uploading ssh public key /Users/you/.ssh/id_rsa.pub

{% endhighlight %}

#### Create The Application "Space" On Heroku

A [stack](https://devcenter.heroku.com/articles/stack) in Heroku represents the machinery needed to run an application.  The current stack for running Java applications is called "cedar".  (Cedar because C is the third level of the alphabet; the two prior stacks were Aspen and Bamboo.)  You should create this stack while inside the directory containing the code for lesson 7, which is prepared for heroku and play development.  In the next listing this is represented by `path/to/tutorial`:

{% highlight console %}

$ cd path/to/tutorial/src/com/travelport/uapi/unit3/lesson7
$ heroku apps:create --buildpack git@github.com:iansmith/heroku-buildpack-scala.git --stack cedar
Creating furious-ocean-1011... done, stack is cedar

{% endhighlight %}

The last part will vary for each user that does the tutorial; in our example here, heroku has chosen "furious-ocean-1011" as the application name for us. These are always two short words plus a number, but yours will be unique.  You can get the details about your application like this. We'll use "furious-ocean-1011" in this text but you should use your application name.

{% highlight console %}

$ heroku apps:info --app furious-ocean-1011
=== furious-ocean-1011
Domain Name:   furious-ocean-1011.herokuapp.com
Git URL:       git@heroku.com:furious-ocean-1011.git
Owner:         joesmith@example.com
Stack:         cedar
Web URL:       http://furious-ocean-1011.herokuapp.com/

{% endhighlight %}

It's worth noting what has just happened here! Your application has been assigned to a server and can be reached on the public internet with the URL http://furious-ocean-1011.herokuapp.com/.  This is going to be great fun!

### Register 'furious-ocean-1011' With Facebook

By going to the Facebook Developer Programe [Apps Tab](https://developers.facebook.com/apps/) you'll be presented with a list of all your existing facebook apps.  Whether you have done one before or not, you can use the "Create New App" in the upper right corner of the page to create a new app registration with Facebook.

     Be sure to *NOT* check the box on this dialog related to Heroku!

<br/>
<img src="images/fb-create.png">
<br/>

The reason to not check the box related to Heroku is because this feature of Facebook will force you to use languages other than Java, such as Python, PHP, or Ruby. (When Java becomes supporting via this method, it will of course be better than setting up Heroku by hand as we have done above.)  Although it is not required that your application name and application namespace be constructed as we have done in this screenshot, this is how we will assume you have done this in the rest of this lesson.

The following screen will now appear so you can hook your application to [Canvas Pages](http://developers.facebook.com/docs/guides/canvas/) in Facebook.  Fill in the fields as shown below:

<br/>
<img src="images/fb-config.png">
<br/>
 
The circled part of the screenshot above is the URL where your application will be visible to users; this URL is "inside" facebook but all the "code" will be hosted externally on your Java application running on Heroku.

### Security Dialogs

Finally, you'll need to configure your [authentication dialog](https://developers.facebook.com/docs/opengraph/authentication/) for facebook.  You click on the "Auth Dialog" in the upper left of your app settings to edit this. You can ignore most of these  settings unless you feel obligated or interested in setting them.  The most critical settings are at the bottom of the page and these are shown here:

<br/>
<img src="images/fb-auth-settings.png">
<br/>

These authentication settings are the ones we'll need in later lessons for to get access to the necessary data to mix social networking and travel.  This would probably be a good time to click on the links "Preview Current Dialog" and "Preview Referral Dialog" so you can see how users will perceive your application.

### Telling Your Facebook Configuration To Your App

At the top of the screenshot above about how to tell Facebook about your application name there are two "blacked out" parts of screenshot.  These are two bits of information that you need to "install" into your application so it knows how to correctly respond to requests sent by Facebook.  It also needs to know the last part of the circled URL in the middle of that screenshot.  You can do this by using heroku on the command line.  This process sets environment variables for your application when it runs on Heroku.  Again you need to be running in the "lesson7" directory inside your copy of the tutorial code:


{% highlight console %}

$ heroku config:add FBSECRET="xxxxxxxxxxxxxxxxxxxxxxxx" --app furious-ocean-1011
Adding config vars and restarting app... done, v3
  FBSECRET => xxxxxxxxxxxxxxxxxxxxxxxx
$ heroku config:add FBAPPNAMESPACE="furious_ocean/" --app furious-ocean-1011
Adding config vars and restarting app... done, v4
  FBAPPNAMESPACE => furious_ocean
$ heroku config:add FBAPPID="xxxxxxxxxxxxxxxxxxxxxxxx" --app furious-ocean-1011
Adding config vars and restarting app... done, v5
  FBAPPID => xxxxxxxxxxxxxxxxxxxxxxxx

{% endhighlight %}

   Note that the `FBAPPNAMESPACE` should end with a '/' (slash) character after your app name.



----------------------

Proceed to Unit 3, Lesson 8

[Table of Contents](index.html)

{% include JB/comments %}

{% include JB/analytics %}







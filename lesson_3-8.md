---
layout: page
title: "Doing Travel-Related Requests From Inside A Facebook App"
tagline: Using the uAPI in the internals of an application whose interface is viewed through Facebook.
description :
---
{% include JB/setup %}

## Unit 3, Lesson 8

### Objective of Lesson 8

In this lesson, we'll use the TravelPort uAPI in ways that should be familiar to you from the previous units.  However,
we are going to do this _inside_ our facebook application using the data from the end-user's facebook account.

### Set-up

Since we covered this in some detail in the [last lesson](lesson_3-7.html) we will not spend much time on it here. We need 
to duplicate our git setup for lesson 8, just as we did in lesson 7.  

{% highlight console %}
$ # make sure you are in the lesson 8 directory
$ # if you need to add this back into the .gitignore, do so now-- most people won't need it
$ nano ../../../../../../..//uapijava/.gitignore
... add the line src/com/travelport/uapi/unit3/lesson* to this file ...
$ # confirm that it is on the end 
$ git
src/com/travelport/uapi/unit3/lesson*
$ # tell git to put a temporary repository somewhere else... can be almost anywhere EXCEPT the source tree
$ git init 
$ git add -A
$ # don't forget to change this to your application name...
$ git remote add heroku git@heroku.com:furious-ocean-1011.git
$ # confirm that this did what we wanted
$ git remote -v
heroku	git@heroku.com:furious-ocean-1011.git (fetch)
heroku	git@heroku.com:furious-ocean-1011.git (push)
$ # get an new eclipse project
$ play eclipsify
$ # use File > Import > General > Existing Projects into Workspace in eclipse
{% endhighlight %}

We can now begin looking at what is happening in lesson 8.



----------------------

[< Return to Unit 3, Lesson 7](lesson_3-7.html) | [Proceed to Unit 3, Lesson 9 >](lesson_3-9.html)

[Table of Contents](index.html)
<hr>

{% include JB/comments %}

{% include JB/analytics %}


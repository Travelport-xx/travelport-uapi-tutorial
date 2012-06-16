---
layout: page
title: "Better mapping integration in Facebook"
tagline: Making a more interactive Facebook API with uAPI.
description :
---
{% include JB/setup %}

## Unit 3, Lesson 9


### Objective of Lesson 9

Lesson 8 was the steak, Lesson 9 is the sizzle.

In Lesson 9 we show how to build an application that is more interactive than the one in Lesson 8.

The key travel-related topics in this lesson are being more clever with the way you handle the UCode data, and making sure to keep the user interface responsive to the user, even if you are doing more complex calculations in the background.

### More complex uses of UCode

In the [last lesson](lesson_3-8.html) we covered why the UCode data is necessary for dealing with place names.

Last time we used the data to accomplish a relatively simple task: given a place name, compute the nearest airport.  The process is roughly this:

{% highlight console %}
for each airport in largerUnit
  compute distance to airport

return smallest distance found
{% endhighlight %}

"largerUnit" here is either the country name or the state name. This query ends up computing the distance to all the airports in the larger unit, but because the number of airports is relatively small (typically tens, not hundreds) this is manageable.  This approach will clearly not work with all the train stations in densely-populated european countries with dense train networks.

#### Queries useful for rail

The code for Lesson 9 in `Lesson9.java` allows you to pass in a query to find the nearest "terminal" to your desired location.

For rail terminals, the `st_type` is 2, as opposed to 1 for airports.  In lesson ? we repeatedly try to find a train station for a place with the following queries, in this order:

* Find a train station which has a "city" field that starts with the name of the city we are working with.  This query helps with differences in naming cities like "Perth" versus "Perth South."

* Find a train station which has a "name" field that starts with with the name of the city we are working with.  This allows to find "Huddersfield Central" when searching for a train station for the city "Huddersfield", even though Huddersfield North is not labeled as being in the "city" of Huddersfield.  If it were labelled as being in the same city, the first query would have found it.

* Find a train station whose name contains the name of the city we are looking for.  This query allows us to find stations named "Central Huddersfield" but has a precision problem compared to the previous two queries.  It matches "South Huddersfield"  and "Upper Huddersfield St." as well.

* If the place that is being searched has a "state" field (United States, Canada) then search all the train stations within that state.  In many parts of the United States and Canada, the train stations are very widely dispersed and so this allows _something_ to be found for almost any city in the US or Canada.

### Setting up Lesson 9

<br/>
<img src="images/location-fb-setting.png">
<br/>

As in [lesson 8](lesson_3-8.html), you'll need to adjust the URL of your Facebook application to point to a different URL running on our heroku system.  This url is `/visit/start` in your heroku application.  You should probably use this as an opportunity to check your heroku settings for your Travelport configuration as we explained previously.

When you go to your Facebook application you'll see a full screen map application and icons of your friends will "drop in from above" as they are loaded from Facebook.  

<br/>
<img src="images/screencap-lesson9.png">
<br/>

#### Seeing UCode results

You can see the results of the UCode queries by using right-click or control-click (Windows or Macintosh) on a friend's icon.  When you do this, the view on the map will zoom to include the UCode results.  Airports will be marked with red markers and train stations with blue markers.  Here is a screen capture for a friend based in the french city of Rennes.  You can see the four nearby regional airports as well as the central train station in Rennes.

<br/>
<img src="images/screencap-rennes.png">
<br/>

By hovering over a marker in the Facebook application, you can see the distance from the terminal to the friend's location.


### Exercises for the reader

[< Return to Unit 3, Lesson 8](lesson_3-8.html) 

[Table of Contents](index.html)
<hr>

{% include JB/comments %}

{% include JB/analytics %}


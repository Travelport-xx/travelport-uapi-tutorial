---
layout: page
title: Welcome
tagline: Making it easy to build travel applications
---
{% include JB/setup %}

## The goal of this tutorial

Travelport Universal API is the world's first global GDS API to aggregate content from multiple sources including GDS, low cost-carriers and high speed rail operators.

For developers, it means an end to managing and maintaining multiple APIs. You'll have less code to write and more time to spend on your other responsibilities.

This tutorial helps developers understanding how to use the uAPI step by step, from connecting to the uAPI to coding a cool Facebook travel application. 

<p align="center">
<br/>
<img src="images/TP-facebook-app.jpg"/>
<br/>
</p>

## Table of contents

The tutorial is broken into three sections, or "units", each of which has a separate objective; each unit is broken into three "lessons" that take you part of the way towards the goal of the unit.

### Unit 1

The first unit will teach you how to setup and configure the Travelport Universal Api (uAPI) and how to make some basic requests through that API.  With this section complete, you can ask Travelport for information about rail and air travel, such as schedules and availability, and of course get the prices associated with that travel.

Lesson 1: [Setting up to work with the Travelport Universal API](lesson_1-1.html)
	
Lesson 2: [Basic Air Travel Requests](lesson_1-2.html)
	
Lesson 3: [Advanced Travel Shopping](lesson_1-3.html)
	

### Unit 2

The second lesson is about completing a booking for a passenger, using city and geocodes to refine search options, and finding accommodation in an area. With this section complete, you can build a complete flight and hotel booking application.

Lesson 4: [Hotel search](lesson_2-4.html)

Lesson 5: [uCode library and geocode mapping]
	
Lesson 6: [Completing a booking]


### Unit 3

The final unit will help you build a working [Facebook](http://www.facebook.com) application that uses the uAPI to determine things like, "How much would it cost for me to visit my friend Joe on his birthday next month?".

Lesson 7: [Building a travel app for Facebook](lesson_3-7.html)
	
Lesson 8: [Using the Facebook social graph (interacting with friends)]
	
Lesson 9: [Drawing maps in the Facebook app]


## Get started

Proceed to Unit 1, Lesson 1: [Setting up to work with the Travelport Universal API >](lesson_1-1.html)

<!-- 
## Blog Posts

<ul class="posts">
  {% for post in site.posts %}
    <li><span>{{ post.date | date_to_string }}</span> &raquo; <a href="{{ BASE_PATH }}{{ post.url }}">{{ post.title }}</a></li>
  {% endfor %}
</ul>

## This Website

This website is part of a larger system, [GitHub](http://www.github.com), that allows to make your own copy of this website and the tutorial code, raise issues or comment about the code or documentation, make your own changes and have the "pulled" into this tutorial by the authors, and read the work of many others who are using the site.

-->



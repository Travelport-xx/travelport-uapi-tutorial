---
layout: page
title: The TravelPort uAPI Tutorial
tagline: Making it easy to build travel applications
---
{% include JB/setup %}

## The Goals Of The Tutorial

Travelport Universal API is the world's first global GDS API to aggregate content from multiple sources including GDS, low cost-carriers and high speed rail operators.

For developers, it means an end to managing and maintaining multiple APIs. You'll have less code to write and more time to spend on your other responsibilities.

This Tutorial takes developers step by step, from connecting to the uAPI to coding a real Facebook travel application. 

[IMG GOES HERE]


## Chapters

The tutorial is broken into three sections, or "units", each of which has a separate objective; each unit is broken into three "lessons" that take you part of the way towards the goal of the unit.

* Unit 1

The first unit will teach you how to setup and configure the Travelport Universal Api (uAPI) and how to make some basic requests through that API.  With this section complete, you can ask Travelport for information about rail and air travel--such as schedules and availability--and of course get the prices associated with that travel.

* Unit 2

The second lesson is about the common "agency workflows" or actions that travel agents have to perform regularly.  These include creating a booking for a passenger, finding hotel accommodation in an area, and manipulating the agents queue of tasks to perform.

* Unit 3

The final unit will help you build a working [Facebook](http://www.facebook.com) application that uses the uAPI to determine things like, "How much would it cost for me to visit my friend Joe on his birthday next month?".

## Blog Posts

<ul class="posts">
  {% for post in site.posts %}
    <li><span>{{ post.date | date_to_string }}</span> &raquo; <a href="{{ BASE_PATH }}{{ post.url }}">{{ post.title }}</a></li>
  {% endfor %}
</ul>

## This Website

This website is part of a larger system, [GitHub](http://www.github.com), that allows to make your own copy of this website and the tutorial code, raise issues or comment about the code or documentation, make your own changes and have the "pulled" into this tutorial by the authors, and read the work of many others who are using the site.




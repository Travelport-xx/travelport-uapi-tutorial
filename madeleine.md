---
layout: page
title: Madeleine
tagline: Real FB App
---
{% include JB/setup %}

### Needed improvements for FB App

App currently code named [Madeliene](http://en.wikipedia.org/wiki/Col_de_la_Madeleine).

To build a real application at least these improvements must be delivered:

* Popup window between picking a friend and launching a search.  This should allow input of a few travel details:
    
	* One-Way/Round Trip
	* Date(s) of travel
	* Number of adults, number of children

* A "semi-booking" ability to submit an itinerary that ends up in the travel agents queue for ticketing and dealing with payment.  This should probably be connected automatically to some back-end thing that keeps track of facebook user ids for previous customers.

* Improved geographic knowledge  
 
	* For big countries like China, Brazil, and Australia need province/state names.
	* Better England county handling--we don't handle Scotland at all and there are many "county names" for England.
	* List of city names globally might be good, just as a deep backup.  Sergio has this data.
	* Maybe a few alternate names for a few countries to deal with politics, e.g. "Korea" and "Taiwan"

* Ability for either customer or agent to post on the user's timeline when booking travel.  Maybe when the travel agent confirms the purchase?

* Need some of the fancy filtering of results---filter by airline, filter by air vs. rail

* Need to handle events that change the size of the window and re-position the overlay in bottom right.

* Need ability to change origin based on friends... to have somebody other than the current user be "origin".

* Daniele had a number of good improvements, such as moving results to the upper right.

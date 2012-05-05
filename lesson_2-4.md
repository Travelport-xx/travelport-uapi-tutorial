---
layout: page
title: "Hotel Search"
description: "Understanding hotel searches and how to search based on a point of interest rather than a city or airport."
---
{% include JB/setup %}

## Unit 2, Lesson 4

### Objective Of Unit 2

After you have worked your way through the three lessons in this unit, you'll be able to do searching for multiple types travel-related items (hotels and cars) in addition to the transportation we covered in the last unit.  In addition, we are going to focus on creating bookings for hotels, air travel, etc. so you can complete the entire purchase cycle.  We'll finish by putting it all together with a "Universal Record" --- a part of the Universal API that pulls together all the information about trips and travellers.

### Hotel search

After deciding when, where, and how to travel (the subjects of [Unit 1](lesson1) of our tutorial), the next step is usually to try to find accomodation at the destination.  Hotels are the most common target in searching for accomodation.  When this lession is completed you'll have a program that can output information like this:

{% highlight console %}
BW HOTEL MONTGOMERY            [BW:25754]
           PONTORSON FR
           EUR95.00   to EUR179.00  
           10KM from Mont St Michel
           RESERVATION REQUIREMENT IS GUARANTEE
IBIS AVRANCHES MONT ST MICHEL  [RT:23750]
           SAINT QUENTIN SUR L
           EUR72.00   to EUR72.00   
           13KM from Mont St Michel
           RESERVATION REQUIREMENT IS OTHER
{% endhighlight %}

>>> I was pleased to find that the API for hotel results has a latitude and longitude component.  However, it appears that is not currently implemented or at least none of the searches I did resulted in any values being returned.  The code to display a google maps link is checked into the code for Lesson4, but not discussed.  If any results have the latitude and longitude, the google maps link will appear in the text like the above.

### WSDL For Hotel

As we explored previously, you'll need to go through the process of generating the client-side code for the Hotel Service.  The `HotelService` has a number of ports, as did the `AirService` we covered before.  After you generate the client code from `Hotel.wsdl` (in the directory `wsdl/hotel_v17_0` in the provided code), you may want to examine the `HotelAvailabiltySearchReq` and `HotelAvailabilitySearchRsp` as these are the request/response pair of primary importance to the task ahead.

### Getting "More" Data

In the interest of clarity of exposition, we did not discuss in the previous lesson exactly how many search results were available, to be expected, and, perhaps most importantly, how to request more results if the provider of search results can deliver them.  In the case of any kind of search, the Universal API will signal in its responses if more results are available.  At the Java level, you use the method `getNextResultReference` to get access to a "token" that you can use later to tell Travelport what data you have already been returned.  

Historically, the GDSes provided data on "green-screen", character-based terminals. These systems had the notion of a screenful of information--the number of lines of text that the user could see before the top lines scrolled off-screen.  Some APIs to various GDSes have also used, or perhaps "kept", the notion of a "screenful" of information to represent a partial list of results.  In homage to this tradition, we will keep the nomenclature of "a screen" to indicate one _burst_ of information returned.

The typical construction in code for pulling multiple screens of information from a search request looks something like the following Java code.  We are using a hotel search here, but it applies to other searches.

{% highlight java %}
do {
	NextResultReference next = null;
    
	//... prepare a request and get the response...
	
    List<HotelSearchResult> results = response.getHotelSearchResult();
	
	//... process this screenful of results...
	
    // is there more?
    if (rsp.getNextResultReference().size() > 0) {
        // there is, so prepare for it
        next = rsp.getNextResultReference().get(0);
    }
    // keep track of number of times we've hit the server
    ++screens;
    if (next == null) {
        // no more data
        break;
    }
    // prepare for next round by setting the next value into the same request
    req.getNextResultReference().clear();
    req.getNextResultReference().add(next);

} while (screens != MAX_SCREENS);
{% endhighlight %}

A few things are worth talking about from this snippet.  The value returned by `getNextResultReference` is not meaningful other than as a marker to a follow-up request to indicate what part of the full result set has already been seen. Second, the _same_ request object is re-used for each pass around the loop.  The request parameters should be the same each time, with the full requests differing only by the next result reference.  Finally, the loop here keeps track of how many screens have been read and it stops when `MAX_REQUESTS` has been reached.  This is both good policy and safe.  It is good policy because the total list of results may be _far_ larger than you might expect, search for any hotel in Paris, France can yield a great many results! It is safe because this protects you from launching a large, or even infinite, number of requests if you have a bug in your program.

>>>>It's not clear why there can be more than one next result reference in a response or a request.  Some experimentation with the API did not reveal it be used by responses or useful in requests.


### Searching By Location

In this lesson, we are going to be searching for hotels that are "near" some famous landmark.  To do this, one must provide, obviously, the name of the landmark but also not provide a "location" with a city code as we have done previously.  We are going to assume we are looking to find a hotel for a family vacation to Paris, France.  With two adults and two children, we are going to need two hotel rooms and we are planning to spend a couple of days at [DisneyLand Paris](http://www.disneylandparis.com/) so it seems that it might be good to look for a hotel in that area.

For those unfamiliar with Paris' geography, DisneyLand Paris _neé EuroDisney_, is 32km east of the center of Paris proper.  Thus, a hotel search that used the city code "PAR" or any of the Paris airports (north and south of the city) will be unlikely to produce good results.  We need to do our search for this landmark.

>>>>I was surprised that the bedding option was not supported by 1G.  I left in some code, commented out, that actually set up the hotel search to be for 2 rooms with 2 twin beds in one and 1 queen in the other.

The key idea for doing this type of search is to use a search modifier with the location's name contained in it.  You do that with java code like this example from `Lesson4`:

{% highlight java %}
HotelSearchModifiers mods = Lesson4.createModifiers(2, 2);
//...
String ATTRACTION = "EuroDisney";
mods.setReferencePoint(ATTRACTION);
Lesson4.addDistanceModifier(mods, 25);
req.setHotelSearchModifiers(mods);

{% endhighlight %}

The first line creates a search modifier object and the parameters represent the need for two rooms with two adults in the party.  Later on, we add the attraction to the search modifiers and we _do not_ add anything to the hotel location property of the request of object.  We also add a distance object, created by the helper function, that represents a distance of twenty-five kilometers.  This is required to tell the geography searching engine of the uAPI how far away from the attraction to consider.

### Key Elements Of Lesson 4

The main part of the fourth lesson's code, contained in [Lesson4.java](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/uapi/unit2/Lesson4.java) is a loop, as described above in the section about retrieving multiple screens of results. Prior to the loop entry, we set up a `HotelSearchAvailabilityRequest` with the key parameters based on destination attraction and the dates of travel.  

As we go around the loop of reading bunches of results, we keep track of the hotel that has the lowest minimum price and the hotel that is closest (in kilometers) to our attraction.  After reading several screens of data, we display the lowest priced option and closest with some details from the response object, in particular the `HotelSearchResult` that represents these two "good" choices.  

As we have done in the [previous lessons](lesson2.html) concerning decoding the results of air searches, we build a map that tracks the "key" to "value" mapping for items in the response.  In this lesson, the type of this object is the `Helper.VendorLocMap`; we construct this object as we read each screen of information but we were not able to find any cases where this map provided us needed information that was not present in either in the `HotelSearchResult` or `HotelProperty` objects that we were processing.  This may vary based on your choice of provider.

>>> I also wrote some code, now commented out, that tried to do a "media links" lookup. This appeared to be a way to get pictures and such from the hotel in question, but I got an error message that this was not supported.  I left the code in because it seemed pretty useful if some providers supported it.

### A Small Exercise

Although we can't recommend renting a car in the city of Paris proper as their are faster and more convenient means of public transit, out in the area near DisneyLand Paris a car is much more manageable.  It may be that renting a car is a useful option for our family on this trip, so we want to add a car search to see how much it might cost and how that would effect the total price of the stay.  For this small enhancement, we'll make sure we look for a mini-van with air-conditioning and an automatic transmission from the _airport_; we'll expect the family to keep the vehicle for the whole of their trip.

For this exercise, as before, you'll need to generate the client side code for the WSDL in `wsdl/vehicle_v17_0/vehicle.wsdl`.  You can follow the same "recipie" we have used in all the lessons so far: Create a request object, get the port object representing the functionality, and then call `service()` and finally decode the response object.

We won't detail too much about how to add this feature to the code for lesson 4 but only point on some potential "gotchas":

* You need to supply a date _and_ time for pickup and delivery.  These, unlike hotel search, are not using the XML objects but just raw strings in the format "2012-08-20T11:59:00".

>>>> I had actually written the code to do the request before I found out that there was no provider available!  

You should be able to print out the results of the search with the name of the vendor (`VendorCode` field), the location to pick up the vehicle, the type of vehicle (`Description`), and the price (`EstimatedTotalAmount`) like this:

{% highlight console %}
FORD ESCAPE OR SIMILAR  [Vendor: ZR]
         EUR905.27
{% endhighlight %}


### Further Exercises for the reader


* Similarly to the last exercise where we used a different service for a vehicle search, try to convert each price to Thai Bhat. To do this, you can use the `UtilCurrencyConversionPortType` in the `wsdl/Util_v17_0/Util.wsdl` definition.  Naturally, the classes to use are `CurrencyConversionReq` and `CurrencyConversionRsp` who contain `CurrencyConversion` objects.

* There are a large list of amenties that are provided for each hotel.  Decode this list and display them to the user.  Each amenity is represented by a [four letter code](http://support.travelport.com/webhelp/uapi/Content/Hotel/Shared_Hotel_Topics/Hotel%20Amenities.htm) and you should create a table to print these out in a nice way for the user.

----------------------

[Proceed To Lesson5](lesson5.html)
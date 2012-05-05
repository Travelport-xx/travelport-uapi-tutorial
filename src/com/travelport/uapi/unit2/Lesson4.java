package com.travelport.uapi.unit2;

import java.math.BigInteger;
import java.util.*;

import javax.xml.datatype.*;

import com.travelport.schema.common_v15_0.*;
import com.travelport.schema.hotel_v17_0.*;
import com.travelport.schema.vehicle_v17_0.*;
import com.travelport.service.hotel_v17_0.HotelFaultMessage;
import com.travelport.service.hotel_v17_0.HotelMediaLinksServicePortType;
import com.travelport.service.hotel_v17_0.HotelSearchServicePortType;
import com.travelport.service.vehicle_v17_0.VehicleFaultMessage;
import com.travelport.service.vehicle_v17_0.VehicleSearchServicePortType;
import com.travelport.tutorial.support.WSDLService;
import com.travelport.uapi.unit1.Helper;
import com.travelport.uapi.unit1.Helper.VendorLocMap;

public class Lesson4 {
    /**
     * Program entry point.
     * 
     * @param argv
     *            cmd line args
     */
    public static void main(String[] argv) {

        // get the port and request, as usual
        HotelSearchServicePortType port = WSDLService.getHotelSearch(false);
        HotelMediaLinksServicePortType media = WSDLService.getHotelMedia(true);

        HotelSearchAvailabilityReq req = new HotelSearchAvailabilityReq();

        HotelSearchModifiers mods = Lesson4.createModifiers(2, 2);

        /*
         * bedding requests can't be handled by 1G HotelBedding parents = new
         * HotelBedding(); parents.setType(TypeBedding.QUEEN);
         * parents.setNumberOfBeds(1); mods.getHotelBedding().add(parents);
         * 
         * HotelBedding kids = new HotelBedding();
         * kids.setType(TypeBedding.TWIN); kids.setNumberOfBeds(2);
         * mods.getHotelBedding().add(kids);
         */

        // have to give our branch id
        req.setTargetBranch(System.getProperty("travelport.targetBranch"));

        // family for disney
        String ATTRACTION = "Mont St Michel";
        mods.setReferencePoint(ATTRACTION);

        // within certain radius of attraction
        Lesson4.addDistanceModifier(mods, 25);
        req.setHotelSearchModifiers(mods);

        // when staying?
        int ARR = 90, DEP=97;
        HotelStay stay = Lesson4.createCheckInAndOut(Lesson4.futureDateAsXML(ARR), 
                Lesson4.futureDateAsXML(DEP));
        req.setHotelStay(stay);
        
        // point of sale
        req.setBillingPointOfSaleInfo(Helper.tutorialBPOSInfo(6, 2));

        // where we store results
        Helper.VendorLocMap locMap = new Helper.VendorLocMap();
        HotelSearchAvailabilityRsp rsp = null;
        int screens = 0, MAX_SCREENS = 3;

        HotelSearchResult closest = null, cheapest = null;
        int lowestDistance = Integer.MAX_VALUE;
        double lowestPrice = Double.MAX_VALUE;
        // we walk through each result
        try {
            do {
                NextResultReference next = null;
                VendorLocMap loc = new VendorLocMap();

                // run the request, possibly from some middle point
                rsp = port.service(req);
                // merge everyone into the map
                loc.mergeAll(rsp.getVendorLocation());

                List<HotelSearchResult> results = rsp.getHotelSearchResult();
                for (Iterator<HotelSearchResult> iterator = results.iterator(); iterator.hasNext();) {
                    HotelSearchResult r = (HotelSearchResult) iterator.next();
                    HotelProperty p = r.getHotelProperty();
                    if ((p.getAvailability() == null) || (!p.getAvailability().equals(TypeHotelAvailability.AVAILABLE))) {
                        continue;
                    }
                    // get the price, check for lowest...
                    String minPriceWithCurrency = r.getMinimumAmount();
                    // first 3 chars are currency code
                    String price = minPriceWithCurrency.substring(3);
                    double min = Double.parseDouble(price);
                    if (min < lowestPrice) {
                        cheapest = r;
                        lowestPrice = min;
                    }
                    // check for closest
                    int dist = p.getDistance().getValue().intValue();
                    if (dist < lowestDistance) {
                        closest = r;
                        lowestDistance = dist;
                    }

                }

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
                // prepare for next round by setting the next value into this
                // request
                req.getNextResultReference().clear();
                req.getNextResultReference().add(next);

            } while (screens != MAX_SCREENS);

            if (closest==null) {
                System.out.println("No hotels found near "+ATTRACTION);
            } else {
                printHotel(closest, locMap, ATTRACTION, media);
                printHotel(cheapest, locMap, ATTRACTION, media);
            }

            searchVehicle("CDG",ARR,DEP); 
            
        } catch (NumberFormatException e) {
            System.err.println("unable to parse hotel price: " + e.getMessage());
        } catch (HotelFaultMessage e) {
            System.err.println("error reading hotel data: " + e.getMessage());
        } catch (VehicleFaultMessage e) {
            System.err.println("error reading vehicle data: " + e.getMessage());
        }

    }

    /**
     * Very basic vehicle search for a family vehicle.
     * 
     * @param string
     * @throws VehicleFaultMessage 
     */
    public static void searchVehicle(String airportCode, int daysInFuturePickup,
            int daysInFutureReturn) throws VehicleFaultMessage {
        VehicleSearchServicePortType port = WSDLService.getVehicleSearch(true);
        VehicleSearchAvailabilityReq req = new VehicleSearchAvailabilityReq();
        
        req.setBillingPointOfSaleInfo(Helper.tutorialBPOSInfo(4, 2));
        
        VehicleDateLocation dateLoc = new VehicleDateLocation();
        dateLoc.setPickupLocationNumber(airportCode);
        dateLoc.setPickupLocationType(TypeVehicleLocation.AIRPORT);
        dateLoc.setReturnLocationNumber(airportCode);
        dateLoc.setReturnLocationType(TypeVehicleLocation.AIRPORT);
        
        //just use noon here
        String suffix = "T12:00:00";
        dateLoc.setPickupDateTime(Helper.daysInFuture(daysInFuturePickup)+suffix);
        dateLoc.setReturnDateTime(Helper.daysInFuture(daysInFutureReturn)+suffix);
        req.setVehicleDateLocation(dateLoc);
        
        VehicleSearchModifiers searchMods = new VehicleSearchModifiers();
        VehicleModifier mods = new VehicleModifier();
        mods.setAirConditioning(Boolean.TRUE);
        mods.setTransmissionType(TypeVehicleTransmission.AUTOMATIC);
        mods.setVehicleClass(TypeVehicleClass.MINIVAN);
        searchMods.getVehicleModifier().add(mods);
        req.setVehicleSearchModifiers(searchMods);
        
        VehicleSearchAvailabilityRsp rsp= port.service(req);
    }

    /**
     * Display a hotel on the terminal.
     * 
     * @param r
     *            the result that we want to print
     * @param locMap
     *            the location map for looking up vendor locations (unused)
     * @param attraction
     *            name of the attraction for the distance
     * @param port the service port for trying to lookup media about the hotel
     * @throws HotelFaultMessage 
     */
    public static void printHotel(HotelSearchResult r, VendorLocMap locMap, 
            String attraction, HotelMediaLinksServicePortType port) throws HotelFaultMessage {
        
        /* not provided?
        HotelMediaLinksReq req = new HotelMediaLinksReq();
        req.setBillingPointOfSaleInfo(Helper.tutorialBPOSInfo(6, 2));
        req.getHotelProperty().add(r.getHotelProperty());
        HotelMediaLinksRsp rsp = port.service(req);
        */
        
        // at the moment, it doesn't appear that there is any really useful
        // information in the vendor location map... that may vary by provider
        HotelProperty p = r.getHotelProperty();
        Formatter fmt = new Formatter(System.out);
        fmt.format("%-30s [%s:%s]\n", p.getName(), p.getHotelChain(), p.getHotelCode());

        String addr = p.getPropertyAddress().getAddress().get(0);
        fmt.format("%10s %s\n", "", addr);

        String ratingsText = flattenRatingsToText(r.getHotelProperty().getHotelRating());
        fmt.format("%10s %-10s to %-10s \n", "", r.getMinimumAmount(), r.getMaximumAmount());
        fmt.format("%10s %s%s from %s\n", "", p.getDistance().getValue(), p.getDistance().getUnits(), attraction);
        if (!ratingsText.equals("")) {
            fmt.format("%10s %s\n", "", ratingsText);
        }
        fmt.format("%10s RESERVATION REQUIREMENT IS %s\n", "", r.getHotelProperty().getReserveRequirement());
        
        if (r.getHotelProperty().getCoordinateLocation()!=null) {
            float lat = r.getHotelProperty().getCoordinateLocation().getLatitude();
            float lon = r.getHotelProperty().getCoordinateLocation().getLongitude();
            fmt.format("%5s http://maps.google.com/?q=%.6f,%.6f\n",
                    "",lat,lon);
        }
    }

    public static String flattenRatingsToText(List<HotelRating> ratings) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<HotelRating> iterator = ratings.iterator(); iterator.hasNext();) {
            HotelRating rating = (HotelRating) iterator.next();
            List<BigInteger> nums = rating.getRating();
            if (nums.isEmpty()) {
                continue;
            }
            builder.append(rating.getRatingProvider() + " says ");
            for (Iterator<BigInteger> numIter = nums.iterator(); numIter.hasNext();) {
                BigInteger num = (BigInteger) numIter.next();
                builder.append(num + " ");
            }
            builder.append(" on scale of " + rating.getRatingRange().getMinimumRating() + " to "
                    + rating.getRatingRange().getMaximumRating());
        }
        return builder.toString();
    }

    /**
     * No sense in having more than one factory.
     */
    protected static DatatypeFactory factory;

    protected static DatatypeFactory getFactory() {
        if (factory != null) {
            return factory;
        }
        try {
            factory = DatatypeFactory.newInstance();
            return factory;
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Whoa! No data type factory found!");
        }
    }

    /**
     * Return the object that represents the amount of days in the future given.
     * 
     * @return this object can be used in requests that refer to a calendar date
     */
    public static XMLGregorianCalendar futureDateAsXML(int daysInFuture) {
        Date d = new Date();
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, daysInFuture);
        return getFactory().newXMLGregorianCalendarDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                DatatypeConstants.FIELD_UNDEFINED);
    }
    
    /**
     * Create the modifiers that always work.
     * 
     * @param numAdults
     * @param numRooms
     * @return
     */
    public static HotelSearchModifiers createModifiers(int numAdults, int numRooms) {
        HotelSearchModifiers mods = new HotelSearchModifiers();
        mods.setNumberOfAdults(2);
        mods.setNumberOfRooms(2);
        return mods;

    }

    public static HotelLocation createCityLocation(String cityCode) {
        HotelLocation location = new HotelLocation();
        location.setLocation(cityCode);
        location.setLocationType(TypeHotelLocation.CITY);
        return location;
    }

    public static void addDistanceModifier(HotelSearchModifiers mods, long km) {
        Distance distance = new Distance();
        distance.setUnits("km");
        distance.setValue(BigInteger.valueOf(km));
        mods.setDistance(distance);
    }

    public static HotelStay createCheckInAndOut(XMLGregorianCalendar in, XMLGregorianCalendar out) {
        // when staying?
        HotelStay stay = new HotelStay();
        // use the factory to make XML dates
        stay.setCheckinDate(Lesson4.futureDateAsXML(45));
        stay.setCheckoutDate(Lesson4.futureDateAsXML(47));
        return stay;
    }
}

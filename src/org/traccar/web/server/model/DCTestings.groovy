package org.traccar.web.server.model

import com.vividsolutions.jts.geom.Coordinate
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS
import org.geotools.referencing.GeodeticCalculator
import org.opengis.referencing.crs.CoordinateReferenceSystem

/**
 * Created by admin on 18/04/15.
 */

def haversine(lat1, lon1, lat2, lon2) {
    def R = 6372.8
    // In kilometers
    def dLat = Math.toRadians(lat2 - lat1)
    def dLon = Math.toRadians(lon2 - lon1)
    lat1 = Math.toRadians(lat1)
    lat2 = Math.toRadians(lat2)

    def a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
    def c = 2 * Math.asin(Math.sqrt(a))
    R * c
}

haversine(36.12, -86.67, 33.94, -118.40)

CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
// CoordinateReferenceSystem crs2 = CRS.decode("EPSG:3857");

def start = new Coordinate(56.959906, 24.128901)   // 142.71
def end = new Coordinate(56.960695, 24.129442)



// the following code is based on JTS.orthodromicDistance( start, end, crs )
GeodeticCalculator gc = new GeodeticCalculator(crs);
gc.setStartingPosition(JTS.toDirectPosition(start, crs));
gc.setDestinationPosition(JTS.toDirectPosition(end, crs));

double distance = gc.getOrthodromicDistance();
// distance = JTS.orthodromicDistance(start, end, crs)
// distance = haversine(56.959906, 24.128901, 56.960695, 24.129442)*1000

int totalmeters = (int) distance;
int km = totalmeters / 1000;
int meters = totalmeters - (km * 1000);
float remaining_cm = (float) (distance - totalmeters) * 10000;
remaining_cm = Math.round(remaining_cm);
float cm = remaining_cm / 100;

System.out.println("Distance = " + km + "km " + meters + "m " + cm + "cm");
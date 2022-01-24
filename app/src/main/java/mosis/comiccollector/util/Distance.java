package mosis.comiccollector.util;

public class Distance {
    public static double calculateInKm(double lat1, double lon1, double lat2, double lon2) {

        double R = 6371; // Radius of the earth in km
        double dLat = Units.deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = Units.deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Units.deg2rad(lat1)) * Math.cos(Units.deg2rad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;

//        double theta = lon1 - lon2;
//        double dist = Math.sin(Units.deg2rad(lat1)) * Math.sin(Units.deg2rad(lat2)) + Math.cos(Units.deg2rad(lat1)) * Math.cos(Units.deg2rad(lat2)) * Math.cos(Units.deg2rad(theta));
//        dist = Math.acos(dist);
//        dist = Units.rad2deg(dist);
//        dist = dist * 60 * 1.1515;

        // there was one more arg. (..., char unit);
//        if (unit == 'K') {
//            dist = dist * 1.609344;
//        } else if (unit == 'N') {
//            dist = dist * 0.8684;
//        }
//        return (dist);
    }
}

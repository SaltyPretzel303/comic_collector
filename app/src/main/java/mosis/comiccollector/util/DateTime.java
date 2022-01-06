package mosis.comiccollector.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTime {
    private static final String DEFAULT_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static String now() {
        SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_FORMAT);
        Date date = new Date();

        return formatter.format(date);
    }

    public static Date parse(String input) {
        SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_FORMAT);
        try {
            return formatter.parse(input);
        } catch (ParseException e) {
            return null;
        }
    }
}

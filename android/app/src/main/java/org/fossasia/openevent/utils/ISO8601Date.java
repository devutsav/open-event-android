package org.fossasia.openevent.utils;

import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;

import org.fossasia.openevent.OpenEventApp;
import org.fossasia.openevent.data.Event;
import org.fossasia.openevent.dbutils.DbSingleton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

/**
 * Helper class for handling a most common subset of ISO 8601 strings
 * (in the following format: "2008-03-01T13:00:00+01:00"). It supports
 * parsing the "Z" timezone, but many other less-used features are
 * missing.
 */
public final class ISO8601Date {
    /**
     * Transform Calendar to ISO 8601 string.
     */

    public static String eventTimezone = "";
    public static final String TIMEZONE_MODE = "timezone_mode";


    public static String fromCalendar(final Calendar calendar) {
        Date date = calendar.getTime();
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .format(date);
        //to add the ':' to timezone
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /**
     * Get current date and time formatted as ISO 8601 string.
     */
    public static String now() {
        return fromCalendar(GregorianCalendar.getInstance());
    }

    public static String dateFromCalendar(Calendar currentDate ) {
        return fromCalendar(currentDate).split("T")[0];
    }



    public static String getTimeZoneDateString(final Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM yyyy, HH:mm, z");
        dateFormat.setTimeZone(getEventTimezone());
        String DateToStr = dateFormat.format(date);
        return DateToStr;
    }

    public static Date getTimeZoneDate(final Date date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM yyyy, HH:mm, z");
        dateFormat.setTimeZone(getEventTimezone());
        String DateToStr = dateFormat.format(date);
        return date;
    }

    public static String get24HourTime(final Date date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm ");
        dateFormat.setTimeZone(getEventTimezone());
        return dateFormat.format(date);
    }

    public static String get12HourTime(final Date date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("KK:mm a");
        dateFormat.setTimeZone(getEventTimezone());
        return dateFormat.format(date);
    }


    public static Date getDateObject(final String iso8601String) {
        setEventTimezone();
        StringBuilder s = new StringBuilder();
        s.append(iso8601String).append("Z");
        String final1 = s.toString();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date date = null;
        try {
            date = format.parse(final1);
        } catch (ParseException e) {
            Timber.e("Parsing Error Occurred at ISO8601Date::getDateObject.");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return date;
    }

    public static TimeZone getEventTimezone() {
        TimeZone selected;
        if (!PreferenceManager.getDefaultSharedPreferences(OpenEventApp.getAppContext()).getBoolean(TIMEZONE_MODE, false)) {
            setEventTimezone();
            selected = TimeZone.getTimeZone(eventTimezone);
        } else {
            selected = TimeZone.getDefault();
        }
        return selected;

    }

    public static void setEventTimezone() {
        if (eventTimezone.isEmpty()) {
            Event event = DbSingleton.getInstance().getEventDetails();
            String[] tzIds = TimeZone.getAvailableIDs();
            List<String> timeZones = new ArrayList<String>();

            Geocoder geocoder = new Geocoder(OpenEventApp.getAppContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(event.getLatitude(), event.getLongitude(), 1);
                for (String timeZoneId : tzIds) {

                    if (timeZoneId.endsWith(addresses.get(0).getCountryName())) {
                        timeZones.add(timeZoneId);
                    }
                }
                ISO8601Date.eventTimezone = (timeZones.get(1));


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
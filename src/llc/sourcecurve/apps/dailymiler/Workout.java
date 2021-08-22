/**
 * 
 */
package llc.sourcecurve.apps.dailymiler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;

/**
 * http://www.dailymile.com/api/documentation#create_entry
 * 
 */
public class Workout
{
    /**
     * text of the note to post or the "how did it go?" text to accompany a
     * workout
     */
    protected String message; // string (optional);

    /**
     * the latitude of this entry, between -90 and 90
     */
    protected Float lat; // (optional);

    /**
     * the longitude of this entry, between -180 and 180
     */
    protected Float lon; // (optional)

    /**
     * Recognized activity types
     * 
     */
    public enum ActivityType
    {
        running, cycling, swimming, walking, hiking, fitness
    }

    /** the activity type */
    protected ActivityType activity_type; // (optional)

    /**
     * Recognized feeling types
     */
    public enum Feeling
    {
        great, good, alright, blah, tired, injured
    }

    /** the feeling */
    protected Feeling felt; // (optional)

    /**
     * when the workout was done, formatted in ISO 8601. ex:
     * 2011-01-11T03:54:43Z
     */
    protected Date completed_at; // (optional)

    /**
     * 
     */
    public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** the distance indicated by units */
    protected Float distance; // key = 'value' (optional)

    /** Recognized unit types */
    public enum Units
    {
        miles, kilometers, yards, meters
    }

    /** Units we are using */
    protected Units units; // (optional)

    /**
     * the number of seconds spent working out
     */
    protected Integer duration; // seconds (optional)

    /**
     * the number of calories burned during the workout
     */
    protected Integer calories; // (optional)

    /**
     * optional title for a workout
     */
    protected String title; // (optional)

    /*
     * TODO Include if sharing an image:
     * 
     * media[type], string
     * image
     * media[url], string
     * the URL to the photo
     */

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return the lat
     */
    public Float getLat()
    {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(Float lat)
    {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public Float getLon()
    {
        return lon;
    }

    /**
     * @param lon the lon to set
     */
    public void setLon(Float lon)
    {
        this.lon = lon;
    }

    /**
     * @return the activity_type
     */
    public ActivityType getActivity_type()
    {
        return activity_type;
    }

    /**
     * @param activityType the activity_type to set
     */
    public void setActivity_type(ActivityType activityType)
    {
        activity_type = activityType;
    }

    /**
     * @return the felt
     */
    public Feeling getFelt()
    {
        return felt;
    }

    /**
     * @param felt the felt to set
     */
    public void setFelt(Feeling felt)
    {
        this.felt = felt;
    }

    /**
     * @return the completed_at
     */
    public Date getCompleted_at()
    {
        return completed_at;
    }

    /**
     * @param completedAt the completed_at to set
     */
    public void setCompleted_at(Date date, TimeZone zone)
    {
        date.setTime(date.getTime() + zone.getRawOffset());
        completed_at = date;
    }

    /**
     * @return the distance
     */
    public Float getDistance()
    {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Float distance)
    {
        this.distance = distance;
    }

    /**
     * @return the units
     */
    public Units getUnits()
    {
        return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(Units units)
    {
        this.units = units;
    }

    /**
     * @return the duration
     */
    public Integer getDuration()
    {
        return duration;
    }

    /**
     * Set the duration
     * 
     * @param hms seconds or minutes:seconds or hours:minutes:seconds
     * @throws ParseException
     */
    public void setDuration(String hms) throws ParseException
    {
        if (hms == null || hms.length() < 1) return;

        int seconds = 0;
        String[] values = hms.toString().split(":");

        switch (values.length)
        {
            case 0:
                return;
            case 1:
                seconds = Integer.parseInt(values[0].trim());
                break;
            case 2:
                seconds = Integer.parseInt(values[0].trim()) * 60
                        + Integer.parseInt(values[1].trim());
                break;
            case 3:
                seconds = Integer.parseInt(values[0].trim()) * 3600
                        + Integer.parseInt(values[1].trim()) * 60
                        + Integer.parseInt(values[2].trim());
                break;
            default:
                throw new ParseException("Invalid value for duration", values.length);
        }
        duration = seconds;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    /**
     * @return the calories
     */
    public Integer getCalories()
    {
        return calories;
    }

    /**
     * @param calories the calories to set
     */
    public void setCalories(Integer calories)
    {
        this.calories = calories;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return
     * @throws Exception
     */
    public JSONObject toJSON() throws Exception
    {
        JSONObject json = new JSONObject();

        if (message != null) json.put("message", message);
        if (lat != null) json.put("lat", lat.floatValue());
        if (lon != null) json.put("lon", lon.floatValue());

        if (activity_type != null)
        {
            JSONObject workout = new JSONObject();

            workout.put("activity_type", activity_type.toString());
            if (felt != null) workout.put("felt", felt.toString());
            if (completed_at != null) workout.put("completed_at", dateFormat.format(completed_at));
            if (distance != null)
            {
                JSONObject obj = new JSONObject();
                obj.put("value", distance.floatValue());

                if (units != null) obj.put("units", units.toString());
                workout.put("distance", obj);
            }

            if (duration != null) workout.put("duration", duration.intValue());
            if (calories != null) workout.put("calories", calories.intValue());
            if (title != null) workout.put("title", title);

            json.put("workout", workout);
        }

        return json;
    }
}

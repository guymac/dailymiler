package llc.sourcecurve.apps.dailymiler;

import java.util.Calendar;
import java.util.TimeZone;

import llc.sourcecurve.apps.dailymiler.Workout.ActivityType;
import llc.sourcecurve.apps.dailymiler.Workout.Feeling;
import llc.sourcecurve.apps.dailymiler.Workout.Units;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Bare-bones daily mile workout post entry Activity
 * 
 */
public class DailyMilerActivity extends Activity
{
    /**
     * The request URL made to authorize the app
     */
    static final String REQUEST_URL = "https://api.dailymile.com/entries.json?oauth_token=";

    /** Logging identifier */
    private final String TAG = getClass().getName();

    /** User's OAuth token, empty if app has not been authorized */
    private String token = null;

    /** Our workout instance */
    private Workout workout = new Workout();

    /**
     * 
     * 
     */
    protected class PostWorkoutTask extends AsyncTask <Void, Void, String>
    {
        // TODO progress dialog private final ProgressDialog progress;

        private final Activity activity;

        /**
         * Create a background task to post the workout.
         * 
         * @param activity
         */
        PostWorkoutTask(Activity activity)
        {
            this.activity = activity;

            // progress = new ProgressDialog(activity);
        }

        /*
         * @Override
         * protected void onCancelled()
         * {
         * 
         * }
         * 
         * @Override
         * protected void onPreExecute()
         * {
         * 
         * }
         */
        @Override
        protected String doInBackground(Void... voids)
        {
            try
            {
                HttpPost req = new HttpPost(REQUEST_URL + token);

                Log.i(TAG, "Posting to " + req.getURI().toString());
                req.setHeader("Content-Type", "application/json");
                String json = workout.toJSON().toString();
                Log.d(TAG, json);
                req.setEntity(new StringEntity(json));

                HttpParams params = new BasicHttpParams();

                // TODO test to see if all this is really needed
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
                HttpProtocolParams.setUseExpectContinue(params, true);

                SchemeRegistry schReg = new SchemeRegistry();
                schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

                DefaultHttpClient client = new DefaultHttpClient(conMgr, params);
                HttpResponse res = client.execute(req);
                StatusLine statusLine = res.getStatusLine();

                int ret = statusLine.getStatusCode();

                // handle throttling
                if (ret == HttpStatus.SC_SERVICE_UNAVAILABLE)
                {
                    Header[] headers = res.getHeaders("Retry-After");

                    if (headers.length > 0)
                    {
                        String value = headers[0].getValue();
                        return "Server busy, pleae retry after " + value + " seconds";
                    }

                    return "Server busy, please retry later.";
                }

                if (ret == HttpStatus.SC_BAD_GATEWAY)
                {
                    return "Server down, please retry later";
                }

                if (ret != HttpStatus.SC_CREATED && ret != HttpStatus.SC_OK)
                {
                    Log.i(TAG, statusLine.toString());
                    return statusLine.getReasonPhrase();
                }

            }
            catch (Exception ex)
            {
                Log.d(TAG, ex.getMessage(), ex);
                return ex.getClass().getSimpleName();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (activity.isFinishing()) return;

            if (result != null)
            {
                new AlertDialog.Builder(activity).setTitle("Post Failed").setIcon(0).setCancelable(
                        false).setMessage(result).setNegativeButton("OK", null).show();
                return;

            }

            new AlertDialog.Builder(activity).setTitle("Success!").setIcon(0).setCancelable(false)
                    .setPositiveButton("OK", null).setMessage(
                            "Your workout was posted to DailyMile").show();
        }

    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Context context = this;

        Spinner spinner = (Spinner) findViewById(R.id.DaySpinner);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView <?> parent, View view, int pos, long id)
            {
                final Calendar cal = Calendar.getInstance();
                switch (pos)
                {
                    case 0:
                        return;
                    case 1:
                        cal.roll(Calendar.DATE, -1);
                        workout.setCompleted_at(cal.getTime(), TimeZone.getDefault());
                        break;
                    default:
                        cal.roll(Calendar.DATE, -2);
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int date = cal.get(Calendar.DAY_OF_MONTH);
                        new DatePickerDialog(context, new OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int yr, int mo, int dt)
                            {
                                cal.set(Calendar.YEAR, yr);
                                cal.set(Calendar.MONTH, mo);
                                cal.set(Calendar.DAY_OF_MONTH, dt);
                                Log.i(TAG, cal.getTime().toString());
                                workout.setCompleted_at(cal.getTime(), TimeZone.getDefault());
                            }
                        }, year, month, date).show();
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView <?> arg0)
            {
                // TODO Auto-generated method stub

            }
        });

        try
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            token = prefs.getString("OAUTH_TOKEN", "");
        }
        catch (Exception ex)
        {
            Log.d(TAG, "Could not open preferences", ex);
        }

        if (token == null || "".equalsIgnoreCase(token))
        {
            Log.i(TAG, "No token found");
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkAuthorization();
    }

    /**
     * Look to see if user has authorized based on token value in preferences
     */
    public void checkAuthorization()
    {
        if (token != null && !"".equalsIgnoreCase(token))
        {
            Log.i(TAG, "Found auth token " + token);

            return;
        }

        final Context context = this;
        new AlertDialog.Builder(this).setTitle("Authorization Required").setCancelable(true)
                .setNegativeButton("Cancel", null).setIcon(0).setMessage(
                        "A one-time login to DailyMile is required").setPositiveButton("OK",
                        new OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1)
                            {
                                arg0.dismiss();
                                startActivity(new Intent().setClass(context,
                                        AuthorizorActivity.class).setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        }).show();
    }

    /**
     * Handle post workout
     * 
     * @param button event source
     */
    public void postWorkout(View button)
    {
        checkAuthorization();

        workout = new Workout();

        try
        {
            updateActivityType();
            updateWorkoutNote();
            updateWorkoutDistance();
            updateWorkoutDistanceUnits();
            updateWorkoutDuration();
            updateWorkoutFelt();
            updateWorkoutTitle();
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage(), ex);
            new AlertDialog.Builder(this).setTitle("Invalid Entry").setCancelable(false).setIcon(0)
                    .setNegativeButton("OK", null).setMessage(ex.getMessage()).show();
            return;
        }
        /*
         * try
         * {
         * Log.i(TAG, workout.toJSON().toString());
         * }
         * catch (Exception ex)
         * {
         * ex.printStackTrace();
         * }
         */
        new PostWorkoutTask(this).execute();
    }

    /**
     * Cancel the Activity
     * 
     * @param button
     */
    public void cancelPost(View button)
    {
        finish();
    }

    private void updateActivityType()
    {
        Spinner spinner = (Spinner) findViewById(R.id.ActivityTypeSpinner);
        Object item = spinner.getSelectedItem();

        if (spinner.getSelectedItemPosition() >= spinner.getCount() - 1) return;

        workout.setActivity_type(ActivityType.valueOf(item.toString().toLowerCase()));
    }

    private void updateWorkoutNote()
    {
        EditText edittext = (EditText) findViewById(R.id.WorkoutMessageText);

        Editable message = edittext.getText();

        if (message.length() == 0)
        {
            workout.setMessage(null);
        }
        else
        {
            workout.setMessage(message.toString());
        }
    }

    private void updateWorkoutDistance()
    {
        EditText edittext = (EditText) findViewById(R.id.DistanceValueText);
        Editable text = edittext.getText();
        if (text.length() < 1) return;
        workout.setDistance(Float.valueOf(text.toString()));
    }

    private void updateWorkoutDistanceUnits()
    {
        Spinner spinner = (Spinner) findViewById(R.id.DistanceUnitsSpinner);
        Object item = spinner.getSelectedItem();
        workout.setUnits(Units.valueOf(item.toString().toLowerCase()));
    }

    private void updateWorkoutDuration() throws Exception
    {
        EditText edittext = (EditText) findViewById(R.id.DurationText);
        Editable text = edittext.getText();
        if (text.length() < 1) return;
        workout.setDuration(text.toString());
    }

    private void updateWorkoutFelt()
    {
        Spinner spinner = (Spinner) findViewById(R.id.FeltSpinner);
        if (spinner.getSelectedItemPosition() < 1) return;

        workout.setFelt(Feeling.valueOf(spinner.getSelectedItem().toString().toLowerCase()));
    }

    private void updateWorkoutTitle()
    {
        EditText edittext = (EditText) findViewById(R.id.WorkoutTitleText);

        Editable title = edittext.getText();

        if (title.length() == 0)
        {
            workout.setTitle(null);
        }
        else
        {
            workout.setTitle(title.toString());
        }
    }

}

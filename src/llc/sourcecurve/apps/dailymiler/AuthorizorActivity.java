/**
 * 
 */
package llc.sourcecurve.apps.dailymiler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * OAuth 2.0 draft 11 handler for DailyMile
 * 
 * Based upon the code shown at
 * http://blog.doityourselfandroid.com/2010/11/10/oauth-flow-in-android-app/
 */
public class AuthorizorActivity extends Activity
{
    private final String TAG = getClass().getName();

    /**
     * This would be used for a web server that makes a request on behalf of
     * the user
     */
    static final String CLIENT_SECRET = "CPKMAn5LJNjQMuHAS4KgmYNYoHPbOMIqPDNHi334";

    /**
     * The intercepted URL that indicates the user has authorized, specified
     * in initial application setup with server
     */
    static final String CLIENT_CALLBACK_URL = "x-oauthflow://callback";

    /**
     * The key in the callback URL fragment
     */
    static final String CLIENT_TOKEN_KEY = "access_token";

    /**
     * The request URL made to authorize the app
     */
    static final String REQUEST_URL = "https://api.dailymile.com/oauth/authorize?"
            + "response_type=token&client_id=cy8BatWO1kIqMg920m9VfmXPqHHfJfpMRSXYs3tG&"
            + "redirect_uri=" + CLIENT_CALLBACK_URL;

    /**
     * Flags for launching Intent
     */
    static final int ONE_OFF = Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY
            | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_NEW_TASK;

    /**
     * Task that launches the request for authorization
     * 
     */
    private class AuthRequestTask extends AsyncTask <Void, Void, Void>
    {

        private final Context context;

        AuthRequestTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            Log.i(TAG, "Starting auth request");

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(REQUEST_URL))
                    .setFlags(ONE_OFF);
            getApplicationContext().startActivity(intent);

            return null;
        }

    };

    /**
     * Task that handles the authorization response
     * 
     */
    private class AuthResponseTask extends AsyncTask <String, Void, Void>
    {
        private final Context context;

        AuthResponseTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... params)
        {
            Log.i(TAG, "Saving authorization token");
            Context context = getApplicationContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            final Editor edit = prefs.edit();
            edit.putString("OAUTH_TOKEN", params[0]);
            // edit.putString("OAUTH_TOKEN_SECRET", CLIENT_SECRET);
            edit.commit();

            Log.i(TAG, "Starting activity");
            getApplicationContext().startActivity(
                    new Intent(context, DailyMilerActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            return null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        new AuthRequestTask(this).execute();
    }

    /**
     * Called when the user has authorized, intercepts the callback URL.
     */
    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        try
        {
            Log.i(TAG, "Receiving callback");
            Log.i(TAG, intent.getData().toString());

            final String token = intent.getData().getFragment().substring(
                    1 + CLIENT_TOKEN_KEY.length());
            new AuthResponseTask(this).execute(token);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Callback failed", ex);
        }

        finish();
    }
}

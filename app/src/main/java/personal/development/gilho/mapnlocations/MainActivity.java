package personal.development.gilho.mapnlocations;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;
    protected TextView mLat, mLon, mUpdateTime, mOrigin, mDestination, mDuration, mDistance, mMode;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    private String mLastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //extract the views
        mLat = (TextView)findViewById(R.id.latti);
        mLon = (TextView)findViewById(R.id.longi);
        mUpdateTime = (TextView)findViewById(R.id.update_time);
        mOrigin = (TextView)findViewById(R.id.view_origin);
        mDestination = (TextView)findViewById(R.id.view_destination);
        mDuration = (TextView)findViewById(R.id.view_duration);
        mDistance = (TextView)findViewById(R.id.view_distance);
        mMode = (TextView)findViewById(R.id.view_mode);


        // create instance of googleapiclien
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    // this callback is triggered when the client is ready


        // check if there is permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String permission = "No Permission";
            // if not permission, request it
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
                // there is permission
                String permission = "permission granted";
            }

        // get last known location
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
            mLat.setText(String.valueOf("Latitude: " + mLocation.getLatitude()));
            mLon.setText(String.valueOf("Longitude: " + mLocation.getLongitude()));
        }

        // getting location updates

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        // distance matrix
        new Distance().execute();
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    public void updateUI() {
        mLat.setText(String.valueOf("Latitude: " + mLocation.getLatitude()));
        mLon.setText(String.valueOf("Longitude: " + mLocation.getLongitude()));
        mUpdateTime.setText(mLastUpdateTime);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates)
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    public class Distance extends AsyncTask<Void, Void, JSONObject> {

        private final String TAG = Distance.class.getSimpleName();
        public final String API_KEY = "&key=AIzaSyDXaH7PtTD5SGUbrUrWxwm-latFtsCjf2E";
        public final String API_SYNTAX = "https://maps.googleapis.com/maps/api/distancematrix/json?";
        public String tempString = "origins=-33.8758805,151.2137771&destinations=-33.8690463,151.2051954&mode=bicycling";
        public String url = API_SYNTAX + tempString + API_KEY;

        public Distance() {
            // leave empty for now
        }

        public String makeServiceCall(String reqUrl) {

            String response = null;

            try {

                // make connection
                URL url = new URL(reqUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");

                // read response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);

            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
            } catch (ProtocolException e) {
                Log.e(TAG, "ProtocolException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }

            return response;
        }

        private String convertStreamToString(InputStream is) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return sb.toString();

        }

        @Override
        protected JSONObject doInBackground(Void...array) {

            Distance sh = new Distance();
            JSONObject jsonObject;

            try {
                String jsonStr = sh.makeServiceCall(url);
                jsonObject = new JSONObject(jsonStr);
                return jsonObject;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            return null;

        }

        @Override
        protected void onPostExecute(JSONObject result) {

            JSONObject durationOb = null;
            JSONObject distanceOb = null;
            JSONObject rowOb = null;
            JSONObject elementOb = null;
            String origin = null;
            String destination = null;
            String distance = null;
            String duration = null;

            JSONArray rowArray, elementArray;

            try {
                // get first-level json objects (easy)
                origin = result.getString("origin_addresses");
                destination = result.getString("destination_addresses");

                // first, create row json object (by extracting the array and then converting that)
                rowArray = result.getJSONArray("rows");
                rowOb = rowArray.getJSONObject(0);

                // secondly, extract the element array and then convert that to a json object
                elementArray = rowOb.getJSONArray("elements");
                elementOb = elementArray.getJSONObject(0);

                // thirdly, get the duration and distance objects
                durationOb = elementOb.getJSONObject("duration");
                distanceOb = elementOb.getJSONObject("distance");

                // finally, get the strings
                duration = durationOb.getString("text");
                distance = distanceOb.getString("text");

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            // UI
            mOrigin.setText(origin);
            mDestination.setText(destination);
            mDistance.setText(distance);
            mDuration.setText(duration);

        }

    }

}

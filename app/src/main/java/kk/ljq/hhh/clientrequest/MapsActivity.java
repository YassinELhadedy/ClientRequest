package kk.ljq.hhh.clientrequest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    @BindView(R.id.etOrigin) EditText from;
    @BindView(R.id.etDestination)  EditText to;

     @BindView(R.id.btnFindPath) Button send_request;
     @BindView(R.id.tvDistance)  TextView distance;
     @BindView(R.id.tvDuration)  TextView time;
    private DatabaseReference mFirebaseDatabaseReference;
    private double lat;
    private double lon;
    private static int counter=0;
    private int min_distanc;
    private double min_time;
    private ProgressDialog progressDialog;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private  String mUsername;
    private  String mPhotoUrl;

    private ArrayList<DriverInfo>driverInfoArrayList=new ArrayList<>();
    private static boolean send_check=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, Clientlogin.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }


           // initialize();
            driverInfoArrayList.clear();
            progressDialog = new ProgressDialog(this);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
            } else {
                showGPSDisabledAlertToUser();
            }


            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }



        }
    }
   /* private void initialize() {
        from=(EditText)findViewById(R.id.etOrigin);
        to=(EditText)findViewById(R.id.etDestination);
        time=(TextView)findViewById(R.id.tvDuration);
        distance=(TextView)findViewById(R.id.tvDistance);
        send_request=(Button)findViewById(R.id.btnFindPath);



    }
*/

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }
    @OnClick(R.id.btnFindPath)
    public void onclick(){
        progressDialog.setMessage("loading");
        progressDialog.show();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("delivery_location5");
        mFirebaseDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DriverInfo driverInfo = dataSnapshot.getValue(DriverInfo.class);
                driverInfoArrayList.add(driverInfo);
                String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + lat + "," + lon + "&destinations=" + driverInfo.getD_lat() + "," + driverInfo.getD_lon() + "&mode=driving&key=AIzaSyAGGykGf86ITJ5IwaD9NZWIcdEYOsXxKc8";
                //new GeoTask(MapsActivity.this).execute(url);

                StringRequest post=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            // Log.d("JSON",json);
                            JSONObject root=new JSONObject(response);
                            JSONArray array_rows=root.getJSONArray("rows");
                            Log.d("JSON","array_rows:"+array_rows);
                            JSONObject object_rows=array_rows.getJSONObject(0);
                            Log.d("JSON","object_rows:"+object_rows);
                            JSONArray array_elements=object_rows.getJSONArray("elements");
                            Log.d("JSON","array_elements:"+array_elements);
                            JSONObject object_elements=array_elements.getJSONObject(0);
                            Log.d("JSON","object_elements:"+object_elements);
                            JSONObject object_duration=object_elements.getJSONObject("duration");
                            JSONObject object_distance=object_elements.getJSONObject("distance");
                            String object=object_duration.getString("value")+","+object_distance.getString("value");
                            setDoublemin(object);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                Volley.newRequestQueue(MapsActivity.this).add(post);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            Location userCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (userCurrentLocation != null) {
                mMap.clear();
                MarkerOptions currentUserLocation = new MarkerOptions();
                LatLng currentUserLatLang = new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude());
                lat=userCurrentLocation.getLatitude();
                lon=userCurrentLocation.getLongitude();

                currentUserLocation.position(currentUserLatLang);
                mMap.addMarker(currentUserLocation);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLatLang, 16));

            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onConnected(null);
        } else {
            Toast.makeText(MapsActivity.this, "No Permitions Granted", Toast.LENGTH_SHORT).show();
        }
    }


    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    public void setDoublemin(String min) {
        String res[]=min.split(",");

        if(counter==0){
            min_distanc=Integer.parseInt(res[1])/1000;
            min_time=Double.parseDouble(res[0])/60;
            counter++;


        }
        else {
            if(Integer.parseInt(res[1])/1000<min_distanc&&Double.parseDouble(res[0])/60<min_time){
                min_distanc=Integer.parseInt(res[1])/1000;
                min_time=Double.parseDouble(res[0]);
                counter++;




            }
        }
        from.setText("here");
        from.setEnabled(false);

        to.setText(driverInfoArrayList.get(counter).getD_name());
        to.setEnabled(false);
        time.setText("" + (int) (min_time / 60) + " hr " + (int) (min_time % 60) + " min");
        distance.setText(  ""+min_distanc + "KM");
        progressDialog.dismiss();
       // delete_item();
        send_request();



    }
    private void  send_request(){
        if(send_check==false) {


            DateFormat df = new SimpleDateFormat(" hh:mm");
            String date = df.format(Calendar.getInstance().getTime());
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            DriverInfo driverInfo = new DriverInfo("" + lat + "/" + lon, mUsername, date, driverInfoArrayList.get(counter).getD_Email(), mPhotoUrl);
            mFirebaseDatabaseReference.child("order feild")
                    .push().setValue(driverInfo);
            send_check=true;
        }else {
            Update_from_db();
        }
    }
    public void Update_from_db(){





        mFirebaseDatabaseReference =FirebaseDatabase.getInstance().getReference().child("order feild");

        mFirebaseDatabaseReference.orderByChild("add_rating_order").equalTo(mUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    // Toast.makeText(getApplicationContext(),"update location",Toast.LENGTH_SHORT).show();
                    snapshot.getRef().child("d_Email").setValue(driverInfoArrayList.get(counter).getD_Email());




                    //Toast.makeText(getApplicationContext(),"update location",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"update cancelled",Toast.LENGTH_SHORT).show();

            }
        });






    }

}

package com.example.hakureireimu.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.widget.RadioButton;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private Timer timer = null;
    private int TypeTobata = 0;
    private boolean isRest = false;
    private SoundPool soundPool;
    boolean loaded = false;
    private int soundID1;
    private int soundID2;
    private LocationManager locationManager;
    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
      //  locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final TextView editTimerTxt = (TextView) findViewById(R.id.timerText);
        final TextView editCountTxt = (TextView) findViewById(R.id.countText);
        final TextView editDoWork = (TextView) findViewById(R.id.textDoWork);
        Button b = (Button) findViewById(R.id.button_Start);
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundID1 = soundPool.load(this, R.raw.bombattackspacesru, 1);
        soundID2 = soundPool.load(this, R.raw.bombattackspacesru1, 1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                h = new Handler() {
                    public void handleMessage(android.os.Message msg) {
                        // обновляем TextView
                        Boolean b = msg.getData().getBoolean("isCounterChange");
                        if (b) {
                            Integer i = msg.getData().getInt("setCounter");
                            editCountTxt.setText(i.toString());
                        }
                        Double f = msg.getData().getDouble("SetTime");
                        editTimerTxt.setText(((Integer) f.intValue()).toString());

                        if (isRest)
                            editDoWork.setText("Отдыхайте!");
                        else
                            editDoWork.setText("Занимайтесь!");

                    }

                    ;
                };
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            Double timerVal = Double.valueOf(editTimerTxt.getText().toString());
                            timerVal = timerVal + 1;
                            Bundle b = new Bundle();
                            Message ms = new Message();
                            b.putBoolean("isCounterChange", false);
                            int cntTobata = 0;
                            switch (TypeTobata) {
                                case 0:
                                    cntTobata = 10;
                                    break;
                                case 1:
                                    cntTobata = 20;
                                    break;
                                case 2:
                                    cntTobata = 40;
                                    break;
                            }
                            if (isRest)
                                cntTobata = cntTobata / 2;
                            if (timerVal >= cntTobata) {
                                timerVal = 0.0;
                                Integer countTabata = Integer.valueOf(editCountTxt.getText().toString());
                                countTabata = isRest ? countTabata : countTabata + 1;
                                b.putInt("setCounter", countTabata);
                                b.putBoolean("isCounterChange", true);
                                isRest = isRest ? false : true;
                                // Getting the user sound settings
                                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                                float actualVolume = (float) audioManager
                                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                                float maxVolume = (float) audioManager
                                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                float volume = actualVolume / maxVolume;
                                if (isRest)
                                    soundPool.play(soundID1, volume, volume, 1, 0, 1f);
                                else
                                    soundPool.play(soundID2, volume, volume, 1, 0, 1f);


                            }

                            b.putDouble("SetTime", timerVal);

                            ms.setData(b);
                            h.sendMessage(ms);


                        }
                    }, 1000, 1000);
                }

            }
        });
        Button b1 = (Button) findViewById(R.id.button_Stop);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer!=null)
                {
                    timer.cancel();
                    timer = null;
                    ContentValues values = new ContentValues();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    values.put(TobataBase.DATE,dateFormat.format(new Date())
                            );

                    values.put(TobataBase.COUNT,editCountTxt.getText().toString());

                    Uri uri = getContentResolver().insert(
                            TobataBase.CONTENT_URI, values);

                    Toast.makeText(getBaseContext(),
                            uri.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        Button b2 = (Button) findViewById(R.id.button_Clear);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editTimerTxt.setText("0");
                editCountTxt.setText("0");
                isRest = false;
            }
        });
        RadioButton rb1 = (RadioButton) findViewById(R.id.typeTobata1);
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeTobata = 0;

            }
        });
        RadioButton rb2 = (RadioButton) findViewById(R.id.typeTobata2);
        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeTobata = 1;
            }
        });
        RadioButton rb3 = (RadioButton) findViewById(R.id.typeTobata3);
        rb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeTobata = 2;
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(((RadioButton)findViewById(R.id.typeTobata1)).isChecked())
            TypeTobata = 0;
        if(((RadioButton)findViewById(R.id.typeTobata2)).isChecked())
            TypeTobata = 1;
        if(((RadioButton)findViewById(R.id.typeTobata3)).isChecked())
            TypeTobata = 2;
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
       //        1000 * 10, 10, locationListener);
       // checkEnabled();

       // locationManager.requestLocationUpdates(
       //         LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
        //       locationListener);

    }


    @Override
    protected void onPause() {
        super.onPause();

    //    locationManager.removeUpdates(locationListener);

    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();


            //showLocation(locationManager.getLastKnownLocation(provider));

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            }
            else
                tvStatusGPS.setText("Status: --");
            // else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
             //   tvStatusNet.setText("Status: " + String.valueOf(status));
            //}
        }
    };
    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        }else
            tvLocationGPS.setText("--");
         //else if (location.getProvider().equals(
          //      LocationManager.NETWORK_PROVIDER)) {
          // tvLocationNet.setText(formatLocation(location));
        //}
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "--";
        return String.format(
               "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
               location.getLatitude(), location.getLongitude(), new Date(
                       location.getTime()));
    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager
               .isProviderEnabled(LocationManager.GPS_PROVIDER));
      //  tvEnabledNet.setText("Enabled: "
       //         + locationManager
       //        .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // Method to start the service
    public void startService(View view) {
        startService(new Intent(getBaseContext(), TabataService.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), TabataService.class));
    }
}

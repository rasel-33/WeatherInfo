package com.example.weatherinfo;

import static com.example.weatherinfo.R.id.idIVIcon;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private TextInputLayout textInputLayout;
    private TextView forecastTV;
    public String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        forecastTV = findViewById(R.id.idTVForecast);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(idIVIcon);
        textInputLayout = findViewById(R.id.idTILCity);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLatitude(), location.getLongitude());
        System.out.println(cityName);

        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if(city.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    hideKeyboardFrom(MainActivity.this, view);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Please Location provide permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private String getCityName(double latitude, double longitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            for(Address adr: addresses){
                if(adr != null){
                    String city = adr.getAdminArea();
                    if(city!=null && !city.equals("")){
                        cityName = city;
                    }else{
                        Log.d("TAG", "CITY NOT FOUND");
                        Toast.makeText(this, "INPUT CITY NOT FOUND", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return cityName;
    }


    private void getWeatherInfo(String cityName) {
        System.out.println("Getting weather info for " + cityName + "...");
        String url = "https://api.weatherapi.com/v1/forecast.json?key=c320c711664443d1b34135844231703&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature + "°C");
                    String cityName = response.getJSONObject("location").getString("name");
                    cityNameTV.setText(cityName);
                    cityEdt.setText("");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String windSpeed = response.getJSONObject("current").getString("wind_kph");
                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay == 1){
                        //https://cdn5.f-cdn.com/contestentries/329593/14249726/5699389419de1_thumb900.jpg
                        //set color to black
                        temperatureTV.setTextColor(Color.BLACK);
                        conditionTV.setTextColor(Color.BLACK);
                        cityNameTV.setTextColor(Color.BLACK);
                        textInputLayout.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                        forecastTV.setTextColor(Color.BLACK);
                        cityEdt.setTextColor(Color.BLACK);

                        Picasso.get().load("https://t4.ftcdn.net/jpg/03/88/05/29/360_F_388052905_eYTQpbqPwuXOEXROA9zvF8PNzo5EsX8N.webp").into(backIV);
                    }else{
                        temperatureTV.setTextColor(Color.WHITE);
                        conditionTV.setTextColor(Color.WHITE);
                        cityNameTV.setTextColor(Color.WHITE);
                        textInputLayout.setHintTextColor(ColorStateList.valueOf(Color.WHITE));
                        forecastTV.setTextColor(Color.WHITE);
                        cityEdt.setTextColor(Color.WHITE);
                        Picasso.get().load("https://images.theconversation.com/files/427950/original/file-20211022-13-1yw1v30.jpeg?ixlib=rb-1.1.0&q=45&auto=format&w=1000&fit=clip").into(backIV);
                    }
                    JSONObject forecast = response.getJSONObject("forecast");
                    JSONObject forecast0 = forecast.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecast0.getJSONArray("hour");

                    for(int i=0;i<hourArray.length();i++){
                        JSONObject hourObject = hourArray.getJSONObject(i);
                        String time = hourObject.getString("time");
                        String temper = hourObject.getString("temp_c");
                        String img = hourObject.getJSONObject("condition").getString("icon");
                        String wind = hourObject.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time, temper, img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                cityNameTV.setText("Not Found");
                Toast.makeText(MainActivity.this, "Please Enter a valid city name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

package com.snehash.socialwork;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.record.formula.functions.Column;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String points="";
    int countval;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.textview);

        //Reading the excel file
        /*String filepath="C:\\Users\\sneha\\Desktop\\ProjectWork\\Pilot 79 Data points.xlsx";
        try {
            File file=new File(filepath);
            FileInputStream fileInputStream=new FileInputStream(file);

            POIFSFileSystem poifsFileSystem=new POIFSFileSystem(fileInputStream);
            HSSFWorkbook hssfWorkbook=new HSSFWorkbook(poifsFileSystem);
            HSSFSheet hssfSheet=hssfWorkbook.getSheetAt(0);

            Iterator<Row> rowIter=hssfSheet.rowIterator();
            while(rowIter.hasNext()) {
                HSSFRow hssfRow=rowIter.next();

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/

        //Reading text file

        BufferedReader reader;
        StringBuffer buf = new StringBuffer();
        final List<String> latList = new ArrayList<>();
        final List<String> longList = new ArrayList<>();
        try {
            reader=new BufferedReader(new InputStreamReader(getAssets().open("gpscoords.txt")));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                String[] string=mLine.split(",");
                latList.add(string[0]);
                longList.add(string[1]);
            }
            Log.d("LAT",latList.toString());
            Log.d("LONG",longList.toString());
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy( Criteria.ACCURACY_FINE );

            String mocLocationProvider = LocationManager.GPS_PROVIDER;//lm.getBestProvider( criteria, true );

            if ( mocLocationProvider == null ) {
                Toast.makeText(getApplicationContext(), "No location provider found!", Toast.LENGTH_SHORT).show();
                return;
            }
            lm.addTestProvider(mocLocationProvider, false, false,
                    false, false, true, true, true, 0, 5);
            lm.setTestProviderEnabled(mocLocationProvider, true);

            Location loc = new Location(mocLocationProvider);
            Location mockLocation = new Location(mocLocationProvider);
            for(countval=0;countval<latList.size();countval++) {
                mockLocation.setLatitude(Double.parseDouble(latList.get(countval)));  // double
                mockLocation.setLongitude(Double.parseDouble(longList.get(countval)));
                mockLocation.setAltitude(loc.getAltitude());
                mockLocation.setTime(System.currentTimeMillis());
                mockLocation.setAccuracy(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                lm.setTestProviderLocation( mocLocationProvider, mockLocation);
                //Async Task call
                try {
                    String resData = new GetJSONData().execute("https://volta.gethornet.com/api/v3/members/near.json?page=1&per_page=100").get();
                    Log.d("DATA",resData);
                    int countval=0;
                    JSONObject jsonObject=new JSONObject(resData);
                    JSONArray jsonArray=jsonObject.getJSONArray("members");
                    for(int i=0;i<jsonArray.length();i++) {
                        JSONObject jobj=jsonArray.getJSONObject(i);
                        JSONObject object=jobj.getJSONObject("member");
                        if(!object.isNull("distance")) {
                            Double dist = Double.parseDouble(object.getString("distance"));
                            if(dist<=1.24) {
                                Log.d("OBJECT",object.toString());
                                Log.d("OUTPUT", "id:" + object.get("id").toString() + " Display_Name:" + object.get("display_name").toString() + " age:"
                                        + object.get("age") + " Account:" + object.getJSONObject("account").get("username").toString() + " Distance:" +
                                        object.get("distance").toString());
                                countval+=1;
                            }
                        }
                    }
                    Log.d("COUNT",String.valueOf(countval));
                   /* String resData = new GetJSONData().execute("https://search.scruffapp.com/app/location?location="+latList.get(countval)+","+longList.get(countval)+"&client_version=5.1007").get();
                    Log.d("DATA",resData);
                    int countval=0;
                    JSONObject jsonObject=new JSONObject(resData);
                    JSONArray jsonArray=jsonObject.getJSONArray("members");
                    for(int i=0;i<jsonArray.length();i++) {
                        JSONObject jobj=jsonArray.getJSONObject(i);
                        JSONObject object=jobj.getJSONObject("member");
                        if(!object.isNull("distance")) {
                            Double dist = Double.parseDouble(object.getString("distance"));
                            if(dist<=1.0) {
                                // Log.d("DIST", dist.toString());
                                countval+=1;
                            }
                        }
                    }
                    Log.d("COUNT",String.valueOf(countval));*/
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                //Toast.makeText(getApplicationContext(), String.valueOf(mockLocation.getLatitude()) + "," + String.valueOf(mockLocation.getLongitude()), Toast.LENGTH_SHORT).show();

                //textView.setText();
                Log.d("LOCATION",String.valueOf(countval)+":"+String.valueOf(mockLocation.getLatitude())+ "," + String.valueOf(mockLocation.getLongitude()));
                /*try {
                    Thread.sleep(10);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class GetJSONData extends AsyncTask<String,Void,String> {
    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {
        String response="";
        HttpURLConnection connection=null;
        try {
            URL urlval = new URL(params[0]);
            //URL url=new URL("https://volta.gethornet.com/api/v3/members/near.json?page=1&per_page=100");
            connection=(HttpURLConnection) urlval.openConnection();
            //Log.d("URL",urlval.toString());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization","Hornet fffa47f726e4b22586a8ddf86276987b");
            connection.setRequestProperty("User-Agent","Mozilla/5.0");
            connection.setRequestProperty("Accept","*//*");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            //Log.d("RES",String.valueOf(connection.getResponseCode()));
            BufferedReader in;
            if(connection.getResponseCode()==200) {
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
            }
            else {
                in = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuffer responseData = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseData.append(inputLine);
            }
            in.close();
            Log.d("RESPONSE",responseData.toString());
            response=responseData.toString();

            /*URL urlval = new URL(params[0]);
            connection=(HttpURLConnection) urlval.openConnection();
            //Log.d("URL",urlval.toString());
            connection.setRequestMethod("GET");
            //connection.setRequestProperty("Authorization","Hornet fffa47f726e4b22586a8ddf86276987b");
            //connection.setRequestProperty("User-Agent","Mozilla/5.0");
            //connection.setRequestProperty("Accept","*//*");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            //Log.d("RES",String.valueOf(connection.getResponseCode()));
            BufferedReader in;
            if(connection.getResponseCode()==200) {
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
            }
            else {
                in = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuffer responseData = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseData.append(inputLine);
            }
            in.close();
            Log.d("RESPONSE",responseData.toString());
            response=responseData.toString();*/
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            connection.disconnect();
        }


        return response;
    }

    @Override
    protected void onProgressUpdate(Void... value) {

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

    }
}


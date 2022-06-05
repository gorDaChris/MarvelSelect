package com.example.marvelselect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    //API VARIABLES - START
    //base URL - can later add more specific paths
    /* REPLACE 'xxx' WITH YOUR OWN CHOICE OF ID */
    String base = "http://gateway.marvel.com/v1/public/characters?events=xxx&";
    String base1 = "http://gateway.marvel.com/v1/public/characters?events=xxx&";
    String base2 = "http://gateway.marvel.com/v1/public/characters?events=xxx&";
    //declaring MD5 digest hash variable of timestamp, private key, and public key
    MessageDigest hash;
    //API VARIABLES - END

    //VIEWPAGER VARIABLES - START
    //array of images - Bitmap values
    static ArrayList<Bitmap> images = new ArrayList<>();
    static ArrayList<Bitmap> images1 = new ArrayList<>();
    static ArrayList<Bitmap> images2 = new ArrayList<>();
    //ViewPager2 UI variables
    static ViewPager2 head;
    static ViewPager2 mid;
    static ViewPager2 bottom;
    //VIEWPAGER VARIABLES - END

    //UI
    //TextViews displaying currently selected superhero names
    static TextView heroName;
    static TextView midName;
    static TextView bottomName;
    //Button to switch to locker activity
    Button create;

    //ArrayList - holds character images
    static ArrayList<URL> imgList;
    //ArrayLists storing names of superheros in each ViewPager2
    static ArrayList<String> nameList = new ArrayList<>();
    static ArrayList<String> nameList1 = new ArrayList<>();
    static ArrayList<String> nameList2 = new ArrayList<>();

    //variables containing position of ViewPager - to identify superheros chosen
    static int hero1Pos;
    static int hero2Pos;
    static int hero3Pos;
    //position of team in ListView
    int listPos = 0;


    //constant keys
    public static final String EXTRA_HEROPOS1 = "com.example.application.example.EXTRA_HEROPOS1";
    public static final String EXTRA_HEROPOS2 = "com.example.application.example.EXTRA_HEROPOS2";
    public static final String EXTRA_HEROPOS3 = "com.example.application.example.EXTRA_HEROPOS3";
    public static final String EXTRA_LISTPOS = "com.example.application.example.EXTRA_LISTPOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find views
        head = findViewById(R.id.id_teaminfo_viewPager_page);
        mid = findViewById(R.id.id_main_viewPager_head1);
        bottom = findViewById(R.id.id_main_viewPager_head2);
        heroName = findViewById(R.id.id_main_name);
        midName = findViewById(R.id.id_main_name1);
        bottomName = findViewById(R.id.id_main_name2);
        create = findViewById(R.id.id_main_button_create);

        //OnClickListener for create Button
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calling method to send user to Locker
                openLocker();
                //increment variable referencing ListView position
                listPos++;
            }
        });

        //ACCESSING API DATA - START
        /* INSERT YOUR OWN PUBLIC & PRIVATE API KEY STRINGS HERE */

        //creating valid URL to retrieve JSON
        //creating timezone for URL to access
        TimeZone tz = TimeZone.getTimeZone("EST");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String ts = df.format(new Date());
        //adding timestamp to url
        base += "ts=" + ts + "&";
        base1 += "ts=" + ts + "&";
        base2 += "ts=" + ts + "&";
        //creating MD5 digest hash of timestamp, private key, and public key
        try {
            //looking for MD5 digest hash code
            hash = MessageDigest.getInstance("MD5");
            //update hash
            hash.update((ts + privateKey + publicKey).getBytes(), 0, (ts + privateKey + publicKey).length());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String hashVal = new BigInteger(1, hash.digest()).toString(16);
        //adding public key and hash to url - now the URL can be used!
        base += "apikey=" + publicKey + "&hash=" + hashVal;
        base1 += "apikey=" + publicKey + "&hash=" + hashVal;
        base2 += "apikey=" + publicKey + "&hash=" + hashVal;

        //declaring url
        URL url = null;
        URL url1 = null;
        URL url2 = null;
        try {
            //setting URL with JSON data to url variable
            url = new URL(base);
            url1 = new URL(base1);
            url2 = new URL(base2);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d("URL", url.toString());

        //method call
        new RetrieveTask(images, 0, nameList).execute(url);
        new RetrieveTask(images1, 1, nameList1).execute(url1);
        new RetrieveTask(images2, 2, nameList2).execute(url2);
        //ACCESSING API DATA - END
    }//onCreate

    //method to send user to Locker
    private void openLocker() {
        //intent to Locker class
        Intent intent = new Intent(this, Locker.class);
        //send data containing ViewPager positions
        intent.putExtra(EXTRA_HEROPOS1, hero1Pos);
        intent.putExtra(EXTRA_HEROPOS2, hero2Pos);
        intent.putExtra(EXTRA_HEROPOS3, hero3Pos);
        //send data containing team's position in ListView
        intent.putExtra(EXTRA_LISTPOS, listPos);
        //startActivity with created intent
        startActivity(intent);
    }//openLocker

    //AsyncTask - running url on separate thread
    private static class RetrieveTask extends AsyncTask<URL, Void, ArrayList<URL>> {

        //variables
        ArrayList<Bitmap> list;
        int id;
        ArrayList<String> names;

        //constructor
        public RetrieveTask(ArrayList<Bitmap> list, int id, ArrayList<String> names) {
            //set variables
            this.list = list;
            this.id = id;
            this.names = names;
        }

        @Override
        protected ArrayList<URL> doInBackground(URL... urls) {
            try {
                //create URL object
                URL url = urls[0];
                //create URLConnection object
                URLConnection connection = url.openConnection();
                //create InputStream object
                InputStream stream = connection.getInputStream();
                //create BufferedReader object
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                //concatenate data to one String value
                String line;
                String data = "";
                while((line = reader.readLine()) != null) {
                    data += line;
                }
                //close BufferedReader
                reader.close();

                //initializing imgList
                imgList = new ArrayList<>();

                //running in for loop in order to access each character from each URL
                for(int i = 0; i < 20; i++) {
                    //converts JSON string to JSONObject
                    JSONObject dataObj = new JSONObject(data);
                    //JSON object - data
                    JSONObject info = dataObj.getJSONObject("data");
                    //JSON array - results
                    JSONArray results = info.getJSONArray("results");

                    //JSON object
                    JSONObject sup = results.getJSONObject(i);
                    //JSON object - thumbnail
                    JSONObject thumb = sup.getJSONObject("thumbnail");
                    //accessing superhero's name
                    String name = sup.getString("name");
                    //separating names of superheros taken from different URLs
                    if(id == 0)
                        nameList.add(name);
                    if(id == 1)
                        nameList1.add(name);
                    if(id == 2)
                        nameList2.add(name);
                    //accessing superhero's image
                    String img = thumb.getString("path");
                    String ext = thumb.getString("extension");

                    //finalizing URL to make it valid
                    img += "/landscape_xlarge." + ext;

                    //create URL variables from img String variable
                    URL imgUrl = new URL(img);

                    //adding values to list
                    imgList.add(imgUrl);
                }

                //returns ArrayList
                return imgList;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<URL> urls) {
            //if ArrayList is not empty
            if(urls != null) {
                //loops through all superheros in JSON list
                for (int i = 0; i < urls.size(); i++) {
                    //rendering images so that it can be displayed
                    new LoadImages(list, id).execute(urls.get(i));
                }
            }
        }
    }//RetrieveClass

    //AsyncTask - loading images from JSON URL
    private static class LoadImages extends AsyncTask<URL, Void, Bitmap> {

        //variables
        ArrayList<Bitmap> list;
        int id;

        //constructor
        public LoadImages(ArrayList<Bitmap> list, int id) {
            //set variables
            this.list = list;
            this.id = id;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            URL url = urls[0];
            //create URLConnection object
            URLConnection connection = null;
            try {
                connection = url.openConnection();
                //create InputStream object
                InputStream stream = connection.getInputStream();
                //Bitmap object
                return BitmapFactory.decodeStream(stream);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //IMPLEMENTING VIEWPAGER - START
            //adding bitmap to images ArrayList
            list.add(bitmap);

            //distinguishing between superheros taken from different URLs
            if(id == 0) {
                //set head ViewPager to adapter
                head.setAdapter(new ViewPagerAdapter(list));
                //updates TextView displaying superhero name in order for the data to match up with ViewPager
                head.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        //calls displayName function - passing in current position of ViewPager
                        displayName(position);
                        super.onPageSelected(position);
                    }
                });
            }
            else if(id == 1) {
                //set mid ViewPager to adapter
                mid.setAdapter(new ViewPagerAdapter(list));
                //updates TextView displaying superhero name in order for the data to match up with ViewPager
                mid.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        //calls displayName function - passing in current position of ViewPager
                        displayName(position);
                        super.onPageSelected(position);
                    }
                });
            }
            else if(id == 2) {
                //set bottom ViewPager to adapter
                bottom.setAdapter(new ViewPagerAdapter(list));
                //updates TextView displaying superhero name in order for the data to match up with ViewPager
                bottom.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        //calls displayName function - passing in current position of ViewPager
                        displayName(position);
                        super.onPageSelected(position);
                    }
                });
            }
            //IMPLEMENTING VIEWPAGER - END
        }

        //method to update superhero name in order for it to match up with ViewPager
        private void displayName(int position) {
            //distinguishing between superheros taken from different URLs
            if(id == 0) {
                //set name of superhero shown in ViewPager
                heroName.setText(nameList.get(position));
                //sets current position of ViewPager
                hero1Pos = position;
            }
            else if(id == 1) {
                //set name of superhero shown in ViewPager
                midName.setText(nameList1.get(position));
                //sets current position of ViewPager
                hero2Pos = position;
            }
            else if(id == 2) {
                //set name of superhero shown in ViewPager
                bottomName.setText(nameList2.get(position));
                //sets current position of ViewPager
                hero3Pos = position;
            }
        }
    }//LoadImages
}
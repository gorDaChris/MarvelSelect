package com.example.marvelselect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class Locker extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //UI
    //Button sending user back to team creation
    Button newTeam;
    //ListView containing teams
    ListView listView;
    //ArrayList containing names of teams
    ArrayList<Team> names = new ArrayList<>();
    //adapter for ListView
    ListViewAdapter adapter;
    Button upload;
    Button restore;
    Button clear;

    //CLOUD FIRESTORE VARIABLES - START
    private DocumentReference documentReference = FirebaseFirestore.getInstance().document("data/teams");
    //CLOUD FIRESTORE VARIABLES - END

    //constant keys
    public static final String EXTRA_SHAREDPREFS = "com.example.application.example.EXTRA_SHAREDPREFS";
    public static final String EXTRA_NAMESLIST = "com.example.application.example.EXTRA_NAMESLIST";

    //variables tracking which class Intent came from
    boolean fromMain = true;
    boolean fromInfoTeam = true;

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        //if coming from MainActivity
        if(fromMain) {
            //get superheros that were chosen from MainActivity
            int hero1Pos = intent.getIntExtra(MainActivity.EXTRA_HEROPOS1, 0);
            int hero2Pos = intent.getIntExtra(MainActivity.EXTRA_HEROPOS2, 0);
            int hero3Pos = intent.getIntExtra(MainActivity.EXTRA_HEROPOS3, 0);
            //add Team object to names ArrayList
            names.add(new Team(hero1Pos, hero2Pos, hero3Pos, "unnamed"));
            fromMain = true;
        }
        //if coming from InfoTeam
        else if (fromInfoTeam) {
            //get superheros that were chosen
            int hero1Pos = intent.getIntExtra("hero1Pos", 0);
            int hero2Pos = intent.getIntExtra("hero2Pos", 0);
            int hero3Pos = intent.getIntExtra("hero3Pos", 0);
            //add Team object to names ArrayList
            names.add(new Team(hero1Pos, hero2Pos, hero3Pos, "Test"));
            fromInfoTeam = true;
        }
    }//onStart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker);

        //loads previously saved data
        loadListData();

        //find views
        newTeam = findViewById(R.id.id_locker_new);
        listView = findViewById(R.id.id_locker_listview);
        upload = findViewById(R.id.id_locker_upload);
        restore = findViewById(R.id.id_locker_restore);
        clear = findViewById(R.id.id_locker_clear);

        //OnClickListener for Button
        newTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save data for later
                saveListData();
                //calling method to go back to MainActivity
                openMainActivity();
            }
        });

        //on click listener for upload button - to upload data to Firestore
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTeams(listView);
            }
        });

        //on click listener for restore button - to restore data from database
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTeams(listView);
            }
        });

        //on click listener for clear button - clears ListView data
        clear.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 clearAllData();
             }
         });

        //onItemClickListener for ListView - lets user see team details
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openTeamInfo(position);
            }
        });

        //setting adapter to ListView
        adapter = new ListViewAdapter(this, R.layout.adapter_listview, names);
        listView.setAdapter(adapter);
    }//onCreate

    private void clearAllData() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference sizeRef = firebaseFirestore.collection("data").document("teams");

        //on complete listener for DocumentReference
        sizeRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()) {
                        //get data in form of Hashmap
                        Map<String, Object> map = documentSnapshot.getData();

                        Map<String, Object> delete = new HashMap<>();
                        for(int i = 0; i < map.size(); i++) {
                            delete.put(names.get(i).getTeamName(), FieldValue.delete());
                        }
                        documentReference.update(delete).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("FIRESTORE", "Deleted successfully.");
                            }
                        });
                    }
                    //clear adapter
                    adapter.clear();
                }
            }
        });
    }//clearAllData

    private void openTeamInfo(int pos) {
        Intent intent = new Intent();
        Intent newIntent = new Intent();
        //if coming from MainActivity
        if(fromMain) {
            int listPos = intent.getIntExtra(MainActivity.EXTRA_LISTPOS, 0);
            int hero1Pos = intent.getIntExtra("hero1Pos", 0);
            int hero2Pos = intent.getIntExtra("hero2Pos", 0);
            int hero3Pos = intent.getIntExtra("hero3Pos", 0);

            newIntent = new Intent(this, InfoTeam.class);
            newIntent.putExtra("hero1Pos", names.get(listPos).getHero1());
            newIntent.putExtra("hero2Pos", names.get(listPos).getHero2());
            newIntent.putExtra("hero3Pos", names.get(listPos).getHero3());
        }
        //if not coming from MainActivity
        else {
            int hero1Pos = names.get(pos).hero1;
            int hero2Pos = names.get(pos).hero2;
            int hero3Pos = names.get(pos).hero3;

            newIntent = new Intent(this, InfoTeam.class);
            newIntent.putExtra("hero1Pos", hero1Pos);
            newIntent.putExtra("hero2Pos", hero2Pos);
            newIntent.putExtra("hero3Pos", hero3Pos);
        }
        //start activity with created Intent
        startActivity(newIntent);
    }//openTeamInfo

    private void saveListData() {
        //shared preferences declaration
        SharedPreferences sharedPreferences = getSharedPreferences(EXTRA_SHAREDPREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //gson declaration
        Gson gson = new Gson();
        String json = gson.toJson(names);
        editor.putString(EXTRA_NAMESLIST, json);
        //apply changes
        editor.apply();
    }//saveListData

    private void loadListData() {
        //shared preferences declaration
        SharedPreferences sharedPreferences = getSharedPreferences(EXTRA_SHAREDPREFS, MODE_PRIVATE);
        //gson declaration
        Gson gson = new Gson();
        String json = sharedPreferences.getString(EXTRA_NAMESLIST, null);
        Type type = new TypeToken<ArrayList<Team>>() {}.getType();
        names = gson.fromJson(json, type);

        //creates new ArrayList if names ArrayList is empty
        if(names == null) {
            names = new ArrayList<>();
        }
    }//loadListData

    //open team naming page
    private void openNameTeam(int position) {
        //intent to NameTeam class
        Intent intent = new Intent(this, NameTeam.class);
        //sending data containing current position in ListView
        intent.putExtra("position", position);
        //startActivityForResult - going to NameTeam class in order to retrieve data
        startActivityForResult(intent, 1);
    }//openNameTeam

    //method sending user back to MainActivity
    private void openMainActivity() {
        //intent to MainActivity class
        Intent intent = new Intent(this, MainActivity.class);
        //start activity with intent
        startActivity(intent);
    }//openMainActivity

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if result available
        if(resultCode == RESULT_OK) {
            //set fromMain to false - since this is coming from NameTeam class
            fromMain = false;
            fromInfoTeam = false;
            //get team name that was entered in EditText
            String text = data.getStringExtra(NameTeam.EXTRA_NAME);
            //get appropriate position in ListView
            int position = data.getIntExtra("position", 0);
            //rename team
            names.get(position).setTeamName(text);
            //updates adapter
            adapter.notifyDataSetChanged();
        }
    }//onActivityResult

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(EXTRA_NAMESLIST)) {
            loadListData();
        }
    }//onSharedPreferenceChanged

    //ListViewAdapter method
    public class ListViewAdapter extends ArrayAdapter<Team> {

        //variables
        Context myContext;
        int xml;
        List<Team> listy;

        //constructor
        public ListViewAdapter(@NonNull Context context, int resource, @NonNull List<Team> objects) {
            super(context, resource, objects);
            //setting variables
            myContext = context;
            xml = resource;
            listy = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @Nonnull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View adapterView = inflater.inflate(xml, null);
            //find TextView view
            TextView textView = adapterView.findViewById(R.id.id_adapter_textview);
            //setting values for TextView
            textView.setText(names.get(position).getTeamName());
            //find Button view
            Button rename = adapterView.findViewById(R.id.id_adapter_rename);
            //set tag for Button
            rename.setTag(position);
            //OnClickListener for rename button
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //retrieves position in ListView where Button was clicked
                    int position = (Integer) v.getTag();
                    //open team renaming page method
                    openNameTeam(position);
                }
            });

            return adapterView;
        }
    }//ListViewAdapter

    //object class
    public static class Team implements Parcelable {

        //variables
        private int hero1;
        private int hero2;
        private int hero3;
        private String teamName;

        //constructor
        public Team(int hero1, int hero2, int hero3, String teamName) {
            //setting variables
            this.hero1 = hero1;
            this.hero2 = hero2;
            this.hero3 = hero3;
            this.teamName = teamName;
        }


        protected Team(Parcel in) {
            hero1 = in.readInt();
            hero2 = in.readInt();
            hero3 = in.readInt();
            teamName = in.readString();
        }

        public static final Creator<Team> CREATOR = new Creator<Team>() {
            @Override
            public Team createFromParcel(Parcel in) {
                return new Team(in);
            }

            @Override
            public Team[] newArray(int size) {
                return new Team[size];
            }
        };

        //getter methods
        public int getHero1() {
            return hero1;
        }
        public int getHero2() {
            return hero2;
        }
        public int getHero3() {
            return hero3;
        }
        public String getTeamName() {
            return teamName;
        }
        //mutator method
        public void setTeamName(String newName) {
            teamName = newName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(hero1);
            dest.writeInt(hero2);
            dest.writeInt(hero3);
            dest.writeString(teamName);
        }
    }//Team

    public void saveTeams(View view) {
        Map<String, Object> data = new HashMap<>();
        for(int i = 0; i < names.size(); i++) {
            data.put(names.get(i).getTeamName(), names.get(i));
        }
        //OnSuccessListener for documentReference
        documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("FIRESTORE", "Teams saved.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIRESTORE", "Teams not saved.");
            }
        });
    }//saveTeams

    public void fetchTeams(View view) {
        //onSuccessListener for document reference
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    //gets data in form of Hashmap
                    Map<String, Object> data = documentSnapshot.getData();
                    //ArrayList of Hashmap data
                    ArrayList<Object> list = new ArrayList<>(data.values());

                    ArrayList<Object> sortedList = new ArrayList<>(list);
                    int dynamicSize = list.size();

                    //order data list
                    for(int i = 0; i < list.size(); i++) {
                        sortedList.set(dynamicSize - 1, list.get(i));
                        dynamicSize--;
                    }

                    //take apart Object and convert to Team
                    int hero1Val = 0;
                    int hero2Val = 0;
                    int hero3Val = 0;
                    String teamNameVal = "";
                    String[] strArr;

                    String[] sArr = new String[sortedList.size()];
                    int index = 0;
                    for (Object value : sortedList) {
                        sArr[index] = String.valueOf(value);
                        index++;
                    }

                    //clear adapter
                    adapter.clear();

                    for(int i = 0; i < sArr.length; i++) {
                        strArr = sArr[i].split(", ");
                        //extracts data from string array
                        for(int j = 0; j < 4; j++) {
                            if(strArr[j].contains("hero1")) {
                                int tempIndex = strArr[j].indexOf("=");
                                hero1Val = Integer.parseInt(strArr[j].substring((tempIndex + 1), strArr[j].length() - 1));
                            }
                            else if(strArr[j].contains("hero2")) {
                                int tempIndex = strArr[j].indexOf("=");
                                hero2Val = Integer.parseInt(strArr[j].substring(tempIndex + 1));
                            }
                            else if(strArr[j].contains("hero3")) {
                                int tempIndex = strArr[j].indexOf("=");
                                hero3Val = Integer.parseInt(strArr[j].substring(tempIndex + 1));
                            }
                            else if(strArr[j].contains("teamName")) {
                                int tempIndex = strArr[j].indexOf("=");
                                teamNameVal = strArr[j].substring(tempIndex + 1);
                            }
                        }
                        Team team = new Team(hero1Val, hero2Val, hero3Val, teamNameVal);
                        adapter.add(team);
                    }
                }
            }
        });
    }//fetchTeams
}
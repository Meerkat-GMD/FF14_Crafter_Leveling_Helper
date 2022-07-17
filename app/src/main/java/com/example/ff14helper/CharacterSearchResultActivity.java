package com.example.ff14helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterSearchResultActivity extends AppCompatActivity {

    public static class CharacterData
    {
        int id;
        String imageLink;
        String name;
        String server;
        String lang;

        public CharacterData(int id, String imageLink, String name,String server, String lang)
        {
            this.id = id;
            this.imageLink = imageLink;
            this.name = name;
            this.server = server;
            this.lang = lang;
        }
    }

    public static List<CharacterData> CharacterDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_search_result);

        MakeCharacterInfo();
        Button searchButton = (Button)findViewById(R.id.search);
        searchButton.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(CharacterSearchResultActivity.this);

            dialog.setTitle("Please write your Character Name");

            View dialogView = (View) View.inflate(CharacterSearchResultActivity.this, R.layout.character_search_dialog, null);

            dialog.setView(dialogView);

            dialog.setPositiveButton("Search",
                    (dialog1, which) -> {
                        EditText foreNameEditText = (EditText) dialogView.findViewById(R.id.et_forename);
                        EditText surNameEditText= (EditText) dialogView.findViewById(R.id.et_surname);

                        Thread searchThread = new Thread(() -> Send(foreNameEditText.getText().toString(), surNameEditText.getText().toString()));
                        searchThread.start();

                        try {
                            searchThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finally {
                            MakeCharacterInfo();
                        }
                    });

            dialog.show();
        });
    }

    public void Send(String foreName, String surName)
    {
        CharacterDataList.clear();
        try
        {
            URL url = new URL("https://xivapi.com/character/search?name=" + foreName + "%20" + surName); //&server=Asura"
            URLConnection conn = (HttpURLConnection) url.openConnection();
            String redirect = conn.getHeaderField("Location");
            if(redirect != null)
            {
                conn = new URL(redirect).openConnection();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            Gson gson1 = new Gson();

            Map<String, Object> map = gson1.fromJson(in, Map.class);

            for(Map.Entry<String, Object> entry : map.entrySet())
            {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }

            for(int i = 0; i < ((ArrayList<Object>) map.get("Results")).size(); i++)
            {
                int id = ((Double) ((Map<String,Object>) ((ArrayList<Object>) map.get("Results")).get(i)).get("ID")).intValue();
                String imageLink = ((Map<String,Object>) ((ArrayList<Object>) map.get("Results")).get(i)).get("Avatar").toString();
                String name = ((Map<String,Object>) ((ArrayList<Object>) map.get("Results")).get(i)).get("Name").toString();
                String server = ((Map<String,Object>) ((ArrayList<Object>) map.get("Results")).get(i)).get("Server").toString();
                String lang =  ((Map<String,Object>) ((ArrayList<Object>) map.get("Results")).get(i)).get("Lang").toString();

                CharacterDataList.add(new CharacterData(id,imageLink,name,server,lang));
            }

        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
    }
    private void MakeCharacterInfo()
    {
        LinearLayout char_info_layout = findViewById(R.id.contact_linearLayout);
        char_info_layout.removeAllViewsInLayout();

        for(int i = 0; i < CharacterDataList.size(); i++)
        {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View character_info = inflater.inflate(R.layout.character_info, char_info_layout, false);

            CharacterData data = CharacterDataList.get(i);

            DownloadFilesTask task = new DownloadFilesTask();
            task.imgView = character_info.findViewById(R.id.imageView);
            task.execute(data.imageLink);

            TextView nameText = character_info.findViewById(R.id.textView_name);
            TextView serverText= character_info.findViewById(R.id.textView_server);

            Button moreInformationButton = character_info.findViewById(R.id.button_more_information);

            moreInformationButton.setOnClickListener(v ->{
                Intent intent = new Intent(getApplicationContext(), ClassJobSelectActivity.class);
                intent.putExtra("CharacterID", data.id);
                startActivity(intent);
            });

            nameText.setText(data.name);
            serverText.setText(data.server);
            char_info_layout.addView(character_info);
        }
    }
}

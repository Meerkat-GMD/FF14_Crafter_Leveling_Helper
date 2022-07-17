package com.example.ff14helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassJobSelectActivity extends AppCompatActivity
{
    public static Map<Integer, ClassJobData> ClassJobDataDic = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_select_scroll_view);

        Bundle extras = getIntent().getExtras();
        int characterID = extras.getInt("CharacterID");

        Thread jobThread = new Thread(() -> RequestEachOfClassExp(characterID));
        jobThread.start();
        try{
            jobThread.join();
        }
         catch (Exception e)
        {
            System.err.println(e.toString());
        }
        finally {
            MakeClassInfo();
        }
    }

    private void MakeClassInfo()
    {
        LinearLayout class_info_layout = findViewById(R.id.class_scroll_linearLayout);
        class_info_layout.removeAllViewsInLayout();

        for(int i = Const.CRAFTER_START+1; i < Const.CRAFTER_END; i++)
        {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View character_info = inflater.inflate(R.layout.class_info, class_info_layout, false);

            ClassJobData data = ClassJobDataDic.get(i);

            if(data == null)
            {
                System.out.println("직업" + i);
                continue;
            }

            ImageView classImage = character_info.findViewById(R.id.class_image);
            TextView classNameText = character_info.findViewById(R.id.class_name);
            TextView myExpText= character_info.findViewById(R.id.my_exp);
            TextView goalExpText= character_info.findViewById(R.id.goal_exp);
            TextView classLevelText = character_info.findViewById(R.id.class_level);

            Button classSelectButton = character_info.findViewById(R.id.button_select_class);
            classSelectButton.setOnClickListener(v ->{
                Intent intent = new Intent(getApplicationContext(), ClassJobExpCalculatorActivity.class);
                intent.putExtra("ClassJobData", data);
                startActivity(intent);
            });

            classImage.setImageResource(ClassJobData.Extension.GetClassImageResource(data));
            classNameText.setText(ClassJobData.Extension.GetClassName(data));
            myExpText.setText(String.valueOf(data.nowExp));
            goalExpText.setText(String.valueOf(data.maxExp));
            classLevelText.setText(String.valueOf(data.level));
            class_info_layout.addView(character_info);
        }
    }

    private void RequestEachOfClassExp(int id)
    {
        try
        {
            URL url = new URL("https://xivapi.com/character/" + id);
            URLConnection conn = (HttpURLConnection) url.openConnection();
            String redirect = conn.getHeaderField("Location");
            if(redirect != null)
            {
                conn = new URL(redirect).openConnection();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            Gson gson1 = new Gson();

            Map<String, Object> map = gson1.fromJson(in, Map.class);
            Map<String, Object> info = (Map<String, Object>) map.get("Character");
            ArrayList<Object> classJobs = (ArrayList<Object>) info.get("ClassJobs");

            for(int i = 0; i < classJobs.size(); i++)
            {
                Map<String, Object> classJob = (Map<String, Object>) classJobs.get(i);

                Object classIDObject = classJob.get("ClassID");
                if(classIDObject == null)
                {
                    continue;
                }
                int classID = ((Double) classIDObject).intValue();

                if(classID <= Const.CRAFTER_START || classID >= Const.CRAFTER_END)
                {
                    continue;
                }

                int classLevel = ((Double) classJob.get("Level")).intValue();
                int nowExp = ((Double) classJob.get("ExpLevel")).intValue();
                int maxExp = ((Double) classJob.get("ExpLevelMax")).intValue();
                int remainExp = ((Double) classJob.get("ExpLevelTogo")).intValue();

                ClassJobDataDic.put(classID, new ClassJobData(classID,classLevel,nowExp,maxExp,remainExp));
            }
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
    }
}

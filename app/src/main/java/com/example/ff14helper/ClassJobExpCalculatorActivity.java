package com.example.ff14helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class ClassJobExpCalculatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_job_exp_calculator);

        Intent intent = getIntent();
        if(intent != null)
        {
            ClassJobData data = (ClassJobData) intent.getSerializableExtra("ClassJobData");

            ImageView classImage = findViewById(R.id.exp_class_image);
            TextView className = findViewById(R.id.exp_class_name);
            TextView classExp = findViewById(R.id.exp_class_exp);

            classImage.setImageResource(ClassJobData.Extension.GetClassImageResource(data));
            className.setText(ClassJobData.Extension.GetClassName(data) + "Level : " +  data.level);
            classExp.setText(data.nowExp + "/" + data.maxExp);

            MakeCollectableDeliverInfo(data);
            MakeGuildLeveDeliverInfo(data);
        }
    }

    private void MakeCollectableDeliverInfo(ClassJobData classJobData)
    {
        LinearLayout collectable_layout = findViewById(R.id.collectable_layout);
        collectable_layout.removeAllViewsInLayout();

        int classID = classJobData.id;
        int currentLevel = classJobData.level;

        for(int level = Const.EndWalker_Collectable_Start_Leveling; level <= Const.EndWalker_Collectable_End_Leveling; level+= Const.Increase_Value)
        {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View deliver_info = inflater.inflate(R.layout.deliver_collectable_layout, collectable_layout, false);

            TextView deliverLevel = deliver_info.findViewById(R.id.collectable_level);
            deliverLevel.setText(String.valueOf(level));

            if(level + 1 < currentLevel || currentLevel == 90)
            {
                deliver_info.setBackgroundColor(Color.GRAY);
                collectable_layout.addView(deliver_info);
                continue;
            }


            TextView deliverCount = deliver_info.findViewById(R.id.collectable_deliver);
            int itemID = MainActivity.DataManager.GetClassJobLevelData(classID, level);
            int rewardExpRatio = MainActivity.DataManager.GetCollectableItemRewardExp(itemID);

            if(level + 1 == currentLevel)
            {
                int currentMaxExp = MainActivity.DataManager.GetExpUpData(currentLevel);
                int obtainExp = currentMaxExp * rewardExpRatio / 1000;

                int count = (int) Math.ceil((double) classJobData.remainExp / obtainExp);
                deliverCount.setText(String.valueOf(count)+"times");
            }
            else if(level == currentLevel)
            {
                int currentMaxExp = MainActivity.DataManager.GetExpUpData(currentLevel);
                int obtainExp = currentMaxExp * rewardExpRatio / 1000;

                int count = (int) Math.ceil((double) classJobData.remainExp / obtainExp);

                int nextMaxExp = MainActivity.DataManager.GetExpUpData(currentLevel+1);
                int nextObtainExp = nextMaxExp * rewardExpRatio / 1000;

                count += (int) Math.ceil((double) nextMaxExp / nextObtainExp);

                deliverCount.setText(String.valueOf(count)+"times");
            }
            else if(level > currentLevel)
            {
                int currentMaxExp = MainActivity.DataManager.GetExpUpData(level);
                int obtainExp = currentMaxExp * rewardExpRatio / 1000;

                int count = (int) Math.ceil((double) currentMaxExp / obtainExp);

                int nextMaxExp = MainActivity.DataManager.GetExpUpData(level+1);
                int nextObtainExp = nextMaxExp * rewardExpRatio / 1000;

                count += (int) Math.ceil((double) nextMaxExp / nextObtainExp);

                deliverCount.setText(String.valueOf(count)+"times");
            }

            deliver_info.setBackgroundColor(Color.WHITE);
            collectable_layout.addView(deliver_info);
        }
    }

    private void MakeGuildLeveDeliverInfo(ClassJobData classJobData)
    {
        LinearLayout guildLeve_layout = findViewById(R.id.guild_leve_layout);
        guildLeve_layout.removeAllViewsInLayout();

        int classID = classJobData.id;
        int currentLevel = classJobData.level;

        for(int level = Const.EndWalker_GuildLeve_Start_Leveling; level <= Const.EndWalker_GuildLeve_End_Leveling; level+= Const.Increase_Value)
        {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View deliver_info = inflater.inflate(R.layout.deliver_leve_layout, guildLeve_layout, false);

            TextView deliverLevel = deliver_info.findViewById(R.id.leve_level);
            deliverLevel.setText(String.valueOf(level));

            if(level + 1 < currentLevel)
            {
                deliver_info.setBackgroundColor(Color.GRAY);
                guildLeve_layout.addView(deliver_info);
                continue;
            }


            TextView deliverCount1 = deliver_info.findViewById(R.id.leve_deliver_1);
            TextView deliverCount2 = deliver_info.findViewById(R.id.leve_deliver_2);

            ArrayList<Integer> expReward = MainActivity.DataManager.GetGuildLeve(classID, level);

            if(expReward == null)
            {
                deliver_info.setBackgroundColor(Color.GRAY);
                guildLeve_layout.addView(deliver_info);
                continue;
            }

            int obtainExp1 = expReward.get(0) * 2; // Hq로 납품시 경치가 두배!
            int obtainExp2 = expReward.get(1) * 2;

            if(level + 1 == currentLevel)
            {
                int count1 = (int) Math.ceil((double) classJobData.remainExp / obtainExp1);
                deliverCount1.setText(String.valueOf(count1)+"times");

                int count2 = (int) Math.ceil((double) classJobData.remainExp / obtainExp2);
                deliverCount2.setText(String.valueOf(count2)+"times");
            }
            else if(level == currentLevel)
            {
                int count = (int) Math.ceil((double) classJobData.remainExp / obtainExp1);
                int nextMaxExp = MainActivity.DataManager.GetExpUpData(currentLevel+1);
                count += (int) Math.ceil((double) nextMaxExp / obtainExp1);
                deliverCount1.setText(String.valueOf(count)+"times");

                int count2 = (int) Math.ceil((double) classJobData.remainExp / obtainExp2);
                int nextMaxExp2 = MainActivity.DataManager.GetExpUpData(currentLevel+1);
                count2 += (int) Math.ceil((double) nextMaxExp2 / obtainExp2);
                deliverCount2.setText(String.valueOf(count2)+"times");
            }
            else if(level > currentLevel)
            {
                int currentMaxExp = MainActivity.DataManager.GetExpUpData(level);
                int count = (int) Math.ceil((double) currentMaxExp / obtainExp1);
                int nextMaxExp = MainActivity.DataManager.GetExpUpData(level+1);
                count += (int) Math.ceil((double) nextMaxExp / obtainExp1);
                deliverCount1.setText(String.valueOf(count)+"times");

                int currentMaxExp2 = MainActivity.DataManager.GetExpUpData(level);
                int count2 = (int) Math.ceil((double) currentMaxExp2 / obtainExp2);
                int nextMaxExp2 = MainActivity.DataManager.GetExpUpData(level+1);
                count2 += (int) Math.ceil((double) nextMaxExp2 / obtainExp2);
                deliverCount2.setText(String.valueOf(count2)+"times");
            }

            deliver_info.setBackgroundColor(Color.WHITE);
            guildLeve_layout.addView(deliver_info);
        }
    }

}

package com.example.ff14helper;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Error").setMessage("Data Error from xiv Api");

        Button searchButton = (Button)findViewById(R.id.Button_Search);
        searchButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v)
            {
                ClickSearchButton();
            }
        });

        Thread searchThread = new Thread(() -> DataManager.LoadClassJobLevelData());
        searchThread.start();

        Thread expThread = new Thread(() -> DataManager.LoadLeveUpData());
        expThread.start();

        Thread guildLeveThread = new Thread(() -> DataManager.LoadGuildLeve());
        guildLeveThread.start();

        try {
            searchThread.join();
            expThread.join();
            guildLeveThread.join();
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
        finally {
            System.out.println("DataLoad Finish");
        }
    }


    private void ClickSearchButton()
    {
        Intent intent = new Intent(getApplicationContext(), CharacterSearchResultActivity.class);
        startActivity(intent);
    }

    public static class DataManager
    {
        private static Map<Integer, Map<Integer, Integer>> CollectableItemByLevel = new HashMap<>(); // ClassJobID : [ QuestLevel : ItemID ]
        private static Map<Integer, Integer> CollectableItemRewardExp = new HashMap<>(); // ItemID : RewardExpRatio
        private static Map<Integer,Integer> ExpUpDic = new HashMap<>(); // Level : GoalExp

        private static Map<Integer,Map<Integer, ArrayList<Integer>>> GuildLeveExpReward = new HashMap<>(); // ClassJobID : [ ClassLevel : ExpReward ]

        public static ArrayList<Integer> GetGuildLeve(int classJobID, int classLevel)
        {
            if(!GuildLeveExpReward.containsKey(classJobID))
            {
                Log.e("DataManager : GuildLeveExpReward","No Data : classJobID :" + classJobID);
                return null;
            }

            Map<Integer, ArrayList<Integer>> ExpRewardDic = GuildLeveExpReward.get(classJobID);

            if(!ExpRewardDic.containsKey(classLevel))
            {
                Log.e("DataManager : ExpRewardDic","No Data : classLevel :" + classLevel);
                return null;
            }

            return ExpRewardDic.get(classLevel);
        }

        public static void LoadGuildLeve()
        {
            try {
                for(int classJobID = Const.CRAFTER_START+1; classJobID < Const.CRAFTER_END; classJobID++)
                {
                    for(int classLevel = 80; classLevel < 90; classLevel+=2)
                    {
                        int classJobCategory = classJobID+1;
                        URL url = new URL("https://xivapi.com/Search?indexes=leve&string=&filters=ClassJobLevel="+ classLevel +"," +
                                "ClassJobCategory.ID="+ classJobCategory +"&columns=GameContentLinks,ID,Name,ClassJobCategory,ClassJobLevel,ExpFactor,ExpReward");

                        URLConnection conn = (HttpURLConnection) url.openConnection();
                        String redirect = conn.getHeaderField("Location");
                        if(redirect != null)
                        {
                            conn = new URL(redirect).openConnection();
                        }

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        Gson gson1 = new Gson();

                        Map<String, Object> map = gson1.fromJson(in, Map.class);

                        ArrayList<Object> result = (ArrayList<Object>) map.get("Results");

                        if(!GuildLeveExpReward.containsKey(classJobID))
                        {
                            GuildLeveExpReward.put(classJobID,new HashMap<>());
                        }
                        ArrayList<Integer> expRewardList = new ArrayList<>();

                        for(int i = 0; i < result.size(); i++)
                        {
                            Map<String, Object> resultItem = (Map<String, Object>) result.get(i);
                            int expReward = ((Double)resultItem.get("ExpReward")).intValue();
                            expRewardList.add(expReward);
                        }

                        Map<Integer, ArrayList<Integer>> expRewardDic = GuildLeveExpReward.get(classJobID);
                        expRewardDic.put(classLevel, expRewardList);
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }


        public static int GetClassJobLevelData(int classJobID, int questLevel)
        {
            if(!CollectableItemByLevel.containsKey(classJobID))
            {
                Log.e("DataManager : CollectableItemByLevel","No Data : classJobID :" + classJobID);
                return -1;
            }

            Map<Integer, Integer> QuestLevelAndItemID = CollectableItemByLevel.get(classJobID);

            if(!QuestLevelAndItemID.containsKey(questLevel))
            {
                Log.e("DataManager : CollectableItemByLevel","No Data : QuestLevel :" + questLevel);
                return -1;
            }

            return QuestLevelAndItemID.get(questLevel);
        }

        public static int GetCollectableItemRewardExp(int itemID)
        {
            if(!CollectableItemRewardExp.containsKey(itemID))
            {
                Log.e("DataManager : CollectableItemRewardExp","No Data : itemID :" + itemID);
                return 171;
            }

            return CollectableItemRewardExp.get(itemID);
        }


        public static int GetExpUpData(int level)
        {
            if(level >= 90)
            {
                return 0;
            }

            if(!ExpUpDic.containsKey(level))
            {
                Log.e("DataManager : ExpUpDic","No Data : Level :" + level);
                return 0;
            }

            return ExpUpDic.get(level);
        }

        public static void LoadClassJobLevelData()
        {
            try {
                URL url = new URL("https://xivapi.com/CollectablesShopItem?limit=3000&columns=ID,Item.ID,CollectablesShopItemGroup,CollectablesShopRewardScrip");
                URLConnection conn = (HttpURLConnection) url.openConnection();
                String redirect = conn.getHeaderField("Location");
                if(redirect != null)
                {
                    conn = new URL(redirect).openConnection();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Gson gson1 = new Gson();

                Map<String, Object> map = gson1.fromJson(in, Map.class);

                ArrayList<Object> result = (ArrayList<Object>) map.get("Results");

                for(int i = 0; i < result.size(); i++)
                {
                    Map<String, Object> resultItem = (Map<String, Object>) result.get(i);
                    Map<String, Object> collectablesShopItemGroup = (Map<String, Object>) resultItem.get("CollectablesShopItemGroup");


                    if (collectablesShopItemGroup == null)
                    {
                        continue;
                    }

                    int collectablesShopItemGroupID = ((Double) collectablesShopItemGroup.get("ID")).intValue();

                    if (collectablesShopItemGroupID != 62)
                    {
                        continue;
                    }

                    Map<String, Object> itemGroup = (Map<String, Object>) resultItem.get("Item");

                    if (itemGroup == null) {
                        continue;
                    }

                    int itemID = ((Double) itemGroup.get("ID")).intValue();


                    /*
                    Thread loadCollectableThread = new Thread(()->
                    {
                        try {
                            LoadCollectableItemByLevel(itemID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    loadCollectableThread.start();
                    */

                    LoadCollectableItemByLevel(itemID);
                    Map<String, Object> collectablesShopRewardScrip = (Map<String, Object>) resultItem.get("CollectablesShopRewardScrip");

                    if (collectablesShopRewardScrip == null)
                    {
                        continue;
                    }

                    int collectablesShopRewardScripID = ((Double) collectablesShopRewardScrip.get("ID")).intValue();

                    /*
                    Thread loadCollectableExpThread = new Thread(()->
                    {
                        try {
                            LoadCollectableItemRewardExp(itemID, collectablesShopRewardScripID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    loadCollectableExpThread.start();
                    */
                    LoadCollectableItemRewardExp(itemID, collectablesShopRewardScripID);
                }
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }

        private static void LoadCollectableItemRewardExp(int itemID, int collectablesShopRewardScripID) throws IOException
        {
            URL url2 = new URL("https://xivapi.com/CollectablesShopRewardScrip?ids=" + collectablesShopRewardScripID + "&columns=ExpRatioHigh");
            URLConnection conn2 = (HttpURLConnection) url2.openConnection();
            String redirect2 = conn2.getHeaderField("Location");
            if(redirect2 != null)
            {
                conn2 = new URL(redirect2).openConnection();
            }

            BufferedReader in2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
            Gson gson2 = new Gson();

            Map<String, Object> map2 = gson2.fromJson(in2, Map.class);
            ArrayList<Object> result2 = (ArrayList<Object>) map2.get("Results");
            Map<String,Object> expRatioHigh = (Map<String, Object>) result2.get(0);
            int exp = ((Double)expRatioHigh.get("ExpRatioHigh")).intValue();
            CollectableItemRewardExp.put(itemID, exp);
        }

        private static void LoadCollectableItemByLevel(int itemID) throws IOException
        {
            URL url2 = new URL("https://xivapi.com/Item?ids=" + itemID + "&columns=ID,Recipes");
            URLConnection conn2 = (HttpURLConnection) url2.openConnection();
            String redirect2 = conn2.getHeaderField("Location");
            if(redirect2 != null)
            {
                conn2 = new URL(redirect2).openConnection();
            }

            BufferedReader in2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
            Gson gson2 = new Gson();

            Map<String, Object> map2 = gson2.fromJson(in2, Map.class);
            ArrayList<Object> result2 = (ArrayList<Object>) map2.get("Results");

            for(int j = 0; j < result2.size(); j++)
            {
                Map<String,Object> item = (Map<String, Object>) result2.get(j);
                ArrayList<Object> itemRecipes = (ArrayList<Object>) item.get("Recipes");
                if(itemRecipes == null)
                {
                    continue;
                }

                Map<String,Object> itemRecipe = (Map<String, Object>) itemRecipes.get(0);
                int jobID = ((Double) itemRecipe.get("ClassJobID")).intValue();
                int questLevel = ((Double) itemRecipe.get("Level")).intValue();

                if(!CollectableItemByLevel.containsKey(jobID))
                {
                    CollectableItemByLevel.put(jobID,new HashMap<>());
                }

                Map<Integer, Integer> levelItemDic = CollectableItemByLevel.get(jobID);
                levelItemDic.put(questLevel, itemID);
            }
        }

        public static void LoadLeveUpData()
        {
            try
            {
                URL url = new URL("https://xivapi.com/ParamGrow?ids=80,81,82,83,84,85,86,87,88,89,90&columns=ID,ExpToNext");
                URLConnection conn = (HttpURLConnection) url.openConnection();
                String redirect = conn.getHeaderField("Location");
                if(redirect != null)
                {
                    conn = new URL(redirect).openConnection();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Gson gson1 = new Gson();

                Map<String, Object> map = gson1.fromJson(in, Map.class);

                ArrayList<Object> result = (ArrayList<Object>) map.get("Results");

                for(int i = 0; i < result.size(); i++)
                {
                    Map<String,Object> item = (Map<String, Object>) result.get(i);
                    int goalExp = ((Double) item.get("ExpToNext")).intValue();
                    int level =  ((Double) item.get("ID")).intValue();
                    ExpUpDic.put(level,goalExp);
                }
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }
    }
}


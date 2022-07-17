package com.example.ff14helper;

import java.io.Serializable;
import java.util.Locale;

public class ClassJobData implements Serializable
{
    int id;
    int level;
    int nowExp;
    int maxExp;
    int remainExp;

    public ClassJobData(int id, int level, int nowExp,int maxExp, int remainExp)
    {
        this.id = id;
        this.level = level;
        this.nowExp = nowExp;
        this.maxExp = maxExp;
        this.remainExp = remainExp;
    }

    public static class Extension
    {
        public static String GetClassName(ClassJobData classJobData)
        {
            int id = classJobData.id;

            switch (id)
            {
                case Const.CRP:
                    return "Carpenter";
                case Const.BSM:
                    return "BlackSmith";
                case Const.ARM:
                    return "Armorer";
                case Const.GSM:
                    return "GoldSmith";
                case Const.LTW:
                    return "LeatherWorker";
                case Const.WVR:
                    return "Weaver";
                case Const.ALC:
                    return "Alchemist";
                case Const.CUL:
                    return "Culinarian";
                default:
                    return "";
            }
        }

        public static int GetClassImageResource(ClassJobData classJobData)
        {
            int id = classJobData.id;

            switch (id)
            {
                case Const.CRP:
                    return R.drawable.carpenter;
                case Const.BSM:
                    return R.drawable.blacksmith;
                case Const.ARM:
                    return R.drawable.armorer;
                case Const.GSM:
                    return R.drawable.goldsmith;
                case Const.LTW:
                    return R.drawable.leatherworker;
                case Const.WVR:
                    return R.drawable.weaver;
                case Const.ALC:
                    return R.drawable.alchemist;
                case Const.CUL:
                    return R.drawable.culinarian;
                default:
                    return -1;
            }
        }
    }
}

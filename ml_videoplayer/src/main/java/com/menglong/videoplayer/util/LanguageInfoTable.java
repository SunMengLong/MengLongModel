package com.menglong.videoplayer.util;

import android.content.Context;

import com.star.util.LanguageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenweiqiang on 2017/10/30.
 */

public class LanguageInfoTable {
    private List<LanguageInfo> languageTable = null;
    private static LanguageInfoTable instance = null;
    private Context mContext;

    public static class LanguageInfo {
        public String key;
        public String shortName;
        public String fullName;

        public LanguageInfo(String key, String shortName, String fullName) {
            this.key = key;
            this.shortName = shortName;
            this.fullName = fullName;
        }
    }

    public static LanguageInfoTable getInstance(Context context) {
        if (instance == null) {
            instance = new LanguageInfoTable();
            instance.mContext = context.getApplicationContext();
            instance.initLanguageInfo();
        }
        return instance;
    }

    public void initLanguageInfo() {
        if (this.languageTable == null) {
            this.languageTable = new ArrayList<LanguageInfo>();
            this.languageTable.add(new LanguageInfo("eng", "EN", "English"));
            this.languageTable.add(new LanguageInfo("fre", "FR", "Français"));
            this.languageTable.add(new LanguageInfo("swa", "SW", "Kiswahili"));
            this.languageTable.add(new LanguageInfo("por", "PT", "Portugues"));
            this.languageTable.add(new LanguageInfo("yor", "YO", "Yorùbá"));
            this.languageTable.add(new LanguageInfo("lug", "LG", "Luganda"));
            this.languageTable.add(new LanguageInfo("hin", "HI", "Hindi"));
        }
    }

    public int getLanguageOrderIndex(String key) {
        for (int i = 0; i < languageTable.size(); i++) {
            LanguageInfo language = languageTable.get(i);
            if (language.key.equals(key)) {
                return i;
            }
        }
        return -1;
    }


    public String getLanguageKey(String shortName) {
        for (int i = 0; i < languageTable.size(); i++) {
            LanguageInfo language = languageTable.get(i);
            if (language.shortName.equals(shortName)) {
                return language.key;
            }
        }
        return null;
    }

    public String getLanguageFullName(String shortName) {
        for (int i = 0; i < languageTable.size(); i++) {
            LanguageInfo language = languageTable.get(i);
            if (language.shortName.equals(shortName)) {
                return language.fullName;
            }
        }
        return null;
    }

    public LanguageInfoTable.LanguageInfo getLanguageInfo(String key) {
        if (key != null) {
            for (int i = 0; i < languageTable.size(); i++) {
                LanguageInfo language = languageTable.get(i);
                if (language.key.equals(key)) {
                    return language;
                }
            }
        }
        return null;
    }

    public String getLanguageKeyList() {
        String keyList = "";
        //app语言优先级最高，其次是正常顺序
        String langApp = LanguageUtil.getInstance(mContext).getAppLanguage();
        langApp = this.getLanguageKey(langApp.toUpperCase());
        if (langApp != null) {
            keyList = langApp + ";";
        }
        for (int i = 0; i < languageTable.size(); i++) {
            LanguageInfo language = languageTable.get(i);
            if (!language.key.equals(langApp)) {
                keyList += language.key;
                keyList += ";";
            }
        }
        return keyList;
    }

    /*
    获取默认音轨语言
     */
    public String readDefaultAudioLanguageKey(String channelID, int type) {
        String key = LanguageUtil.getInstance(mContext).getAudioLanguage();
        if (key.equals("")) {
            key = LanguageUtil.getInstance(mContext).getAppLanguage();
            key = this.getLanguageKey(key.toUpperCase());
        }
        return key;
    }

    /*
    设置默认音轨语言
     */
    public void saveDefaultAudioLanguageKey(String language) {
        LanguageUtil.getInstance(mContext).setAudioLanguage(language);
    }
}

package com.example.bluetooth;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DowonYoon on 2017-07-11.
 */

@IgnoreExtraProperties
public class FirebasePost {
    public int menu1;
    public int menu2;
    public int menu3;
    public int menu4;
    public String msg;
    public String type;
//    public String gender;

    public FirebasePost(){
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public FirebasePost(int menu1, int menu2, int menu3, int menu4, String msg, String type) {
        this.menu1 = menu1;
        this.menu2 = menu2;
        this.menu3 = menu3;
        this.menu4 = menu4;
        this.msg = msg;
        this.type = type;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("menu1", menu1);
        result.put("menu2", menu2);
        result.put("menu3", menu3);
        result.put("menu4", menu4);
        result.put("msg", msg);
        result.put("type", type);
        return result;
    }
}

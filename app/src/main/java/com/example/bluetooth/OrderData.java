package com.example.bluetooth;

public class OrderData {
    String type;
    MyAdapter adapter;

    OrderData(String _type, MyAdapter _adapter){
        type = _type;
        adapter = _adapter;
    }
}

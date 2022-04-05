// 하나의 주문에 사용되는 주문 유형, 요청 사항, 주문 내역을 구조화

package com.example.bluetooth;

public class OrderData {
    String type;
    String request;
    MyAdapter adapter;

    OrderData(String _type, String _request, MyAdapter _adapter){
        type = _type;
        request = _request;
        adapter = _adapter;
    }
}

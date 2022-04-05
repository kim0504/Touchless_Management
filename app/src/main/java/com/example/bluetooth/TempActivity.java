package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TempActivity extends AppCompatActivity {

    //======================================== BLUETOOTH ========================================//
    TextView mTvReceiveData;
    Button mBtnConnect; //블루투스 연결에 사용하는 버튼

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //블루투스-아두이노 연결 번호

    //======================================== LISTVIEW =========================================//
    String[] menu_list = {"곱창", "막창", "대창", "모둠"}; // 메뉴 변경 시 수정 필요

    // ListviewList, AdapterList, dataArr: 화면에 출력되는 각 주문 칸에 적용되는 파트
    // TextviewList : 주문 유형, 번호, 색상을 변경할 때 사용하는 리스트
    // CompleteList : 완료된 주문의 유형,이름,내역,요청 사항을 저장하는 리스트
    // OrderTypeNumList : 주문의 유형과 번호를 저장하는 리스트
    // LayoutList : 8개의 레이아웃을 저장하는 리스트
    // Request_TextviewList, Request_List : 요청 사항 관련 리스트
    List<ListView> ListviewList = new ArrayList<>();
    List<MyAdapter> AdapterList = new ArrayList<>();
    ArrayList<MyData> dataArr;
    List<TextView> TextviewList = new ArrayList<>();
    List<OrderData> CompleteList = new ArrayList<>();
    List<String> OrderTypeNumList = new ArrayList<>();
    List<LinearLayout> LayoutList = new ArrayList<>();
    List<TextView> Request_TextviewList = new ArrayList<>();
    List<String> Request_List = new ArrayList<>();

    int loc = 0; //현재 위치 표현
    int turn_flag = 0; // 주문 페이지와 완료 페이지를 판단
    long waitTime = 0; // 주문 페이지와 완료 페이지를 넘어가는 과정에 사용

    //======================================== CLOCK =========================================//
    TextView clockTextView ;
    private static Handler mHandler ;

    //======================================== FIREBASE ========================================//
    private DatabaseReference mPostReference;
    long order_count = 0;
    int temp_id;
    int menu1;
    int menu2;
    int menu3;
    int menu4;
    String msg;
    String type = "";
    String strTime = "data";

    Button turn_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_temp);

    //======================================== FIREBASE ========================================//
        Calendar cal = Calendar.getInstance() ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        strTime = sdf.format(cal.getTime());

        LoadInitiate();

    //========================================== CLOCK ==========================================//
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Calendar cal = Calendar.getInstance() ;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String strTime = sdf.format(cal.getTime());
                clockTextView = findViewById(R.id.Clock) ;
                clockTextView.setText(strTime) ;
            }
        } ;

        class NewRunnable implements Runnable {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace() ;
                    }
                    mHandler.sendEmptyMessage(0) ;

                    getFirebaseDatabase(); //파이어베이스 실시간 업데이트 함수
                }
            }
        }

        NewRunnable nr = new NewRunnable() ;
        Thread t = new Thread(nr) ;
        t.start() ;

    //======================================== LISTVIEW =========================================//
        ListView listView0 = findViewById(R.id.listView0);
        ListView listView1 = findViewById(R.id.listView1);
        ListView listView2 = findViewById(R.id.listView2);
        ListView listView3 = findViewById(R.id.listView3);
        ListView listView4 = findViewById(R.id.listView4);
        ListView listView5 = findViewById(R.id.listView5);
        ListView listView6 = findViewById(R.id.listView6);
        ListView listView7 = findViewById(R.id.listView7);

        ListviewList.add(listView0);
        ListviewList.add(listView1);
        ListviewList.add(listView2);
        ListviewList.add(listView3);
        ListviewList.add(listView4);
        ListviewList.add(listView5);
        ListviewList.add(listView6);
        ListviewList.add(listView7);

        TextView textView0 = findViewById(R.id.order_num0);
        TextView textView1 = findViewById(R.id.order_num1);
        TextView textView2 = findViewById(R.id.order_num2);
        TextView textView3 = findViewById(R.id.order_num3);
        TextView textView4 = findViewById(R.id.order_num4);
        TextView textView5 = findViewById(R.id.order_num5);
        TextView textView6 = findViewById(R.id.order_num6);
        TextView textView7 = findViewById(R.id.order_num7);

        TextviewList.add(textView0);
        TextviewList.add(textView1);
        TextviewList.add(textView2);
        TextviewList.add(textView3);
        TextviewList.add(textView4);
        TextviewList.add(textView5);
        TextviewList.add(textView6);
        TextviewList.add(textView7);

        LinearLayout layout0 = findViewById(R.id.layout0);
        LinearLayout layout1 = findViewById(R.id.layout1);
        LinearLayout layout2 = findViewById(R.id.layout2);
        LinearLayout layout3 = findViewById(R.id.layout3);
        LinearLayout layout4 = findViewById(R.id.layout4);
        LinearLayout layout5 = findViewById(R.id.layout5);
        LinearLayout layout6 = findViewById(R.id.layout6);
        LinearLayout layout7 = findViewById(R.id.layout7);

        LayoutList.add(layout0);
        LayoutList.add(layout1);
        LayoutList.add(layout2);
        LayoutList.add(layout3);
        LayoutList.add(layout4);
        LayoutList.add(layout5);
        LayoutList.add(layout6);
        LayoutList.add(layout7);

        TextView request0 = findViewById(R.id.request_textview0);
        TextView request1 = findViewById(R.id.request_textview1);
        TextView request2 = findViewById(R.id.request_textview2);
        TextView request3 = findViewById(R.id.request_textview3);
        TextView request4 = findViewById(R.id.request_textview4);
        TextView request5 = findViewById(R.id.request_textview5);
        TextView request6 = findViewById(R.id.request_textview6);
        TextView request7 = findViewById(R.id.request_textview7);

        Request_TextviewList.add(request0);
        Request_TextviewList.add(request1);
        Request_TextviewList.add(request2);
        Request_TextviewList.add(request3);
        Request_TextviewList.add(request4);
        Request_TextviewList.add(request5);
        Request_TextviewList.add(request6);
        Request_TextviewList.add(request7);


    //======================================== BLUETOOTH ========================================//
        mTvReceiveData = (TextView)findViewById(R.id.tvReceiveData);
        mBtnConnect = (Button)findViewById(R.id.btnConnect);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });

        mBluetoothHandler = new Handler(){
            @SuppressLint({"HandlerLeak", "ResourceAsColor", "ResourceType"})
            public void handleMessage(Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mTvReceiveData.setText("");
                    mTvReceiveData.setText(readMessage);

                    switch (mTvReceiveData.getText().charAt(0)){
                        case 'o': // 가까이 또는 멀어지는 움직임일 때 받는 데이터
                            if(turn_flag == 0){ // 주문 페이지가 0, 완료 페이지는 1
                                if(ListviewList.get(loc).getAdapter() != null) {
                                    if (ListviewList.get(loc).getAdapter().getCount() != 0) {
                                        Toast.makeText(getApplicationContext(), "조리 완료!", Toast.LENGTH_SHORT).show();

                                        CompleteList.add(0, new OrderData((String) TextviewList.get(loc).getText(), (String) Request_TextviewList.get(loc).getText(), AdapterList.get(loc)));
                                        AdapterList.remove(loc);
                                        OrderTypeNumList.remove(loc);
                                        Request_List.remove(loc);

                                        PrintOrder();

                                        LayoutList.get(loc).setBackgroundResource(R.drawable.line);
                                        loc = 0; // 선택 칸 초기화
                                        LayoutList.get(loc).setBackgroundResource(R.drawable.selected);
                                    }
                                    else Toast.makeText(getApplicationContext(), "주문이 없습니다.", Toast.LENGTH_SHORT).show(); // 0번 레이아웃에서 주문이 없는 경우
                                }
                                else Toast.makeText(getApplicationContext(), "주문이 없습니다.", Toast.LENGTH_SHORT).show(); // 주문을 한 번도 받지 않았을 때
                            }
                            break;
                        case 'L': // 왼쪽으로 가는 움직이면 받는 데이터
                            if(loc>0){
                                LayoutList.get(loc).setBackgroundResource(R.drawable.line); loc -= 1;
                                LayoutList.get(loc).setBackgroundResource(R.drawable.selected); }
                            break;
                        case 'R': // 오른쪽으로 가는 움직이면 받는 데이터
                            if(loc<7){
                                LayoutList.get(loc).setBackgroundResource(R.drawable.line); loc += 1;
                                LayoutList.get(loc).setBackgroundResource(R.drawable.selected); }
                            break;
                        case 'U': // 위쪽으로 가는 움직이면 받는 데이터
                            if(loc>3){
                                LayoutList.get(loc).setBackgroundResource(R.drawable.line); loc -= 4;
                                LayoutList.get(loc).setBackgroundResource(R.drawable.selected); }
                            else ChangePage();
                            break;
                        case 'D': // 아래쪽으로 가는 움직이면 받는 데이터
                            if(loc<4){
                                LayoutList.get(loc).setBackgroundResource(R.drawable.line); loc += 4;
                                LayoutList.get(loc).setBackgroundResource(R.drawable.selected); }
                            break;
                    }
                }
            }
        };
    }



    @SuppressLint("MissingPermission")
    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("MissingPermission")
    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }


    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 화면에 리스트뷰를 재출력 해주는 기능
    public void PrintOrder(){
        if(AdapterList.size() > 8){ // 주문이 8개를 넘었을 시에 실행
            for(int i=0; i<8; i++){
                ListviewList.get(i).setAdapter(AdapterList.get(i));
                StoreOrDeliver(i);
                Request_TextviewList.get(i).setText(Request_List.get(i));
            }
        }
        else{ // 주문이 8개 이하인 경우 실행
            for(int i=0; i<AdapterList.size(); i++){
                ListviewList.get(i).setAdapter(AdapterList.get(i));
                StoreOrDeliver(i);
                Request_TextviewList.get(i).setText(Request_List.get(i));
            }
            // 내용 없는 리스트뷰로 만드는 코드
            for(int i=AdapterList.size(); i<8; i++){
                ListviewList.get(i).setAdapter(new MyAdapter(getApplicationContext(), R.layout.list_item, new ArrayList<MyData>()));
                TextviewList.get(i).setText("주문");
                TextviewList.get(i).setBackgroundResource(R.color.origin);
                Request_TextviewList.get(i).setText("");
            }
        }
    }

    // 주문 유형에 따라 UI를 바꿔주는 기능
    public void StoreOrDeliver(int loc){
        if(OrderTypeNumList.get(loc).charAt(0) == '배'){
            TextviewList.get(loc).setText(OrderTypeNumList.get(loc));
            TextviewList.get(loc).setBackgroundResource(R.color.deliver);
        }
        else{
            TextviewList.get(loc).setText(OrderTypeNumList.get(loc));
            TextviewList.get(loc).setBackgroundResource(R.color.store);
        }
    }

    // 주문 페이지와 완료 페이지가 바뀌는 과정과 완료 페이지의 출력에 사용하는 함수
    @SuppressLint("ResourceAsColor")
    public void CheckTurnFlag(Button TurnBtn){
        if(turn_flag == 0){ // 주문 페이지로 바뀌는 과정과 완료 내역 출력하는 부분
            TurnBtn.setText("완료 페이지");
            TurnBtn.setBackgroundResource(R.drawable.complete_btn_shape);
            TurnBtn.setTextColor(Color.BLACK);
            mBtnConnect.setVisibility(View.GONE);
            LayoutList.get(loc).setBackgroundResource(R.drawable.line);

            if(CompleteList.size() > 8){
                for(int i=0; i<8; i++){
                    ListviewList.get(i).setAdapter(CompleteList.get(i).adapter);
                    TextviewList.get(i).setText(CompleteList.get(i).type);
                    TextviewList.get(i).setBackgroundResource(R.color.purple_200);
                    Request_TextviewList.get(i).setText(CompleteList.get(i).request);
                }
            }
            else{
                for(int i=0; i<CompleteList.size(); i++){
                    ListviewList.get(i).setAdapter(CompleteList.get(i).adapter);
                    TextviewList.get(i).setText(CompleteList.get(i).type);
                    TextviewList.get(i).setBackgroundResource(R.color.purple_200);
                    Request_TextviewList.get(i).setText(CompleteList.get(i).request);
                }
                for(int i=CompleteList.size(); i<8; i++){
                    ListviewList.get(i).setAdapter(new MyAdapter(getApplicationContext(),
                            R.layout.list_item, new ArrayList<MyData>()));
                    TextviewList.get(i).setText("주문");
                    TextviewList.get(i).setBackgroundResource(R.color.origin);
                }
            }
            turn_flag = 1;
        }
        else{ // 주문 페이지로 넘어가기 전에 UI 및 초기화 하는 과정
            TurnBtn.setText("주문 페이지");
            TurnBtn.setBackgroundResource(R.drawable.btn_shape);
            TurnBtn.setTextColor(Color.WHITE);
            mBtnConnect.setVisibility(View.VISIBLE);
            loc = 0;
            LayoutList.get(loc).setBackgroundResource(R.drawable.selected);
            PrintOrder();
            turn_flag = 0;
        }
    }

    //위로 가는 동작 후 2초 안에 한 번 더 동작해야 넘어가게 만든 기능
    public void ChangePage(){
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - waitTime;

        if(0 <= gapTime && 3000 >= gapTime) {
            CheckTurnFlag(findViewById(R.id.turn_btn)); }
        else {
            waitTime = curTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show(); }
    }

    // 앱 실행 시 초기에 파이어베이스에 저장 되는 데이터베이스의 경로를 가져오는 함수
    public void LoadInitiate(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                order_count = dataSnapshot.getChildrenCount();
                Log.e("order_count", ": "+order_count);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        Query sortbyAge = FirebaseDatabase.getInstance().getReference().child(strTime);
        sortbyAge.addListenerForSingleValueEvent(postListener);
    }

    // 앱 실행 중 파이어베이스의 데이터가 변경된 경우 실행되는 함수
    public void getFirebaseDatabase(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    FirebasePost get = postSnapshot.getValue(FirebasePost.class);
                    String[] info = {String.valueOf(get.menu1), String.valueOf(get.menu2),
                            String.valueOf(get.menu3), String.valueOf(get.menu4), get.msg, get.type};
                    String Result = info[0]+info[1]+info[2]+info[3]+info[4]+info[5];

                    // 마지막에 들어온 새 주문을 찾아 어뎁터 리스트에 추가하는 과정
                    if(Long.parseLong(postSnapshot.getKey()) == order_count+1){
                        dataArr = new ArrayList<MyData>();
                        for(int i=0; i<4; i++){
                            if(info[i].charAt(0) != '0'){
                                dataArr.add(new MyData(menu_list[i], info[i]));
                            }
                        }
                        Request_List.add("요청 사항 : "+info[4]);
                        if(info[5].charAt(0) == '홀'){
                            OrderTypeNumList.add("홀 주문 - "+postSnapshot.getKey()); }
                        else OrderTypeNumList.add("배달 주문 - "+postSnapshot.getKey());

                        MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, dataArr);
                        AdapterList.add(adapter);
                        //
                        PrintOrder();
                        order_count += 1;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase","loadPost:onCancelled", databaseError.toException());
            }
        };
        Query sortbyAge = FirebaseDatabase.getInstance().getReference().child(strTime);
        sortbyAge.addListenerForSingleValueEvent(postListener);
    }
}

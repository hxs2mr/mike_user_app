package open.lanya.com.ipad_mike;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import open.lanya.com.ipad_mike.bluetooth.AnimationFactory;
import open.lanya.com.ipad_mike.bluetooth.BluetoothLeService;
import open.lanya.com.ipad_mike.bluetooth.SampleGattAttributes;
import open.lanya.com.ipad_mike.net.RestClent;
import open.lanya.com.ipad_mike.net.callback.ISuccess;

import static open.lanya.com.ipad_mike.util.TimeUtils.dateToStamp;

public class MainActivity extends Activity implements OnClickListener {
    private MainActivity context;
    public static BluetoothLeService mBluetoothLeService;
    private String Tag="MainActivity";
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;
    private final int PERMISSION_PHONE_STATE= 0xb02;

    /** 显示当前进度 */
    private AppCompatTextView dialog_text;

    /** 圆形进度提示图片 */
    private AppCompatImageView imgProgress;

    private RelativeLayout pro_relative ;

    private AppCompatImageView xueya_connect_status_iv;//血压的计连接的状态图
    private AppCompatImageView wen_connect_status_iv;//血压的计连接的状态图
    private AppCompatImageView xueyan_connect_status_iv;//血压的计连接的状态图

    private AppCompatTextView xueya_onclick_tv;//血压点击连接
    private AppCompatTextView wen_onclick_tv;//血糖点击连接
    private AppCompatTextView xueyan_onclick_tv;//血氧点击连接

    private AppCompatTextView xueya_shou_value_tv;//收缩压的值

    private AppCompatTextView xueya_shu_value_tv;//舒张压的值

    private AppCompatTextView mai_value_tv;//脉搏的值

    private AppCompatTextView yan_value_tv;//血氧的值

    private AppCompatTextView wen_value_tv;//体温的值


    private AppCompatTextView xueya_shou_time_tv;//收缩压测量的时间
    private AppCompatTextView xueya_shu_time_tv;//舒张压测的时间
    private AppCompatTextView mai_time_tv;//脉搏测量的时间
    private AppCompatTextView xueyan_time_tv;//血氧测量的时间
    private AppCompatTextView wen_time_tv;//温度测量的时间
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //禁用home件定义标志
    private int connect_number=0;//连接的设备数;
    private String UUid="";
    public static Mac mac;
     private  int put_count = 0;
    BluetoothAdapter mBluetoothAdapter ;//= BluetoothAdapter.getDefaultAdapter();

    private int REQUEST_ENABLE_BT=0x11;
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0x11:
                    String log=(String)msg.obj;
                    String []logs=log.split(",");
                    //xueya_shou_value_tv.setText(logs[0]);
                    //连接设备
                    break;
                case 0x22:
                    String status_c=(String)msg.obj;
                    //xueya_shou_value_tv.setText(status_c);
                    if(status_c.contains("FSRKB_BT-001"))//代表血压计连接成功
                    {
                        xueya_connect_status_iv.setBackgroundResource(R.mipmap.yes_connect);
                        xueya_onclick_tv.setText("已连接");
                        xueya_onclick_tv.setTextColor(Color.parseColor("#798DAA"));


                    }else if(status_c.contains("FSRKB-EWQ01")){//代表体温计连接成功
                        wen_connect_status_iv.setBackgroundResource(R.mipmap.yes_connect);
                        wen_onclick_tv.setText("已连接");
                        wen_onclick_tv.setTextColor(Color.parseColor("#798DAA"));
                    }
                    break;
                case 0x33:
                    String status_d=(String)msg.obj;
                   // xueya_shou_value_tv.setText(status_d);
                    if(status_d.contains("FSRKB_BT-001"))//代表血压计连接成功
                    {
                        xueya_connect_status_iv.setBackgroundResource(R.mipmap.no_connect);
                        xueya_onclick_tv.setText("点击连接");
                        xueya_onclick_tv.setTextColor(Color.parseColor("#49A2FF"));
                    }else if(status_d.contains("FSRKB-EWQ01")){//代表体温计连接成功
                        wen_connect_status_iv.setBackgroundResource(R.mipmap.no_connect);
                        wen_onclick_tv.setText("点击连接");
                        wen_onclick_tv.setTextColor(Color.parseColor("#49A2FF"));
                    }
                    break;
                case 0x44:
                    String ble_data=(String)msg.obj;

                    if(ble_data.contains("FSRKB_BT-001")){//代表是血压计传过来得数据
                                    //                              d0c204cb 00 49 01 9f
                                //你获取的数据是从FSRKB_BT-001发出的d0c205cc  82  4b  46    00 52 结束得时候
                        ble_data = ble_data.replace("你获取的数据是从FSRKB_BT-001发出的d0c20","");
                        if(ble_data.substring(0,1).equals("5"))//代表测试完成
                        {
                           String  shou_16_value =  ble_data.substring(5,7);//截取收缩压得16进制得数据
                            int shou_data = Integer.parseInt(shou_16_value,16);
                            xueya_shou_value_tv.setText(shou_data+"");

                            String  shu_16_value =  ble_data.substring(3,5);//截取舒张压得16进制得数据
                            int shu_data = Integer.parseInt(shu_16_value,16);
                            xueya_shu_value_tv.setText(shu_data+"");

                            String  mai_16_value =  ble_data.substring(7,9);//截取脉搏得16进制得数据
                            int mai_data = Integer.parseInt(mai_16_value,16);
                            mai_value_tv.setText(mai_data+"");


                            SimpleDateFormat    formatter    =   new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss     ");
                            Date    curDate    =   new Date(System.currentTimeMillis());//获取当前时间
                            String    str    =    formatter.format(curDate);
                            if(shou_data>0&&shu_data>0)
                            {
                                put_data(shou_data+""+"/"+shu_data+"",0,   dateToStamp(str,"yyyy-MM-dd HH:mm:ss"));
                            }

                            if(mai_data>0)
                            {
                                put_data(mai_data+"",3,   dateToStamp(str,"yyyy-MM-dd HH:mm:ss"));
                            }


                         xueya_shou_time_tv.setText(str);
                         xueya_shu_time_tv.setText(str);
                         mai_time_tv.setText(str);
                        }else {//还在测试当中
                            String  shou_16_value =  ble_data.substring(5,7);//截取收缩压得16进制得数据
                            int shou_data = Integer.parseInt(shou_16_value,16);
                            xueya_shou_value_tv.setText(shou_data+"");
                        }

                    }else  if(ble_data.contains("FSRKB-EWQ01"))//代表是体温计传过来得数据
                    {         //0220dd08ffee010000000000e5  失败得
                        //成功得0220dd08ff015e000000000055
                        ble_data=ble_data.replace("你获取的数据是从FSRKB-EWQ01发出的0220dd08ff","");
                        ble_data=ble_data.substring(0,4);
                        if(ble_data.equals("ee01"))
                        {
                            wen_value_tv.setText("测量有误");
                        }else {
                            int wen_data = Integer.parseInt(ble_data,16);
                            float wen_data_end = (float)wen_data/10;
                            wen_value_tv.setText(wen_data_end+"");
                            if(put_count == 0)
                            {
                                SimpleDateFormat    formatter    =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date    curDate    =   new Date(System.currentTimeMillis());//获取当前时间
                                String    str    =    formatter.format(curDate);
                                wen_time_tv.setText(str);
                                put_data(wen_data_end+"",2,dateToStamp(str,"yyyy-MM-dd HH:mm:ss"));
                            }
                            put_count++;
                            if(put_count==10)
                            {
                                put_count =0;
                            }
                        }
                    }
                    break;
                case 0x55:
                    String write_success=(String)msg.obj;
                    xueya_shu_value_tv.setText(write_success);
                    break;
                case 0x66:
                    String device_status=(String)msg.obj;
                    xueya_shou_value_tv.setText(device_status);
                    break;
                case 1111:
                    pro_relative.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }

        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
       // this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED); ///禁用home关键代码
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED||this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE,}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        mac = new Mac(this);
        initViews();
        initEvents();
        Intent serviceIntent=new Intent(context,BluetoothLeService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= 11)
        {
            setFinishOnTouchOutside(false);//点击外部消失与否
        }
        startCon();
        dismiss(10);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(receiver);


    }

    private  IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
        intentFilter.addAction(BluetoothLeService.SEARCH_DEVICE);
        intentFilter.addAction(BluetoothLeService.WRITE_SUCCEED);
        intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY);
        return intentFilter;
    }

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            System.out.println("*******action*******:"+action);
            Message message=new Message();
            if(BluetoothLeService.SEARCH_DEVICE.equals(action)){
                String devices=intent.getStringExtra("device_name");
                message.what=0x11;
                message.obj=devices;
                System.out.println("*******devices*******:"+devices);
                handler.sendMessage(message);
            }else if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                String status=intent.getStringExtra("connected");
                imgProgress.clearAnimation();
                imgProgress.setImageResource(R.mipmap.bluecon_1);
                dialog_text.setText("连接成功");
                dismiss(1);
                message.what=0x22;
                message.obj=status;
                handler.sendMessage(message);
                ++connect_number;
            }else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                String status=intent.getStringExtra("disconnected");

                imgProgress.clearAnimation();
                imgProgress.setImageResource(R.mipmap.bluecon_0);
                dialog_text.setText("连接失败");
                //dismiss(3);
                System.out.println("*****断开连接后status*****"+status);
                //断开连接后
                if(!action.equals("search_device"))
                {
                    --connect_number;
                    if(connect_number<0)
                    {
                        connect_number =0 ;
                    }
                }

                System.out.println("*****connect_number*****"+connect_number);
                message.what=0x33;
                message.obj=status;
                handler.sendMessage(message);
                if(connect_number == 0)
                {
                        startCon();
                        dismiss(10);
                }else {
                    dismiss(2);
                }

            }else if(BluetoothLeService.EXTRA_DATA.equals(action)){
                String data=intent.getStringExtra("ble_data");
                message.what=0x44;
                message.obj=data;
                System.out.println("*******data*******:"+data);
                handler.sendMessage(message);
            }else if(BluetoothLeService.WRITE_SUCCEED.equals(action)){
                String data=intent.getStringExtra("write_succeed");
                message.what=0x55;
                message.obj=data;
                handler.sendMessage(message);
            }else if(BluetoothLeService.ACTION_NOTIFY.equals(action)){
                String data=intent.getStringExtra("devicename_status");
                message.what=0x66;
                message.obj=data;
               // System.out.println("*******设备的状态*******:"+data);
                handler.sendMessage(message);

            }else if(BluetoothLeService.CONNNECT_TIME.equals(action))
            {
                String data=intent.getStringExtra("connect_time");
                System.out.println(data+"===================");
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(Tag, "service成功");

            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            String names2="FSRKB_BT-001,FSRKB-EWQ01";//et_ble_names.getText().toString().trim();//设备集号
            // 开启连接操作
            if(mBluetoothLeService!=null){
                System.out.println( "设置名字");
                mBluetoothLeService.setName(names2);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void initEvents() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //new
        xueya_onclick_tv.setOnClickListener(context);
        wen_onclick_tv.setOnClickListener(context);
        xueyan_onclick_tv.setOnClickListener(context);

        if(mBluetoothAdapter == null){
            //表明此手机不支持蓝牙
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){ //蓝牙未开启，则开启蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mBluetoothAdapter.enable();
            Intent serviceIntent=new Intent(context,BluetoothLeService.class);
            bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
            String names2="FSRKB_BT-001,FSRKB-EWQ01";//et_ble_names.getText().toString().trim();//设备集号
            // 开启连接操作
            if(mBluetoothLeService!=null){
                System.out.println( "设置名字");
                mBluetoothLeService.setName(names2);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            if(requestCode == RESULT_OK){
                //蓝牙已经开启
                Toast.makeText(this,"蓝牙已开起",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"请开启蓝牙才能使用该功能",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        /*
        private AppCompatTextView mai_value_tv;//脉搏的值

        private AppCompatTextView yan_value_tv;//血氧的值

        private AppCompatTextView wen_value_tv;//体温的值*/
        xueya_connect_status_iv = findViewById(R.id.xueya_left_image);
        wen_connect_status_iv = findViewById(R.id.wen_left_image);
        xueyan_connect_status_iv = findViewById(R.id.xueyan_left_image);

        xueya_onclick_tv  =findViewById(R.id.xueya_connect_status_tv);
        wen_onclick_tv  =findViewById(R.id.wen_connect_status_tv);
        xueyan_onclick_tv  =findViewById(R.id.xueyan_connect_status_tv);

        xueya_shou_value_tv = findViewById(R.id.xueya_shou_value_tv);
        xueya_shu_value_tv = findViewById(R.id.xueya_shu_value_tv);
        mai_value_tv = findViewById(R.id.mai_value_tv);
        yan_value_tv = findViewById(R.id.yan_value_tv);
        wen_value_tv = findViewById(R.id.wen_value_tv);

        xueya_shou_time_tv = findViewById(R.id.xueya_shou_time);
        xueya_shu_time_tv = findViewById(R.id.xueya_shu_time);
        mai_time_tv = findViewById(R.id.mai_time);
        xueyan_time_tv = findViewById(R.id.xueyan_time);
        wen_time_tv = findViewById(R.id.wen_time);

        dialog_text = findViewById(R.id.dialog_text);
        imgProgress = findViewById(R.id.dialog_pro);
        pro_relative = findViewById(R.id.pro_relative);
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
         /* case R.id.bt_send:
                String content=et_content.getText().toString().trim();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(context, "请输入指令内容", 3000).show();
                    return;
                }
                if(mBluetoothLeService!=null&& SampleGattAttributes.SEND_CHARACT!=null){
                    Log.e(Tag, "mBluetoothLeService不为空");
                    SampleGattAttributes.SEND_CHARACT.setValue(hexStringToBytes(content));
                    mBluetoothLeService.wirteCharacteristic(SampleGattAttributes.SEND_CHARACT);
                }
                break;*/
            case R.id.wen_connect_status_tv://点击连接温度计
                if(pro_relative.getVisibility()==View.GONE)
                {
                    pro_relative.setVisibility(View.VISIBLE);
                    startCon();
                    dismiss(10);
                }
                String names="FSRKB-EWQ01";//et_ble_names.getText().toString().trim();
                if(TextUtils.isEmpty(names)){
                    Toast.makeText(context, "请输入名称", 3000).show();
                    return;
                }
                if(mBluetoothLeService!=null){
                    Log.e(Tag, "设置名字");
                    mBluetoothLeService.setName(names);
                }
                break;
            case R.id.xueya_connect_status_tv://点击连接血压
                if(pro_relative.getVisibility()==View.GONE)
                {
                    pro_relative.setVisibility(View.VISIBLE);
                    startCon();
                    dismiss(10);
                }
                String names1="FSRKB_BT-001";//et_ble_names.getText().toString().trim();
                if(TextUtils.isEmpty(names1)){
                    Toast.makeText(context, "请输入名称", 3000).show();
                    return;
                }
                if(mBluetoothLeService!=null){
                    Log.e(Tag, "设置名字");
                    mBluetoothLeService.setName(names1);
                }
                break;
            case R.id.xueyan_connect_status_tv:
                Toast.makeText(context,"暂无血氧计",Toast.LENGTH_SHORT).show();
                break;

      /*      case R.id.bt_confirm2:
                String names2="FSRKB_BT-001,FSRKB-EWQ01";//et_ble_names.getText().toString().trim();
                if(TextUtils.isEmpty(names2)){
                    Toast.makeText(context, "请输入名称", 3000).show();
                    return;
                }
                if(mBluetoothLeService!=null){
                    Log.e(Tag, "设置名字");
                    mBluetoothLeService.setName(names2);
                }
                break;*/

            default:
                break;
        }

    }
    private  byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private  byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private void startCon() {
        imgProgress.setImageResource(R.mipmap.progress);
        imgProgress.startAnimation(AnimationFactory.rotate2Self());
        dialog_text.setText("正在搜索设备");
    }
    public void dismiss(final long time) {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(time * 1000);
                    Message msg = new Message();
                    msg.what = 1111;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                return true;
            default:
                return false;
        }
    }

    private void put_data(String data,int type,String time)
    {
        RestClent.builder()
                .url("signs_input")
                .params("region_id","")
                .params("gov_id","")
                .params("user_id","")
                .params("doctor_id","")
                .params("record_id",mac.uniqueId)
                .params("value",data)
                .params("type",type)
                .params("times",time)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println("上传成功返回的数据***************"+response);
                    }
                })
                .build()
                .post();
    }
}

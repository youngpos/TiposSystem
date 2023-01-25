package tipsystem.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import com.sewoo.port.android.BluetoothPort;
import com.sewoo.request.android.RequestHandler;


import android.app.ProgressDialog;

import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import kr.co.tipos.tips.R;

public class BluetoothConnectMenu {
    private static final String TAG = "BluetoothConnectMenu";
    private static final int REQUEST_ENABLE_BT = 2;


    ArrayAdapter<String> adapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Vector<BluetoothDevice> remoteDevices;
    private BroadcastReceiver searchFinish;
    private BroadcastReceiver searchStart;
    private BroadcastReceiver discoveryResult;
    private Thread hThread;
    private Context context;
    // UI
    //private EditText btAddrBox;
    //private Button connectButton;
    //private Button searchButton;
    //private ListView list;

    private LinearLayout linear;

    private EditText btAddrBox;
    private Button connectButton;
    private Button searchButton;
    private ListView list;

    private TextView mStatusPrinter;

    // BT
    private BluetoothPort bluetoothPort;
    private AlertDialog dialog;

    // Set up Bluetooth.
    private void bluetoothSetup()
    {
        // Initialize
        clearBtDevData();
        //bluetoothPort = BluetoothPort.getInstance();
        //BluetoothPort.getInstance();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            // Device does not support Bluetooth
            return;
        }
        if (!mBluetoothAdapter.isEnabled())
        {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(context, "블루투스를 켜주세요!", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "//temp";
    private static final String fileName = dir + "//BTPrinter";
    private String lastConnAddr;
    private void loadSettingFile(EditText btAddrBox)
    {
        int rin = 0;
        char [] buf = new char[128];
        try
        {
            FileReader fReader = new FileReader(fileName);
            rin = fReader.read(buf);
            if(rin > 0)
            {
                lastConnAddr = new String(buf,0,rin);
                btAddrBox.setText(lastConnAddr);
            }
            fReader.close();
        }
        catch (FileNotFoundException e)
        {
            Log.i(TAG, "Connection history not exists.");
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void saveSettingFile()
    {
        try
        {
            File tempDir = new File(dir);
            if(!tempDir.exists())
            {
                tempDir.mkdir();
            }
            FileWriter fWriter = new FileWriter(fileName);
            if(lastConnAddr != null)
                fWriter.write(lastConnAddr);
            fWriter.close();
        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    // clear device data used list.
    private void clearBtDevData()
    {
        remoteDevices = new Vector<BluetoothDevice>();
    }
    // add paired device to list
    private void addPairedDevices()
    {
        BluetoothDevice pairedDevice;
        Iterator<BluetoothDevice> iter = (mBluetoothAdapter.getBondedDevices()).iterator();
        while(iter.hasNext())
        {
            pairedDevice = iter.next();

            /** 2020-07-13 세우 SDK 버전 변경 1092 -> 1104
            if(bluetoothPort.isValidAddress(pairedDevice.getAddress()))
            {
                remoteDevices.add(pairedDevice);
                adapter.add(pairedDevice.getName() +"\n["+pairedDevice.getAddress()+"] [Paired]");
            }
             **/
            remoteDevices.add(pairedDevice);
            adapter.add(pairedDevice.getName() +"\n["+pairedDevice.getAddress()+"] [Paired]");
        }
    }


    public BluetoothConnectMenu(Context mcontext, BluetoothPort bluetoothPort){
        this.context = mcontext;
        this.bluetoothPort = bluetoothPort;


    }

    //@Override
   // protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_bluetooth_connect_menu);

    public void setStart(TextView textView){


        this.mStatusPrinter = textView;

        //final EditText btAddrBox;
        //final Button connectButton;
        //final Button searchButton;
        //final ListView list;

        linear = (LinearLayout)View.inflate(context, R.layout.activity_bluetooth_connect_menu, null);

        btAddrBox = (EditText) linear.findViewById(R.id.EditTextAddressBT);
        connectButton = (Button) linear.findViewById(R.id.ButtonConnectBT);
        searchButton = (Button) linear.findViewById(R.id.ButtonSearchBT);
        list = (ListView) linear.findViewById(R.id.ListView01);

        //final LinearLayout linear = (LinearLayout)View.inflate(context, R.layout.activity_bluetooth_connect_menu, null);

        // Setting
        //btAddrBox = (EditText) linear.findViewById(R.id.EditTextAddressBT);
        //connectButton = (Button) linear.findViewById(R.id.ButtonConnectBT);
        //searchButton = (Button) linear.findViewById(R.id.ButtonSearchBT);
        //list = (ListView) linear.findViewById(R.id.ListView01);
        //context = this;
        // Setting
        loadSettingFile(btAddrBox);
        bluetoothSetup();
        // Connect, Disconnect -- Button
        connectButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!bluetoothPort.isConnected()) // Connect routine.
                {
                    try
                    {
                        btConn(mBluetoothAdapter.getRemoteDevice(btAddrBox.getText().toString()));
                    }
                    catch(IllegalArgumentException e)
                    {
                        // Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
                        Log.e(TAG,e.getMessage(),e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    catch (IOException e)
                    {
                        Log.e(TAG,e.getMessage(),e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else // Disconnect routine.
                {
                    // Always run.
                    btDisconn();
                }
            }
        });
        // Search Button
        searchButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!mBluetoothAdapter.isDiscovering())
                {
                    clearBtDevData();
                    adapter.clear();
                    mBluetoothAdapter.startDiscovery();
                }
                else
                {
                    mBluetoothAdapter.cancelDiscovery();
                }
            }
        });
        // Bluetooth Device List
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        list.setAdapter(adapter);
        addPairedDevices();
        // Connect - click the List item.
        list.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                BluetoothDevice btDev = remoteDevices.elementAt(arg2);
                try
                {
                    if(mBluetoothAdapter.isDiscovering())
                    {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    btAddrBox.setText(btDev.getAddress());

                    // 2021.07.21.김영목. 팝업창 강제로 지우기
                    if(dialog.isShowing())
                        dialog.dismiss();
                    btConn(btDev);
                }
                catch (IOException e)
                {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        // UI - Event Handler.
        // Search device, then add List.
        discoveryResult = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String key;
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(remoteDevice != null)
                {
                    if(remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED)
                    {
                        key = remoteDevice.getName() +"\n["+remoteDevice.getAddress()+"]";
                    }
                    else
                    {
                        key = remoteDevice.getName() +"\n["+remoteDevice.getAddress()+"] [Paired]";
                    }

                    /** 2020-07-13 세우 SDK 버전 변경 1092 -> 1104
                    if(bluetoothPort.isValidAddress(remoteDevice.getAddress()))
                    {
                        remoteDevices.add(remoteDevice);
                        adapter.add(key);
                    }
                     **/
                    remoteDevices.add(remoteDevice);
                    adapter.add(key);
                }
            }
        };
        context.registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        searchStart = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                connectButton.setEnabled(false);
                btAddrBox.setEnabled(false);
                searchButton.setText("검색 중지");
            }
        };
        context.registerReceiver(searchStart, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        searchFinish = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                connectButton.setEnabled(true);
                btAddrBox.setEnabled(true);
                searchButton.setText("검색");
            }
        };
        context.registerReceiver(searchFinish, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));


        dialog = new AlertDialog.Builder(context)
                .setTitle("블루투스연결")
                .setView(linear)
                .show();

    }

    //@Override
    //protected void onDestroy()
    public void close(){
        try
        {
            saveSettingFile();
            bluetoothPort.disconnect();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (InterruptedException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        if((hThread != null) && (hThread.isAlive()))
        {
            hThread.interrupt();
            hThread = null;
        }
        context.unregisterReceiver(searchFinish);
        context.unregisterReceiver(searchStart);
        context.unregisterReceiver(discoveryResult);
        //super.onDestroy();
    }

    // Bluetooth Connection method.
    private void btConn(final BluetoothDevice btDev) throws IOException
    {
        Log.d(btDev.toString(),"Bluetooth Connection");
        new connTask().execute(btDev);
    }
    // Bluetooth Disconnection method.
    private void btDisconn()
    {
        try
        {
            bluetoothPort.disconnect();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        if((hThread != null) && (hThread.isAlive()))
            hThread.interrupt();
        // UI
        connectButton.setText("연결");
        list.setEnabled(true);
        btAddrBox.setEnabled(true);
        searchButton.setEnabled(true);
        mStatusPrinter.setText("발행 목록 : LK-P30II(연결안됨)");
        Toast toast = Toast.makeText(context, "연결해제", Toast.LENGTH_SHORT);
        toast.show();
    }

    // Bluetooth Connection Task.
    class connTask extends AsyncTask<BluetoothDevice, Void, Integer>
    {
        private final ProgressDialog dialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute()
        {
            Log.d("111","Bluetooth Connection Task");
            dialog.setTitle("Bluetooth");
            dialog.setMessage("연결중");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(BluetoothDevice... params)
        {
            Integer retVal = null;
            try
            {
//				bluetoothPort.connect(params[0]);
                lastConnAddr = params[0].getAddress();
                Log.d(lastConnAddr.toString(),"Bluetooth Connection Address");
                bluetoothPort.connect(lastConnAddr);
                retVal = new Integer(0);
                Log.d(retVal.toString(),"Bluetooth Connection retVal");
            }
            catch (IOException e)
            {
                Log.e(TAG, e.getMessage());
                retVal = new Integer(-1);

                Log.d(retVal.toString(),"Bluetooth Connection Error retVal");
            }
            return retVal;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            if(result.intValue() == 0)	// Connection success.
            {
                RequestHandler rh = new RequestHandler();
                hThread = new Thread(rh);
                hThread.start();
                // UI
                connectButton.setText("연결해제");
                list.setEnabled(false);
                btAddrBox.setEnabled(false);
                searchButton.setEnabled(false);
                mStatusPrinter.setText("발행 목록 : LK-P30II(연결됨)");
                if(dialog.isShowing())
                    dialog.dismiss();
                Toast toast = Toast.makeText(context, "연결됨", Toast.LENGTH_SHORT);
                toast.show();
            }
            else	// Connection failed.
            {
                if(dialog.isShowing())
                    dialog.dismiss();
                mStatusPrinter.setText("발행 목록 : LK-P30II(연결실패)");
                Toast.makeText(context, "블루투스연결 실패", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
    }
}

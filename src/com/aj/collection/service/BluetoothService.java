package com.aj.collection.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/**
 * 蓝牙服务类
 * @author Administrator
 *
 */
public class BluetoothService
{
	private Context context=null;
	private static final String DEVICE_NAME="CBT";
    private BluetoothDevice mBluetoothDevice=null;
    private static BluetoothSocket bluetoothSocket = null;
    private boolean isConnection = false;
    //将蓝牙模拟成串口服务
    private static final UUID uuid = UUID    
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static OutputStream outputStream = null;
    private BluetoothAdapter mBluetoothAdapter=null;
    public BluetoothService(Context context,BluetoothAdapter mBluetoothAdapter)
    {
    	this.context=context;
    	this.mBluetoothAdapter=mBluetoothAdapter;
    }
	public void initIntentFilter() {    
        // 设置广播信息过滤    
        IntentFilter intentFilter = new IntentFilter();    
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);    
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);    
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);    
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);    
        // 注册广播接收器，接收并处理搜索结果    
        context.registerReceiver(receiver, intentFilter);
	}
    
	 /**  
     * 蓝牙广播接收器  
     */    
    public BroadcastReceiver receiver = new BroadcastReceiver() {    
    
        ProgressDialog progressDialog = null;    
    
        @Override    
        public void onReceive(Context context, Intent intent) {    
            String action = intent.getAction();    
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {    
                BluetoothDevice device = intent    
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);    
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) { 
                	String dn=device.getName();
                	if(dn.equals(DEVICE_NAME))
                	{
                		mBluetoothDevice=device;
                		System.out.println(mBluetoothDevice.getName());
                		System.out.println(mBluetoothDevice.getAddress());
                		mBluetoothAdapter.cancelDiscovery();
                	}
                } else {    
                }    
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {    
                progressDialog = ProgressDialog.show(context, "请稍等...",    
                        "搜索蓝牙设备中...", true);    
    
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED    
                    .equals(action)) {    
                System.out.println("设备搜索完毕");    
                progressDialog.dismiss();    
    
                // bluetoothAdapter.cancelDiscovery();    
            }    
            /*if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {    
                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {    
                    System.out.println("--------打开蓝牙-----------");    
                } else if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {    
                    System.out.println("--------关闭蓝牙-----------");    
                }    
            }   */ 
    
        }
    };  
    /**  
     * 搜索蓝牙设备  
     */    
    public void searchDevices() {    
    
        // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去    
        this.mBluetoothAdapter.startDiscovery();    
    }    
    /**  
     * 连接蓝牙设备  
     */    
    public boolean connect() {   
    	System.out.println(1111111);
        if (!this.isConnection) {    
        	System.out.println(2222222);
            try {    
                bluetoothSocket = this.mBluetoothDevice    
                        .createRfcommSocketToServiceRecord(uuid);    
                bluetoothSocket.connect();    
                outputStream = bluetoothSocket.getOutputStream();    
                this.isConnection = true;    
                if (this.mBluetoothAdapter.isDiscovering()) {    
                    System.out.println("关闭适配器！");    
                    this.mBluetoothAdapter.isDiscovering();    
                }    
            } catch (Exception e) {    
                Toast.makeText(context, "连接失败！", 1).show();    
                return false;    
            }    
            Toast.makeText(context, this.mBluetoothDevice.getName() + "打印机连接成功！",    
                    Toast.LENGTH_SHORT).show();    
            return true;    
        } else {    
            return true;    
        }    
    }    
    
    /**  
     * 断开蓝牙设备连接  
     */    
    public void disconnect() {    
        System.out.println("断开蓝牙设备连接");    
        try {    
            bluetoothSocket.close();    
            outputStream.close();    
        } catch (IOException e) {
            e.printStackTrace();    
        }    
    }    
    /**  
     * 发送数据  
     */    
    public void send(String sendData) {    
        if (this.isConnection) {    
            System.out.println("开始打印！！");    
            try {    
                //byte[] data = sendData.getBytes("gbk");    
                byte[] data = {0x0C};    
                outputStream.write(data, 0, data.length);    
                outputStream.flush();    
            } catch (IOException e) {    
                Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT)    
                        .show();    
            }    
        } else {    
            Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)    
                    .show();    
    
        }    
    }    
}

package com.example.marcinwlodarczyk.tabbed;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.UUID;


public class bluetoothManager{
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    public boolean connFlag = false;
    BluetoothDevice device;

    private Activity activityContext = null; // v1

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC - adress
    private static String address = "98:D3:31:FB:4F:0F";

    public bluetoothManager(Activity activityContext) throws IOException {
        this.activityContext = activityContext;
        connect();
    }

    public bluetoothManager() throws IOException {

        btAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothAdapter

        /*BluetoothDevice device = btAdapter.getRemoteDevice(address);
        btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        outStream = btSocket.getOutputStream();
        btSocket.connect();*/
    }

    public void setActivityContext(Activity activityContext){
        this.activityContext=activityContext;
    }


    public void connect(){

        btAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothAdapter
        checkBTState();

        try {
            device = btAdapter.getRemoteDevice(address);
        }catch (IllegalArgumentException e){
            //errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }


        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            //errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
            //Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
        } catch (IOException | NullPointerException e) {
            try {
                btSocket.close();
            } catch (IOException | NullPointerException e2) {
                //errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException | NullPointerException e) {
            //errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

    }

    public boolean getStatus(){
        try {
            btSocket.isConnected();
            return true;
        }catch (NullPointerException e){
            return false;
        }
    }

    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nВ переменной address у вас прописан 00:00:00:00:00:00, вам необходимо прописать реальный MAC-адрес Bluetooth модуля";
            msg = msg +  ".\n\nПроверьте поддержку SPP UUID: " + MY_UUID.toString() + " на Bluetooth модуле, к которому вы подключаетесь.\n\n";
        }
    }

    private boolean checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается");
            return false;
        } else {
            if (btAdapter.isEnabled()) {
                //Log.d(TAG, "...Bluetooth включен...");
                return true;
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                activityContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return false;

            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(activityContext, title + " - " + message, Toast.LENGTH_LONG).show();
        activityContext.finish();
    }
}


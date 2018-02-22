/////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 3M and its licensors 2013. All Rights Reserved. This software and         //
// associated files are licensed under the terms of the signed license agreement.  All     //
// sample code & sample applications are provided for demonstration purposes only and      //
// should not be used for commercial or diagnostic purposes.                               //
/////////////////////////////////////////////////////////////////////////////////////////////

package com.mmm.healthcare.scopes.device.androidtextsample;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.mmm.healthcare.scope.BitmapFactory;
import com.mmm.healthcare.scope.ConfigurationFactory;
import com.mmm.healthcare.scope.Errors;
import com.mmm.healthcare.scope.IBluetoothManager;
import com.mmm.healthcare.scope.IStethoscopeListener;
import com.mmm.healthcare.scope.Stethoscope;

public class AndroidTextSample extends Activity implements OnClickListener {

    private Stethoscope stethoscope;
    private boolean recordFlag = false; // Flag that controls recording with the press of M button
    private static String audioFilePath; // audioFilePath for saving .wav file
    private long maximum_recording_length = 20000; // Maximum recording length 20000 milliseconds
    private long recordstart;
    private boolean isConnected = false;
    int total;
    private TextView consoleView;
    private Button connectDisconnectButton;
    private Spinner stethoscopeSelector;

    private Handler handler = new Handler();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Register the activity to allow license check.
        ConfigurationFactory.setContext(this);

        // Register audiofilepath
        audioFilePath= Environment.getExternalStorageDirectory().getAbsolutePath();

        consoleView = (TextView) findViewById(R.id.console_view);
        consoleView.setMovementMethod(new ScrollingMovementMethod());

        stethoscopeSelector = (Spinner) findViewById(R.id.stethoscope_spinner);
        connectDisconnectButton = (Button) findViewById(R.id.connect_disconnect_button);
        connectDisconnectButton.setOnClickListener(this);

        try {

            // Populate stethoscopeSelector with paired stethoscopes.
            IBluetoothManager bluetoothManager = ConfigurationFactory
                    .getBluetoothManager();
            Vector<Stethoscope> scope = bluetoothManager.getPairedDevices();
            ArrayAdapter<Stethoscope> arrayAdapter = new ArrayAdapter<Stethoscope>(
                    this, R.layout.stethoscope_list, R.id.list_view_text,
                    bluetoothManager.getPairedDevices());
            stethoscopeSelector.setAdapter(arrayAdapter);

        } catch (Exception e) {
            writeToConsole(e.toString());
        }
    }

    /**
     * Occurs when the connect/disconnect button is clicked.
     */
    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.connect_disconnect_button) {
            if (!isConnected) {

                try {

                    // Connect to the stethoscope.
                    selectStethoscope();
                    changeButtonToDisconnect();

                } catch (IOException e) {
                    writeToConsole("Could not connect to "
                            + stethoscope.getName());
                }

            } else {

                // Disconnect from the stethosocpe.
                stethoscope.disconnect();
                changeButtonToConnect();
            }
        }
    }

    /**
     * Connects to the stethoscope the user selected. Also generates a report,
     * adds event listeners, and sets the stethoscope's display.
     * 
     * @throws IOException
     */
    private void selectStethoscope() throws IOException {
        int itemPosition = stethoscopeSelector.getSelectedItemPosition();

        stethoscope = (Stethoscope) stethoscopeSelector.getAdapter().getItem(
                itemPosition);
        addStethoscopeListener();
        stethoscope.connect();

        writeToConsole("Connected to " + stethoscope.getName());
        generateStethoscopeReport();

        try {

            writeToConsole("Setting display to sample.bmp");

            stethoscope.setDisplay(BitmapFactory.createBitmap("sample.bmp"));

        } catch (Exception e) {
            writeToConsole("Could not set sample image.");
        }

        
    }

    /**
     * Creates a stethoscope report.
     */
    private void generateStethoscopeReport() {
        writeToConsole("");
        writeToConsole("==== General  ====");
        writeToConsole("Name = " + stethoscope.getName());
        writeToConsole("Filter = " + stethoscope.getFilter());
        writeToConsole("SoundAmplificationLevel = "
                + stethoscope.getSoundAmplificationLevel());

        writeToConsole("");
        writeToConsole("==== Buttons ====");
        writeToConsole("IsMButtonEnabled = "
                + stethoscope.getIsMButtonEnabled());
        writeToConsole("IsFilterButtonEnabled = "
                + stethoscope.getIsFilterButtonEnabled());
        writeToConsole("IsPlusAndMinusButtonsEnabled = "
                + stethoscope.getIsPlusAndMinusButtonsEnabled());

        writeToConsole("");
        writeToConsole("==== Timeout Settings  ====");
        writeToConsole("SleepTimeoutMinutes = "
                + stethoscope.getSleepTimeoutMinutes());
        writeToConsole("ActiveTimeoutDeciseconds = "
                + stethoscope.getActiveTimeoutDeciseconds());
        writeToConsole("BacklightTimeoutDeciseconds = "
                + stethoscope.getBacklightTimeoutDeciseconds());
        writeToConsole("BluetoothTimeoutDeciseconds = "
                + stethoscope.getBluetoothTimeoutDeciseconds());
        writeToConsole("AutomaticOffTimeoutDeciseconds = "
                + stethoscope.getAutomaticOffTimeoutDeciseconds());
        writeToConsole("BluetoothPairTimeoutDeciseconds = "
                + stethoscope.getBluetoothPairTimeoutDeciseconds());
    }

    /**
     * Adds listeners to the stethoscope.
     */
    private void addStethoscopeListener() {
        stethoscope.addStethoscopeListener(new IStethoscopeListener() {

            @Override
            public void plusButtonDown(boolean isLongButtonClick) {

                writeToConsole("");
                writeToConsole("The plus button has been clicked.");
                writeToConsole("The sound amplification level "
                        + "changed to <"
                        + stethoscope.getSoundAmplificationLevel() + ">.");
                writeToConsole("The plus button click was "
                        + (isLongButtonClick ? "long" : "short") + ".");

            }

            @Override
            public void onAndOffButtonDown(boolean isLongButtonClick) {

                writeToConsole("");
                writeToConsole("The on and off button has been clicked.");
                writeToConsole("The on and off button click was "
                        + (isLongButtonClick ? "long" : "short") + ".");
            }

            @Override
            public void minusButtonDown(boolean isLongButtonClick) {

                writeToConsole("");
                writeToConsole("The minus button has been clicked.");
                writeToConsole("The sound amplification level changed"
                        + " to <" + stethoscope.getSoundAmplificationLevel()
                        + ">.");
                writeToConsole("The minus button click was "
                        + (isLongButtonClick ? "long" : "short") + ".");

            }

            @Override
            public void mButtonDown(boolean isLongButtonClick) {
                writeToConsole("");
                writeToConsole("The m button has been clicked.");
                writeToConsole("The m button click was "
                        + (isLongButtonClick ? "long" : "short") + ".");
                /* Open an input stream, send the input buffer to the outputstream
                 *  recordFlag controls the while loop
                 */
                String audioFileName =audioFilePath+"/temp.wav";
                writeToConsole("Save file: "+audioFileName);
                stethoscope.startAudioInput();
                stethoscope.startAudioOutput();

//                recordFlag = true;
                recordFlag =!recordFlag;
                if (recordFlag){
                    recordstart = SystemClock.elapsedRealtime();
                    total =0; // Initialized total number of bytes for incoming session to zero
                }

                try {
                    while(recordFlag){
                        byte[] buffer = new byte[128];
                        int bytesreadcount = stethoscope.getAudioInputStream().read(buffer,0,buffer.length);
                        long currenttime = SystemClock.elapsedRealtime();

                        writeToConsole("Bytes read:"+Integer.toString(bytesreadcount));

                        if (bytesreadcount<=0){
                            Thread.sleep(100);
                            continue;
                        }
                        stethoscope.getAudioOutputStream().write(buffer);
                        total = total +bytesreadcount; // Count total number of incoming bytes
                        if (currenttime-recordstart>=maximum_recording_length){
                            stethoscope.stopAudioInputAndOutput();
                            writeToConsole("Recording performed for 20 seconds, Size: "
                                    +Integer.toString(total)+" bytes");
                            break;
                        }

                    }
                    if(!recordFlag){
                        stethoscope.stopAudioInputAndOutput();
//                        writeToConsole("Recording performed for "+(SystemClock.elapsedRealtime()-recordstart)/1000);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void filterButtonDown(boolean isLongButtonClick) {

                writeToConsole("");
                writeToConsole("The filter button has been clicked.");
                writeToConsole("The filter changed to <"
                        + stethoscope.getFilter() + ">.");
                writeToConsole("The filter button click was "
                        + (isLongButtonClick ? "long" : "short") + ".");
            }

            @Override
            public void error(Errors error, String message) {
                writeToConsole("");
                writeToConsole("The stethoscope has encountered an error. "
                        + error.name() + ": " + message);
            }

            @Override
            public void lowBatteryLevel() {
                writeToConsole("");
                writeToConsole("The stethoscope has a low battery level.");
            }

            @Override
            public void disconnected() {
                writeToConsole("");
                writeToConsole("The stethoscope <" + stethoscope.getName()
                        + "> has been disconnected.");
                changeButtonToConnect();
            }

            @Override
            public void mButtonUp() {
                writeToConsole("");
                writeToConsole("The m button has been released.");
//                recordFlag=false;
//                stethoscope.stopAudioInputAndOutput();
            }

            @Override
            public void plusButtonUp() {
                writeToConsole("");
                writeToConsole("The plus button has been released");
                writeToConsole("The sound amplification level "
                        + "changed to <"
                        + stethoscope.getSoundAmplificationLevel() + ">.");
            }

            @Override
            public void minusButtonUp() {
                writeToConsole("");
                writeToConsole("The minus button has been released.");
                writeToConsole("The sound amplification level "
                        + "changed to <"
                        + stethoscope.getSoundAmplificationLevel() + ">.");
            }

            @Override
            public void filterButtonUp() {

                writeToConsole("");
                writeToConsole("The filter button has been released.");
            }

            @Override
            public void onAndOffButtonUp() {
                writeToConsole("");
                writeToConsole("The power button has been released.");
            }

            @Override
            public void endOfOutputStream() {
                // used only during audio streaming. Not used in this sample.
            }

            @Override
            public void endOfInputStream() {
                // used only during audio streaming. Not used in this sample.
            }

            @Override
            public void outOfRange(boolean isOutOfRange) {

                if (isOutOfRange == true) {

                    // stethoscope is out of range.

                    writeToConsole("");
                    writeToConsole("The stethoscope is out of range.");
                } else {

                    // stethoscope came back in to range.

                    writeToConsole("");
                    writeToConsole("The stethoscope came back in to range.");
                }

            }

            @Override
            public void underrunOrOverrunError(boolean isUnderrun) {

                // Not used.
            }
        });
    }

    /**
     * Changes the button state from disconnect to connect.
     */
    private void changeButtonToConnect() {
        isConnected = false;
        stethoscopeSelector.setEnabled(true);
        stethoscopeSelector.setFocusable(true);

        connectDisconnectButton.setText("Connect");

    }

    /**
     * Changes the button state from connect to disconnect.
     */
    private void changeButtonToDisconnect() {
        // change to disconnect.
        isConnected = true;

        stethoscopeSelector.setEnabled(false);
        stethoscopeSelector.setFocusable(false);

        connectDisconnectButton.setText("Disconnect");
    }

    /**
     * Writes a message to the console.
     * 
     * @param string
     *            The string to write to the console.
     */
    private void writeToConsole(String string) {
        handler.post(new ConsoleWriter(string));
    }

    /**
     * A class used for writing text to the console window.
     * 
     * @author 3M Company
     * 
     */
    private class ConsoleWriter implements Runnable {

        String message;

        ConsoleWriter(String text) {
            message = text;
        }

        @Override
        public void run() {

            synchronized (consoleView) {
                int linesInConsoleView = 19;
                int amountToScrollDown = consoleView.getLineHeight()
                        * (consoleView.getLineCount() - linesInConsoleView);

                consoleView.append(message + "\n");
                consoleView.scrollTo(0, amountToScrollDown);
            }
        }
    }

}
package com.lksim.drone.dancer;

import org.usb4java.Device;

import se.bitcraze.crazyflielib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflielib.crazyflie.Crazyflie;
import se.bitcraze.crazyflielib.crazyradio.ConnectionData;
import se.bitcraze.crazyflielib.crazyradio.Crazyradio;
import se.bitcraze.crazyflielib.crazyradio.RadioDriver;
import se.bitcraze.crazyflielib.crtp.CrtpDriver;
import se.bitcraze.crazyflielib.param.ParamListener;
import se.bitcraze.crazyflielib.toc.Toc;

import java.io.File;
import java.io.IOException;

/**
 * Created by rimashm on 11/13/16.
 */


public class Test {

    private Crazyflie mCrazyflie;
    private CrtpDriver mDriver;

     private String mRadioChannelDefaultValue;
    private String mRadioDatarateDefaultValue;

    private static UsbLink  mUsbLink;

    private int mNoRingEffect = 0;
    private int mCpuFlash = 0;

    private File mCacheDir;

    public static void main(String args[])
    {
        System.out.println("Hello");

        //UsbLink link = new UsbLink();
        //Crazyradio radio = new Crazyradio(link);

        Test test = new Test();
        test.connect();

    }

    private void connect() {
        // ensure previous link is disconnected
        disconnect();

        int radioChannel = 1;//Integer.parseInt(1);
        int radioDatarate = 1;//Integer.parseInt(1);

        mDriver = null;

        if(isCrazyradioAvailable()) {
            try {
                mDriver = new RadioDriver(new UsbLink());
            } catch (IllegalArgumentException e) {
                //Log.d(LOG_TAG, e.getMessage());
                //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //use BLE
/*            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&
                    getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
                if (mPreferences.getBoolean(PreferencesActivity.KEY_PREF_BLATENCY_BOOL, false)) {
                    Log.d(LOG_TAG, "Using bluetooth write with response");
                    mDriver = new BleLink(this, true);
                } else {
                    Log.d(LOG_TAG, "Using bluetooth write without response");
                    mDriver = new BleLink(this, false);
                }
            } else {
                // TODO: improve error message
                Log.e(LOG_TAG, "No BLE support available.");
            }*/
        }

        if (mDriver != null) {

            // add listener for connection status
            mDriver.addConnectionListener(crazyflieConnectionAdapter);

            mCrazyflie = new Crazyflie(mDriver, mCacheDir);

            // connect
            mCrazyflie.connect(new ConnectionData(radioChannel, radioDatarate));

//            mCrazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {
//
//                @Override
//                public void dataReceived(CrtpPacket packet) {
//                    Log.d(LOG_TAG, "Received console packet: " + packet);
//                }
//
//            });
        } else {
            //Toast.makeText(this, "Cannot connect: Crazyradio not attached and Bluetooth LE not available", Toast.LENGTH_SHORT).show();
        }
    }

    protected Toc mParamToc;

    private ConnectionAdapter crazyflieConnectionAdapter = new ConnectionAdapter() {

        @Override
        public void connectionRequested(String connectionInfo) {
            new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), "Connecting ...", Toast.LENGTH_SHORT).show();
                }
            };
        }

        @Override
        public void connected(String connectionInfo) {
           new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    /*if (mCrazyflie != null && mCrazyflie.getDriver() instanceof BleLink) {
                        mToggleConnectButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button_connected_ble));
                    } else {
                        mToggleConnectButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button_connected));
                    }*/
                }
            };
            mCrazyflie.startConnectionSetup();
        }

        @Override
        public void setupFinished(String connectionInfo) {
            final Toc paramToc = mCrazyflie.getParam().getToc();
            if (paramToc != null && paramToc.getTocSize() > 0) {
                mParamToc = paramToc;
                new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(), "Parameters TOC fetch finished: " + paramToc.getTocSize(), Toast.LENGTH_SHORT).show();
                    }
                };
                //activate buzzer sound button when a CF2 is recognized (a buzzer can not yet be detected separately)
                mCrazyflie.getParam().addParamListener(new ParamListener("cpu", "flash") {
                    @Override
                    public void updated(String name, Number value) {
                        mCpuFlash = mCrazyflie.getParam().getValue("cpu.flash").intValue();
                        //enable buzzer action button when a CF2 is found (cpu.flash == 1024)
                        if (mCpuFlash == 1024) {
                            new Runnable() {
                                @Override
                                public void run() {
                                    //mBuzzerSoundButton.setEnabled(true);
                                }
                            };
                        }
                        //Log.d(LOG_TAG, "CPU flash: " + mCpuFlash);
                    }
                });
                mCrazyflie.getParam().requestParamUpdate("cpu.flash");
                //set number of LED ring effects
                mCrazyflie.getParam().addParamListener(new ParamListener("ring", "neffect") {
                    @Override
                    public void updated(String name, Number value) {
                        mNoRingEffect = mCrazyflie.getParam().getValue("ring.neffect").intValue();
                        //enable LED ring action buttons only when ring.neffect parameter is set correctly (=> hence it's a CF2 with a LED ring)
                        if (mNoRingEffect > 0) {
                            new Runnable() {
                                @Override
                                public void run() {
                                   // mRingEffectButton.setEnabled(true);
                                   // mHeadlightButton.setEnabled(true);
                                }
                            };
                        }
                        //Log.d(LOG_TAG, "No of ring effects: " + mNoRingEffect);
                    }
                });
                mCrazyflie.getParam().requestParamUpdate("ring.neffect");
            }

           // startSendJoystickDataThread();
        }

        @Override
        public void connectionLost(String connectionInfo, final String msg) {
            new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    //mToggleConnectButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button));
                }
            };
            disconnect();
        }

        @Override
        public void connectionFailed(String connectionInfo, final String msg) {
            new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            };
            disconnect();
        }

        @Override
        public void disconnected(String connectionInfo) {
            new Runnable() {
                @Override
                public void run() {
                   // Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                   // mToggleConnectButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button));
                    //disable action buttons after disconnect
                    //mRingEffectButton.setEnabled(false);
                   // mHeadlightButton.setEnabled(false);
                    //mBuzzerSoundButton.setEnabled(false);
                }
            };
        }

        @Override
        public void linkQualityUpdated(final int quality) {
            /*new Runnable() {
                @Override
                public void run() {
                    mFlightDataView.setLinkQualityText(quality + "%");
                }
            };*/
        }
    };

    public void disconnect() {
        //Log.d(LOG_TAG, "disconnect()");
        if (mCrazyflie != null) {
            mCrazyflie.disconnect();
            mCrazyflie = null;
        }
        /*if (mSendJoystickDataThread != null) {
            mSendJoystickDataThread.interrupt();
            mSendJoystickDataThread = null;
        }
*/
        if (mDriver != null) {
            mDriver.removeConnectionListener(crazyflieConnectionAdapter);
        }

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // link quality is not available when there is no active connection
                mFlightDataView.setLinkQualityText("n/a");
            }
        });*/
    }

    public static boolean isCrazyradioAvailable() {

        mUsbLink = new UsbLink();

        Device usbDevice = null;
        try {
            usbDevice = mUsbLink.findDevices((short) Crazyradio.CRADIO_VID, (short) Crazyradio.CRADIO_PID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(usbDevice != null)
            return true;
        return false;
    }
}

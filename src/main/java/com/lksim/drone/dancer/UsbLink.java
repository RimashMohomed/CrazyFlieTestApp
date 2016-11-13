package com.lksim.drone.dancer;

import org.usb4java.*;
import se.bitcraze.crazyflielib.usb.CrazyUsbInterface;

import java.io.IOException;

/**
 * Created by rimashm on 11/13/16.
 */
public class UsbLink implements CrazyUsbInterface {

    public final static int CRADIO_VID = 0x1915; //Vendor ID
    public final static int CRADIO_PID = 0x7777; //Product ID

    private DeviceList mUsbManager;
    private Device mUsbDevice;
    private Interface mIntf;
    private EndpointDescriptor mEpIn;
    private EndpointDescriptor mEpOut;
    private DeviceHandle mConnection;
    private Context mContext;

    @Override
    public void initDevice(int usbVid, int usbPid) throws IOException, SecurityException {
        mContext = new Context();
        int result = LibUsb.init(mContext);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
    }

    @Override
    public void releaseInterface() {

    }

    @Override
    public boolean isUsbConnected() {
        return false;
    }

    @Override
    public int sendControlTransfer(int requestType, int request, int value, int index, byte[] data) {
        return 0;
    }

    @Override
    public int sendBulkTransfer(byte[] data, byte[] receiveData) {
        return 0;
    }

    @Override
    public Device findDevices(int usbVid, int usbPid) throws IOException {
        // Read the USB device list

        initDevice(CRADIO_VID, CRADIO_PID);
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(mContext,list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try
        {
            // Iterate over all devices and scan for the right one
            for (Device device: list)
            {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                if (descriptor.idVendor() == usbVid && descriptor.idProduct() == usbPid) return device;
            }
        }
        finally
        {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }

    @Override
    public float getFirmwareVersion() {
        return 0;
    }

    @Override
    public String getSerialNumber() {
        return null;
    }

    @Override
    public void bulkWrite(byte[] data) {

    }

    @Override
    public byte[] bulkRead() {
        return new byte[0];
    }
}

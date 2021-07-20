package com.printerlogic.printerlogic.jipp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class JMDnsUtils implements Runnable{

    private Context context = null;
    private WifiManager.MulticastLock multicastLock;
    public void setContext(Context context)
    {
        this.context =  context;
    }

    @Override
    public void run() {
        // Create a JmDNS instance
        WifiManager wifiMgr = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        /*WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ipAdd = wifiInfo.getIpAddress();
        byte[] bytes =Ints.toByteArray(ipAdd);*/


        try {

            final InetAddress deviceIpAddress = getDeviceIpAddress(wifiMgr);
            multicastLock = wifiMgr.createMulticastLock(getClass().getName());
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
            Log.i("printer", "Starting ZeroConf probe....");
            JmDNS jmdns = JmDNS.create();
            jmdns.addServiceListener("_http._tcp.local.", new JmDNSListener());

            /*InetAddress address = InetAddress.getByAddress(bytes);

            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            // Add a service listener
            jmdns.addServiceListener("_http._tcp.local.", new JmDNSListener());

            Thread.sleep(30000);*/

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InetAddress getDeviceIpAddress(WifiManager wifi) {
        InetAddress result = null;
        try {
            // default to Android localhost
            result = InetAddress.getByName("10.0.0.2");

            // figure out our wifi address, otherwise bail
            WifiInfo wifiinfo = wifi.getConnectionInfo();
            int intaddr = wifiinfo.getIpAddress();
            byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff),
                    (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff) };
            result = InetAddress.getByAddress(byteaddr);
        } catch (UnknownHostException ex) {
            Log.w("printer", String.format("getDeviceIpAddress Error: %s", ex.getMessage()));
        }

        return result;
    }

    private static class JmDNSListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            Log.i("printer","Service added: " + event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            Log.i("printer","Service removed: " + event.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            Log.i("printer","Service resolved: " + event.getInfo());
        }


    }



}

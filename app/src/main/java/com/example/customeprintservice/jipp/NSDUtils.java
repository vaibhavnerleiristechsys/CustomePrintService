package com.example.customeprintservice.jipp;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class NSDUtils implements Runnable
{

    private static final String TAG = "printer";
    private static final String SERVICE_TYPE = "_services._dns-sd._udp";
    //private static final String SERVICE_TYPE = "_ipp._tcp.";
    private String mServiceName ="_ipp";
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener = null;
    private NsdServiceInfo mService;
    private NsdManager.DiscoveryListener mDiscoveryListener = null;

    private Context context = null;
    public void setContext(Context context)
    {
        this.context =  context;
    }

    @Override
    public void run() {

        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        /*NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("printerDiscover");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);*/
        initializeDiscoveryListener();

    }

    public void initializeDiscoveryListener() {


        mResolveListener = new NsdManager.ResolveListener(){

            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {

            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {

                InetAddress printerHost =  nsdServiceInfo.getHost();
                int printerPort =  nsdServiceInfo.getPort();
                String serviceName = nsdServiceInfo.getServiceName();
                Log.d(TAG, "PrinterHost: " + printerHost.toString() + "PrinterPort: " + printerPort + " ServiceName: " + serviceName);
            }
        };

        // Instantiate a new DiscoveryListener
         mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                //Log.d(TAG, "Service discovery success" + service);

                InetAddress printerHost =  service.getHost();
                int printerPort =  service.getPort();
                String serviceName = service.getServiceName();
                //Log.d(TAG, "PrinterHost: " + printerHost.toString() + "PrinterPort: " + printerPort + " ServiceName: " + serviceName);


                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                }
                /*else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("NsdChat")) {


                    //mNsdManager.resolveService(service, mResolveListener);
                }*/
                else
                {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }


        };




        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);


    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
            }
        };
    }
}

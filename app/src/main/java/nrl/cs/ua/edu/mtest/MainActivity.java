package nrl.cs.ua.edu.mtest;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

  private static final int M_SEND = 0;
  private static final int M_RECV = 1;
  private static final int U_SEND = 2;
  private static final int U_RECV = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /**
     * Multicast sender
     */
    Thread mSender = new Thread(new Runnable() {
      @Override
      public void run() {
        // acquire multicast lock
        Context mContext = getApplication().getApplicationContext();

        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        try {
          MulticastSocket multicastSocket = new MulticastSocket(2214);

          // find your Wifi Network Interface
          NetworkInterface wifiNetworkInterface = findWifiNetworkInterface();
          if (wifiNetworkInterface != null) {
            multicastSocket.setNetworkInterface(wifiNetworkInterface);
          }

          String msg = "Hello";
          InetAddress group = InetAddress.getByName("239.22.22.114");
          multicastSocket.joinGroup(group);
          DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, 2214);
          int counter = 0;
          while (counter < 300) {
            multicastSocket.send(hi);
            System.out.println("Sending " + counter++ + " packet");
            Thread.sleep(1000);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    /**
     * Multicast receiver
     */
    Thread mReceiver = new Thread(new Runnable() {
      @Override
      public void run() {
        // acquire multicast lock
        Context mContext = getApplication().getApplicationContext();

        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        try {
          MulticastSocket multicastSocket = new MulticastSocket(2214);

          // find your Wifi Network Interface
          NetworkInterface wifiNetworkInterface = findWifiNetworkInterface();
          if (wifiNetworkInterface != null) {
            multicastSocket.setNetworkInterface(wifiNetworkInterface);
          }
          // ******************
          byte[] buf = new byte[256];
          InetAddress group = InetAddress.getByName("239.22.22.114");
          multicastSocket.joinGroup(group);
          int counter = 0;
          while (counter < 300) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            if ("Hello".equals(received)) {
              System.out.println("Received packet: " + received + " [ " + (counter++) + " ]");
            }
          }
          multicastSocket.leaveGroup(group);
          multicastSocket.close();
          // ******************
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    /**
     * Unicast sender
     */
    Thread uSender = new Thread(new Runnable() {
      @Override
      public void run() {
        String msg = "Hello";
        DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
            new InetSocketAddress("192.168.11.10", 7777));
        try {
          DatagramSocket socket = new DatagramSocket(7777);
          int counter = 0;
          while (counter < 300) {
            socket.send(hi);
            System.out.println("Sending " + counter++ + " packet");
            Thread.sleep(1000);
          }
        } catch (SocketException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    /**
     * Unicast receiver
     */
    Thread uReceiver = new Thread(new Runnable() {
      @Override
      public void run() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
          DatagramSocket socket = new DatagramSocket(7777);
          int counter = 0;
          while (counter < 300) {
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            if ("Hello".equals(received)) {
              System.out.println("Received packet: " + received + " [ " + (counter++) +  " ]");
            }
          }
        } catch (SocketException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    int mode = M_SEND;
    switch (mode) {
      case M_SEND:
        mSender.start();
        break;
      case M_RECV:
        mReceiver.start();
        break;
      case U_SEND:
        uSender.start();
        break;
      case U_RECV:
        uReceiver.start();
        break;
    }
  }

  /**
   * Find your WiFi network interface
   *
   * @return network interface
   */
  private NetworkInterface findWifiNetworkInterface() {
    Enumeration<NetworkInterface> enumeration = null;
    try {
      enumeration = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      e.printStackTrace();
    }

    NetworkInterface wlan0;
    while (enumeration.hasMoreElements()) {
      wlan0 = enumeration.nextElement();
      if (wlan0.getName().equals("wlan0")) {
        return wlan0;
      }
    }
    return null;
  }
}

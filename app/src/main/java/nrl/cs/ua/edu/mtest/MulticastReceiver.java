package nrl.cs.ua.edu.mtest;

import java.io.IOException;
import java.net.*;

public class MulticastReceiver extends Thread {
  protected MulticastSocket socket = null;
  protected byte[] buf = new byte[256];

  public void run() {
    try {
      socket = new MulticastSocket(2214);
      InetAddress group = InetAddress.getByName("239.22.22.112");
      socket.joinGroup(group);
      while (true) {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        if ("Hello".equals(received)) {
          System.out.println("Received packet: " + received);
          break;
        }
      }
      socket.leaveGroup(group);
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

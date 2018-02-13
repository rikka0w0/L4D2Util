package rikka.l4d2util.common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Administrator on 2/13/2018.
 */

public class L4D2Server {
    private final static byte[] requestQueryServerInfo = {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x54, 0x53, 0x6f, 0x75,
            0x72, 0x63, 0x65, 0x20, 0x45, 0x6e, 0x67, 0x69,
            0x6e, 0x65, 0x20, 0x51, 0x75, 0x65, 0x72, 0x79,
            0x00 };

    private final static byte[] requestGetPlayerList = {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x55, 0x00, 0x00, 0x00,
            0x00 };

    public static class Info {
        public String serverName;
        public String mapName;
        public String directory;
        public String gameType;
        public byte playerCount;
        public byte slotCount;
    }

    private static int getSegmentLength(byte[] bytes, int offset, int length) {
        int pos = offset;
        for (; pos < length; pos++) {
            if (bytes[pos] == 0)
                break;
        }
        return pos - offset;
    }

    public static Info QueryServerInfo(String hostname, int port) {
        try {
            InetAddress hostAddr = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(5000);

            DatagramPacket request = new DatagramPacket(requestQueryServerInfo, requestQueryServerInfo.length, hostAddr, port);
            DatagramPacket response = new DatagramPacket(new byte[512], 512);
            socket.send(request);
            socket.receive(response);

            int recvBufLength = response.getLength();
            byte[] recvBuf = response.getData();
            // Check magic number and header
            if ((recvBuf[0] & 0xff) != 0xff ||
                    (recvBuf[1] & 0xff) != 0xff ||
                    (recvBuf[2] & 0xff) != 0xff ||
                    (recvBuf[3] & 0xff) != 0xff ||
                    (recvBuf[4] & 0xff) != 0x49 ||
                    (recvBuf[5] & 0xff) != 0x11) {
                // Invalid responce from server
                return null;
            }

            Info info = new Info();

            int segmentStart = 6;
            int segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
            info.serverName = new String(recvBuf, segmentStart, segmentLength, "UTF-8");
            segmentStart += segmentLength + 1;

            segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
            info.mapName = new String(recvBuf, segmentStart, segmentLength, "UTF-8");
            segmentStart += segmentLength + 1;

            segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
            info.directory = new String(recvBuf, segmentStart, segmentLength, "UTF-8");
            segmentStart += segmentLength + 1;

            segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
            info.gameType = new String(recvBuf, segmentStart, segmentLength, "UTF-8");
            segmentStart += segmentLength + 1;

            if (recvBuf[segmentStart] != 0x26 || recvBuf[segmentStart + 1] != 0x02) {
                // Invalid responce from server\n
                return null;
            }

            info.playerCount = recvBuf[segmentStart+2];
            info.slotCount = recvBuf[segmentStart+3];

            return info;
        } catch (Exception e) {
            return null;
        }
    }

    public static void GetPlayerList(String hostname, int port) {
        try {
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(10000);
            InetAddress hostAddr = InetAddress.getByName(hostname);

            byte[] req = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x54, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65,
                    0x20, 0x45, 0x6e, 0x67, 0x69, 0x6e, 0x65, 0x20, 0x51, 0x75, 0x65, 0x72, 0x79, 0x00 };

            byte[] req2 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x55, 0x00, 0x00, 0x00, 0x00};

            DatagramPacket request = new DatagramPacket(req2, req2.length, hostAddr, port);
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
            socket.send(request);
            socket.receive(response);

            byte[] rep = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x55, 0x00, 0x00, 0x00, 0x00};
            rep[5] = response.getData()[5];
            rep[6] = response.getData()[6];
            rep[7] = response.getData()[7];
            rep[8] = response.getData()[8];
            request = new DatagramPacket(rep, rep.length, hostAddr, port);
            socket.send(request);
            socket.receive(response);


            String result = new String(response.getData(), 0, response.getLength(), "ASCII");
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

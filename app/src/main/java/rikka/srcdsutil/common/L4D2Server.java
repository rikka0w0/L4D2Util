package rikka.srcdsutil.common;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import rikka.srcdsutil.R;

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

    public enum SrcDsVersion {
        UNKNOWN(0), L4D2(1), CS16(2);

        public final int val;
        SrcDsVersion(int val) {
            this.val = val;
        }
    }

    private final static int gameNameLocateIndex[] = {R.string.game_unknown, R.string.game_l4d2, R.string.game_cs16};

    public static class Info {
        public String serverName;
        public String mapName;
        public String directory;
        public String gameType;
        public byte playerCount;
        public byte slotCount;
        public SrcDsVersion version;

        private Info() {
            this.version = SrcDsVersion.UNKNOWN;
        }

        public int getGameVersionLocateIndex() {
            return gameNameLocateIndex[version.val];
        }
    }

    private static int getSegmentLength(byte[] bytes, int offset, int length) {
        int pos = offset;
        for (; pos < length; pos++) {
            if (bytes[pos] == 0)
                break;
        }
        return pos - offset;
    }

    private static Info parse_l4d2_responce(byte[] recvBuf, int recvBufLength) throws UnsupportedEncodingException {
        Info info = new Info();
        info.version = SrcDsVersion.L4D2;

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
    }

    private static Info parse_cs16_responce(byte[] recvBuf, int recvBufLength) throws UnsupportedEncodingException {
        Info info = new Info();
        info.version = SrcDsVersion.CS16;

        int segmentStart = 5;
        int segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
        segmentStart += segmentLength + 1;
        segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
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

        info.playerCount = recvBuf[segmentStart+0];
        info.slotCount = recvBuf[segmentStart+1];

        return info;
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
            if ((recvBuf[0] & 0xff) == 0xff &&
                    (recvBuf[1] & 0xff) == 0xff &&
                    (recvBuf[2] & 0xff) == 0xff &&
                    (recvBuf[3] & 0xff) == 0xff) {

                if (
                        (recvBuf[4] & 0xff) == 0x49 &&
                                (recvBuf[5] & 0xff) == 0x11) {
                    return parse_l4d2_responce(recvBuf, recvBufLength);
                } else if ((recvBuf[4] & 0xff) == 0x6D) {
                    return parse_cs16_responce(recvBuf, recvBufLength);
                }

                return new Info();
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] GetPlayerList(String hostname, int port) {
        try {
            InetAddress hostAddr = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(10000);

            DatagramPacket request = new DatagramPacket(requestGetPlayerList, requestGetPlayerList.length, hostAddr, port);
            DatagramPacket response = new DatagramPacket(new byte[512], 512);
            socket.send(request);
            socket.receive(response);

            byte[] recvBuf = response.getData();
            // Check magic number and header
            if (    (recvBuf[0] & 0xff) != 0xff ||
                    (recvBuf[1] & 0xff) != 0xff ||
                    (recvBuf[2] & 0xff) != 0xff ||
                    (recvBuf[3] & 0xff) != 0xff ||
                    (recvBuf[4] & 0xff) != 0x41) {
                // Invalid responce from server
                return null;
            }

            // Reform request packet
            recvBuf[4] = requestGetPlayerList[4];
            request = new DatagramPacket(recvBuf, recvBuf.length, hostAddr, port);
            socket.send(request);
            socket.receive(response);

            int recvBufLength = response.getLength();
            recvBuf = response.getData();
            // Check magic number and header
            if (    (recvBuf[0] & 0xff) != 0xff ||
                    (recvBuf[1] & 0xff) != 0xff ||
                    (recvBuf[2] & 0xff) != 0xff ||
                    (recvBuf[3] & 0xff) != 0xff ||
                    (recvBuf[4] & 0xff) != 0x44) {
                // Invalid responce from server
                return null;
            }

            int playerCount = recvBuf[5];
            String[] players = new String[playerCount];

            int segmentStart = 7;
            for (int i=0; i<playerCount; i++) {
                int segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
                players[i] = new String(recvBuf, segmentStart, segmentLength, "UTF-8");
                segmentStart += segmentLength + 10;
            }

            return players;
        } catch (Exception e) {
            return null;
        }
    }
}

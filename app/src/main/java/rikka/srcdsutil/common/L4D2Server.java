package rikka.srcdsutil.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;
import rikka.srcdsutil.R;

/**
 * Created by Administrator on 2/13/2018.
 */

public class L4D2Server {
    private final static byte[] requestQueryServerInfo =hexStrToBinaryStr("FF FF FF FF 54 53 6F 75 72 63 65 20 45 6E 67 69 6E 65 20 51 75 65 72 79 00");

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
        Log.d("a2s", "准备处理:"+hexTohexStr(recvBuf));
        Info info = new Info();
        info.version = SrcDsVersion.L4D2;

        int segmentStart = 6;
        int segmentLength = getSegmentLength(recvBuf, segmentStart, recvBufLength);
        info.serverName = new String(recvBuf, segmentStart, segmentLength, "UTF-8");
        Log.d("a2s", "准备处理:"+info.serverName);
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
    public static final void arraycopy(byte[] src,int srcPos,byte[] dest,int destPos,int length)
    {
        for(int i=0;i<length;i++)
        {
            dest[destPos+i]=src[srcPos+i];
        }
    }
    public static final String hexTohexStr(byte[] data){
        try {
            char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5','6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            StringBuffer stringBuffer=new StringBuffer();
            for(int i=0;i<data.length;i++)
            {
                stringBuffer.append(HEX_CHAR[(data[i]<0?data[i]+256:data[i])/16]);
                stringBuffer.append(HEX_CHAR[(data[i]<0?data[i]+256:data[i])%16]);
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }
    public static final byte[] hexStrToBinaryStr(String hexString) {
        if(hexString==null) return null;
        String[] tmp = hexString.split(" ");
        byte[] tmpBytes = new byte[tmp.length];
        int i = 0;
        for (String b : tmp) {
            if (b.equals("FF")) {
                tmpBytes[i++] = -1;
            } else {
                tmpBytes[i++] = Integer.valueOf(b, 16).byteValue();
            }
        }
        return tmpBytes;
    }
    public static final byte[] SendData(DatagramSocket socket,String ip, int port, byte[] data) throws IOException {
        Log.d("a2s", "准备发送:"+hexTohexStr(data));
        InetAddress address = InetAddress.getByName(ip);
        DatagramPacket datagramPacket=new DatagramPacket(data,data.length,address,port);
        socket.setSoTimeout(500);
        socket.send(datagramPacket);
        //构建一个数据接收对象
        byte[] receBuf = new byte[4096];
        DatagramPacket recePacket = new DatagramPacket(receBuf, receBuf.length);
        socket.receive(recePacket);
        Integer length=recePacket.getLength();
        byte[] resBytes=recePacket.getData();
        byte[] f=new byte[length];
        arraycopy(resBytes,0,f,0,length);

        Log.d("a2s", "接收的数据"+hexTohexStr(f));
        return f;
    }
    public static Info QueryServerInfo(String hostname, int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(0);
            socket.setSoTimeout(5000);

            byte[] recvBuf =SendData(socket, hostname, port,requestQueryServerInfo);

            if(recvBuf[4]==(byte) 0x41)
            {
                byte[] newPacket=new byte[requestQueryServerInfo.length+4];
                arraycopy(requestQueryServerInfo,0,newPacket,0,requestQueryServerInfo.length);

                arraycopy(recvBuf,5,newPacket,requestQueryServerInfo.length,4);
                recvBuf =SendData(socket, hostname, port,newPacket);
            }
            // Check magic number and header
            if ((recvBuf[0] & 0xff) == 0xff &&
                    (recvBuf[1] & 0xff) == 0xff &&
                    (recvBuf[2] & 0xff) == 0xff &&
                    (recvBuf[3] & 0xff) == 0xff) {

                if (
                        (recvBuf[4] & 0xff) == 0x49 &&
                                (recvBuf[5] & 0xff) == 0x11) {
                    return parse_l4d2_responce(recvBuf, recvBuf.length);
                } else if ((recvBuf[4] & 0xff) == 0x6D) {
                    return parse_cs16_responce(recvBuf, recvBuf.length);
                }

                return new Info();
            }

            return null;
        } catch (Exception e) {
            Log.e("a2s", "连接失败"+e.getMessage());
            return null;
        }finally {
            if(socket!=null){
                socket.close();
            }
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

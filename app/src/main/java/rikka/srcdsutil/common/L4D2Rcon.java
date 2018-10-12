package rikka.srcdsutil.common;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Random;

public class L4D2Rcon {
    public final String hostname;
    public final int port;
    public final Socket socket;

    private final Random rand = new Random();

    public L4D2Rcon(String hostname, int port) throws UnknownHostException, IOException {
        if (hostname == null || hostname.trim().isEmpty()) {
            throw new IllegalArgumentException("Host can't be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port is out of range");
        }

        this.hostname = hostname;
        this.port = port;
        this.socket = new Socket(hostname, port);
    }

    public void disconnect() throws IOException {
        this.socket.close();
    }

    public boolean authorize(String password) {
        int requestId = rand.nextInt();
        try {
            writeSourcePacket(socket.getOutputStream(), requestId, SERVERDATA_AUTH, password.getBytes());

            boolean recvPacket0 = false;
            boolean recvPacket2 = false;

            SourcePacket packet = readSourcePacket(socket.getInputStream());
            if (packet.requestId != requestId)
                return false;
            if (packet.type == SERVERDATA_RESPONSE_VALUE)
                recvPacket0 = true;
            if (packet.type == SERVERDATA_AUTH_RESPONSE)
                recvPacket2 = true;

            packet = readSourcePacket(socket.getInputStream());
            if (packet.requestId != requestId)
                return false;
            if (packet.type == SERVERDATA_RESPONSE_VALUE)
                recvPacket0 = true;
            if (packet.type == SERVERDATA_AUTH_RESPONSE)
                recvPacket2 = true;

            return recvPacket0 && recvPacket2;
        } catch (Exception e) {
            return false;
        }
    }

    public String command(String cmd) {
        int requestId = rand.nextInt();
        try {
            writeSourcePacket(socket.getOutputStream(), requestId, SERVERDATA_EXECCOMMAND, cmd.getBytes());

            SourcePacket packet = readSourcePacket(socket.getInputStream());
            if (packet.requestId != requestId)
                return null;

            return new String(packet.payload, Charset.forName("UTF-8"));
        } catch (Exception e) {
            return null;
        }
    }
    /////////////////////////////////////////////
    /// SourcePacket
    ////////////////////////////////////////////
    private static final int SERVERDATA_RESPONSE_VALUE = 0;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_AUTH = 3;

    private static int getPacketLength(int bodyLength) {
        // 4 bytes for length + x bytes for body length
        return 4 + bodyLength;
    }

    private static int getBodyLength(int payloadLength) {
        // 4 bytes for requestId, 4 bytes for type, x bytes for payload, 2 bytes for two
        // null bytes
        return 4 + 4 + payloadLength + 2;
    }

    private static void writeSourcePacket(OutputStream stream, int requestId, int type, byte[] payload)
            throws IOException {
        int bodyLength = getBodyLength(payload.length);
        int packetLength = getPacketLength(bodyLength);

        ByteBuffer buffer = ByteBuffer.allocate(packetLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(bodyLength);
        buffer.putInt(requestId);
        buffer.putInt(type);
        buffer.put(payload);

        // Null bytes terminators
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        stream.write(buffer.array());
    }

    private static SourcePacket readSourcePacket(InputStream stream) throws IOException {
        // Header is 3 4-bytes ints
        byte[] header = new byte[4 * 3];

        // Read the 3 ints
        stream.read(header);

        try {
            // Use a bytebuffer in little endian to read the first 3 ints
            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            int length = buffer.getInt();
            int requestId = buffer.getInt();
            int type = buffer.getInt();

            // Payload size can be computed now that we have its length
            byte[] payload = new byte[length - 4 - 4 - 2];

            DataInputStream dis = new DataInputStream(stream);

            // Read the full payload
            dis.readFully(payload);

            // Read the null bytes
            dis.read(new byte[2]);

            return new SourcePacket(requestId, type, payload);
        } catch (BufferUnderflowException | EOFException e) {
            throw new MalformedPacketException("Cannot read the whole packet");
        }
    }

    private static class SourcePacket {
        public final int requestId, type;
        public final byte[] payload;

        public SourcePacket(int requestId, int type, byte[] payload) {
            this.requestId = requestId;
            this.type = type;
            this.payload = payload;
        }
    }

    private static class MalformedPacketException extends IOException {
        public MalformedPacketException(String message) {
            super(message);
        }
    }
}

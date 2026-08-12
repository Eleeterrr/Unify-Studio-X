package eleeter.unifystudiox.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class DiscordIPC
{
    private static String id;
    private static RandomAccessFile windowsPipe;
    private static OutputStream out;
    private static InputStream in;
    private static boolean connected = false;
    private static int nonce = 0;
    private static Thread callbackThread;

    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;


    public static void setId(String clientId)
    {
        id = clientId;
    }

    public static void init()
    {
        try
        {
            connect();
            handshake();
            connected = true;
            System.out.println("[Discord] Connected!");
            startCallbackThread();
        } catch (Exception e)
        {
            System.err.println("[Discord] Could not connect (Discord might not be running): " + e.getMessage());
        }
    }

    public static void setActivity(String details, String state)
    {
        if (!connected) return;
        long now = System.currentTimeMillis() / 1000;
        String n = String.valueOf(++nonce);

        String payload = "{"
                + "\"cmd\":\"SET_ACTIVITY\","
                + "\"args\":{"
                + "\"pid\":" + ProcessHandle.current().pid() + ","
                + "\"activity\":{"
                + "\"details\":\"" + escape(details) + "\","
                + "\"state\":\"" + escape(state) + "\","
                + "\"timestamps\":{\"start\":" + now + "},"
                + "\"assets\":{"
                + "\"large_image\":\"unify_logo\","
                + "\"large_text\":\"Unify Studio X\""
                + "}"
                + "}"
                + "},"
                + "\"nonce\":\"" + n + "\""
                + "}";

        try
        {
            send(OP_FRAME, payload);
            read();
        } catch (Exception e)
        {
            System.err.println("[Discord] setActivity failed: " + e.getMessage());
            connected = false;
        }
    }

    public static void shutdown()
    {
        if (!connected)
        {
            return;
        }

        try
        {
            send(OP_CLOSE, "{}");
        } catch (Exception ignored)
        {
        }
        try
        {
            if (windowsPipe != null)
            {
                windowsPipe.close();
            }

            if (out != null)
            {
                out.close();
            }

        } catch (Exception ignored)
        {
        }
        if (callbackThread != null) callbackThread.interrupt();
        connected = false;
        System.out.println("[Discord] Disconnected.");
    }


    private static void connect() throws Exception
    {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
        {
            for (int i = 0; i <= 9; i++)
            {
                try
                {
                    windowsPipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                    return;
                } catch (Exception ignored)
                {
                }
            }
            throw new IOException("No Discord pipe found on Windows");
        } else
        {
            String[] dirs =
                    {
                            System.getenv("XDG_RUNTIME_DIR"),
                            System.getenv("TMPDIR"),
                            System.getenv("TMP"),
                            System.getenv("TEMP"),
                            "/tmp"
                    };
            for (String dir : dirs)
            {
                if (dir == null)
                {
                    continue;
                }

                for (int i = 0; i <= 9; i++)
                {
                    try
                    {
                        var addr = UnixDomainSocketAddress.of(dir + "/discord-ipc-" + i);
                        var channel = SocketChannel.open(StandardProtocolFamily.UNIX);
                        channel.connect(addr);
                        out = Channels.newOutputStream(channel);
                        in = Channels.newInputStream(channel);
                        return;
                    } catch (Exception ignored)
                    {
                    }
                }
            }
            throw new IOException("No Discord socket found on Linux/macOS");
        }
    }

    private static void handshake() throws Exception
    {
        String payload = "{\"v\":1,\"client_id\":\"" + id + "\"}";
        send(OP_HANDSHAKE, payload);
        read();
    }

    private static void startCallbackThread()
    {
        callbackThread = new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted() && connected)
            {
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException e)
                {
                    break;
                }
            }
        }, "Discord-IPC-Thread");
        callbackThread.setDaemon(true);
        callbackThread.start();
    }

    private static void send(int opcode, String json) throws Exception
    {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(8 + data.length).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(opcode);
        buf.putInt(data.length);
        buf.put(data);
        byte[] bytes = buf.array();

        if (windowsPipe != null)
        {
            windowsPipe.write(bytes);
        } else
        {
            out.write(bytes);
            out.flush();
        }
    }

    private static String read() throws Exception
    {
        byte[] header = readBytes(8);
        int length = ByteBuffer.wrap(header, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();


        byte[] body = readBytes(length);
        return new String(body, StandardCharsets.UTF_8);
    }

    private static byte[] readBytes(int count) throws Exception
    {
        byte[] buf = new byte[count];
        int read = 0;
        while (read < count)
        {
            int r;
            if (windowsPipe != null) r = windowsPipe.read(buf, read, count - read);
            else r = in.read(buf, read, count - read);
            if (r == -1) throw new EOFException("Discord closed the connection");
            read += r;
        }
        return buf;
    }

    private static String escape(String s)
    {
        if (s == null)
        {
            return "";
        }

        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

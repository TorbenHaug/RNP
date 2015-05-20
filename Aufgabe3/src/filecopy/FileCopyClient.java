package filecopy;

/* FileCopyClient.java
 Version 0.1 - Muss erg�nzt werden!!
 Praktikum 3 Rechnernetze BAI4 HAW Hamburg
 Autoren:
 */

import sun.awt.Mutex;

import java.io.*;

import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileCopyClient extends Thread {

    // -------- Constants
    public final static boolean TEST_OUTPUT_MODE = true;

    public final int UDP_PACKET_SIZE = 1008;
    // -------- Public parms
    public String servername;


    public int serverPort;

    public String sourcePath;

    public String destPath;

    public int windowSize;

    public long serverErrorRate;

    // -------- ClientConnectionOptions
    private final InetAddress serverAddress;

    private DatagramSocket socket = null;

    private DatagramPacket packet;

    byte[] receiveData;

    byte[] sendByte = new byte[1000];
    FCpacket fCpacket;

    private LinkedList<FCpacket> sendBuf;

    long seqNo = 1;

    int maxBuffSize = 1;

    // -------- Streams
    private FileInputStream inputFile;

    Thread sendThread;

    Thread receiveThread;
    // -------- Variables
    // current default timeout in nanoseconds
    private long timeoutValue = 100000000L;
    Lock bufferMutex;

    private final Condition notEmpty;
    private final Condition notFull;

    // ... ToDo


    // Constructor
    public FileCopyClient(String serverArg, String serverPortArg, String sourcePathArg,
                          String destPathArg, String windowSizeArg, String errorRateArg) throws UnknownHostException {
        servername = serverArg;
        serverPort = Integer.parseInt(serverPortArg);
        sourcePath = sourcePathArg;
        destPath = destPathArg;
        windowSize = Integer.parseInt(windowSizeArg);
        serverErrorRate = Long.parseLong(errorRateArg);
        serverAddress = InetAddress.getByName(servername);
        sendBuf = new LinkedList<FCpacket>();
        receiveData = new byte[UDP_PACKET_SIZE];
        bufferMutex = new ReentrantLock();
        notEmpty = bufferMutex.newCondition();
        notFull = bufferMutex.newCondition();
    }

    public void runFileCopyClient() throws IOException {
        socket = new DatagramSocket();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendState();
            }
        });
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveState();
            }
        });
        receiveThread.start();

        fCpacket = makeControlPacket();
        packet = new DatagramPacket(fCpacket.getSeqNumBytesAndData(), fCpacket.getLen()+8, serverAddress, serverPort);
        testOut(" this is the data: " + new String(fCpacket.getData(), "UTF-8"));
        insertPacketintoBuffer(fCpacket);
        socket.send(packet);

        inputFile = new FileInputStream(sourcePath);
        sendThread.start();
        // ToDo!!
    }

    private void sendState() {
        try {
            int readNoBytes = 0;
            while((readNoBytes = inputFile.read(sendByte)) != -1) {
                fCpacket = new FCpacket(seqNo++, sendByte, readNoBytes);
                packet = new DatagramPacket(fCpacket.getSeqNumBytesAndData(), fCpacket.getLen() + 8, serverAddress, serverPort);
                insertPacketintoBuffer(fCpacket);
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void receiveState() {
        try {
            packet = new DatagramPacket(receiveData, 8);
            socket.receive(packet);
            long ackNumber = makeLong(packet.getData(), 0, 8);
            testOut("this is the ackNumber: " + ackNumber);
            removeFomBuffer(ackNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Timer Operations
     */
    public void startTimer(FCpacket packet) {
    /* Create, save and start timer for the given FCpacket */
        FC_Timer timer = new FC_Timer(timeoutValue, this, packet.getSeqNum());
        packet.setTimer(timer);
        timer.start();
    }

    public void cancelTimer(FCpacket packet) {
    /* Cancel timer for the given FCpacket */
        testOut("Cancel Timer for packet" + packet.getSeqNum());

        if (packet.getTimer() != null) {
            packet.getTimer().interrupt();
        }
    }

    /**
     * Implementation specific task performed at timeout
     */
    public void timeoutTask(long seqNum) {
        // ToDo
    }


    /**
     * Computes the current timeout value (in nanoseconds)
     */
    public void computeTimeoutValue(long sampleRTT) {

        // ToDo
    }


    /**
     * Return value: FCPacket with (0 destPath;windowSize;errorRate)
     */
    public FCpacket makeControlPacket() {
   /* Create first packet with seq num 0. Return value: FCPacket with
     (0 destPath ; windowSize ; errorRate) */
        String sendString = destPath + ";" + windowSize + ";" + serverErrorRate;
        byte[] sendData = null;
        try {
            sendData = sendString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new FCpacket(0, sendData, sendData.length);
    }

    public void testOut(String out) {
        if (TEST_OUTPUT_MODE) {
            System.err.printf("%,d %s: %s\n", System.nanoTime(), Thread
                    .currentThread().getName(), out);
        }
    }

    private void insertPacketintoBuffer(FCpacket insertPacket) {
    /* Insert the packet into the receive buffer at the right position */
        bufferMutex.lock();
        while(sendBuf.size() >= maxBuffSize){
            try {
                notFull.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!sendBuf.contains(insertPacket)) { // no duplicates!
            sendBuf.add(insertPacket);
            // sort in ascending order using the seq num
            Collections.sort(sendBuf);
            notEmpty.signal();
        }
        bufferMutex.unlock();
    }

    private void removeFomBuffer(long seqNo){
        bufferMutex.lock();
        while (sendBuf.isEmpty()){
            try {
                notEmpty.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int i=0;
        for(i = 0;i<sendBuf.size();i++){
            if (sendBuf.get(i).getSeqNum() == seqNo){
                break;
            }
        }
        try {
            sendBuf.remove(i);
            notFull.signal();
        }catch(IndexOutOfBoundsException e){}
        bufferMutex.unlock();
    }
    private long makeLong(byte[] buf, int i, int length) {
        long r = 0;
        length += i;

        for (int j = i; j < length; j++)
            r = (r << 8) | (buf[j] & 0xffL);

        return r;
    }

    public static void main(String argv[]) throws Exception {
        FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2],
                argv[3], argv[4], argv[5]);
        myClient.runFileCopyClient();
    }

}

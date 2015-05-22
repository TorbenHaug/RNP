package filecopy;

/* FileCopyClient.java
 Version 0.1 - Muss erg�nzt werden!!
 Praktikum 3 Rechnernetze BAI4 HAW Hamburg
 Autoren:
 */

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
    private static int ackCount = 0;
    private static long averageRTT;

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

    private LinkedList<FCpacket> sendBuf;

    private boolean sending = true;
    static long seqNo = 1;

    int congestionWindow = 1;

    // -------- Streams
    private FileInputStream inputFile;

    Thread sendThread;

    Thread receiveThread;
    // -------- Variables
    // current default timeout in nanoseconds
    private long timeoutValue = 40;
    private long expRtt = 40; // nanoseconds
    private long jitter = 20;

    private long threshold = 8;
    Lock bufferMutex;

    private final Condition notEmpty;
    private final Condition notFull;
    private int congestionCount = 0;
    private static int timeOutCount = 0;

    synchronized public long getTimeoutValue() {
        return timeoutValue;
    }

    synchronized public void setTimeoutValue(long timeoutValue) {
        this.timeoutValue = timeoutValue;
    }

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
        bufferMutex = new ReentrantLock();
        notEmpty = bufferMutex.newCondition();
        notFull = bufferMutex.newCondition();
    }

    public void runFileCopyClient() throws IOException, InterruptedException {
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

        FCpacket fCpacket = makeControlPacket();
       // DatagramPacket packet = new DatagramPacket(fCpacket.getSeqNumBytesAndData(), fCpacket.getLen()+8, serverAddress, serverPort);
        testOut("Package : " + fCpacket.getSeqNum() + " this is the data: " + new String(fCpacket.getData(), "UTF-8"));
        insertPacketintoBuffer(fCpacket);
        sendPacket(fCpacket);

        inputFile = new FileInputStream(sourcePath);
        sendThread.start();
        sendThread.join();
        receiveThread.join();

    }

    private void sendPacket(FCpacket toSend) throws IOException {
        DatagramPacket packet = new DatagramPacket(toSend.getSeqNumBytesAndData(), toSend.getLen() + 8, serverAddress, serverPort);
        testOut("Send Package: " + toSend.getSeqNum());
        socket.send(packet);
        startTimer(toSend);
        toSend.setTimestamp(System.nanoTime());
    }

    private void sendState() {
        try {
            int readNoBytes = 0;
            byte[] sendByte = new byte[1000];
            while((readNoBytes = inputFile.read(sendByte)) != -1) {
                testOut("Bytes read: " + readNoBytes);
                FCpacket fCpacket = new FCpacket(seqNo++, sendByte, readNoBytes);
                insertPacketintoBuffer(fCpacket);
                sendPacket(fCpacket);
            }
            sending = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void receiveState() {
        try {
            while(sending || !sendBuf.isEmpty()) {
                byte[] receiveData = new byte[8];
                DatagramPacket packet = new DatagramPacket(receiveData, 8);
                socket.receive(packet);
                ackCount++;
                long ackNumber = makeLong(packet.getData(), 0, 8);
                testOut("this is the ackNumber: " + ackNumber);
                FCpacket acknowlagedPacket = removeFomBuffer(ackNumber);
                if(acknowlagedPacket != null) {
                    cancelTimer(acknowlagedPacket);
                    long duration = System.nanoTime() - acknowlagedPacket.getTimestamp();
                    computeTimeoutValue(duration);
                    averageRTT += duration;
                    //TODO: Schauen wofür?
                    acknowlagedPacket.setValidACK(true);
                    if (congestionWindow < threshold){
                        congestionWindow = congestionWindow + 1;
                    }else{
                        congestionCount++;
                        if (congestionCount >= congestionWindow && congestionWindow < windowSize){
                            congestionWindow++;
                            congestionCount = 0;
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Timer Operations
     */
    public void startTimer(FCpacket packet) {
    /* Create, save and start timer for the given FCpacket */
        FC_Timer timer = new FC_Timer(getTimeoutValue(), this, packet.getSeqNum());
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
        FCpacket timedOutPacket = getFromBuffer(seqNum);

        //Wegen Raisecondition;
        if(timedOutPacket != null) {
            try {
                timeOutCount++;
                setTimeoutValue(getTimeoutValue() *2);
                threshold = congestionWindow/2;
                congestionWindow = 1;
                sendPacket(timedOutPacket);
            } catch (IOException e) {
                testOut("Unable to send to server");
                e.printStackTrace();
            }
        }
    }


    /**
     * Computes the current timeout value (in nanoseconds)
     */
    public void computeTimeoutValue(long rtt) {
        double x = 0.25;
        double y= x/2;

        expRtt = (long) ((1.0-y) * expRtt + y * rtt);

        jitter = (long) ((1.0-x) * jitter + x * Math.abs(rtt - expRtt));

        setTimeoutValue(expRtt + 4 * jitter);
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

    /**
     *
     * @param out
     */
    public void testOut(String out) {
        if (TEST_OUTPUT_MODE) {
            System.err.printf("%,d %s: %s\n", System.nanoTime(), Thread
                    .currentThread().getName(), out);
        }
    }

    /**
     *
     * @param insertPacket
     */
    private void insertPacketintoBuffer(FCpacket insertPacket) {
    /* Insert the packet into the receive buffer at the right position */
        bufferMutex.lock();
        while(sendBuf.size() >= congestionWindow){
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

    /**
     *
     * @param seqNo
     * @return
     */
    private FCpacket removeFomBuffer(long seqNo){
        FCpacket retval = null;
        bufferMutex.lock();
        while (sendBuf.isEmpty()){
            try {
                notEmpty.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int i=0;
        for(i = 0;i < sendBuf.size();i++){
            if (sendBuf.get(i).getSeqNum() == seqNo){
                break;
            }
        }
        try {
            retval = sendBuf.remove(i);
            testOut("Remove: " + retval.getSeqNum());
            notFull.signal();
        }catch(IndexOutOfBoundsException e){}
        bufferMutex.unlock();
        return retval;
    }

    /**
     *
     * @param seqNo
     * @return
     */
    private FCpacket getFromBuffer(long seqNo){
        FCpacket retval = null;
        bufferMutex.lock();
        int i=0;
        for(i = 0;i < sendBuf.size();i++){
            if (sendBuf.get(i).getSeqNum() == seqNo){
                break;
            }
        }
        try {
            testOut("Get: " + i);
            retval = sendBuf.get(i);
        }catch(IndexOutOfBoundsException e){}
        bufferMutex.unlock();
        return retval;
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
        long startTime = System.currentTimeMillis();
        myClient.runFileCopyClient();
        myClient.join();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Timeouts: " + timeOutCount);
        System.out.println("AckCount: " + ackCount);
        System.out.println("AvargeRTT: " + ((averageRTT/seqNo)/1000000) + "ms");
    }

}

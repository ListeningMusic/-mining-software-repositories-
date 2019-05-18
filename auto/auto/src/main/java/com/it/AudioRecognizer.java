package com.it;

import javax.sound.sampled.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *录音然后进行频域转换，再提取指纹构建哈希表
 *
 */

public class AudioRecognizer {
    Boolean running = false;
    double highscores[][];
    long points[][];
    Map<Long, List<DataPoint>> hashMap; // Map<哈希值，对应的歌曲名和指纹出现时间的信息>
    Map<Integer, Map<Integer, Integer>> matchMap; // Map<SongId, Map<Offset,Count>> Map<歌曲名，Map<时间差，时间差计数>>

    // 录音的参数
    public AudioFormat getFormat() {
        float sampleRate = 44100; // 采样频率
        int sampleSizeInBits = 8; // 1,2 信道数（单声道为 1，立体声为 2，等等）
        int channels = 1;         // 单声道 1,2 信道数（单声道为 1，立体声为 2，等等）
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian); // 构造具有线性 PCM 编码和给定参数的 AudioFormat
    }

    // TargetDataLine是声音的输入(麦克风),而SourceDataLine是输出(音响,耳机)。
    public void listenSound() throws LineUnavailableException {
        AudioFormat format = getFormat();

        // lineClass - 该信息对象所描述的数据行的类
        // format - 所需的格式
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

        // 启动麦克风？
        line.open(format);
        line.start();

        // 读取TargetDataLine中的数据
        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                // 字节数组输出流在内存中创建一个字节数组缓冲区，所有发送到输出流的数据保存在该字节数组缓冲区中
                // 字节数组输出流不需要目标源，因为是输出到内存中
                // 创建一个32字节（默认大小）的缓冲区
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                running = true;
                int n = 0;
                byte[] buffer = new byte[(int) 1024];
                try {
                    System.out.println("正在聆听。。。");
                    while (running) {
                        n++;
                        if (n > 1000) {
                            break;
                        }
                        int count = line.read(buffer, 0, buffer.length);
                        if (count > 0) {
                            out.write(buffer, 0, count);
                        }
                    }
                    determineFingerprints(makeFFT(out));
                    out.close();
                    line.close();
                } catch (IOException e) {
                    System.err.println("I/O problems: " + e);
                    System.exit(-1);
                }
            }
        });
        listenThread.start();
    }

    // 对音频进行FFT变换
    public Complex[][] makeFFT(ByteArrayOutputStream out) {

        // toByteArray():创建一个新分配的字节数组。数组的大小和当前输出流的大小，内容是当前输出流的拷贝
        byte audio[] = out.toByteArray();
        final int totalSize = audio.length;
        int chunkSize = 4096;//以4096byte(4KB)为一个数据块

        // 采样的块数
        int amountPossible = totalSize / chunkSize;
        Complex[][] results = new Complex[amountPossible][];

        // 代码的内层循环将采样数据放入一个复数数组中（虚部为0），外层循环遍历每一块数据，并进行FFT变换
        // j是行，i是列
        for (int j = 0; j < amountPossible; j++) {

            // 每块数据块大小的复数数组
            Complex[] complex = new Complex[chunkSize];
            for (int i = 0; i < chunkSize; i++) {

                // 将时域数据放入具有虚数的复数中
                // 虚部为零
                complex[i] = new Complex(audio[(j * chunkSize) + i], 0);
            }

            // 对块进行FFT转换 二维数组可以省略行？
            results[j] = FFT.fft(complex);
        }
        return results;
    }

    public static final int UPPER_LIMIT = 300;
    public static final int LOWER_LIMIT = 30;
    public static final int[] RANGE = new int[]{40, 80, 120, 180, UPPER_LIMIT + 1};

    // 找出频率所属范围
    public static int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq)
            i++;
        return i;
    }

    // 对音频频域信息提取指纹
    public String determineFingerprints(Complex[][] results) {
        String info = null;
        System.out.println("正在匹配。。。");

        // highscores用来保存最高振幅值
        highscores = new double[results.length][5];
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < 5; j++) {
                highscores[i][j] = 0;
            }
        }

        // points用来保存最高振幅的频率值
        points = new long[results.length][5];
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < 5; j++) {
                points[i][j] = 0;
            }
        }

        // matchMap用来在匹配时存储songId和对应的offset和count信息
        matchMap = new HashMap<Integer, Map<Integer, Integer>>();
        DataPoint point = null;

        // listPoints用来存放从数据库中检索出的指纹为h的点及其对应的songId和time信息
        List<DataPoint> listPoints = null;
        listPoints = new ArrayList<DataPoint>();

        // 连接数据库
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet set = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
            con = DriverManager.getConnection(url, "root", "1234");
            con.setAutoCommit(false);

            // results是在前一步骤中获得的复数矩阵，results.length表示行长度（行数），即前面的j
            // 每个数据块的序号就代表了时间,即此处的t
            for (int t = 0; t < results.length; t++) {
                for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {

                    // Get the magnitude
                    double mag = Math.log(results[t][freq].abs() + 1);

                    // Find out which range we are in
                    int index = getIndex(freq);

                    // 保存最高幅度及相应的频率
                    if (mag > highscores[t][index]) {
                        highscores[t][index] = mag;
                        points[t][index] = freq;
                    }
                }

                // 测试打印points中的结果
                /*for (int i = 0; i < points.length; i++) {
                    for (int j = 0; j < 5; j++) {
                        System.out.println(points[i][j]);
                    }
                }*/

                // 构建hash值，即音频指纹
                long h = hash(points[t][0], points[t][1], points[t][2], points[t][3]);
                //System.out.println(points[t][0] + " " + points[t][1] + " " + points[t][2] + " " + points[t][3]);
                //System.out.println(h);

                // 构建hash表
                // Map<K,V>
                // Map<哈希值，对应的歌曲名和指纹出现时间的信息>
                // Map<SongId, Map<Offset,Count>> Map<歌曲名，Map<时间差，时间差计数>>

                String sql = "select * from fingerprints where hashtag=?";
                stmt = con.prepareStatement(sql);
                stmt.setLong(1, h);
                set = stmt.executeQuery();
                hashMap = new HashMap<Long, List<DataPoint>>();

                while (set.next()) {
                    long hash = set.getLong("hashtag");
                    long songId = set.getLong("songId");
                    long time = set.getLong("time");

                    //point是一个DataPoint类的对象
                    point = new DataPoint((int) songId, (int) time);
                    listPoints.add(point);
                    hashMap.put(hash, listPoints);
                }


                List<DataPoint> listPoints2 = null;

                // hashmap中所保存的也是同一个指纹所对应的信息
                if ((listPoints2 = hashMap.get(h)) != null) {

                    // dP是对应同一个指纹的不同歌曲的信息，或者同一歌曲不同时间的信息
                    for (DataPoint dP : listPoints2) {

                        // offset：时间差？,hash表中的时间和数据块时间的差
                        int offset = Math.abs(dP.getTime() - t);
                        Map<Integer, Integer> tmpMap = null;
                        if ((tmpMap = matchMap.get(dP.getSongId())) == null) {
                            tmpMap = new HashMap<Integer, Integer>();
                            tmpMap.put(offset, 1);
                            matchMap.put(dP.getSongId(), tmpMap);
                        } else {
                            Integer count = tmpMap.get(offset);
                            if (count == null) {
                                tmpMap.put(offset, new Integer(1));
                            } else {
                                tmpMap.put(offset, new Integer(count + 1));
                            }
                            matchMap.put(dP.getSongId(), tmpMap);
                        }
                    }
                }
            }

            int bestCount = 0;
            int bestSong = -1;
            HashMap<Integer, Integer> mMap = new HashMap<>();

            for (DataPoint points : listPoints) {
                Map<Integer, Integer> tmpMap2 = matchMap.get(points.getSongId());
                int bestCountForSong = 0;
                for (Map.Entry<Integer, Integer> entry : tmpMap2.entrySet()) {
                    if (entry.getValue() > bestCountForSong) {
                        bestCountForSong = entry.getValue();
                    }
                    //System.out.println("Time offset = " + entry.getKey() + ", Count = " + entry.getValue());
                }
                if (bestCountForSong > bestCount) {
                    bestCount = bestCountForSong;
                    bestSong = points.getSongId();
                }
            }
            String sql = "select * from song_info where song_id=?";
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, bestSong);
            set = stmt.executeQuery();
            while (set.next()) {
                info = set.getString("information");
            }
            System.out.println("最佳匹配：" + info);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String geming = info;
        System.out.println(geming);
        return geming;
    }

    // 向指纹库中添加歌曲指纹
    public void addFingerprint(Complex[][] results, long songId) {

        // 连接数据库
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
            con = DriverManager.getConnection(url, "root", "1234");
            con.setAutoCommit(false);
            highscores = new double[results.length][5];
            for (int i = 0; i < results.length; i++) {
                for (int j = 0; j < 5; j++) {
                    highscores[i][j] = 0;
                }
            }
            points = new long[results.length][5];
            for (int i = 0; i < results.length; i++) {
                for (int j = 0; j < 5; j++) {
                    points[i][j] = 0;
                }
            }
            for (int t = 0; t < results.length; t++) {
                for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {

                    // Get the magnitude
                    double mag = Math.log(results[t][freq].abs() + 1);

                    // Find out which range we are in
                    int index = getIndex(freq);

                    // 保存最高幅度的相应的频率
                    if (mag > highscores[t][index]) {
                        points[t][index] = freq;
                        highscores[t][index] = mag;
                    }
                }
                    /*for (int i = 0; i < points.length; i++) {
                        for (int j = 0; j < 4; j++) {
                            System.out.println(points[i][j]);
                        }
                    }*/

                long h2 = hash(points[t][0], points[t][1], points[t][2], points[t][3]);
                System.out.println(points[t][0] + " " + points[t][1] + " " + points[t][2] + " " + points[t][3]);
                System.out.println(h2);
                String sql = "INSERT INTO fingerprints (hashtag,songId,time) VALUES(?,?,?)";
                stmt = con.prepareStatement(sql);
                stmt.setLong(1, h2);
                stmt.setLong(2, songId);
                stmt.setLong(3, t);
                stmt.executeUpdate();
                System.out.println("execute");
            }
            stmt.close();
            con.commit();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSongInfo() {

        // 连接数据库
        Connection con = null;
        PreparedStatement stmt = null;
        int song_id = 1;

        //读取文件夹歌曲信息
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false&useUnicode=true&characterEncoding=utf8";
            con = DriverManager.getConnection(url, "root", "1234");
            con.setAutoCommit(false);
            String dirPath = "D:\\auto\\auto\\src\\main\\webapp\\music";
            File files = new File(dirPath);
            File[] fs = files.listFiles();
            if (files.exists()) {
                for (File f : fs) {
                    String sql = "INSERT INTO song_info (song_id,information) VALUES(?,?)";
                    stmt = con.prepareStatement(sql);
                    stmt.setInt(1, song_id);
                    stmt.setString(2, f.getName());
                    stmt.executeUpdate();
                    song_id++;
                }
            }
            stmt.close();
            con.commit();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 构建hash值的方法
    private static final int FUZ_FACTOR = 2; //?

    // 返回一个long类型的数值（作为hash值），将数据块的前四个值用来构建哈希值
    private long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }
}


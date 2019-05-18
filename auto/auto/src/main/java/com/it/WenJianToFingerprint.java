package com.it;

import org.tritonus.sampled.convert.PCM2PCMConversionProvider;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/*
 * 识别音频文件信息，并转换成音频指纹
 *
 * */

public class WenJianToFingerprint {

    AudioInputStream in;
    String filePath = "D:\\auto\\auto\\src\\main\\webapp\\music\\33 - Gonna Fly Now.mp3";
    File file = new File(filePath);
    boolean running = false;

    public void shiBieWenJianLiu() throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        AudioInputStream din = null;
        AudioInputStream outDin = null;
        in = AudioSystem.getAudioInputStream(file);
        PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();
        AudioFormat formatTmp = null;
        //AudioFormat format2=getFormat();
        TargetDataLine lineTmp = null;
        final AudioRecognizer ar = new AudioRecognizer();
        //获得in的文件格式
        AudioFormat baseFormat = in.getFormat();
        System.out.println(baseFormat.toString());

        //转换文件编码
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                false);

        //把in流的音频格式转换成decodeFormat格式
        din = AudioSystem.getAudioInputStream(decodedFormat, in);
        if (!conversionProvider.isConversionSupported(ar.getFormat(), decodedFormat)) {
            System.out.println("Conversion is not supported");
        }
        System.out.println(decodedFormat.toString());
        outDin = conversionProvider.getAudioInputStream(ar.getFormat(), din);
        System.out.println (ar.getFormat().toString());

        //formatTmp = decodedFormat;
        //DataLine.Info info = new DataLine.Info(TargetDataLine.class, ar.getFormat());
        //lineTmp = (TargetDataLine) AudioSystem.getLine(info);

        final AudioInputStream outDinSound = outDin;

        Thread shiBieThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // running = true;
                int n = 0;
                byte[] buffer = new byte[(int) 1024];
                try {
                    while ((n = outDinSound.read(buffer)) != -1) {
                        out.write(buffer, 0, buffer.length);
                    }

                    Complex[][] results = ar.makeFFT(out);
                    ar.addFingerprint(results, 33);

                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        shiBieThread.start();
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        WenJianToFingerprint wjtfp = new WenJianToFingerprint();
        wjtfp.shiBieWenJianLiu();
    }
}


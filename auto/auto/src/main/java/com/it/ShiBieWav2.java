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

public class ShiBieWav2 {

    static int j = 0;
    AudioInputStream in;
    String filePath = "D:\\" + (j++) + ".mp3";
    File file = new File(filePath);
    boolean running = false;

    public String shiBieWenJianLiu() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        String jieguo = null;
        AudioInputStream din = null;
        AudioInputStream outDin = null;
        in = AudioSystem.getAudioInputStream(file);
        PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();
        AudioFormat formatTmp = null;
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
        System.out.println(ar.getFormat().toString());

        final AudioInputStream outDinSound = outDin;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // running = true;
        int n = 0;
        byte[] buffer = new byte[(int) 1024];
        try {
            while ((n = outDinSound.read(buffer)) != -1) {
                out.write(buffer, 0, buffer.length);
            }
            Complex[][] results = ar.makeFFT(out);
            //System.out.println(ar.determineFingerprints(results)+" "+"1");
            jieguo = ar.determineFingerprints(results);
            System.out.println("jieguo:" + jieguo);

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jieguo;
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        ShiBieWav2 wjtfp = new ShiBieWav2();
        wjtfp.shiBieWenJianLiu();
    }
}


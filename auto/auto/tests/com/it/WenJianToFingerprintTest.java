package com.it;

import org.junit.Before;
import org.junit.Test;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class WenJianToFingerprintTest {

    WenJianToFingerprint wjtfp;

    @Before
    public void setUp(){
        wjtfp = new WenJianToFingerprint();
    }

    @Test
    public void shiBieWenJianLiu() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        wjtfp.shiBieWenJianLiu();
    }
}
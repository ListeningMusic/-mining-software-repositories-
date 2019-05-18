package com.it;

import org.junit.Before;
import org.junit.Test;
import javax.sound.sampled.LineUnavailableException;
import static org.junit.Assert.assertEquals;

public class AudioRecognizerTest {

    AudioRecognizer testAR;

    @Before
    public void setUp() throws Exception {
        testAR=new AudioRecognizer();
    }

    @Test
    public void getIndex() {
        assertEquals(0,testAR.getIndex(38));
        assertEquals(1,testAR.getIndex(50));
        assertEquals(2,testAR.getIndex(100));
        assertEquals(3,testAR.getIndex(150));
        assertEquals(4,testAR.getIndex(200));
    }

    @Test
    public void addSongInfo(){
        testAR.addSongInfo();
    }

    @Test
    public void listenSound() throws LineUnavailableException {
        testAR.listenSound();
    }

}
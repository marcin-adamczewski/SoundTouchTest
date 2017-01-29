package com.appunite.soundtouchtest.soundtouch;


public class BPMDetect {

    private native final static long newInstance(int numChannels, int sampleRate);
    private native final void deleteInstance(long handle);
    private native final float getBPM(long handle, String inputFile);

    // Load the native library upon startup
    static
    {
        System.loadLibrary("soundtouch-jni");
    }

    long handle = 0;


    public BPMDetect(int numChannels, int sampleRate) {
        handle = newInstance(numChannels, sampleRate);
    }

    public float getBPM(String inputFile) {
        return getBPM(handle, inputFile);
    }

    public void close()
    {
        deleteInstance(handle);
        handle = 0;
    }
}

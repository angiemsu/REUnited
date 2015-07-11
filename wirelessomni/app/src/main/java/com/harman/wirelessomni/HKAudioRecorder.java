package com.harman.wirelessomni;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class HKAudioRecorder {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private static int sampleRateInHz = 44100;
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;//AudioFormat.CHANNEL_IN_MONO;  //AudioFormat.CHANNEL_IN_STEREO;
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    
    private int bufferSizeInBytes = 0;

    private AudioRecord audioRecord;  
    private boolean isRecord = false;

    private String rawAudioName = null;  

    private String wavAudioName = null;
    
    public HKAudioRecorder(String url) {
    	StringBuilder rawUrl = new StringBuilder();
    	int indx = url.lastIndexOf(".");
    	rawUrl.append(url.substring(0, indx));
    	rawUrl.append(".raw");
    	rawAudioName = rawUrl.toString();
    	wavAudioName = url;
    	creatAudioRecord();
    }  

    private void creatAudioRecord() {  
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,  
                channelConfig, audioFormat);  

        audioRecord = new AudioRecord(audioSource, sampleRateInHz,  
                channelConfig, audioFormat, bufferSizeInBytes);
    }  

    public void startRecord() {  
        audioRecord.startRecording(); 
        isRecord = true; 
        new Thread(new AudioRecordThread()).start();  
    }  

    public void stopRecord() {  
        if (audioRecord != null) {  
            System.out.println("stopRecord");
            isRecord = false;
            audioRecord.stop();  
            audioRecord.release();
            audioRecord = null;  
        }  
    }  
  
    class AudioRecordThread implements Runnable {  
        @Override  
        public void run() {  
            writeDateTOFile();
            copyWaveFile(rawAudioName, wavAudioName);
        }  
    }  

    private void writeDateTOFile() {  
        byte[] audiodata = new byte[bufferSizeInBytes];  
        FileOutputStream fos = null;  
        int readsize = 0;  
        try {  
            File file = new File(rawAudioName);  
            if (file.exists()) {
                file.delete();  
            }  
            fos = new FileOutputStream(file);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        while (isRecord == true) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                try {  
                    fos.write(audiodata);  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }
        }

        try {  
            fos.close();
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    private void copyWaveFile(String inFilename, String outFilename) {  
        FileInputStream in = null;  
        FileOutputStream out = null;  
        long totalAudioLen = 0;  
        long totalDataLen = totalAudioLen + 36;  
        long longSampleRate = sampleRateInHz;  
        int channels = 2;  
        long byteRate = 16 * sampleRateInHz * channels / 8;  
        byte[] data = new byte[bufferSizeInBytes];  
        try {  
            in = new FileInputStream(inFilename);  
            out = new FileOutputStream(outFilename);  
            totalAudioLen = in.getChannel().size();  
            totalDataLen = totalAudioLen + 36;  
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,  
                    longSampleRate, channels, byteRate);  
            while (in.read(data) != -1) {  
                out.write(data);  
            }  
            in.close();  
            out.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}

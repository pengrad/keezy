package com.pengrad.keezy.test;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: stas
 * Date: 07.05.14 23:40
 */

public class AudioRecordManager {

    RecordAudio recordAudio = new RecordAudio();

    public void startRecord(String path) {
        recordAudio.file = path;
        new Thread(recordAudio).start();
    }

    public void stopRecord(Runnable endCallback) {
        recordAudio.endCallback = endCallback;
        recordAudio.cancel();
        recordAudio = new RecordAudio();
    }

    public void serFrequency(int frequency) {
        FREQUENCY = frequency;
    }

    public void set8bit(boolean bit8) {
        AUDIO_FORMAT = bit8 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
    }

    private static int FREQUENCY = 44100;
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static class RecordAudio implements Runnable {
        //        public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
        //        public static final int FREQUENCY = 44100;   // 22050, 11025, 16000, 8000  44100
//        public static final int RECORDER_BPP = 16;


        private volatile boolean cancel = false;
        private volatile Runnable endCallback;

        private String file;
        private AudioRecord audioRecord;


        private byte RECORDER_BPP;

        public void cancel() {
            cancel = true;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            RECORDER_BPP = (byte) (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_8BIT ? 8 : 16);

            int BUFFER_SIZE = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, AUDIO_FORMAT);

            Log.d("++++++", "Start record with freq:" + FREQUENCY);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
            List<byte[]> recordList = new ArrayList<byte[]>();
            int readSum = 0;
            audioRecord.startRecording();
            while (!cancel) {
                ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
                int bufferReadResult = audioRecord.read(bb, BUFFER_SIZE);
//                if(bb.hasArray()) recordList.add(bb.array());
                byte[] data = new byte[bufferReadResult];
                bb.get(data, 0, data.length);
                recordList.add(data);
                readSum += bufferReadResult;
            }
            audioRecord.stop();
            audioRecord.release();

            byte[] data = new byte[readSum];
            int j = 0;
            for (byte[] d : recordList) {
                for (int i = 0; i < d.length; i++, j++) {
                    data[j] = d[i];
                }
            }


//            byte[] res = new EndPointDetection(data, FREQUENCY).doEndPointDetection();
            byte[] res = new byte[data.length];
            new Gate().process(data, res);


            List<byte[]> resList = new ArrayList<byte[]>();
            resList.add(res);
//            writeWaveFile(file, resList, res.length);

            writeWaveFile(file, recordList, readSum);

            if (endCallback != null) endCallback.run();
        }

        private void writeWaveFile(String outFilename, List<byte[]> data, int size) {
            long longSampleRate = FREQUENCY;
            int channels = 1;
            long byteRate = RECORDER_BPP * FREQUENCY * channels / 8;
            Log.d("+++++", "byteRate: " + byteRate);
            try {
                FileOutputStream out = new FileOutputStream(outFilename);
                int totalAudioLen = size;
                int totalDataLen = totalAudioLen + 36;

                writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

                for (byte[] bytes : data) out.write(bytes);

                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
            byte[] header = new byte[44];

            header[0] = 'R';  // RIFF/WAVE header
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
            header[12] = 'f';  // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;  // format = 1
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
            header[32] = (byte) (2 * 16 / 8);  // block align
            header[33] = 0;
            header[34] = RECORDER_BPP;  // bits per sample
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


    public static class EndPointDetection {
        private byte[] originalSignal; //input
        private byte[] silenceRemovedSignal;//output
        private int samplingRate;
        private int firstSamples;
        private int samplePerFrame;

        public EndPointDetection(byte[] originalSignal, int samplingRate) {
            this.originalSignal = originalSignal;
            this.samplingRate = samplingRate;
            samplePerFrame = this.samplingRate / 1000;
            firstSamples = samplePerFrame * 200;// according to formula
        }

        public byte[] doEndPointDetection() {
            // for identifying each sample whether it is voiced or unvoiced
            float[] voiced = new float[originalSignal.length];
            float sum = 0;
            double sd = 0.0;
            double m = 0.0;
            // 1. calculation of mean
            for (int i = 0; i < firstSamples; i++) {
                sum += originalSignal[i];
            }
            m = sum / firstSamples;// mean
            sum = 0;// reuse var for S.D.

            // 2. calculation of Standard Deviation
            for (int i = 0; i < firstSamples; i++) {
                sum += Math.pow((originalSignal[i] - m), 2);
            }
            sd = Math.sqrt(sum / firstSamples);
            // 3. identifying one-dimensional Mahalanobis distance function
            // i.e. |x-u|/s greater than ####3 or not,
            for (int i = 0; i < originalSignal.length; i++) {
                if ((Math.abs(originalSignal[i] - m) / sd) > 0.3) { //0.3 =THRESHOLD.. adjust value yourself
                    voiced[i] = 1;
                } else {
                    voiced[i] = 0;
                }
            }
            // 4. calculation of voiced and unvoiced signals
            // mark each frame to be voiced or unvoiced frame
            int frameCount = 0;
            int usefulFramesCount = 1;
            int count_voiced = 0;
            int count_unvoiced = 0;
            int voicedFrame[] = new int[originalSignal.length / samplePerFrame];
            // the following calculation truncates the remainder
            int loopCount = originalSignal.length - (originalSignal.length % samplePerFrame);
            for (int i = 0; i < loopCount; i += samplePerFrame) {
                count_voiced = 0;
                count_unvoiced = 0;
                for (int j = i; j < i + samplePerFrame; j++) {
                    if (voiced[j] == 1) {
                        count_voiced++;
                    } else {
                        count_unvoiced++;
                    }
                }
                if (count_voiced > count_unvoiced) {
                    usefulFramesCount++;
                    voicedFrame[frameCount++] = 1;
                } else {
                    voicedFrame[frameCount++] = 0;
                }
            }
            // 5. silence removal
            silenceRemovedSignal = new byte[usefulFramesCount * samplePerFrame];
            int k = 0;
            for (int i = 0; i < frameCount; i++) {
                if (voicedFrame[i] == 1) {
                    for (int j = i * samplePerFrame; j < i * samplePerFrame + samplePerFrame; j++) {
                        silenceRemovedSignal[k++] = originalSignal[j];
                    }
                }
            }
            // end
            return silenceRemovedSignal;
        }
    }

    public static class Gate {
        protected boolean open = false;
        protected int attack = 20;
        protected double treshold = 5.0;
        protected transient RingFloat ring = new RingFloat();
        protected transient float peak = 0.0f;
        protected transient int peakPos = 0;

        public int calculateSampleCount(int milliseconds) {
            int sr = 44100;
            int count = (sr * milliseconds) / 1000;
            return count;
        }

        public void process(byte[] data, byte[] out) {
            int attack = getAttack();
            int bufSize = calculateSampleCount(attack);
            float triggerValue = (float) (0.01 * getTreshold());
            ring.ensureCapacity(bufSize);
            boolean open = isOpen();


            for (int i = 0; i < data.length; i++) {
                byte a = data[i];
                float abs = (a < 0) ? -a : a;
                float old = ring.get(bufSize);
                ring.put(a);
                if (a > peak) {
                    peak = a;
                    peakPos = 0;
                } else if (peakPos >= bufSize) { // old peak has 'timed out'
                    peak = 0.0f;
                    for (int j = 0; j < bufSize; j++) { // find peak in buffered data
                        float b = ring.get(j);
                        if (b < 0) {
                            b = -b;
                        }
                        if (b > peak) {
                            peak = b;
                            peakPos = j - 1; // ('++' below, so -1 here)
                        }
                    }
                }
                open = (peak >= triggerValue);
                out[i] = open ? a : 0;
                peakPos++;
            }
            setOpen(open); // propagate last status of local variable open to property and ui
        }

        /**
         */
        public int getAttack() {
            return attack;
        }

        /**
         */
        public double getTreshold() {
            return treshold;
        }

        /**
         */
        public void setAttack(int i) {
            if (attack != i) {
                attack = i;
            }
        }

        /**
         */
        public void setTreshold(double s) {
            if (treshold != s) {
                treshold = s;
            }
        }

        /**
         */
        public boolean isOpen() {
            return open;
        }

        /**
         */
        public void setOpen(boolean b) {
            if (open != b) {
                open = b;
            }
        }

    } // end Gate

    public static class RingFloat extends FifoFloat {

        // ------------------------------------------------------------------------
        // --- fields                                                           ---
        // ------------------------------------------------------------------------

        protected float[] buffer;

        protected int pos;


        // ------------------------------------------------------------------------
        // --- constructors                                                     ---
        // ------------------------------------------------------------------------

        public RingFloat() {
            super(); // (many of the superclass's features are replaced with a different implementation here)
            buffer = new float[DEFAULT_ALLOCATION_SIZE];
            pos = 0;
        }

        public RingFloat(int initialCapacity) {
            this();
            ensureCapacity(initialCapacity);
        }


        // ------------------------------------------------------------------------
        // --- methods                                                          ---
        // ------------------------------------------------------------------------

        public void ensureCapacity(int size) {
            if (buffer.length < size) {
                float[] newbuffer = new float[size];
                System.arraycopy(buffer, 0, newbuffer, size - buffer.length, buffer.length);
                buffer = newbuffer;
            }
        }

        /**
         * Only the remaining buffer content will be used by the fifo-queue.
         */
        public void put(FloatBuffer buf) {
            // untested
            int length = buf.capacity();
            if (length > buffer.length) {
                throw new BufferOverflowException();
            }
            int l = buffer.length - pos;
            if (length > l) {
                // copy and wrap around at end
                buf.get(buffer, pos, l);
                this.pos = length - l;
                buf.get(buffer, 0, this.pos);
            } else {
                // simple copy
                buf.get(buffer, pos, length);
                pos += length;
            }
        }

        public void put(float[] f) {
            put(f, 0, f.length);
        }

        public void put(float[] f, int offset, int length) {
            if (length > buffer.length) {
                throw new BufferOverflowException();
            }
            int l = buffer.length - pos;
            if (length > l) {
                // copy and wrap around at end
                System.arraycopy(f, offset, buffer, pos, l);
                this.pos = length - l;
                System.arraycopy(f, offset + l, buffer, 0, this.pos);
            } else {
                // simple copy
                System.arraycopy(f, offset, buffer, pos, length);
                pos += length;
            }
        }

        public void put(float f) {
            buffer[pos++] = f;
            if (pos >= buffer.length) {
                pos = 0;
            }
        }

        public float get(int diff) {
            int i = ((pos - diff) + buffer.length) % buffer.length;
            return buffer[i];
        }

    } // end RingFloat

    public static class FifoFloat {

        // ------------------------------------------------------------------------
        // --- static field                                                     ---
        // ------------------------------------------------------------------------

        public static int DEFAULT_ALLOCATION_SIZE = 2048;


        // ------------------------------------------------------------------------
        // --- fields                                                           ---
        // ------------------------------------------------------------------------

        protected ArrayList fifo;

        protected int avail;

        protected FloatBuffer appendable;

        protected int allocationSize;


        // ------------------------------------------------------------------------
        // --- constructor                                                      ---
        // ------------------------------------------------------------------------

        public FifoFloat() {
            this.fifo = new ArrayList();
            this.appendable = null;
            this.allocationSize = DEFAULT_ALLOCATION_SIZE;
        }


        // ------------------------------------------------------------------------
        // --- methods                                                          ---
        // ------------------------------------------------------------------------

        /**
         * Only the remaining buffer content will be used by the fifo-queue.
         */
        public void put(FloatBuffer buf) {
            synchronized (fifo) {
                this.fifo.add(buf);
                this.avail += buf.remaining();
                this.appendable = null;
            }
        }

        public void put(float[] f) {
            getAppendable().put(f);
            this.avail += f.length;
        }

        public void put(float[] f, int offset, int length) {
            getAppendable().put(f, offset, length);
            this.avail += length;
        }

        public void put(float f) {
            getAppendable().put(f);
        }

        public void get(float[] arr, int n) {
            if (n <= this.avail) {
                synchronized (fifo) {
                    this.avail -= n;
                    int pos = 0;
                    while (n > 0) {
                        FloatBuffer b = (FloatBuffer) fifo.get(0);
                        int c = b.remaining();
                        if (c > n) {
                            b.get(arr, pos, n);
                            n = 0;
                        } else {
                            b.get(arr, pos, c); // whole rest of buffer
                            n -= c;
                            pos += c;
                            fifo.remove(0);
                        }
                    }
                }
            } else {
                throw new BufferUnderflowException();
            }
        }

        public void get(float[] arr) {
            this.get(arr, arr.length);
        }

        public float get() {
            float[] arr = new float[1];
            get(arr, 1);
            return arr[0];
        }

        public int available() {
            return this.avail;
        }

        public boolean isEmpty() {
            return (this.avail == 0);
        }

        public void ensureCapacity(int size) {
            // ??
            if ((this.appendable == null) || (this.appendable.capacity() < size)) {
                synchronized (fifo) {
                    this.appendable = null; // force new allocation with next put()
                    if (size > this.allocationSize) {
                        this.allocationSize = size; // next call to getAppendable will allocate appropriate amount
                    }
                }

            }
        }

        protected FloatBuffer getAppendable() {
            if (this.appendable == null) {
                synchronized (fifo) {
                    this.appendable = FloatBuffer.allocate(DEFAULT_ALLOCATION_SIZE);
                    this.fifo.add(this.appendable);
                }
            }
            return this.appendable;
        }

    } // end FifoFloat
}

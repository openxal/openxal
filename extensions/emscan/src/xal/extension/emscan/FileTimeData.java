/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sashazhukov
 */
public class FileTimeData<E extends DataRecord> extends AbstractTimeData<E> {

    private static int INTSIZE = Integer.SIZE / 8;
    private static int LONGSIZE = Long.SIZE / 8;
    private static int DOUBLESIZE = Double.SIZE / 8;
    private static int DATESHIFT = 2;
    private List<Long> offsetList;
    private final RandomAccessFile backingFile;

    public static File convertCSVData(String fileName, int skip) throws FileNotFoundException, IOException {

        long startTime = System.currentTimeMillis();

        File f = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(f));
        File binaryOutput = new File(f.getCanonicalPath() + ".dat");
        RandomAccessFile randomAccessFile = new RandomAccessFile(binaryOutput, "rw");

        List<Long> offsets = new ArrayList<Long>();

	br.readLine(); //skip header line
        for (int i = 0; true; i++) {

            String line = br.readLine();
            if (line == null) {
                break;
            }
	    
            String[] tokens = line.split(",");

            try {
//                offsets.add(randomAccessFile.getFilePointer());
                //assembles date string back
		
		long offset = randomAccessFile.getFilePointer();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < DATESHIFT; j++) {
                    sb.append(tokens[j]);

                }

                String dateString = sb.toString();
                dateString = dateString.substring(0, dateString.length() - 6);
                DateFormat df = new SimpleDateFormat("MMM d yyyy HH:mm:ss.SSS");
                long millis = df.parse(dateString).getTime();



                double[] wfData = parseStrings(tokens, DATESHIFT, skip);
                int size = wfData.length * DOUBLESIZE + LONGSIZE;
                byte[] bytes = new byte[size + INTSIZE];
                ByteBuffer buffer = ByteBuffer.wrap(bytes);

                buffer.putInt(size);
                buffer.putLong(millis);
                for (double value : wfData) {
                    buffer.putDouble(value);

                }
                randomAccessFile.write(bytes);
	        offsets.add(offset);

            } catch (Exception ex) {
                ex.printStackTrace();
                badLine(i);
                continue;
            }
        }

        final int numberOfRecords = offsets.size();

        byte[] bytes = new byte[numberOfRecords * Long.SIZE / 8];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (long value : offsets) {
            buffer.putLong(value);

        }
        randomAccessFile.write(bytes);
        randomAccessFile.writeInt(numberOfRecords);



        br.close();
        randomAccessFile.close();

        Logger.getLogger(FileTimeData.class.getName()).log(Level.INFO,
                "Converted {0} records from CSV [{2}] to binary [{3}] in {1} mS",
                new Object[]{numberOfRecords, System.currentTimeMillis() - startTime, fileName, binaryOutput.getAbsoluteFile()});

        return binaryOutput;
    }
    protected int numberOfRecords = 0;

    public FileTimeData(DataRecordFactory<E> factory, final File file) {
        super(factory);
        long length = 0;
        List<Long> offsets = null;
        RandomAccessFile inFile = null;


        int size = 0;

        try {
            inFile = new RandomAccessFile(file, "r");
            length = inFile.length();

            inFile.seek(length - INTSIZE);
            size = inFile.readInt();
            offsets = new ArrayList<Long>(size);

            inFile.seek(length - size * LONGSIZE - INTSIZE);
            byte[] bytes = new byte[size * LONGSIZE];
            inFile.read(bytes, 0, size * LONGSIZE);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            for (int i = 0; i < size; i++) {
                offsets.add(buffer.getLong());
            }
            Logger.getLogger(getClass().getName()).log(Level.FINE,
                    "Offsets {0}", offsets.toString());

        } catch (IOException ex) {
            Logger.getLogger(FileTimeData.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            // we will have empty list if exception occurred
            if (offsets == null) {
                offsets = new ArrayList<Long>();
            }
            offsetList = Collections.unmodifiableList(offsets);
            backingFile = inFile;
            numberOfRecords = size;
        }

    }

    public void close() throws IOException {
        backingFile.close();
    }

    private static void badLine(int i) {
        System.out.println("Bad line at " + i + " will skip it");
    }

    private static double[] parseStrings(String[] tokens, int waveformStart, int skip) {

        int length = tokens.length - waveformStart-skip;
        double[] data = new double[length];
	
	data[0] = Double.parseDouble(tokens[waveformStart]);
	data[1] = Double.parseDouble(tokens[waveformStart+1]);
        for (int i = 2; i < length; i++) {
            data[i] = Double.parseDouble(tokens[waveformStart + skip+i]);
        }
        return data;
    }

    public E getDataRecord(int index) throws IndexOutOfBoundsException {
        try {
            long offset = offsetList.get(index);
            backingFile.seek(offset);
            int size = backingFile.readInt();
            byte[] bytes = new byte[size];
            backingFile.read(bytes);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int dataLength = (size - FileTimeData.LONGSIZE) / FileTimeData.DOUBLESIZE;
            double[] data = new double[dataLength];
            long millis = buffer.getLong();
            for (int i = 0; i < dataLength; i++) {
                data[i] = buffer.getDouble();
            }
            return getFactory().create(millis, data);
        } catch (IOException ioe) {
            return null;
        }

    }

    public int size() {
        return numberOfRecords;
    }
}

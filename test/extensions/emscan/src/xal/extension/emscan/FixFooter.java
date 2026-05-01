/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author az9
 */
public class FixFooter {

	private static final int INTSIZE = Integer.SIZE / 8;
	private static final int LONGSIZE = Long.SIZE / 8;
	private static final int DOUBLESIZE = Double.SIZE / 8;

	public static void main(String[] args) {
		
		if(args.length<1){
			System.out.println("Fix footer filename. \n Warning! The original file will be overwritten");
			return ;
		}
		
		String file = args[0];
		System.out.println("Fix footer " + file);
		long length = 0;
		List<Long> offsets = null;
		RandomAccessFile inFile = null;

		int size = 0;

		try {
			inFile = new RandomAccessFile(file, "rw");
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
			
			if(offsets.get(0)!=0){
				offsets.add(0,0l);
				offsets.remove(offsets.size()-1);
				inFile.seek(length - size * LONGSIZE - INTSIZE);
				for(long off : offsets){
					inFile.writeLong(off);
				}
			}
			
			inFile.close();
			
			System.out.println("Fix footer ended" );
			Logger.getLogger(FixFooter.class.getName()).log(Level.FINE,
				"Offsets {0}", offsets.toString());

		} catch (IOException ex) {
			Logger.getLogger(FileTimeData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}

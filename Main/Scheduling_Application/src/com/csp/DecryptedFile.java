package com.csp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.connection.Dbconn;


public class DecryptedFile {

	public static final int DATA_Chunk = 4;
    public static final int PAIR_SHARDS = 2;
    public static final int TOTAL_Chunk = 6;

    public static final int BYTES_IN = 4;
    public String filepath = "C:/Data/";
	//public static String filesplit = "C:/Data/split/";
	
	@SuppressWarnings("resource")
	public static void Decoder(String filename,String filepath)
	{
		
		 final File originalFile = new File(Dbconn.filechunk+"org.txt");
		 try {
	        // Read in any of the shards that are present.
	        // (There should be checking here to make sure the input
	        // shards are the same size, but there isn't.)
	        final byte [] [] shards = new byte [TOTAL_Chunk] [];
	        final boolean [] shardPresent = new boolean [TOTAL_Chunk];
	        int shardSize = 0;
	        int shardCount = 0;
	        for (int i = 0; i < TOTAL_Chunk; i++) {
	            File shardFile = new File(filepath+filename);
	            if (shardFile.exists()) {
	                shardSize = (int) shardFile.length();
	                shards[i] = new byte [shardSize];
	                shardPresent[i] = true;
	                shardCount += 1;
	                InputStream in = new FileInputStream(shardFile);
	                in.read(shards[i], 0, shardSize);
	                in.close();
	                System.out.println("Read " + shardFile);
	            }
	        }

	        // We need at least DATA_SHARDS to be able to reconstruct the file.
	        if (shardCount < DATA_Chunk) {
	            System.out.println("Not enough shards present");
	            return;
	        }

	        // Make empty buffers for the missing shards.
	        for (int i = 0; i < TOTAL_Chunk; i++) {
	            if (!shardPresent[i]) {
	                shards[i] = new byte [shardSize];
	            }
	        }

	        // Use Reed-Solomon to fill in the missing shards
	        Secureconding reedSolomon = new Secureconding(DATA_Chunk, PAIR_SHARDS);
	        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

	        // Combine the data shards into one buffer for convenience.
	        // (This is not efficient, but it is convenient.)
	        byte [] allBytes = new byte [shardSize * DATA_Chunk];
	        for (int i = 0; i < DATA_Chunk; i++) {
	            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
	        }

	        // Extract the file length
	        int fileSize = ByteBuffer.wrap(allBytes).getInt();

	        // Write the decoded file
	        File decodedFile = new File(originalFile.getParentFile(), originalFile.getName());
	        OutputStream out;
			
				out = new FileOutputStream(decodedFile);
				out.write(allBytes, BYTES_IN, fileSize);
		        System.out.println("Wrote " + decodedFile);
			} catch (Exception  e) {
				// 
				e.printStackTrace();
			}
	        
		
	}
	
	
	
	@SuppressWarnings("resource")
	public static String download(String filename)
	{
		
		final File originalFile = new File(Dbconn.Output+filename);
	     
	     // Read in any of the shards that are present.
	     // (There should be checking here to make sure the input
	     // shards are the same size, but there isn't.)
	     final byte [] [] shards = new byte [TOTAL_Chunk] [];
	     final boolean [] shardPresent = new boolean [TOTAL_Chunk];
	     int shardSize = 0;
	     int shardCount = 0;
	     for (int i = 0; i < TOTAL_Chunk; i++) {
	      File shardFile = new File(Dbconn.filechunk+filename+"_"+i+".txt");
	         if (shardFile.exists()) {
	             shardSize = (int) shardFile.length();
	             shards[i] = new byte [shardSize];
	             shardPresent[i] = true;
	             shardCount += 1;
	             InputStream in;
				try {
					in = new FileInputStream(shardFile);
					 in.read(shards[i], 0, shardSize);
		             in.close();
		             System.out.println("Read " + shardFile);
				} catch (Exception  e) {
					// 
					e.printStackTrace();
				}
	            
	         }
	     }

	     // We need at least DATA_SHARDS to be able to reconstruct the file.
	     if (shardCount < DATA_Chunk) {
	         System.out.println("Not enough shards present");
	        // return;
	     }

	     // Make empty buffers for the missing shards.
	     for (int i = 0; i < TOTAL_Chunk; i++) {
	         if (!shardPresent[i]) {
	             shards[i] = new byte [shardSize];
	         }
	     }

	     // Use Reed-Solomon to fill in the missing shards
	     Secureconding reedSolomon = new Secureconding(DATA_Chunk, PAIR_SHARDS);
	     reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

	     // Combine the data shards into one buffer for convenience.
	     // (This is not efficient, but it is convenient.)
	     byte [] allBytes = new byte [shardSize * DATA_Chunk];
	     for (int i = 0; i < DATA_Chunk; i++) {
	         System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
	     }

	     // Extract the file length
	     int fileSize = ByteBuffer.wrap(allBytes).getInt();

	     // Write the decoded file
	     File decodedFile = new File(originalFile.getParentFile(), originalFile.getName());
	     OutputStream out;
		try {
			out = new FileOutputStream(decodedFile);
			 out.write(allBytes, BYTES_IN, fileSize);
		     System.out.println("Wrote " + decodedFile);
		} catch (Exception  e) {
			// 
			e.printStackTrace();
		}
		String filedata=fileread(Dbconn.Output+filename);
		return filedata;
	    
	}
	public static String fileread(String filepath)
	{
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
	
			// get file path and read file
			try {
				br = new BufferedReader(new FileReader(filepath));
			
			// file line by line read and check null
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			
			}
			br.close();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}
			return sb.toString();
	}
}

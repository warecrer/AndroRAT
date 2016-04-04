package utils;

import java.io.*;
import java.util.*;

public class wavIO {

	private String myPath;
	private long myChunkSize;
	private long mySubChunk1Size;
	private int myFormat;
	private long myChannels;
	private long mySampleRate;
	private long myByteRate;
	private int myBlockAlign;
	private int myBitsPerSample;
	private long myDataSize;
	public byte[] myData;

	public String getPath() {
		return myPath;
	}

	public void setPath(String newPath) {
		myPath = newPath;
	}

	public wavIO() {
		myPath = "";
	}

	public wavIO(String tmpPath) {
		myPath = tmpPath;
	}

	public void readRaw() {
		try {
			File f = new File(myPath);
			DataInputStream inFile = new DataInputStream(new FileInputStream(f));
			myDataSize = (int) f.length();
			myData = new byte[(int) myDataSize];
			inFile.read(myData);
			inFile.close();
		} catch (Exception e) {

		}
	}

	public boolean read() {
		DataInputStream inFile = null;
		myData = null;
		byte[] tmpLong = new byte[4];
		byte[] tmpInt = new byte[2];

		try {
			inFile = new DataInputStream(new FileInputStream(myPath));
			String chunkID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte()
					+ (char) inFile.readByte();
			inFile.read(tmpLong);
			myChunkSize = byteArrayToLong(tmpLong);

			String format = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte()
					+ (char) inFile.readByte();

			String subChunk1ID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte()
					+ (char) inFile.readByte();

			inFile.read(tmpLong);
			mySubChunk1Size = byteArrayToLong(tmpLong);

			inFile.read(tmpInt);
			myFormat = byteArrayToInt(tmpInt);

			inFile.read(tmpInt);
			myChannels = byteArrayToInt(tmpInt);

			inFile.read(tmpLong);
			mySampleRate = byteArrayToLong(tmpLong);

			inFile.read(tmpLong);
			myByteRate = byteArrayToLong(tmpLong);

			inFile.read(tmpInt);
			myBlockAlign = byteArrayToInt(tmpInt);

			inFile.read(tmpInt);
			myBitsPerSample = byteArrayToInt(tmpInt);

			String dataChunkID = "" + (char) inFile.readByte() + (char) inFile.readByte() + (char) inFile.readByte()
					+ (char) inFile.readByte();

			inFile.read(tmpLong);
			myDataSize = byteArrayToLong(tmpLong);
			myData = new byte[(int) myDataSize];
			inFile.read(myData);
			inFile.close();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public void setHeaders() {
		myChunkSize = myDataSize + 36;
		mySubChunk1Size = 16;
		myFormat = 1;
		myChannels = 1;
		mySampleRate = 11025;
		myByteRate = 20050;
		myBlockAlign = 2;
		myBitsPerSample = 16;
	}

	public boolean save() {
		try {
			DataOutputStream outFile = new DataOutputStream(new FileOutputStream(myPath));
			outFile.writeBytes("RIFF");
			outFile.write(intToByteArray((int) myChunkSize), 0, 4);
			outFile.writeBytes("WAVE");
			outFile.writeBytes("fmt ");
			outFile.write(intToByteArray((int) mySubChunk1Size), 0, 4);
			outFile.write(shortToByteArray((short) myFormat), 0, 2);
			outFile.write(shortToByteArray((short) myChannels), 0, 2);
			outFile.write(intToByteArray((int) mySampleRate), 0, 4);
			outFile.write(intToByteArray((int) myByteRate), 0, 4);
			outFile.write(shortToByteArray((short) myBlockAlign), 0, 2);
			outFile.write(shortToByteArray((short) myBitsPerSample), 0, 2);
			outFile.writeBytes("data");
			outFile.write(intToByteArray((int) myDataSize), 0, 4);
			outFile.write(myData);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}

		return true;
	}

	public boolean save2() {
		try {
			DataOutputStream outFile = new DataOutputStream(new FileOutputStream(myPath));

			outFile.writeBytes("RIFF");
			outFile.writeInt(Integer.reverseBytes((int) myChunkSize));
			outFile.writeBytes("WAVE");
			outFile.writeBytes("fmt ");
			outFile.writeInt(Integer.reverseBytes((int) mySubChunk1Size));
			outFile.writeShort(Short.reverseBytes((short) myFormat));
			outFile.writeShort(Short.reverseBytes((short) myChannels));
			outFile.writeInt(Integer.reverseBytes((int) mySampleRate));
			outFile.writeInt(Integer.reverseBytes((int) myByteRate));
			outFile.writeShort(Short.reverseBytes((short) myBlockAlign));
			outFile.writeShort(Short.reverseBytes((short) myBitsPerSample));
			outFile.writeBytes("data");
			outFile.writeInt(Integer.reverseBytes((int) myDataSize));
			outFile.write(myData);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}

		return true;
	}

	public String getSummary() {
		String newline = "<br>";
		String summary = "<html>Format: " + myFormat + newline + "Channels: " + myChannels + newline + "SampleRate: "
				+ mySampleRate + newline + "ByteRate: " + myByteRate + newline + "BlockAlign: " + myBlockAlign + newline
				+ "BitsPerSample: " + myBitsPerSample + newline + "DataSize: " + myDataSize + "</html>";
		return summary;
	}

	public static int byteArrayToInt(byte[] b) {
		int start = 0;
		int low = b[start] & 0xff;
		int high = b[start + 1] & 0xff;
		return (int) (high << 8 | low);
	}

	public static long byteArrayToLong(byte[] b) {
		int start = 0;
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = b[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return accum;
	}

	private static byte[] intToByteArray(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);
		return b;
	}

	public static byte[] shortToByteArray(short data) {
		return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
	}

}
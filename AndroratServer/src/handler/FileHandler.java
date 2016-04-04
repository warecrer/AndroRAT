package handler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import server.Server;
import Packet.FilePacket;
import Packet.Packet;
import Packet.PreferencePacket;
import gui.GUI;

public class FileHandler implements PacketHandler {

	private GUI gui;
	private int channel;
	private String imei;
	private String dir;
	private String dwnName;
	private short nextNumSeq = 0;
	private HashMap<Short, byte[]> tempData;
	private long dataLength = 0;
	private short max = 10;
	private FileOutputStream fout;

	public FileHandler(int chan, String imei, GUI gui, String dir, String dwnName) {
		channel = chan;
		this.imei = imei;
		this.gui = gui;
		this.dir = dir;
		this.dwnName = dwnName;
		tempData = null;
		File f = new File(dir);
		if (!f.exists())
			f.mkdirs();
		f = new File(dir, dwnName);
		try {
			fout = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			gui.logErrTxt("File not found on Server.");
		}
		tempData = new HashMap<Short, byte[]>();
	}

	@Override
	public void receive(Packet p, String imei) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handlePacket(Packet p, String temp_imei, Server c) {
		c.getChannelHandlerMap().get(imei).getStorage(channel).reset();
		FilePacket packet = (FilePacket) p;
		try {
			int length = packet.getData().length;
			short numSeq = packet.getNumSeq();

			if (numSeq == nextNumSeq) {
				fout.write(packet.getData());
				dataLength += length;
				fillFile(numSeq);
				if (packet.getMf() == 1) {
					nextNumSeq++;
				} else {
					gui.logTxt("File transfert complete !");
					fout.close();
					c.getChannelHandlerMap().get(imei).removeListener(channel);
				}
			} else {
				if (tempData.size() <= max)
					tempData.put(numSeq, packet.getData());
				else {
					gui.logErrTxt("File chunk missing. Stop");
					fout.close();
					c.getChannelHandlerMap().get(imei).removeListener(channel);
				}
			}
		} catch (IOException e) {
			gui.logErrTxt("IOException while trying to write in the file");
			c.getChannelHandlerMap().get(imei).removeListener(channel);
		}
	}

	private void fillFile(short numSeq) throws IOException {
		short num = numSeq;
		while (tempData.containsKey(num + 1)) {
			fout.write(tempData.get(num + 1));
			tempData.remove(num + 1);
			num++;
		}
		nextNumSeq = num;
	}

}

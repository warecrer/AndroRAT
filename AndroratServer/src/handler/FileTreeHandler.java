package handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import server.Server;
import Packet.FileTreePacket;
import Packet.Packet;
import gui.GUI;

public class FileTreeHandler implements PacketHandler {

	private GUI gui;
	private int channel;
	private String imei;

	public FileTreeHandler(int chan, String imei, GUI gui) {
		channel = chan;
		this.imei = imei;
		this.gui = gui;
	}

	@Override
	public void receive(Packet p, String imei) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handlePacket(Packet p, String temp_imei, Server c) {
		gui.logTxt("File tree data has been received");
		c.getChannelHandlerMap().get(imei).removeListener(channel);
		FileTreePacket packet = (FileTreePacket) p;
		gui.updateFileTree(imei, packet.getList());
	}
}

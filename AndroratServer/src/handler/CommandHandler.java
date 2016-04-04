package handler;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;

import server.ClientHandler;
import server.Server;

import inout.Protocol;
import Packet.CommandPacket;
import Packet.LogPacket;
import Packet.Packet;
import handler.ChannelDistributionHandler;

public class CommandHandler implements PacketHandler {
	private short command;
	private byte[] arg;

	public CommandHandler() {

	}

	@Override
	public void handlePacket(Packet p, String temp_imei, Server c) {

		command = ((CommandPacket) p).getCommand();
		arg = ((CommandPacket) p).getArguments();

		switch (command) {
		case Protocol.CONNECT:

			ByteArrayInputStream bis = new ByteArrayInputStream(arg);
			ObjectInputStream in;
			Hashtable<String, String> h = null;
			try {
				in = new ObjectInputStream(bis);
				h = (Hashtable<String, String>) in.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String new_imei = h.get("IMEI");

			c.getGui().logTxt("CONNECT command received from " + new_imei);
			if (!c.getClientMap().containsKey(new_imei)) {

				ClientHandler ch = c.getClientMap().get(temp_imei);
				ChannelDistributionHandler cdh = c.getChannelHandlerMap().get(temp_imei);

				ch.updateIMEI(new_imei);
				c.getClientMap().remove(temp_imei);
				c.getChannelHandlerMap().remove(temp_imei);
				c.getClientMap().put(new_imei, ch);
				c.getChannelHandlerMap().put(new_imei, cdh);
				c.getChannelHandlerMap().get(new_imei).registerListener(1, new LogPacket());
				c.getChannelHandlerMap().get(new_imei).registerHandler(1,
						new ClientLogHandler(1, new_imei, c.getGui()));
			} else {
				ClientHandler ch1 = c.getClientMap().get(temp_imei);
				ChannelDistributionHandler cdh1 = c.getChannelHandlerMap().get(new_imei);

				c.getClientMap().remove(temp_imei);
				c.getChannelHandlerMap().remove(temp_imei);
				c.getChannelHandlerMap().remove(new_imei);

				c.getClientMap().put(new_imei, ch1);
				c.getChannelHandlerMap().put(new_imei, cdh1);

			}
			c.getGui().addUser(new_imei, h.get("Country"), h.get("PhoneNumber"), h.get("Operator"), h.get("SimCountry"),
					h.get("SimOperator"), h.get("SimSerial"));

			break;

		}

	}

	@Override
	public void receive(Packet p, String imei) {
		// TODO Auto-generated method stub

	}

}

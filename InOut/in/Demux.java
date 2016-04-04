package in;

import Packet.TransportPacket;
import inout.Controler;
import inout.Protocol;
import java.nio.ByteBuffer;

public class Demux {

	private Controler controler;
	private TransportPacket p;
	private String imei;
	private ByteBuffer buffer;
	private boolean partialDataExpected, reading;

	public Demux(Controler s, String i) {
		imei = i;
		controler = s;
		reading = true;
		partialDataExpected = false;

	}

	public boolean receive(ByteBuffer buffer) throws Exception {

		while (reading) {

			if (!partialDataExpected) {
				if ((buffer.limit() - buffer.position()) < Protocol.HEADER_LENGTH_DATA) {
					return true;
				}
			}

			if (partialDataExpected)
				partialDataExpected = p.parseCompleter(buffer);
			else {
				p = new TransportPacket();
				partialDataExpected = p.parse(buffer);
			}

			if (partialDataExpected)
				return true;
			else
				controler.Storage(p, imei);

		}

		reading = true;
		return true;
	}

	public void setImei(String i) {
		imei = i;
	}

}

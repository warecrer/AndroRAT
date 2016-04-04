package handler;

import java.io.IOException;
import java.util.ArrayList;

import Packet.TransportPacket;

import inout.Protocol;

public class TemporaryStorage {
	private ArrayList<byte[]> data_temp;

	private byte[] final_data;

	private int total_length;
	private int last_packet_position;
	private int size_counter;
	private boolean end;

	private short data_type;

	public TemporaryStorage() {
		data_temp = new ArrayList<byte[]>();
		last_packet_position = -1;
		size_counter = 0;

	}

	public void reset() {
		data_temp = new ArrayList<byte[]>();
		last_packet_position = -1;
		size_counter = 0;
		end = false;
	}

	public short addPacket(TransportPacket packet) {

		if (packet.getNumSeq() != (last_packet_position + 1))
			return Protocol.PACKET_LOST;

		if (!end) {

			total_length = packet.getTotalLength();
			end = packet.isLast();
			size_counter += packet.getLocalLength();
			data_temp.add(packet.getData());

			if (!end) {
				System.out.println("Paquet " + packet.getNumSeq());
				last_packet_position++;
				return Protocol.PACKET_DONE;
			} else {

				if (size_counter != total_length)
					return Protocol.SIZE_ERROR;

				int i = 0;
				final_data = new byte[total_length];
				for (int n = 0; n < data_temp.size(); n++)
					for (int p = 0; p < data_temp.get(n).length; p++, i++)
						final_data[i] = data_temp.get(n)[p];

				return Protocol.ALL_DONE;
			}

		} else
			return Protocol.NO_MORE;

	}

	public ArrayList<byte[]> getByteData() {
		return data_temp;
	}

	public byte[] getFinalData() {
		return final_data;
	}

	public int getLastPacketPositionReceived() {
		return last_packet_position;
	}

	public int getTotalSize() {
		return total_length;
	}
}

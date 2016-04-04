package Packet;

public class RawPacket implements Packet {
	
	private byte[] data;
	
	public RawPacket() {
	
	}
	
	public RawPacket(byte[] data) {
		this.data = data;
	}

	public byte[] build() {
		return data;
	}

	public void parse(byte[] packet) {
		data = packet;
	}
	
	public byte[] getData() {
		return data;
	}
}

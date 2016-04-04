package Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class SMSPacket implements Packet, Serializable {

	private static final long serialVersionUID = 6169284240601506961L;

	private int id;
	private int thread_id;
	private String address;
	private int person;
	private long date;
	private int read;
	private int type;
	private String body;

	public SMSPacket() {

	}

	public SMSPacket(int id, int thid, String ad, int pers, long dat, int read, String body, int type) {
		this.id = id;
		this.thread_id = thid;
		this.address = ad;
		this.person = pers;
		this.date = dat;
		this.read = read;
		this.body = body;
		this.type = type;
	}

	public byte[] build() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(this);
			return bos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public void parse(byte[] packet) {
		ByteArrayInputStream bis = new ByteArrayInputStream(packet);
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(bis);
			SMSPacket p = (SMSPacket) in.readObject();
			this.id = p.id;
			this.thread_id = p.thread_id;
			this.address = p.address;
			this.body = p.body;
			this.date = p.date;
			this.person = p.person;
			this.read = p.read;
			this.type = p.type;

		} catch (Exception e) {
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int t) {
		this.type = t;
	}

	public int getId() {
		return id;
	}

	public int getThread_id() {
		return thread_id;
	}

	public String getAddress() {
		return address;
	}

	public int getPerson() {
		return person;
	}

	public long getDate() {
		return date;
	}

	public int getRead() {
		return read;
	}

	public String getBody() {
		return body;
	}
}

package out;

import inout.Protocol;

import java.io.DataOutputStream;

import Packet.TransportPacket;

public class Mux 
{

	Sender sender ;
	
	public Mux(DataOutputStream out)
	{
		sender = new Sender(out);
	}


	public void send(int chan,byte[] data)
	{
		try
		{
			TransportPacket tp;
			boolean last = false;
			boolean envoieTotal = false;
			int pointeurData = 0;
			short numSeq = 0;
			int actualLenght;

			while (!envoieTotal) 
			{
				byte[] dataToSend;

				
				if (last || ((Protocol.HEADER_LENGTH_DATA + data.length) < Protocol.MAX_PACKET_SIZE))
				{
					dataToSend = new byte[Protocol.HEADER_LENGTH_DATA + (data.length - pointeurData)];
					last = true ;
					envoieTotal = true ;
				}
				else
					dataToSend = new byte[Protocol.MAX_PACKET_SIZE];
				
				actualLenght = dataToSend.length - Protocol.HEADER_LENGTH_DATA;


				byte[] fragData = new byte[dataToSend.length-Protocol.HEADER_LENGTH_DATA];
				System.arraycopy(data, pointeurData, fragData, 0, fragData.length);
				tp = new TransportPacket(data.length, actualLenght, chan, last, numSeq, fragData);
				dataToSend = tp.build();
					pointeurData = pointeurData + actualLenght;
				numSeq++;
				if ((data.length - pointeurData) <= (Protocol.MAX_PACKET_SIZE - Protocol.HEADER_LENGTH_DATA))
				{
					last = true;
				}
				
				sender.send(dataToSend);

			}
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}
}

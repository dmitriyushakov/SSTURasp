package sstuclient;

public class ByteBuffer {
	private int index;
	private int capacity;
	private byte buf[];
	@SuppressWarnings("unused")
	private ByteBuffer(){};
	public ByteBuffer(int capacity){
		this.capacity=capacity;
		index=0;
		buf=new byte[capacity];
	}
	public void put(byte b){
		if(index==capacity){
			int newcapacity=3*capacity/2;
			byte newbuf[]=new byte[newcapacity];
			System.arraycopy(buf,0,newbuf,0,capacity);
			buf=newbuf;
			capacity=newcapacity;
		}
		buf[index++]=b;
	}
	public byte[] array(){
		byte result[]=new byte[index];
		System.arraycopy(buf,0,result,0,index);
		
		return result;
	}
}

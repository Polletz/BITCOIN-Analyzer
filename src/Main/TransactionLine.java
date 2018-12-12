package Main;

import Utils.utils;
import io.nayuki.bitcoin.crypto.Sha256Hash;

public class TransactionLine {

	private Sha256Hash hash;
	private long timestamp;
	private long altezza;
	
	public TransactionLine(Sha256Hash hash, long timestamp) {
		super();
		this.hash = hash;
		this.timestamp = timestamp;
	}
	
	public long getAltezza() {
		return altezza;
	}

	public void setAltezza(long altezza) {
		this.altezza = altezza;
	}
	
	public Sha256Hash getHash() {
		return hash;
	}

	public void setHash(Sha256Hash hash) {
		this.hash = hash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public static TransactionLine fromLineToTransaction(String line){
		
		String[] tokens = line.split(" ");

		if(tokens.length != 2) return null;
		
		byte[] bytearray = utils.hexStringToByteArray(tokens[0]);
		
		//utils.reverseByteArray(bytearray);
		
		Sha256Hash hash = new Sha256Hash(bytearray);
		long time = Long.parseLong(tokens[1]);
		
		return new TransactionLine(hash,time);
		
	}
	
	public String toString(){
		
		return hash.toString() + " " + altezza;
		
	}
	
}

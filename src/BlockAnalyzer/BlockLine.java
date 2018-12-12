package BlockAnalyzer;

import Utils.utils;
import io.nayuki.bitcoin.crypto.Sha256Hash;

public class BlockLine {

	private Sha256Hash hash;
	private long timestamp;
	private long altezza;
	private Sha256Hash previous;
	private long altezza_reale;
	
	public long getAltezza_reale() {
		return altezza_reale;
	}

	public void setAltezza_reale(long altezza_reale) {
		this.altezza_reale = altezza_reale;
	}

	public Sha256Hash getPrevious() {
		return previous;
	}

	public void setPrevious(Sha256Hash previous) {
		this.previous = previous;
	}

	public BlockLine(Sha256Hash hash, long timestamp) {
		this.hash = hash;
		this.timestamp = timestamp;
	}
	
	public long getAltezza(){
		return altezza;
	}
	
	public void setAltezza(long a){
		altezza = a;
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

	@Override
	public boolean equals(Object obj) {
		if(!hash.equals(((BlockLine) obj).getHash())) return false;
		return true;
	}

	@Override
	public String toString() {
		return utils.bytesToHex(getHash().toBytes()) + " " + utils.bytesToHex(getPrevious().toBytes()) + " " +getTimestamp() + " " + getAltezza_reale();
	}
	
	public static BlockLine fromLineToBlock(String line){
		
		String[] tokens = line.split(" ");

		if(tokens.length != 2) return null;
		
		byte[] bytearray = utils.hexStringToByteArray(tokens[0]);
		
		utils.reverseByteArray(bytearray);
		
		Sha256Hash hash = new Sha256Hash(bytearray);
		long time = Long.parseLong(tokens[1]);
		
		return new BlockLine(hash,time);
		
	}
}

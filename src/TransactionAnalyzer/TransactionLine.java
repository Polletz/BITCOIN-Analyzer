package TransactionAnalyzer;

import Utils.utils;
import io.nayuki.bitcoin.crypto.Sha256Hash;

public class TransactionLine {

	private Sha256Hash hash;
	private long timestamp;	//quando lho ricevuta
	private long timestamp_blocco; // timestamp di quando ho visto il blocco che la contiene -> quando è stata validata
	private long altezza_iniziale; //Altezza nella BlockChain di quando ho ricevuto la tx
	private long altezza_relativa_iniziale; // Altezza locale
	private long altezza_finale; // Altezza nella BlockChain di quando è stato validata la tx
	private long altezza_relativa_finale;
	private long size; // Grandezza in Byte
	private long weight; // Peso nel blocco (non grandezza)
	private long tot_input; // somma dei valori di input
	private long tot_output; // somma dei valori di output
	private long fee; // input - output
	private double feeOnByte; // fee/size
	private String line; // linea di testo
	
	public TransactionLine(Sha256Hash hash, long timestamp, String line) {
		this.hash = hash;
		this.timestamp = timestamp;
		this.line = line;
	}
	
	public long getTimestamp_blocco() {
		return timestamp_blocco;
	}

	public void setTimestamp_blocco(long timestamp_blocco) {
		this.timestamp_blocco = timestamp_blocco;
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(Long long1) {
		this.size = long1;
	}

	public long getWeight() {
		return weight;
	}

	public void setWeight(Long long1) {
		this.weight = long1;
	}

	public long getTot_input() {
		return tot_input;
	}

	public void setTot_input(long inputs_sum) {
		this.tot_input = inputs_sum;
	}

	public long getTot_output() {
		return tot_output;
	}

	public void setTot_output(long outputs_sum) {
		this.tot_output = outputs_sum;
	}

	public long getFee() {
		return fee;
	}

	public void setFee(long l) {
		this.fee = l;
	}

	public double getFeeOnByte() {
		return feeOnByte;
	}

	public void setFeeOnByte(double feeOnByte) {
		this.feeOnByte = feeOnByte;
	}
	
	public long getAltezza_iniziale() {
		return altezza_iniziale;
	}

	public void setAltezza_iniziale(long altezza_iniziale) {
		this.altezza_iniziale = altezza_iniziale;
	}

	public long getAltezza_relativa_iniziale() {
		return altezza_relativa_iniziale;
	}

	public void setAltezza_relativa_iniziale(long altezza_relativa_iniziale) {
		this.altezza_relativa_iniziale = altezza_relativa_iniziale;
	}

	public long getAltezza_finale() {
		return altezza_finale;
	}

	public void setAltezza_finale(long altezza_finale) {
		this.altezza_finale = altezza_finale;
	}

	public long getAltezza_relativa_finale() {
		return altezza_relativa_finale;
	}

	public void setAltezza_relativa_finale(long altezza_relativa_finale) {
		this.altezza_relativa_finale = altezza_relativa_finale;
	}
	
	public String getLine() {
		return line;
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
		
		utils.reverseByteArray(bytearray);
		
		Sha256Hash hash = new Sha256Hash(bytearray);
		long time = Long.parseLong(tokens[1]);
		
		return new TransactionLine(hash,time,line);
		
	}
	
	public String toString(){
		
		return hash.toString() + " " + altezza_iniziale + " " + altezza_finale + " " + size + " " + feeOnByte + " " + timestamp + " " + timestamp_blocco; 
		
	}
	
}

package TransactionAnalyzer;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Utils.utils;

public class TransactionHTTPRequestSender implements Runnable{

	LinkedBlockingQueue<TransactionLine> results;
	String line;
	
	public TransactionHTTPRequestSender(String line, LinkedBlockingQueue<TransactionLine> results) {
		this.line = line;
		this.results = results;
	}
	
	@Override
	public void run() {
		
		JSONParser parser = new JSONParser();
		
		TransactionLine t = TransactionLine.fromLineToTransaction(line);
		
		/**************************************************************************/
		String transaction=null;
		boolean retry = true;
		while(retry){
			try{
				transaction = utils.sendGet("https://blockchain.info/rawtx/" + utils.bytesToHex(t.getHash().toBytes()));
				retry = false;
			}catch(IOException e){
				if(e.getMessage().startsWith("Server returned HTTP response code: 500 for URL:")) return;
			}
		}
		
		JSONObject json;
		try {
			json = (JSONObject) parser.parse(transaction);
			
			t.setAltezza_finale((Long) (json.get("block_height")));
			
			t.setSize((Long) (json.get("size")));
			t.setWeight((Long) (json.get("weight")));
			
			//int n_in = (Integer) (json.get("vin_sz"));
			//int n_out = (Integer) (json.get("vout_sz"));
			
			long inputs_sum = 0;
			long outputs_sum = 0;
			
			JSONArray inputs = (JSONArray) json.get("inputs");
			for (int i = 0; i < inputs.size(); i++) { // Walk through the Array.
			    JSONObject obj = (JSONObject) inputs.get(i);
			    JSONObject previous = (JSONObject) obj.get("prev_out");
			    inputs_sum+=(Long) previous.get("value");
			}
			
			JSONArray outputs = (JSONArray) json.get("out");
			for (int i = 0; i < outputs.size(); i++) { // Walk through the Array.
			    JSONObject obj = (JSONObject) outputs.get(i);
			    outputs_sum+=(Long) obj.get("value");
			}
			
			t.setTot_input(inputs_sum);
			t.setTot_output(outputs_sum);
			t.setFee(inputs_sum-outputs_sum);
			t.setFeeOnByte(t.getFee()/t.getSize());
			
			results.put(t);
		} catch (ParseException e) {
			return;
		}catch (InterruptedException e) {
			return;
		}
		/*********************************************************************************/ 
	}
}

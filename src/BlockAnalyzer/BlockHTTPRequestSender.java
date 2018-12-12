package BlockAnalyzer;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Utils.utils;
import io.nayuki.bitcoin.crypto.Sha256Hash;

public class BlockHTTPRequestSender implements Runnable{
	
	public BlockHTTPRequestSender(String line, LinkedBlockingQueue<BlockLine> results) {
		this.line = line;
		this.results = results;
	}
	
	LinkedBlockingQueue<BlockLine> results;
	String line;
	
	@Override
	public void run() {
		JSONParser parser = new JSONParser();
			
		BlockLine b = BlockLine.fromLineToBlock(line);
		
		/**************************************************************************/
		String block=null;
		boolean retry = true;
		while(retry){
			try{
				block = utils.sendGet("https://blockchain.info/rawblock/" + utils.bytesToHex(b.getHash().toBytes()));
				retry = false;
			}catch(IOException e){
				if(e.getMessage().startsWith("Server returned HTTP response code: 500 for URL:")) return;
			}
		}
		
		JSONObject json;
		try {
			json = (JSONObject) parser.parse(block);
			b.setAltezza_reale((Long)(json.get("height")));
			byte[] bytearray = utils.hexStringToByteArray((String) json.get("prev_block"));
			Sha256Hash previous = new Sha256Hash(bytearray);
			System.out.println("Previous : " + utils.bytesToHex(previous.toBytes()));
			b.setPrevious(previous);
			results.put(b);
		} catch (ParseException e) {
			return;
		}catch (InterruptedException e) {
			return;
		}
		/*********************************************************************************/ 
	}
}

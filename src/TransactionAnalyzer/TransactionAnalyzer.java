package TransactionAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Main.Main;

public class TransactionAnalyzer implements Runnable{

	LinkedBlockingQueue<TransactionLine> toAnalyze;
	LinkedBlockingQueue<TransactionLine> toWrite;
	
	public TransactionAnalyzer(LinkedBlockingQueue<TransactionLine> ta,
			LinkedBlockingQueue<TransactionLine> tw) {
		this.toAnalyze = ta;
		this.toWrite = tw;
	}
	
	@Override
	public void run() {
		
		while(true){
		
			TransactionLine t=null;
			try {
				t = toAnalyze.poll(5, TimeUnit.MINUTES);
				if(t==null) break;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			
			try {
				File file = new File(Main.ORDERED_CHAINS_FILE);
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				
				String line;
				while((line = reader.readLine()) != null){
					
					String[] tokens = line.split(" ");
					if(tokens[0].equals("Catena")){
						
						int length = Integer.parseInt(tokens[1]);
						
						LinkedList<String> catena = new LinkedList<>();
						for(int i=0;i<length;i++) catena.add(reader.readLine());
						
						if(Integer.parseInt(catena.getFirst().split(" ")[3]) < Main.MIN_BLOCK_HEIGHT) continue;
						
						for(int i=0;i<catena.size()-1;i++){
							String[] chain_tokens = catena.get(i).split(" ");
							long left_timestamp = Long.parseLong(chain_tokens[2]);
							long block_height = Long.parseLong(chain_tokens[3]);
							
							chain_tokens = catena.get(i+1).split(" ");
							long right_timestamp = Long.parseLong(chain_tokens[2]);
							
							if(t.getTimestamp()>=left_timestamp && t.getTimestamp()<right_timestamp){
								t.setAltezza_iniziale(block_height);
								t.setAltezza_relativa_iniziale((long) i);
							}
							
							if(t.getAltezza_finale()==block_height){
								t.setAltezza_relativa_finale((long) i);
								t.setTimestamp_blocco(left_timestamp);
								break;
							}
						}
					}
				}
				reader.close();
				if(t.getAltezza_iniziale()>0)
					toWrite.add(t);
				
				System.out.println("Transazione Analizzata");
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				break;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}

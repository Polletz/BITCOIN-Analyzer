package TransactionAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Main.Main;

public class TransactionWriter implements Runnable{
	
	LinkedBlockingQueue<TransactionLine> toWrite;
	
	public TransactionWriter(LinkedBlockingQueue<TransactionLine> tw) {
		this.toWrite = tw;
	}
	
	@Override
	public void run() {
		
		try {
			
			PrintStream filestream = new PrintStream(new File(Main.HEIGHT_TRANSACTION_FILE));
		
			while(true){
				TransactionLine t = toWrite.take();
				if(t==null){
					filestream.close();
					return;
				}
				filestream.println(t);
				System.out.println("Transazione Scritta");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}
}

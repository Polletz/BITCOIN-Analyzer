package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import BlockAnalyzer.BlockHTTPRequestSender;
import BlockAnalyzer.BlockLine;
import BlockAnalyzer.BlockWriter;
import TransactionAnalyzer.TransactionAnalyzer;
import TransactionAnalyzer.TransactionHTTPRequestSender;
import TransactionAnalyzer.TransactionLine;
import TransactionAnalyzer.TransactionWriter;

public class Main {

	public static String BLOCK_FILE = "blocchiSecondaSettimana.txt";
	public static String TRANSACTION_FILE = "transazioniSecondaSettimana.txt";
	public static String CHAINS_FILE = "cateneSecondaSettimana.txt";
	public static String ORDERED_CHAINS_FILE = "cateneOrdinateSecondaSettimana.txt";
	public static String HEIGHT_TRANSACTION_FILE = "TransazioniConAltezzaSecondaSettimana.txt";
	
	public static int MIN_BLOCK_HEIGHT =  544911;
	
	public static List<TransactionLine> Transazioni = new Vector<TransactionLine>();
	
	public static Socket s = new Socket();
	
	public static void main(String[] args) {
		PrintStream logger=null;
		try {
			logger = new PrintStream(new File("log.txt"));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			System.exit(-1);
		}
		/*
		try {
			File file = new File(CHAINS_FILE);
			Files.delete(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			CreaCatenaDiBlocchi(logger);
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}*/
		
		try {
			AnalizzaTransazioni(logger);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void CreaCatenaDiBlocchi(PrintStream logger) throws FileNotFoundException, IOException, InterruptedException{
		
		LinkedBlockingQueue<BlockLine> results = new LinkedBlockingQueue<>();
		ExecutorService executor = Executors.newFixedThreadPool(5);
		BufferedReader br = new BufferedReader(new FileReader(BLOCK_FILE));
			
		String line;
	    while ((line = br.readLine()) != null) {
	    	executor.execute(new BlockHTTPRequestSender(line,results));
	    }
	    br.close();
	    executor.shutdown();
	    logger.println("All BlockHTTPRequestSender threads created.");
	    
	    BlockWriter bw = new BlockWriter(results);
	    Thread t = new Thread(bw);
	    t.start();
	    logger.println("BlockWriter started.");
	    
	    while(!executor.isTerminated()){
	    	Thread.sleep(1000);
	    }
	    logger.println("All BlockHTTPRequestSender threads terminated.");
	    
	    t.join();
	    logger.println("BlockWriter terminated.");
	}
	
	public static void AnalizzaTransazioni(PrintStream logger) throws FileNotFoundException, IOException, InterruptedException{
		
		LinkedBlockingQueue<TransactionLine> toAnalyze = new LinkedBlockingQueue<>();
		LinkedBlockingQueue<TransactionLine> toWrite = new LinkedBlockingQueue<>();
		ExecutorService executor = Executors.newFixedThreadPool(5);
		Vector<Thread> threads = new Vector<>();
		BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE));
			
		long n_tx = 0;
		
		String line;
	    while ((line = br.readLine()) != null) {
	    	executor.execute(new TransactionHTTPRequestSender(line,toAnalyze));
	    	n_tx++;
	    }
	    br.close();
	    executor.shutdown();
	    logger.println("All TransactionHTTPRequestSender thread created : " + n_tx);
	    
	    for(int i=0;i<5;i++){
	    	Thread t = new Thread(new TransactionAnalyzer(toAnalyze, toWrite));
	    	t.start();
	    	threads.add(t);
	    }
	    logger.println("All TransactionAnalyzer threads created.");
	    
	    TransactionWriter tw = new TransactionWriter(toWrite);
	    Thread t = new Thread(tw);
	    t.start();
	    logger.println("TransactionWriter started.");

	    while(!executor.isTerminated()) Thread.sleep(1000);
	    logger.println("All TransactionHTTPRequestSender thread terminated.");
	    
	    for(Thread tr : threads) tr.join();
	    logger.println("All TransactionAnalyzer thread terminated");
	    
	    t.join();
	    logger.println("TransactionWriter terminated.");
	    
	}
}
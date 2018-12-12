package BlockAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import Main.Main;
import Utils.utils;
import io.nayuki.bitcoin.crypto.Sha256Hash;

public class BlockWriter implements Runnable{

	LinkedBlockingQueue<BlockLine> results;
	List<LinkedList<BlockLine>> Catene = new Vector<LinkedList<BlockLine>>();


	public BlockWriter(LinkedBlockingQueue<BlockLine> results) {
		this.results = results;
	}
	
	@Override
	public void run() {
		
		long numero_blocchi_mappa = 0;
		
		while(true){
			BlockLine b;
			try {
				b = results.poll(5, TimeUnit.MINUTES);
				if(b==null) break;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			
			// Se si attacca lo attacco, altrimenti creo un nuovo vettore
			// TODO vedere se si attacca anche davanti (?)
			boolean found = false;
			for(LinkedList<BlockLine> v : Catene){
				if(!v.isEmpty()){
					Sha256Hash first_previous = v.getFirst().getPrevious();
					
					Sha256Hash last_current = v.getLast().getHash();
					long last_altezza = v.getLast().getAltezza();
					
					//Se si attacca davanti
					if(first_previous.compareTo(b.getHash())==0){
						b.setAltezza(v.getFirst().getAltezza()-1);
						v.addFirst(b);
						found = true;
						break;
					}
					
					//Se si attacca dietro
					if(last_current.compareTo(b.getPrevious())==0){
						b.setAltezza(last_altezza+1);
						v.add(b);
						found = true;
						break;
					}
				}
			}
			
			if(!found){
				b.setAltezza(0);
				LinkedList<BlockLine> temp = new LinkedList<BlockLine>();
				temp.add(b);
				Catene.add(temp);
			}
			
			numero_blocchi_mappa++;
			
			if(numero_blocchi_mappa > 10000){
				
				writeChanges();
				
				Catene = new Vector<LinkedList<BlockLine>>();
				numero_blocchi_mappa = 0;
			}
		}
		writeChanges();
		System.out.println("Finito");
		
		int i = 0;
		try {
			while(SistemaCatene()){
				i++;
				System.out.println("Sistemo le catene " + i);
			}
		}catch(NullPointerException n){
			n.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			OrdinaCatene();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public boolean SistemaCatene() throws IOException, NullPointerException{

		boolean modified = false;	
		
		File file = new File(Main.CHAINS_FILE);
		if(!file.exists()){
			return false;
		}
		
		File newFile = new File("temp.txt");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		PrintWriter writer = new PrintWriter(newFile);
		
		HashMap<String,Boolean> catene_già_scritte = new HashMap<>();
		
		String line;
		while((line = reader.readLine()) != null){
			
			String[] tokens = line.split(" ");
			
			if(tokens[0].equals("Catena")){
				
				int length = Integer.parseInt(tokens[1]);
				
				LinkedList<String> catena = new LinkedList<>();
				for(int i=0;i<length;i++) catena.add(reader.readLine());
				if(!catene_già_scritte.containsKey(catena.getFirst())){
					catene_già_scritte.put(catena.getFirst(), true);
					BufferedReader newReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					
					String newLine;
					while((newLine = newReader.readLine()) != null){
						
						String[] newTokens = newLine.split(" ");
						if(newTokens[0].equals("Catena")){
							
							int newLength = Integer.parseInt(newTokens[1]);
							
							LinkedList<String> newCatena = new LinkedList<>();
							for(int i=0;i<newLength;i++) newCatena.add(newReader.readLine());
							if(!catene_già_scritte.containsKey(newCatena.getFirst())){
								
								// Se devo attaccare una catena davanti a quella attuale
								String[] current = catena.getFirst().split(" ");
								String[] previous = newCatena.getLast().split(" ");
								
								if(previous[0].equals(current[1])){
									Iterator<String> i = newCatena.descendingIterator();
									while(i.hasNext()) catena.addFirst(i.next());
									
									catene_già_scritte.put(newCatena.getFirst(), true);
								}
								// Se la devo attaccare dietro
								current = newCatena.getFirst().split(" ");
								previous = catena.getLast().split(" ");
								
								if(current[1].equals(previous[0])){
									for(String s : newCatena) catena.add(s);
									
									catene_già_scritte.put(newCatena.getFirst(), true);
								}
							}
							/*****************************************************************************/
							// Se la catena fa parte di una che già c'è
							String[] first = catena.getFirst().split(" ");
							String[] last = catena.getLast().split(" ");
							for(int j = 0; j < newCatena.size(); j++){
								String[] previous =  newCatena.get(j).split(" ");
								if(first[0].equals(previous[0])){
								
									// Ho trovato che si attacca a questa
									// Devo vedere se posso allungarla
									// Sono alla posizione j
									if(j>0){
										//Ho roba da attaccare davanti
										for(int k=j-1;k>=0;k--){
											catena.addFirst(newCatena.get(k));
										}
										catene_già_scritte.put(newCatena.getFirst(), true);
									}
								}
								if(last[0].equals(previous[0])){
									
									//int scarto = (newCatena.size()-j)-catena.size();
									if(j<newCatena.size()-1){
										//Ho roba da attaccare dietro
										for(int k=j+1;k<newCatena.size();k++) catena.addLast(newCatena.get(k));
										if(!catene_già_scritte.containsKey(newCatena.getFirst())) catene_già_scritte.put(newCatena.getFirst(), true);
									}
								}
								if(catene_già_scritte.containsKey(newCatena.getFirst())) break;
							}
							/*****************************************************************************/
							//Se la newCatena è una sottocatena della mia
							/***************************************************************************/
							
							
							// Se la catena fa parte di una che già c'è
							int my_first = Integer.parseInt(catena.getFirst().split(" ")[3]);
							int my_last = Integer.parseInt(catena.getLast().split(" ")[3]);
							
							int new_first = Integer.parseInt(newCatena.getFirst().split(" ")[3]);
							int new_last = Integer.parseInt(newCatena.getLast().split(" ")[3]);
							
							if(my_first<=new_first && my_last>=new_last)
								if(!catene_già_scritte.containsKey(newCatena.getFirst())) 
									catene_già_scritte.put(newCatena.getFirst(), true);
							
							/***************************************************************************/
						}
					}
					
					writer.println("Catena " + catena.size());
					for(String s : catena){
						writer.println(s);
					}
					
					//catene_già_scritte.put(catena.getFirst(), true);
					
					newReader.close();
					
				}
			}
		}
		
		reader.close();
		writer.close();
		
		// Replace the old file with the new
		
		if(!FileUtils.contentEquals(newFile, file)) modified = true;
		
		//Files.copy(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.move(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		return modified;
	}
		
	public void writeChanges() {
	    
		
		// Se il file non esiste lo creo e ci scrivo dentro le catene
		
		// The files we're working with
	    File file = new File(Main.CHAINS_FILE);
		
		if(!file.exists()){
			try{
				
				PrintStream filestream = new PrintStream(new File(Main.CHAINS_FILE));
				
				System.out.println("Numero Catene : " + Catene.size());
				//Stampare le catene
				for(LinkedList<BlockLine> v : Catene){
					if(!v.isEmpty()){
						filestream.println("Catena " + v.size());
						
						for(BlockLine b : v){
							filestream.println(b.toString());
						}
					}
				}
				
				filestream.close();
			}catch (FileNotFoundException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
	    	return;
		}
		
		
		
	    File newFile = new File("temp.txt");
	    //JSONParser parser = new JSONParser();
	    try {
	    	
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		    PrintWriter writer = new PrintWriter(newFile);
		    
	        String line;
	        
	        // Keep reading lines from the source file until we've read them all
	        while((line = reader.readLine()) != null) {
	            
	        	//Vedo se il blocco in questa linea è l'inizio di una catena, e quanto è lunga
	        	String[] tokens = line.split(" ");
	        	
	        	if(tokens[0].equals("Catena")){
	        		
	        		int length = Integer.parseInt(tokens[1]);
	        		
	        		LinkedList<String> block_lines = new LinkedList<String>();//Qui ci metto le stringhe da riscrivere
	        		
	        		// Metto quelli che già c'erano
					for(int i = 0; i < length; i++){
	        			
	        			String block_line = reader.readLine();
	        			
	        			block_lines.add(block_line);
	        			
	        		}
	        		
	        		// Vedo se si attacca qualcosa davanti
	        		tokens = block_lines.get(0).split(" ");
	        		
					byte[] bytearray = utils.hexStringToByteArray(tokens[1]);
					//utils.reverseByteArray(byte_previous);
					Sha256Hash previous = new Sha256Hash(bytearray);
					
					for(LinkedList<BlockLine> l : Catene){
						//Se si attacca davanti
						if(l.getLast().getHash().compareTo(previous)==0){
							//va inserita in block_lines tutta la catena
	
							Iterator<BlockLine> i = l.descendingIterator();
							while(i.hasNext()){
								
								block_lines.addFirst(i.next().toString());
							}
							
							Catene.remove(l);
							break;
						}
					}
					
					String last = block_lines.get(block_lines.size()-1);
				
					tokens = last.split(" ");
					bytearray = utils.hexStringToByteArray(tokens[0]);
					utils.reverseByteArray(bytearray);
					Sha256Hash hash = new Sha256Hash(bytearray);
					
					for(LinkedList<BlockLine> l : Catene){
						//Se si attacca dietro
						if(l.getFirst().getPrevious().compareTo(hash)==0){
							for(BlockLine b : l){
								block_lines.addLast(b.toString());
								length++;
							}
							Catene.remove(l);
							break;
						}
					}
					/**********************************************************************************/
					for(LinkedList<BlockLine> l : Catene){
						//Se fa parte di una catena già esistente
						
						tokens = block_lines.get(0).split(" ");
						bytearray = utils.hexStringToByteArray(tokens[0]);
						utils.reverseByteArray(bytearray);
						Sha256Hash first_hash = new Sha256Hash(bytearray);
						
						tokens = block_lines.get(block_lines.size()-1).split(" ");
						bytearray = utils.hexStringToByteArray(tokens[0]);
						utils.reverseByteArray(bytearray);
						Sha256Hash last_hash = new Sha256Hash(bytearray);
						
						for(int i = 0; i < l.size(); i++){
							
							if(l.get(i).getHash().compareTo(first_hash)==0){
								// Ho trovato che si attacca a questa
								// Devo vedere se posso allungarla
								// Sono alla posizione i
								
								if(i>0){
									//Ho roba da attaccare davanti
									for(int k=i-1;k>=0;k--){
										block_lines.addFirst(l.get(k).toString());
									}
								}
								Catene.remove(l);
								break;
							}
							if(l.get(i).getHash().compareTo(last_hash)==0){
								if(i<l.size()-1){
									//Ho roba da attaccare dietro
									for(int k=i+1;k<l.size();k++) block_lines.addLast(l.get(k).toString());
								}
								Catene.remove(l);
								break;
							}
						}
					}
					/***************************************************************************************/
					
					
					writer.println("Catena " + block_lines.size());
					for(String s : block_lines) writer.println(s);
					
	        	}
	        }
	        //Scrivo quelle rimaste
	        for(LinkedList<BlockLine> l : Catene){
	        	writer.println("Catena " + l.size());
	        	for(BlockLine b : l) writer.println(b.toString());
	        }
		        
	        reader.close();
	        writer.close();
	        
	    }catch (FileNotFoundException e) {
	    	e.printStackTrace();
			System.exit(-1);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	    // Replace the old file with the new
	    try {
			Files.move(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void OrdinaCatene() throws FileNotFoundException, IOException{
		
		System.out.println("Ordino le catene");
		
		File file = new File(Main.CHAINS_FILE);
		File newFile = new File("temp.txt");
		File cateneOrdinate = new File(Main.ORDERED_CHAINS_FILE);
		
		PrintWriter writer_ord = new PrintWriter(cateneOrdinate);
		boolean fine = true;
		do{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			int indice_minimo=0;
			long minimo = Long.MAX_VALUE;
			int j=0;
			
			String line;
			while((line = reader.readLine()) != null){
				//trovo la catena minima
				String[] tokens = line.split(" ");
				
				if(tokens[0].equals("Catena")){
					int length = Integer.parseInt(tokens[1]);
					
					String first = reader.readLine();
					for(int i = 0; i < length-1; i++) reader.readLine();
					
					tokens = first.split(" ");
					long value = Long.parseLong(tokens[3]);
					
					if(value < minimo){
						minimo = value;
						indice_minimo = j;
					}
				}
				j++;
			}
			reader.close();
			// "indice minimo" è l'indice della catena minima
			// Scrivo la minima catena nel file ordinato, e tutte le altre nell'altro file
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			PrintWriter writer = new PrintWriter(newFile);
			j = 0;
			fine = true;
			while((line = reader.readLine()) != null){
				fine = false;
				String[] tokens = line.split(" ");
				
				if(tokens[0].equals("Catena")){
					int length = Integer.parseInt(tokens[1]);
					
					if(j==indice_minimo){
						
						writer_ord.println("Catena " + length);
						for(int i = 0; i < length; i++) writer_ord.println(reader.readLine());
						
					}else{
						
						writer.println("Catena " + length);
						for(int i = 0; i < length; i++) writer.println(reader.readLine());
						
					}
					j++;
				}
			}
			reader.close();
			writer.close();
			
			//Ricopio i file;
			Files.move(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		}while(!fine);
		
		writer_ord.close();
		Files.copy(cateneOrdinate.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		
		//Analizzo i buchi
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cateneOrdinate)));
		String line;
		
		long min = Long.MAX_VALUE;
		long max = 0;
		int numero_blocchi_totale=0;
		int numero_buchi=0;
		long ultimo_blocco_incontrato=0;
		
		while((line = reader.readLine())!=null){
			
			String[] tokens = line.split(" ");
			if(tokens[0].equals("Catena")){
				
				int length = Integer.parseInt(tokens[1]);
				
				String first = reader.readLine();
				for(int i = 0; i < length-2; i++) reader.readLine();
				String last;
				if(length > 1){
					last = reader.readLine();
				}else{
					last = first;
				}
				
				tokens = first.split(" ");
				long first_height = Long.parseLong(tokens[3]);
				
				tokens = last.split(" ");
				long last_height = Long.parseLong(tokens[3]);
				
				if(first_height>545620){
					numero_blocchi_totale+=length;
					
					if(first_height<min) min=first_height;
					if(last_height>max) max=last_height;
					
					if(ultimo_blocco_incontrato!=0){
						
						//conto i buchi
						numero_buchi+=(first_height-ultimo_blocco_incontrato-1);
						numero_blocchi_totale+=(first_height-ultimo_blocco_incontrato-1);
						
					}
					ultimo_blocco_incontrato=last_height;
				}
			}
		}
		reader.close();
		System.out.println("Ci sono in totale " + numero_blocchi_totale + " blocchi");
		System.out.println("di cui " + numero_buchi + " mancanti");
		System.out.println("Cioè il " + (numero_buchi*100/numero_blocchi_totale) + " %");
		System.out.println("Il più vecchio ha altezza " + min);
		System.out.println("Mentre il più giovane ha altezza " + max);
	}
}

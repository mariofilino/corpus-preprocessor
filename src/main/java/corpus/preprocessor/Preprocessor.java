package corpus.preprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.Trees;

public class Preprocessor {
	public static final String corpus = "corpus/1. Indonesian_Treebank.bracket";
	public static final String compoundRemovedCorpus = "corpus/2. CompoundRemoved_Indonesian_Treebank.bracket";
	public static final String editedCorpus = "corpus/3. Edited_Indonesian_Treebank.bracket";
	public static final String nullObjectCorpus = "corpus/4. nullObjectDeleted.bracket";
	public static final String incompleteSentenceDelCorpus = "corpus/5. incompleteSentenceDeletedCorpus.bracket";
	public static final String nullConjSubordDelCorpus = "corpus/6. nullConjunctionSubordinativeDeletedCorpus.bracket";
	public static final String rootAddedCorpus = "corpus/7. rootAddedCorpus.bracket";
	public static final String treebank = "corpus/8. ID-train.treebank";
		
	public static final String compoundWords = "corpus/kata_majemuk.txt";
	
	public List<Tree> trees;
	
	public Preprocessor(){
		
	}
	
	public void loadTree(String path) throws IOException{
		this.trees = new ArrayList<Tree>();
		File modelFile = new File(path);
    	
    	BufferedReader reader = new BufferedReader(new FileReader(modelFile));
    	TreeReader tr = new PennTreeReader(reader);
    	
    	Tree tree;
    	
    	while((tree = tr.readTree()) != null){
    		trees.add(tree);
    	}
    	
    	tr.close();
    	reader.close();
	}
	
	/**
	 * Mengubah kata majemuk dengan format "(kata1 kata2)" menjadi "kata1_kata2"
	 * @param path
	 * @param outputPath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void removeCompound(String path, String outputPath) throws IOException, InterruptedException{
		File corpusFile = new File(path);
		
		BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
		String line = null;
		
		File compoundRemoved = new File(outputPath);
		BufferedWriter writer = null;
		
		writer = new BufferedWriter(new FileWriter(compoundRemoved));
		
		while((line = reader.readLine()) != null){
			if(line.isEmpty()){
				continue;
			}
			
			String[] compound = null;
			
			Pattern pattern = Pattern.compile("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)");
			Matcher matcher = pattern.matcher(line);
			while(matcher.find()){
				String temp = matcher.group(0).replace("(", "").replace(")", "");
				compound = temp.split(" ");
				
				if(compound.length == 4){
					line = line.replaceFirst("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)", compound[0] + "_" + compound[1] + "_" + compound[2] + "_" + compound[3]);
				}else if(compound.length == 3){
					line = line.replaceFirst("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)", compound[0] + "_" + compound[1] + "_" + compound[2]);
				}else if(compound.length == 2){
					line = line.replaceFirst("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)", compound[0] + "_" + compound[1]);
				}
				
				
				matcher = pattern.matcher(line);
			}
			
			writer.write(line);
			writer.write("\n");
		}
		
		reader.close();
		writer.close();		
	}
	
	/**
	 * Mengubah label null object dengan -NONE-
	 * @param tree
	 * @param parent
	 */
	public void nullObjectModifier(Tree tree, Tree parent){
		if(tree.children().length == 0){
			Pattern pattern = Pattern.compile(".*\\*.*");
			Matcher matcher = pattern.matcher(tree.label().value());
			
			if(matcher.find()){
				parent.label().setValue("-NONE-");
			}
		}else{
			Pattern pattern = Pattern.compile(".*\\*.*");
			Matcher matcher = pattern.matcher(tree.label().value());
			
			if(matcher.find()){
				parent.label().setValue("-NONE-");
			}
			
			for(int i = 0; i < tree.children().length; i++){
				nullObjectModifier(tree.children()[i], tree);
			}
		}
	}
	
	/**
	 * Menghapus pohon kalimat yang bukan kalimat utuh
	 * @param outputPath
	 */
	public void deleteIncompleteSentences(){
		for(int i = 0; i < trees.size(); i++){
			if(!(trees.get(i).label().value().equals("S") || 
			     trees.get(i).label().value().equals("SBAR") || 
			     trees.get(i).label().value().equals("SBARQ") ||
			     trees.get(i).label().value().equals("SINV") ||
			     trees.get(i).label().value().equals("SQ"))){
				trees.remove(i);
			}
		}
	}
	
	/**
	 * Untuk mendapatkan daftar kata majemuk
	 * @param path
	 * @param outputPath
	 * @throws IOException 
	 */
	public void compoundFinder(String path, String outputPath) throws IOException{
		File corpusFile = new File(path);
		
		BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
		String line = null;
		
		List<String> compoundWords = new ArrayList<String>();
				
		while((line = reader.readLine()) != null){
			if(line.isEmpty()){
				continue;
			}
			
			String[] compound = null;
			
			Pattern pattern = Pattern.compile("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)");
			Matcher matcher = pattern.matcher(line);
			while(matcher.find()){
				String temp = matcher.group(0).replace("(", "").replace(")", "");
				compoundWords.add(temp);

				compound = temp.split(" ");
				
				if(compound.length == 4){
					line = line.replaceFirst("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)", compound[0] + "_" + compound[1] + "_" + compound[2] + "_" + compound[3]);
				}else if(compound.length == 3){
					line = line.replaceFirst("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)", compound[0] + "_" + compound[1] + "_" + compound[2]);
				}else if(compound.length == 2){
					line = line.replaceFirst("\\([a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]* [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*( [a-z|A-Z|0-9]*-?[a-z|A-Z|0-9]*)*?\\)", compound[0] + "_" + compound[1]);
				}
				
				matcher = pattern.matcher(line);
			}
		}
		
		reader.close();		
		
		Set<String> uniqueCompoundWords = new HashSet<String>(compoundWords);
		
		File compoundWordsFile = new File(outputPath);
		BufferedWriter writer = new BufferedWriter(new FileWriter(compoundWordsFile));
		
		for(String compoundWord : uniqueCompoundWords){
			writer.write(compoundWord);
			writer.write("\n");
		}
		
		writer.close();
	}
	
	public void nullSubordinativeConjunctionModifier(String path, String outputPath) throws IOException{
		File input = new File(path);
		BufferedReader reader = new BufferedReader(new FileReader(input));
		
		String line = null;
		
		File output = new File(outputPath);
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		
		while((line = reader.readLine()) != null){
			if(line.isEmpty()){
				continue;
			}
			
			String outputString = line.replace("SBAR 0", "SBAR");

			writer.write(outputString);
			writer.write("\n");
		}
		
		writer.close();
		reader.close();
	}
	
	public static void main(String [] args) throws IOException, InterruptedException{
		Preprocessor prep = new Preprocessor();
		
		// Untuk modifikasi kata majemuk
		{
			prep.removeCompound(corpus, compoundRemovedCorpus);
			prep.loadTree(compoundRemovedCorpus);
		}

		// Untuk merapikan format korpus
		{
			File editedCorpusFile = new File(editedCorpus);
			BufferedWriter writer = null;
			
			writer = new BufferedWriter(new FileWriter(editedCorpusFile));
			
			for(int i = 0; i < prep.trees.size(); i++){
				writer.write(prep.trees.get(i).toString());
				writer.write("\n");
			}
			
			writer.close();
		}
		
		// Untuk mengubah null object
		{
			prep.loadTree(editedCorpus);
			for(int i = 0; i < prep.trees.size(); i++){
				prep.nullObjectModifier(prep.trees.get(i), null);
			}
			
			File file = new File(nullObjectCorpus);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			for(int i = 0; i < prep.trees.size(); i++){
				writer.write(prep.trees.get(i).toString());
				writer.write("\n");
			}
			
			writer.close();
		}
		
		// Untuk menghapus pohon kalimat yang bukan kalimat utuh
		{
			prep.deleteIncompleteSentences();
			
			File file = new File(incompleteSentenceDelCorpus);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			for(int i = 0; i < prep.trees.size(); i++){
				writer.write(prep.trees.get(i).toString());
				writer.write("\n");
			}
			
			writer.close();
		}
		
		// Untuk mendapatkan daftar kata majemuk
		{
			prep.compoundFinder(corpus, compoundWords);
		}
		
		// Untuk menghapus konjungsi subordinatif yang tidak ada
		{
			prep.nullSubordinativeConjunctionModifier(incompleteSentenceDelCorpus, nullConjSubordDelCorpus);
		}
		
		// Mengubah format korpus menjadi penn String
		{
			prep.loadTree(nullConjSubordDelCorpus);
			
			File output = new File(rootAddedCorpus);
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			
			for(int i = 0; i < prep.trees.size(); i++){
				writer.write("(ROOT " + prep.trees.get(i).toString() + ")");
				writer.write("\n");
			}
			
			writer.close();
		}
	}
}

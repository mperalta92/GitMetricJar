package git;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

public class Main {
	
	public static Hashtable<String, Integer> data1, data2;

	public static void main(String[] args){
		
		Option nameO = new Option("n", "project name");
		nameO.setArgs(1);
		nameO.setRequired(true);

		Option urlO = new Option("r", "repository remote url");
		urlO.setArgs(1);
		urlO.setRequired(true);
		
		Option outO = new Option("o", "output file path");
		outO.setArgs(1);
		outO.setRequired(true);
		
		Option tempO = new Option("t", "output temp folder path");
		tempO.setArgs(1);
		tempO.setRequired(true);

		Option helpO = new Option("h", "print help");

		Options options = new Options();
		options.addOption(nameO);
		options.addOption(urlO);
		options.addOption(outO);
		options.addOption(tempO);
		options.addOption(helpO);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}
		
		String projectName= cmd.getOptionValue(nameO.getOpt());
		String remoteUrl=cmd.getOptionValue(urlO.getOpt());
		String outPath=cmd.getOptionValue(outO.getOpt());
		String tempPath= cmd.getOptionValue(tempO.getOpt());
		
		data1=new Hashtable<String,Integer>();
		data2=new Hashtable<String,Integer>();
//		System.out.println("El nombre del proyecto es:"+projectName);
//		System.out.println("La URL es:"+remoteUrl);
//		System.out.println("La ubicación del archivo de salida es:"+outPath);
		
		try {
			makeMetrics(projectName,remoteUrl,outPath, tempPath);
		} catch (Exception e1) {
			;
		}
		
		try {
			createTsvFiles(projectName ,outPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static void createTsvFiles(String projectName, String outPath) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String outBugFile = outPath.concat("\\" + projectName+"_Bugs.tsv");
		String outVersionFile = outPath.concat("\\" + projectName+"_Versions.tsv");
		OutputStream osB = new FileOutputStream(outBugFile);
		PrintWriter pwB = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(osB),StandardCharsets.UTF_8));
		OutputStream osV = new FileOutputStream(outVersionFile);
		PrintWriter pwV = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(osV),StandardCharsets.UTF_8));
		
		Enumeration<String> keys=data1.keys();
		while(keys.hasMoreElements()){
			String aux=keys.nextElement();
			pwB.println(aux+"\t"+data1.get(aux));
			
		}
		pwB.close();
		
		Enumeration<String> keys2=data2.keys();
		while(keys2.hasMoreElements()){
			String aux=keys2.nextElement();
			pwV.println(aux+"\t"+data2.get(aux));
			
		}
		pwV.close();
		
		
	}

	private static void makeMetrics(String projectName, String remoteUrl,
			String outPath, String tempPath) {
		
		String localPath = tempPath;
		Repository localRepo;
		Git git;
		try{
		localPath= localPath.concat(projectName);
		localRepo = new FileRepository(localPath + "/.git");
		git = new Git(localRepo);
		/*---------clone remote repository---------------------*/
		//preguntar si existe el repositorio en el folder del localpath, si existe hacer pull , si no clone
		 if(hasAtLeastOneReference(localRepo)){			
    	        	gitPull(git);
			 
		 }else{
    	        	gitGlone(localPath, remoteUrl);
         }
		 GitHubMetric metricOne= new BugMetric();
		 GitHubMetric metricTwo = new VersionMetric();
		
		try{
			metricOne.getData(git, localRepo, data1);
			metricTwo.getData(git, localRepo, data2);
		}
		finally{
		git.close();
		System.err.println("Fin del Analisis.\n");
		}
		 				
	} catch (Exception e) {
		// It can´t create a Local repository
		System.err.println("problema al crear repositorio local");
		e.printStackTrace();
	}
}
	
	
	 public static  boolean hasAtLeastOneReference(Repository repo) {

		    for (Ref ref : repo.getAllRefs().values()) {
		        if (ref.getObjectId() == null) {
		        	continue;
		        }
		        return true;
		    }

		    return false;
		}

	public static void gitPull(Git git) {
			// This method execute a pull from remote repository
			try {
				git.pull().call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	public static void gitGlone(String localPath, String remotePath){
			CloneCommand clone =Git.cloneRepository().setURI(remotePath);        
	        try {
				clone.setDirectory(new File(localPath)).call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	
}

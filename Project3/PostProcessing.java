import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class PostProcessing {
	public static void processFile(String path) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
	
		for(String line : lines)
			System.out.println(line);
		
		System.out.println("******************************");
		
		int total = getTotalNumberOfBigrams(lines);
		int mostCommon = getMostCommonBigram(lines);
		int count = getTop10PercentContributers(lines, total);
		
		System.out.println("******************************");
		
		System.out.println(total);
		System.out.println(mostCommon);
		System.out.println(count);
	}
	
	public static int getTotalNumberOfBigrams(List<String> lines){
		int total = 0;
		for(String line : lines){
			total += Integer.parseInt(line.split("\t")[1]);
		}
		return total;
	}
	
	public static int getMostCommonBigram(List<String> lines){
		int max = -1;
		for(String line : lines){
			int count = Integer.parseInt(line.split("\t")[1]);
			max = max > count ? max : count;
		}
		return max;
	}
	
	public static int getTop10PercentContributers(List<String> lines, int total){
		int count = 0, current = 0;
		String[] strLines = lines.toArray(new String[0]);
	    Arrays.sort(strLines, new Comparator<String>() {
	      public int compare(String s1, String s2) {
	        return (Integer.parseInt(s2.split("\t")[1]) - Integer.parseInt(s1.split("\t")[1]));
	      }
	    });
	    
	    for(String line : strLines){
	    	System.out.println(line);
	    }
	    
	    for(String line : strLines){
	    	current += Integer.parseInt(line.split("\t")[1]);
	    	if(current >= total * 0.1){
	    		count += 1;
	    		break;
	    	}
	    	count += 1;
	    }
	    return count;
	}
	
	
	public static void main(String[] args) throws IOException {
		String path = args[0];
		if(path == null) throw new IOException("No file provided");				
		PostProcessing.processFile(path); 
	}
}
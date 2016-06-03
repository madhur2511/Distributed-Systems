import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class PostProcessing {
	private static final String separator = "\t";

	public static void processFile(String path) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);

		int total = getTotalNumberOfBigrams(lines);
		int mostCommon = getMostCommonBigram(lines);
		int count = getTop10PercentContributers(lines, total);

		System.out.println("******************************");

		System.out.println("The total number of bigrams: " + total);
		System.out.println("The most common bigram: " + mostCommon);
		System.out.println("The number of bigrams required to add up to 10% of all bigrams: " + count);

		System.out.println("******************************");
	}

	public static int getTotalNumberOfBigrams(List<String> lines){
		int total = 0;
		for(String line : lines){
			total += Integer.parseInt(line.split(separator)[1]);
		}
		return total;
	}

	public static int getMostCommonBigram(List<String> lines){
		int max = -1;
		for(String line : lines){
			int count = Integer.parseInt(line.split(separator)[1]);
			max = max > count ? max : count;
		}
		return max;
	}

	public static int getTop10PercentContributers(List<String> lines, int total){
		int count = 0, current = 0;
		String[] strLines = lines.toArray(new String[0]);
	    Arrays.sort(strLines, new Comparator<String>() {
	      public int compare(String s1, String s2) {
	        return (Integer.parseInt(s2.split(separator)[1]) - Integer.parseInt(s1.split(separator)[1]));
	      }
	    });
	    for(String line : strLines){
	    	current += Integer.parseInt(line.split(separator)[1]);
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

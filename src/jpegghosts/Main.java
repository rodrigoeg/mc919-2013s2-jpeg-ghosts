package jpegghosts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    // http://docs.opencv.org/java/
	static { System.loadLibrary("opencv_java246"); }
	
    public static void main(String[] args) {
    	System.out.println("Starting");
    	//generateDataSet();
    	
    	runMethod();
		
		
    	
    }

	private static void runMethod() {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		File folder = new File("source_images");
		String folderDestination = "result_images"; 
    	File[] files = folder.listFiles(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	    	return name.toLowerCase().endsWith(".jpg") ||
    	        		name.toLowerCase().endsWith(".jpeg") ||
    	        		name.toLowerCase().endsWith(".tif");
    	    }
    	});  
    	
		Arrays.sort(files);
		int blockSize = 8;
		JpegGhosts jpegGhosts = new JpegGhosts();
		
		for (final File fileEntry : files) {
			if (!fileEntry.isDirectory()) {
				//jpegGhosts.simplerVersion(fileEntry, folderDestination, executor);
				jpegGhosts.fullVersion(fileEntry, blockSize, folderDestination, executor);
			}
		}
		
		executor.shutdown();
	}
    
    
	private static void generateDataSet() {
		DataSetGenerate dataSet = new DataSetGenerate("generate_dataset_original_images", "source_images");
    	dataSet.generate();
	}
    
}
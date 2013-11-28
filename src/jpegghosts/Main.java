package jpegghosts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class Main {

    // http://docs.opencv.org/java/
	static { System.loadLibrary("opencv_java246"); }
	
    public static void main(String[] args) {
    	//generateDataSet();
    	
    	runMethod();
		
		System.out.println("Finished");
    	
    }

	private static void runMethod() {
		File folder = new File("source_images");
		String folderDestination = "result_images"; 
    	File[] files = folder.listFiles(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	    	return name.toLowerCase().endsWith(".jpg")||
    	        		name.toLowerCase().endsWith(".jpeg") ||
    	        		name.toLowerCase().endsWith(".tif");
    	    }
    	});  
    	
		Arrays.sort(files);
		int blockSize = 8;
		JpegGhosts jpegGhosts = new JpegGhosts();
		
		for (final File fileEntry : files) {
			if (!fileEntry.isDirectory()) {
				//jpegGhosts.simplerVersion(fileEntry, folderDestination);
				jpegGhosts.fullVersion(fileEntry, blockSize, folderDestination);
			}
		}
	}
    
    
	private static void generateDataSet() {
		DataSetGenerate dataSet = new DataSetGenerate("generate_dataset_original_images", "source_images");
    	dataSet.generate();
	}
    
}
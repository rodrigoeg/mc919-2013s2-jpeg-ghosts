package jpegghosts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class Main {

    // http://docs.opencv.org/java/
	static { System.loadLibrary("opencv_java246"); }
	
    public static void main(String[] args) {
    	generateDataSet();
    	
    	runMethod();
		
		System.out.println("Finished");
    	
    }

	private static void runMethod() {
		File folder = new File("tampered_images");
    	File[] files = folder.listFiles(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	        return name.toLowerCase().endsWith(".jpeg");
    	    }
    	});  
    	
		Arrays.sort(files);
		int blockSize = 8;
		JpegGhosts jpegGhosts = new JpegGhosts();
		
		for (final File fileEntry : files) {
			if (!fileEntry.isDirectory()) {
				//jpegGhosts.simplerVersion(fileEntry);
				jpegGhosts.fullVersion(fileEntry, blockSize);
			}
		}
	}
    
    
	private static void generateDataSet() {
		DataSetGenerate dataSet = new DataSetGenerate("original_images", "tampered_images");
    	dataSet.generate();
	}
    
}
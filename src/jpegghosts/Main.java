package jpegghosts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    // http://docs.opencv.org/java/
	static { System.loadLibrary("opencv_java246"); }
	
	private static double TRESHOlD = 0.76;
	
    public static void main(String[] args) throws FileNotFoundException {
    	System.out.println("Starting");
    	//generateDataSet();
    	
    	//runMethod();
    	
    	identifyTamper();
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
	
	private static void identifyTamper() throws FileNotFoundException {
		File folder = new File("result_images");
		File[] imagesNames = folder.listFiles();
    	
		JpegGhosts jpegGhosts = new JpegGhosts();
		
		Arrays.sort(imagesNames);
		for (final File imageFolder : imagesNames) {
			if (imageFolder.isDirectory()) {
				
				File[] files = imageFolder.listFiles(new FilenameFilter() {
		    	    public boolean accept(File dir, String name) {
		    	    	return name.toLowerCase().endsWith(".png");
		    	    }
		    	});
				
				boolean tampered = false;
				Scanner scanner = new Scanner(new File("source_images/" + imageFolder.getName() + ".txt"));
				int width = scanner.nextInt();
		    	int height = scanner.nextInt();
		    	int x = scanner.nextInt();
		    	int y = scanner.nextInt();
		    	scanner.close();

		    	Arrays.sort(files);
				for (final File fileEntry : files) {
					if (!fileEntry.isDirectory()) {
						double difference = jpegGhosts.detectTamper(fileEntry, width, height, x, y);
						System.out.println(fileEntry.getName());
						System.out.println(difference);
						if (difference > TRESHOlD) {
							tampered = true;
						}
					}
				}
				
				if (tampered) {
					System.out.println(imageFolder.getName() + " is modified");
				}
				else {
					System.out.println(imageFolder.getName() + " is not modified");
				}
				
			}
		}
	}
    
}
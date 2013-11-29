package jpegghosts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

public class DataSetGenerate {
	private String inputFolder;
	private String outputFolder;
	final private float MIN_HEIGHT = 0.10f;
	final private float MIN_WIDTH = 0.10f;
	final private float MAX_HEIGHT = 0.60f;
	final private float MAX_WIDTH = 0.60f;

	public DataSetGenerate(String folderPath, String outputFolderPath) {
		this.inputFolder = folderPath;
		this.outputFolder = outputFolderPath;
	}

	public void generate() {
		File folder = new File(inputFolder);
		File[] files = folder.listFiles(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	        return name.toLowerCase().endsWith(".jpg")||
    	        		name.toLowerCase().endsWith(".jpeg") ||
    	        		name.toLowerCase().endsWith(".tif");
    	    }
    	});  
		Arrays.sort(files);
		int count = 1;
		
		for (final File fileEntry : files) {
			if (!fileEntry.isDirectory()) {
				System.out.println("File " + count + " of " + files.length);
				System.out.println("Creating tampered image from " + fileEntry.getPath());

				// Read an image
				Mat image = Highgui.imread(fileEntry.getPath());
				
				// Generate a tampered image
				Mat tamperedRegion = tamperImage(image);

				// Save tampered image
				saveImage(image, fileEntry.getName() + ".jpg");
				
				// Save an image reference to point out where the source image was modified
				saveImage(tamperedRegion, fileEntry.getName() + ".png");
				
				count++;
			}
		}
	}

	private Mat tamperImage(Mat image) {
		// Calculate the position of the region
		Rect tamperPosition = generateRectToTamper(image);	
		Mat imageRegion = new Mat(image, tamperPosition);
		
		
		// Generate rectangle with the compressed image region
		Mat imageReference = generateBlackAndWhite(image, imageRegion, tamperPosition); 

		// Compress a region o the image
		compressImageRegion(image, imageRegion, tamperPosition);	
    	
    	return imageReference;
	}

	private Rect generateRectToTamper(Mat image) {
//		Random random = new Random();
//		int maxWidth = (int) (image.width() * MAX_WIDTH);
//		int maxHeight = (int) (image.height() * MAX_HEIGHT);
//		int minWidth = (int) (image.width() * MIN_WIDTH);
//		int minHeight = (int) (image.height() * MIN_HEIGHT);
		
//		int ghostWidth = minWidth + random.nextInt(maxWidth - minWidth + 1);
//		int ghostHeight = minHeight + random.nextInt(maxHeight - minHeight + 1);
		int ghostWidth = 200;
		int ghostHeight = 200;

		Size ghostSize = new Size(ghostWidth, ghostHeight);
		
//		int x = random.nextInt(image.width() - (int)ghostSize.width);
//		int y = random.nextInt(image.height() - (int)ghostSize.height);
		int x = (image.width() / 2) -  ghostWidth / 2;
		int y = (image.height() / 2) -  ghostHeight / 2;
		
		// Get the image region
		Rect tamper = new Rect(x, y, (int)ghostSize.width, (int)ghostSize.height);
		return tamper;
	}

	private void compressImageRegion(Mat image, Mat imageRegion, Rect tamperPosition) {
		MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 30);

		MatOfByte byteImage = new MatOfByte(); 
		Highgui.imencode(".jpg", imageRegion, byteImage, params);
		Mat jpegImage = Highgui.imdecode(byteImage, Highgui.CV_LOAD_IMAGE_COLOR);
		
    	Mat imageSubmat = image.submat(tamperPosition);
    	jpegImage.copyTo(imageSubmat);
	}
	
	
	private Mat generateBlackAndWhite(Mat image, Mat imageRegion, Rect tamperPosition) {
		// black region
		Mat background = new Mat(image.rows(), image.cols(), CvType.CV_8UC1, new Scalar(0));
		
		// blank region
		Mat foreground = new Mat(imageRegion.rows(), imageRegion.cols(), CvType.CV_8UC1, new Scalar(255));
		
		// sub image region
		Mat bSubmat = background.submat(tamperPosition);
    	foreground.copyTo(bSubmat);
    	
    	return background;
	}

	private void saveImage(Mat image, String name) {
		MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 90);
		Highgui.imwrite(outputFolder + "/" + name, image, params);
	}
}

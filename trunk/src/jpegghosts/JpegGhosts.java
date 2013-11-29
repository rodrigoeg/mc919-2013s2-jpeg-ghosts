package jpegghosts;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class JpegGhosts {
	public void simplerVersion(final File fileEntry, String folderDestination, ExecutorService executor) {
    	// Read an image
		Mat image = Highgui.imread(fileEntry.getPath());

		for (int quality = 30; quality <= 100; quality = quality + 1) {
			generateImageForSimpleVersion(image, quality, fileEntry, folderDestination, executor);
		}
    }
    
    public void fullVersion(final File fileEntry, int blockSize, String folderDestination, ExecutorService executor) {
		// Read an image
		Mat image = Highgui.imread(fileEntry.getPath());
		
//		generateImageForFullVersion(image, 65, fileEntry, blockSize, folderDestination, executor);
		for (int quality = 30; quality <= 100; quality = quality + 1) {
			generateImageForFullVersion(image, quality, fileEntry, blockSize, folderDestination, executor);
		}
    }
    
    private void generateImageForSimpleVersion(final Mat image, final int quality, final File fileEntry, final String folderDestination, ExecutorService executor) {
    	Thread thread = new Thread() {
	        public void run() {
			    
	        	// variar a qualidade
				MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, quality);
				MatOfByte byteImage = new MatOfByte(); 
				Highgui.imencode(".jpg", image, byteImage, params);
				Mat imageDoubleCompressed = Highgui.imdecode(byteImage, Highgui.CV_LOAD_IMAGE_COLOR);

			
				Mat differenceImage = image.clone();
				
				for (int h = 0; h < image.height(); h++) {
					for (int w = 0; w < image.width(); w++) {
						double sum = 0;
						
				        double[] pixelImage = image.get(h, w);
				        double[] pixelImageDoubleCompressed = imageDoubleCompressed.get(h, w);
				        
				        for (int channel = 0; channel < image.channels(); channel++) {
				        	double difference = pixelImage[channel] - pixelImageDoubleCompressed[channel];
				        	sum += difference * difference;
				        }
						
						
						sum /= 3;
				        					
						double[] data = new double[3];
						data[0] = data[1] = data[2] = sum;
						
						differenceImage.put(h, w, data);
					}
				}
						
				System.out.println(fileEntry.getName() + '-'+ quality + ".png");
				String folderName = fileEntry.getName().replaceFirst("[.][^.]+$", "").replaceFirst("[.][^.]+$", "");
				File dir = new File(folderDestination + "/" + folderName);
				dir.mkdir();
				Highgui.imwrite(folderDestination + "/" + folderName + "/" + fileEntry.getName() + '-'+ quality + ".png", differenceImage);
	        }
	    };
	    executor.execute(thread);
    }
    
    private void generateImageForFullVersion(final Mat image, final int quality, final File fileEntry, final int blockSize, final String folderDestination, ExecutorService executor) {
    	Thread thread = new Thread() {
	        public void run() {
			    
				// variar a qualidade
				MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, quality);
				MatOfByte byteImage = new MatOfByte(); 
				Highgui.imencode(".jpg", image, byteImage, params);
				Mat imageDoubleCompressed = Highgui.imdecode(byteImage, Highgui.CV_LOAD_IMAGE_COLOR);

			
				Mat differenceImage = image.clone();
				
				
				double min = Double.POSITIVE_INFINITY;
				double max = 0;
				for (int h = 0; h < image.height(); h++) {
					for (int w = 0; w < image.width(); w++) {
						double sum = 0;
						for (int bX = 0; bX < blockSize; bX++) {
							for (int bY = 0; bY < blockSize; bY++) {
								if ((h + bY < image.height()) && (w + bX < image.width())) {
							        double[] pixelImage = image.get(h + bY, w + bX);
							        double[] pixelImageDoubleCompressed = imageDoubleCompressed.get(h + bY, w + bX);
							        
							        for (int channel = 0; channel < image.channels(); channel++) {
							        	double difference = pixelImage[channel] - pixelImageDoubleCompressed[channel];
							        	sum += difference * difference;
							        }
								}
							}
				        }
						
						sum /= (blockSize * blockSize);
						sum /= 3;
						
						if (sum < min) {
							min = sum;
						}
						
						if (sum > max) {
							max = sum;
						}
						
						double[] data = new double[3];
						data[0] = data[1] = data[2] = sum;
						
						differenceImage.put(h, w, data);
					}
				}
				
				for (int h = 0; h < image.height(); h++) {
					for (int w = 0; w < image.width(); w++) {
						double[] pixelImage = differenceImage.get(h, w);
						
						pixelImage[0] = (pixelImage[0] - min) / (max - min);
						
						pixelImage[0] *= 255;
						
						pixelImage[1] = pixelImage[2] = pixelImage[0]; 

						differenceImage.put(h, w, pixelImage);					        
					}
				}
			
				System.out.println(fileEntry.getName() + '-'+ quality + ".png");
				String folderName = fileEntry.getName().replaceFirst("[.][^.]+$", "").replaceFirst("[.][^.]+$", "");
				File dir = new File(folderDestination + "/" + folderName);
				dir.mkdir();
				Highgui.imwrite(folderDestination + "/" + folderName + "/" + fileEntry.getName() + '-'+ quality + ".png", differenceImage);
	        }
	    };
	    executor.execute(thread);
    }

    
    public  double detectTamper(final File fileEntry, int width, int height, int x, int y) {
    	Mat differenceImage = Highgui.imread(fileEntry.getPath());
    	
    	Mat histBackground = outsideImageHist(differenceImage, width, height, x, y);
    	Mat histRegion = insideImageHist(differenceImage, width, height, x, y);
    	
//    	System.out.println(histBackground.dump());
//    	System.out.println(histRegion.dump());
    	
    	double max = 0;
    	for (int h = 0; h < histBackground.height(); h++) {
			for (int w = 0; w < histBackground.width(); w++) {
				double[] pixelHistBackground = histBackground.get(h, w);
				double[] pixelHistRegion = histRegion.get(h, w);
				
				// considerar que os pixels mais escuros tem mais peso na hora do calculo da diferenca
				// ignorar pontos com 0 no histograma
				if (pixelHistRegion[0] > 0 && pixelHistBackground[0] > 0) {
					double difference = (pixelHistRegion[0] - pixelHistBackground[0]);
					if (difference > max) {
						max = difference;
					}
				}
			}
    	}
    	
    	return max/256;
    }
    
    private Mat insideImageHist(Mat differenceImage, int width, int height, int x, int y) {
    	List<Mat> images = Arrays.asList(differenceImage);
    	
    	MatOfInt histSize = new MatOfInt(256);

        final MatOfFloat histRange = new MatOfFloat(0f, 256f);

        boolean accumulate = false;
        
        Mat b_hist = new  Mat();
        
        int ghostWidth = width;
		int ghostHeight = height;
        
        Size ghostSize = new Size(ghostWidth, ghostHeight);

        Mat ones = Mat.ones(ghostSize, CvType.CV_8U); // all 1
        Mat mask = Mat.zeros(differenceImage.size(), CvType.CV_8U); // all 0  
		
		
		// Get the image region
		Rect tamper = new Rect(x, y, (int)ghostSize.width, (int)ghostSize.height);
        
        Mat imageSubmat = mask.submat(tamper);
        ones.copyTo(imageSubmat);
        
        Imgproc.calcHist(images, new MatOfInt(0), mask, b_hist, histSize, histRange, accumulate);
        
        Core.normalize(b_hist, b_hist, 0, 256, Core.NORM_MINMAX);
        
        return b_hist;
	}

	public Mat outsideImageHist(Mat differenceImage, int width, int height, int x, int y) {
    	List<Mat> images = Arrays.asList(differenceImage);
    	
    	MatOfInt histSize = new MatOfInt(256);

        final MatOfFloat histRange = new MatOfFloat(0f, 256f);

        boolean accumulate = false;

        int ghostWidth = width;
		int ghostHeight = height;
		
        Mat b_hist = new  Mat();

        Size ghostSize = new Size(ghostWidth, ghostHeight);
        
        Mat zeros = Mat.zeros(ghostSize, CvType.CV_8U); // all 0
        Mat mask = Mat.ones(differenceImage.size(), CvType.CV_8U); // all 1
		
		// Get the image region
		Rect tamper = new Rect(x, y, (int)ghostSize.width, (int)ghostSize.height);
        
        Mat imageSubmat = mask.submat(tamper);
        zeros.copyTo(imageSubmat);
        
        Imgproc.calcHist(images, new MatOfInt(0), mask, b_hist, histSize, histRange, accumulate);
        
        Core.normalize(b_hist, b_hist, 0, 256, Core.NORM_MINMAX);

        return b_hist;
    }
}

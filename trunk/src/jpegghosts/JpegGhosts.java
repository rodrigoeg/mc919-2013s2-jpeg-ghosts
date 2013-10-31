package jpegghosts;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;

public class JpegGhosts {
	public void simplerVersion(final File fileEntry) {
    	// Read an image
		Mat image = Highgui.imread(fileEntry.getPath());

		
		for (int quality = 30; quality <= 100; quality = quality + 1) {
		
			// variar a qualidade
			MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, quality, 0);
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
			File dir = new File("tampered_images/" + folderName);
			dir.mkdir();
			Highgui.imwrite("tampered_images/" + folderName + "/" + fileEntry.getName() + '-'+ quality + ".png", differenceImage);
		}
    }
    
    public void fullVersion(final File fileEntry, int blockSize) {
		// Read an image
		Mat image = Highgui.imread(fileEntry.getPath());
		
		for (int quality = 30; quality <= 100; quality = quality + 1) {
		
			// variar a qualidade
			MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, quality, 0);
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
			File dir = new File("tampered_images/" + folderName);
			dir.mkdir();
			Highgui.imwrite("tampered_images/" + folderName + "/" + fileEntry.getName() + '-'+ quality + ".png", differenceImage);
		}
    }

}

package jpegghosts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;

public class Main {

    // http://docs.opencv.org/java/
	static { System.loadLibrary("opencv_java246"); }
	
    public static void main(String[] args) {
    	generateDataSet();
    	
    	File folder = new File("tampered_images");
    	File[] files = folder.listFiles(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	        return name.toLowerCase().endsWith(".jpeg");
    	    }
    	});  
    	
		Arrays.sort(files);
		int blockSize = 8;
		
		for (final File fileEntry : files) {
			if (!fileEntry.isDirectory()) {
				// Read an image
				Mat image = Highgui.imread(fileEntry.getPath());

				// separar em RGB
//				Mat rImage = new Mat(image.size(), Highgui.CV_LOAD_IMAGE_COLOR);
//				Mat gImage = new Mat(image.size(), Highgui.CV_LOAD_IMAGE_COLOR);
//				Mat bImage = new Mat(image.size(), Highgui.CV_LOAD_IMAGE_COLOR);
//				
//				List<Mat> mvImage = new ArrayList<Mat>();
//				mvImage.add(rImage);
//				mvImage.add(gImage);
//				mvImage.add(bImage);
//				
//				Core.split(image, mvImage);
				
				for (int quality = 30; quality <= 100; quality = quality + 5) {
				
					// variar a qualidade
					MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, quality, 0);
					MatOfByte byteImage = new MatOfByte(); 
					Highgui.imencode(".jpg", image, byteImage, params);
					Mat imageDoubleCompressed = Highgui.imdecode(byteImage, Highgui.CV_LOAD_IMAGE_COLOR);

				
				
				// separar em RGB
//				Mat rImageDoubleCompressed = new Mat(doubleCompressedImage.size(), Highgui.CV_LOAD_IMAGE_COLOR);
//				Mat gImageDoubleCompressed = new Mat(doubleCompressedImage.size(), Highgui.CV_LOAD_IMAGE_COLOR);
//				Mat bImageDoubleCompressed = new Mat(doubleCompressedImage.size(), Highgui.CV_LOAD_IMAGE_COLOR);
//				
//				List<Mat> mvImageDoubleCompressed = new ArrayList<Mat>();
//				mvImage.add(rImageDoubleCompressed);
//				mvImage.add(gImageDoubleCompressed);
//				mvImage.add(bImageDoubleCompressed);
//				
//				Core.split(doubleCompressedImage, mvImageDoubleCompressed);
				
				/*Mat New = new Mat(image.size(), Highgui.CV_LOAD_IMAGE_COLOR);
				
				// fazer as contas....
				int b = 8;
				for (int h = 0; h < image.height() - b; h++) {
					for (int w = 0; w < image.width() - b; w++) {
						double[] test = image.get(h + b, w + b);
						double[] test2 = doubleCompressedImage.get(h + b,  w + b);
						double sum = 0;
						for (int i = 0 ; i < 3; i++) {

							for (int j = 0 ; j < b; j++) {
								for (int k = 0 ; k < b; k++) {
									sum = test[i] - test2[i];
								}	
							}
							sum = sum * sum;
							
							sum = sum / (b * b);
						}
						
						sum = sum / 3;
						New.put(h+ b, w+b, sum);
					}
				}
*/
/*				double[] data3 = doubleCompressedImage.get(287, 162);
				System.out.println(data3[0] +" " + data3[1] + " " + data3[2]);
				
				Mat NewImage = image.clone();
				for (int i = 0; i < image.height(); i++) {
					for (int j = 0; j < image.width(); j++) {
				        double[] data = image.get(i, j);
				        System.out.println(data.length);
				        double[] data2 = doubleCompressedImage.get(i, j);
				        double result = 0;
				        for (int k = 0; k < 3; k++) {
					        result += Math.pow(data[k] - data2[k], 2);
				        }
				        result /= 3;
				        data[0] = data[1] = data[2] = result;
				        NewImage.put(i, j, data);
					}
			    }
				
				Highgui.imwrite("tampered_images/test"+r+".png", NewImage);
				
				
				count++;*/
				
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
							
							pixelImage[0] = pixelImage[0] - min / (max - min);
							
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
		
		System.out.println("Finished");
    	
    }

	private static void generateDataSet() {
		DataSetGenerate dataSet = new DataSetGenerate("original_images", "tampered_images");
    	dataSet.generate();
	}
    
}
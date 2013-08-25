package jpegghosts;

public class Main {

    // http://docs.opencv.org/java/
	static { System.loadLibrary("opencv_java246"); }
	
    public static void main(String[] args) {
    	generateDataSet();
    }

	private static void generateDataSet() {
		DataSetGenerate dataSet = new DataSetGenerate("original_images", "tampered_images");
    	dataSet.generate();
	}
    
}
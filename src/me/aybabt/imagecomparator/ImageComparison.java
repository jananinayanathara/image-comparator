package me.aybabt.imagecomparator;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import me.aybabt.image.compare.NaiveCompare;
import me.aybabt.image.compare.NaiveCompare.Image;

/**
 * Compare two given images and return a score representing their similarity.
 * @author Antoine Grondin <a href="mailto:antoinegrondin@gmail.com">
 * 	antoinegrondin@gmail.com</a>
 *
 */
public class ImageComparison {

	////////////////////////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////////////////////////

	// Constants //
	// - Debug
	private static final boolean D = true;
	// - Config
	private static final String CSV_SEPARATOR = ", ";

	// Members //
	private File mFirstFile = null;
	private File mSecondFile = null;
	private BufferedImage mFirstImg = null;
	private BufferedImage mSecondImg = null;

	////////////////////////////////////////////////////////////////////////////
	// Run me
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Start the program.  Will open a file chooser dialog if no arguments are
	 * given.
	 * @param args "-c" or "--calibrate" will run the comparison against a
	 * predetermined set of images and record the result in the current folder,
	 * in a file named "calibration.csv".
	 */
	public static void main(String[] args) {

		if(args.length == 1){
			if(args[0].equals("-c") || args[0].equals("--calibrate"));
			runCalibration();
		} else {
			new ImageComparison();
		}


	}

	////////////////////////////////////////////////////////////////////////////
	// Constructors
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Use a FileChooser
	 */
	public ImageComparison() {

		System.out.println("NaiveComparison of two images.  " +
				"Lower score the better");

		getFile();
		getImage();

		System.out.println("Score is "+ getScore());
	}

	/**
	 * Use given filename and run calibration
	 * @param fileOne is an image filename
	 * @param fileTwo is an image filename
	 */
	public ImageComparison(String fileOne, String fileTwo){
		URL urlOne = getClass().getResource(fileOne);
		mFirstFile = new File( urlOne.getPath() );
		URL urlTwo = getClass().getResource(fileTwo);
		mSecondFile = new File( urlTwo.getPath() );

		getImage();
	}


	////////////////////////////////////////////////////////////////////////////
	// Public Interface
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Get the score associated with the two images given to this object.
	 * @return a score from 0.0 to infinity, 0 being the best similarity.
	 */
	public float getScore(){

		Image firstImage = new ImageFromBufferedImage(mFirstImg);
		Image secondImage = new ImageFromBufferedImage(mSecondImg);

		NaiveCompare comparator = new NaiveCompare(firstImage, secondImage);

		return comparator.getScore();
	}

	////////////////////////////////////////////////////////////////////////////
	// Logic
	////////////////////////////////////////////////////////////////////////////

	private static void exit(int code){
		System.out.println("EXIT - Leaving with code " + code);
		System.exit(code);
	}

	private void getFile(){

		JFrame frame = new JFrame("Image Comparison");
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"JPG & GIF Images", "jpg", "gif");

		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(frame);

		if(returnVal != JFileChooser.APPROVE_OPTION) {
			exit(107);

		}
		mFirstFile = chooser.getSelectedFile();

		returnVal = chooser.showOpenDialog(frame);

		if(returnVal != JFileChooser.APPROVE_OPTION) {
			exit(115);
		}

		mSecondFile = chooser.getSelectedFile();
	}

	private void getImage(){

		try {
			mFirstImg = ImageIO.read(mFirstFile);
		} catch (IOException e) {
			if(D) e.printStackTrace();
			exit(127);
		}

		try {
			mSecondImg = ImageIO.read(mSecondFile);
		} catch (IOException e) {
			if(D) e.printStackTrace();
			exit(134);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Calibration
	////////////////////////////////////////////////////////////////////////////

	private static void runCalibration(){

		Vector<String> calibration = new Vector<String>();
		// String[] images = getListOfImages();
		String[] images = {
				"greyblue-3508x4960.gif",
				"lightblue-3508x4960.gif",
				"blue-640x480.jpg",
				"real-dog-01.jpg",
				"real-fan-01.jpg",
				"real-fan-03.jpg",
				"real-jungle-02.jpg",
				"real-screen-01.jpg",
				"real-screen-03.jpg",
				"green-640x480.jpg",
				"real-dog-02.jpg",
				"real-fan-02.jpg",
				"real-jungle-01.jpg",
				"real-jungle-03.jpg",
				"real-screen-02.jpg",
				"red-640x480.jpg"
		};

		for(int i = 0; i < images.length; i++){
			for(int j = 0; j < images.length; j++){
				doComparison(images[i], images[j], calibration);
			}
		}

		saveToCSVFile(calibration);

		System.out.println("Finished.");
	}

	private static void doComparison(String fileOne, 
			String fileTwo, 
			Vector<String> output){
		System.out.println("Comparing: \""+ 
				fileOne + 
				"\" <-> \"" + 
				fileTwo + 
				"\"");

		ImageComparison comp = new ImageComparison(fileOne, fileTwo);
		float score = comp.getScore();
		System.out.println("Scored " + score);
		output.add(fileOne + CSV_SEPARATOR + fileTwo + CSV_SEPARATOR + score);
	}

	private static void saveToCSVFile(Vector<String> content){
		// Save to a file
		File outFile;
		outFile = new File("calibration.csv");
		if( outFile.exists() ){
			outFile.delete();
			
		} 
		
		try {

			outFile.createNewFile();

		} catch ( IOException e ) {
			if(D) e.printStackTrace();
			exit(217);
		}

		try {
			FileWriter fileStream = new FileWriter( outFile );

			BufferedWriter bufWriter = new BufferedWriter(fileStream);

			for(String aResult : content){
				bufWriter.write(aResult + '\n');
			}

			bufWriter.close();
		} catch (IOException e) {
			if(D) e.printStackTrace();
			exit(232);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Inner classes
	////////////////////////////////////////////////////////////////////////////

	private class ImageFromBufferedImage implements Image{

		private BufferedImage image = null;

		public ImageFromBufferedImage(BufferedImage image){
			this.image = image;
		}

		@Override
		public byte getRed(short x, short y) {

			int rgb = image.getRGB(x, y);
			byte red = (byte) ( (rgb >> 16)  & 0xFF );
			return red;
		}

		@Override
		public byte getGreen(short x, short y) {
			int rgb = image.getRGB(x, y);
			byte green = (byte) ( (rgb >> 8) & 0xFF );
			return green;
		}

		@Override
		public byte getBlue(short x, short y) {
			int rgb = image.getRGB(x, y);
			byte blue = (byte) ( rgb & 0xFF );
			return blue;
		}

		@Override
		public short getWidth() {
			return (short) image.getWidth();
		}

		@Override
		public short getHeight() {
			return (short) image.getHeight();
		}

		@Override
		public Image getSubImage(short x, short dx, short y, short dy) {
			// Not implemented
			return null;
		}

	}

}

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/*
Source: https://www.youtube.com/watch?v=e4xphUB0Ptg
*/

public class ExtractLSB {
	public static void Extract(String newImageFileString, SecretKey secretKey, Cipher cipher) throws Exception {
		File newImageFile = new File(newImageFileString);
		BufferedImage image;
		TextDecryptor textDecryptor = new TextDecryptor();
		try {
			image = ImageIO.read(newImageFile);
			Pixel[] pixels = GetPixelArray(image);
			String message = ExtractMessageFromPixels(pixels);
			System.out.println("Message: "+textDecryptor.decrypt(message, secretKey, cipher));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Pixel[] GetPixelArray(BufferedImage bufferedImage){
		int height = bufferedImage.getHeight();
		int width = bufferedImage.getWidth();
		Pixel[] pixels = new Pixel[height * width];
		
		int count = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {				
				Color colorToAdd = new Color(bufferedImage.getRGB(x, y));
				pixels[count] = new Pixel(x, y, colorToAdd);
				count++;
			}
		}
		return pixels;
	}
	
	private static String ExtractMessageFromPixels(Pixel[] pixels) { //all pixels. Select all pixels with a message until we the pixel that means end
		boolean completed = false;
		int pixelArrayIndex = 0; 
		StringBuilder messageBuilder = new StringBuilder("");
		while(!completed) { //untill we get all characters
			Pixel[] pixelsToRead = new Pixel[3]; // three pixels
			for(int i = 0; i < 3; i++) {
				pixelsToRead[i] = pixels[pixelArrayIndex];
				pixelArrayIndex++;
			}
			messageBuilder.append(ConvertPixelsToCharacter(pixelsToRead));
			if(IsEndOfMessage(pixelsToRead[2])) {
				completed = true;
			}
		}
		return messageBuilder.toString();
	}
	
	private static char ConvertPixelsToCharacter(Pixel[] pixelsToRead) { // 3 pixels with message
		ArrayList<String> binaryValues = new ArrayList<String>();
		for(int i = 0; i < pixelsToRead.length; i++) {
			String[] currentBinary = TurnPixelIntegersToBinary(pixelsToRead[i]); // every pixel is turned is array of R G and B value

			binaryValues.add(currentBinary[0]); //add R to binaryValues
			binaryValues.add(currentBinary[1]); //add G to binaryValues
			binaryValues.add(currentBinary[2]); //add B to binaryValues ...until all 3 pixels are passed
		}
		return ConvertBinaryValuesToCharacter(binaryValues);
	}
	
	private static String[] TurnPixelIntegersToBinary(Pixel pixel) {
		String[] values = new String[3];
		values[0] = Integer.toBinaryString(pixel.getColor().getRed());
		values[1] = Integer.toBinaryString(pixel.getColor().getGreen());
		values[2] = Integer.toBinaryString(pixel.getColor().getBlue());
		return values;
	}
	
	private static boolean IsEndOfMessage(Pixel pixel) {
		if(TurnPixelIntegersToBinary(pixel)[2].endsWith("1")) {
			return false;
		}
		return true;
	}
	
	private static char ConvertBinaryValuesToCharacter(ArrayList<String> binaryValues) { //array of all R G Bs of 3 pixels
		StringBuilder endBinary = new StringBuilder("");
		for(int i = 0; i < binaryValues.size()-1; i++) {
			endBinary.append(binaryValues.get(i).charAt(binaryValues.get(i).length()-1)); // get the last bit of every R G and B
		}
		String endBinaryString = endBinary.toString();
		String noZeros = RemovePaddedZeros(endBinaryString); //remove zeros
		int ascii = Integer.parseInt(noZeros, 2);
		return (char) ascii;
	}
	
	private static String RemovePaddedZeros(String endBinary) {
		StringBuilder builder = new StringBuilder(endBinary);
		int paddedZeros = 0;
		for(int i = 0; i < builder.length(); i++) {
			if(builder.charAt(i) == '0') {
				paddedZeros++;
			}
			else {
				break;
			}
		}
		for(int i = 0 ; i < paddedZeros; i++) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
}

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/*
Source: https://www.youtube.com/watch?v=e4xphUB0Ptg
*/

public class EmbedLSB {
	public static void Embed(File imageFile, String message, String newImageFileString) {
		File newImageFile = new File(newImageFileString);
		
		BufferedImage image;
		try {
			image = ImageIO.read(imageFile);
			BufferedImage bufferedImage = GetImage(image);
			Pixel[] pixels = GetPixelArray(bufferedImage);
			String[] messageInBinary = ConvertMessageToBinary(message);
			EncodeMessageBinaryInPixels(pixels, messageInBinary);
			ReplacePixelsInNewBufferedImage(pixels, image);
			SaveNewFile(image, newImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private static BufferedImage GetImage(BufferedImage image) {
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
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

	private static String[] ConvertMessageToBinary(String message) {
		int[] messageInAscii = ConvertMessageToAscii(message);
		String[] binary = ConvertAsciiToBinary(messageInAscii);
		return binary;
	}

	private static int[] ConvertMessageToAscii(String message) {
		int[] messageCharactersInAscii = new int[message.length()];
		for(int i = 0; i < message.length(); i++) {
			int asciiValue = (int) message.charAt(i);
			messageCharactersInAscii[i] = asciiValue;
		}
		return messageCharactersInAscii;
	}

	private static String[] ConvertAsciiToBinary(int[] messageInAscii) {
		String[] messageInBinary = new String[messageInAscii.length];
		for(int i = 0; i < messageInAscii.length; i++) {
			String asciiBinary = LeftPadZeros(Integer.toBinaryString(messageInAscii[i]));
			messageInBinary[i] = asciiBinary;
		}
		return messageInBinary;
	}

	private static String LeftPadZeros(String value) {
		StringBuilder paddedValue = new StringBuilder("00000000");
		int offSet = 8 - value.length();
		if (offSet < 0) {
			offSet = 0; // Ensure offset is not negative
		}
		for(int i = 0 ; i < value.length(); i++) {
			paddedValue.setCharAt(i+offSet, value.charAt(i));
		}
		return paddedValue.toString();
	}

	private static void EncodeMessageBinaryInPixels(Pixel[] pixels, String[] messageBinary) { //for every character of the message take 3 pixels
		int pixelIndex = 0;
		boolean isLastCharacter = false;
		for(int i = 0; i < messageBinary.length; i++) {
			Pixel[] currentPixels = new Pixel[] {pixels[pixelIndex], pixels[pixelIndex+1], pixels[pixelIndex+2]};
			if(i+1 == messageBinary.length) {
				isLastCharacter = true; 
			}
			ChangePixelsColor(messageBinary[i], currentPixels, isLastCharacter);
			pixelIndex = pixelIndex +3;
		}
	}
	
	private static void ChangePixelsColor(String messageBinary, Pixel[] pixels, boolean isLastCharacter) { //for every array of 3 pixels (without the last one), takes 3 bits from the character
		int messageBinaryIndex = 0;
		for(int i =0; i < pixels.length-1; i++) {
			char[] messageBinaryChars = new char[] {messageBinary.charAt(messageBinaryIndex), messageBinary.charAt(messageBinaryIndex+1), messageBinary.charAt(messageBinaryIndex+2)}; //devide characteр in 3 parts
			String[] pixelRGBBinary = GetPixelsRGBBinary(pixels[i], messageBinaryChars);
			pixels[i].setColor(GetNewPixelColor(pixelRGBBinary));
			messageBinaryIndex = messageBinaryIndex + 3;
		}
		if(!isLastCharacter) { // for the last pixel, take the last 2 bits from the character and give them to R and G. For B give 1
			char[] messageBinaryChars = new char[] {messageBinary.charAt(messageBinaryIndex), messageBinary.charAt(messageBinaryIndex+1), '1'};
			String[] pixelRGBBinary = GetPixelsRGBBinary(pixels[pixels.length-1], messageBinaryChars);
			pixels[pixels.length-1].setColor(GetNewPixelColor(pixelRGBBinary));
		}else { // for the last pixel, take the last 2 bits from the character and give them to R and G. For B give 0
			char[] messageBinaryChars = new char[] {messageBinary.charAt(messageBinaryIndex), messageBinary.charAt(messageBinaryIndex+1), '0'};
			String[] pixelRGBBinary = GetPixelsRGBBinary(pixels[pixels.length-1], messageBinaryChars); //get new RGBs
			pixels[pixels.length-1].setColor(GetNewPixelColor(pixelRGBBinary)); // set new color
		}
	}
	//change the LSB of the pixel
	private static String[] GetPixelsRGBBinary(Pixel pixel, char[] messageBinaryChars) { // messageBinaryChars - 3 bits from the message
		String[] pixelRGBBinary = new String[3];
		//R
		pixelRGBBinary[0] = ChangePixelBinary(Integer.toBinaryString(pixel.getColor().getRed()), messageBinaryChars[0]); // messageBinaryChars[0] - first bit od the 3bit substring
		//G
		pixelRGBBinary[1] = ChangePixelBinary(Integer.toBinaryString(pixel.getColor().getGreen()), messageBinaryChars[1]);
		//B
		pixelRGBBinary[2] = ChangePixelBinary(Integer.toBinaryString(pixel.getColor().getBlue()), messageBinaryChars[2]);
		return pixelRGBBinary;
	}
	
	private static String ChangePixelBinary(String pixelBinary, char messageBinaryChar) {
		StringBuilder sb = new StringBuilder(pixelBinary);
		sb.setCharAt(pixelBinary.length()-1, messageBinaryChar); //change the last bit of every R, G and B
		return sb.toString();
	}
	
	private static Color GetNewPixelColor(String[] colorBinary) { // get new color
		return new Color(Integer.parseInt(colorBinary[0], 2), Integer.parseInt(colorBinary[1], 2), Integer.parseInt(colorBinary[2], 2));
	}
	
	private static void ReplacePixelsInNewBufferedImage(Pixel[] newPixels, BufferedImage newImage) {
		for(int i = 0; i < newPixels.length; i++) {
			newImage.setRGB(newPixels[i].getX(), newPixels[i].getY(), newPixels[i].getColor().getRGB());
		}
	}
	
	private static void SaveNewFile(BufferedImage newImage, File newImageFile) {
		try {
			ImageIO.write(newImage, "png", newImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

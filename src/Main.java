import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Scanner myObj = new Scanner(System.in);
        System.out.println("What would you like to do?(A/B)\nA:Write a hidden message to an image.\nB:Read a hidden message from an image");

        String action = myObj.nextLine();

        if(action.equals("A")){
            System.out.println("What message would you like to hide?");
            String message = myObj.nextLine();

            System.out.println("Where is your image located? (eg. pictures\\image.png)");
            String input_path = myObj.nextLine();

            System.out.println("Where would you like to save the output image?");
            String output_path = myObj.nextLine();

            Generate_image(message, input_path, output_path);
        }
        else if(action.equals("B")){

            System.out.println("Enter the secret keys with [].");
            String input = myObj.nextLine();
            String[] byteStrings = input.substring(1, input.length() - 1).split(", ");
            byte[] secretKeyBytes = new byte[byteStrings.length];
            for (int i = 0; i < byteStrings.length; i++) {
                secretKeyBytes[i] = Byte.parseByte(byteStrings[i]);
            }

            System.out.println("Enter the location of the image.");
            String output_path = myObj.nextLine();

            System.out.println("Enter the cipher algorithm. (e.g., \"AES\")");
            String cipher_algorithm = myObj.nextLine();

            Extract_text(secretKeyBytes, output_path, cipher_algorithm);
        }
        else {
            System.out.println("Invalid input!");
        }
    }

    public static void Generate_image(String message, String input_path, String output_path) throws Exception {
        File imageFile = new File(input_path);

        TextEncryptor textEncryptor = new TextEncryptor();

        SecretKey key = textEncryptor.generateAESKey();
        Cipher cipher = textEncryptor.getCipher();

        byte[] secretKeyBytes = key.getEncoded();

        //Encrypting message and embedding it to a image
        String encryptedText = textEncryptor.encrypt(message, key, cipher);
        if(imageFile != null) {
            EmbedLSB.Embed(imageFile, encryptedText, output_path);
            System.out.println("Message is hidden successfully");
        }
        System.out.println("Your recipient need to know the secret key and the cipher algorithm name (e.g., \"AES\") to read you message.");
        System.out.println("SecretKey: "+ Arrays.toString(secretKeyBytes));
        System.out.println("Cipher: "+cipher);
    }
    public static void Extract_text(byte[] secretKeyBytes, String output_path, String cipher_algorithm) throws Exception {
        SecretKey receivedSecretKey = new SecretKeySpec(secretKeyBytes, cipher_algorithm);
        Cipher receivedCipher = Cipher.getInstance(cipher_algorithm);
        receivedCipher.init(Cipher.DECRYPT_MODE, receivedSecretKey);
        //Extracting hidden and encryted text and decryting it
        ExtractLSB.Extract(output_path, receivedSecretKey, receivedCipher);
    }
}

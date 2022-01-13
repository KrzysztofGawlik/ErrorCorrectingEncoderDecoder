import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
/**
 *  This program works in 3 modes:
 *
 *      - ENCODE, taking text from send.txt, and converted content saves in encoded.txt
 *        (there are 3 bits per Byte already with the parity pair)
 *
 *      - SEND, takes text from encoded.txt, simulates errors and saves result in
 *        received.txt
 *
 *      - DECODE, decodes and checks parity pair
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Main main = new Main();
        Scanner scan = new Scanner(System.in);
        boolean working = true;
        while (working) {
            System.out.print("Write a mode (encode (2)/send/decode (2)/quit): ");
            switch (scan.nextLine().toLowerCase()) {
                case "encode" -> main.encodeVer1();
                case "encode (2)" -> main.encodeVer2();
                case "send" -> main.send();
                case "decode" -> main.decodeVer1();
                case "decode (2)" -> main.decodeVer2();
                case "quit" -> working = false;
                default -> System.out.println("There is no such mode...");
            }
        }
    }

    void encodeVer1() throws IOException {
        ArrayList<Integer> bytesArray = readFile("send.txt");

        /*
        Checks if the array is divisible by 3, if not then adds 0's.
         */
        while(bytesArray.size() % 3 != 0) {
            bytesArray.add(0);
        }

        /*
        Creates an array with XOR operation results.
         */
        ArrayList<Integer> xorResults = new ArrayList<>();
        int xor;
        for (int i = 0; i < bytesArray.size(); i += 3) {
            xor = bytesArray.get(i) ^ bytesArray.get(i + 1) ^ bytesArray.get(i + 2);
            xorResults.add(xor);
        }

        /*
        Adds XOR results to binary form in array.
         */
        int startPos = 3;
        for (int xorRes : xorResults) {
            bytesArray.add(startPos, xorRes);
            startPos += 4;
        }

        /*
        Doubles all elements of the array.
         */
        ArrayList<Integer> copyOfBytesArray = new ArrayList<>(bytesArray);
        bytesArray.clear();
        for (int bit : copyOfBytesArray) {
            bytesArray.add(bit);
            bytesArray.add(bit);
        }

        writeFile("encoded.txt", bytesArray);
        System.out.println("Message successfully encoded (version 1)!");
    }

    void encodeVer2() throws IOException {
        ArrayList<Integer> bytesArray = readFile("send.txt");

        /*
        Encodes message with Hamming Code [7,4] -> 7 bits: 4 significant, 4 parity bits (used only 3,
        4th is always 0).
         */
        ArrayList<Integer> bytesArrayCopy = new ArrayList<>(bytesArray);
        bytesArray.clear();
        int[] singleByte = new int[8];
        int rep = 0;

        for (int i = 0; i < bytesArrayCopy.size() / 4; i++) {
            singleByte[2] = bytesArrayCopy.get(rep * 4);
            singleByte[4] = bytesArrayCopy.get(rep * 4 + 1);
            singleByte[5] = bytesArrayCopy.get(rep * 4 + 2);
            singleByte[6] = bytesArrayCopy.get(rep * 4 + 3);

            singleByte[0] = (singleByte[2] + singleByte[4] + singleByte[6]) % 2;
            singleByte[1] = (singleByte[2] + singleByte[5] + singleByte[6]) % 2;
            singleByte[3] = (singleByte[4] + singleByte[5] + singleByte[6]) % 2;
            singleByte[7] = 0;

            for (int x : singleByte) bytesArray.add(x);

            rep++;
        }

        writeFile("encoded.txt", bytesArray);
        System.out.println("Message successfully encoded (version 2)!");
    }

    void send() throws IOException {
        ArrayList<Integer> bytesArray = readFile("encoded.txt");

        /*
        Changes one bit per byte in bytesArray
         */
        Random rand = new Random();
        int position;
        for (int i = 0; i < bytesArray.size() / 8; i++) {
            position = rand.nextInt(8) + (i * 8);
            if (bytesArray.get(position) == 0) {
                bytesArray.set(position, 1);
            } else {
                bytesArray.set(position, 0);
            }
        }

        writeFile("received.txt", bytesArray);
        System.out.println("Message successfully sent! Errors simulation completed!");
    }

    void decodeVer1() throws IOException {
        ArrayList<Integer> bytesArray = readFile("received.txt");

        ArrayList<Integer> correctedBytes = new ArrayList<>();
        int[] singleByte = new int[8];

        /*
        Creates single byte array to check the parity
         */
        while (!bytesArray.isEmpty()) {
            for (int i = 0; i < 8; i ++) {
                singleByte[i] = bytesArray.get(0);
                bytesArray.remove(0);
            }

            /*
            Finds correct pairs for XOR equations
             */
            if (singleByte[6] != singleByte[7]) {
                correctedBytes.add(singleByte[0]);
                correctedBytes.add(singleByte[2]);
                correctedBytes.add(singleByte[4]);
            } else {
                if ((singleByte[0] ^ singleByte[2] ^ singleByte[4]) == singleByte[6]) {
                    correctedBytes.add(singleByte[0]);
                    correctedBytes.add(singleByte[2]);
                    correctedBytes.add(singleByte[4]);
                } else {
                    correctedBytes.add(singleByte[1]);
                    correctedBytes.add(singleByte[3]);
                    correctedBytes.add(singleByte[5]);
                }
            }
        }

        writeFile("decoded.txt", correctedBytes);
        System.out.println("Message successfully decoded! (version 1)");
    }

    void decodeVer2() throws IOException {
        ArrayList<Integer> bytesArray = readFile("received.txt");

        ArrayList<Integer> decoded = new ArrayList<>();
        while (!bytesArray.isEmpty()) {
            int wrongBit = 0;
            if (bytesArray.get(0) != (bytesArray.get(2) + bytesArray.get(4) + bytesArray.get(6)) % 2) {
                wrongBit += 1;
            }
            if (bytesArray.get(1) != (bytesArray.get(2) + bytesArray.get(5) + bytesArray.get(6)) % 2) {
                wrongBit += 2;
            }
            if (bytesArray.get(3) != (bytesArray.get(4) + bytesArray.get(5) + bytesArray.get(6)) % 2) {
                wrongBit += 4;
            }
            if (wrongBit == 0) wrongBit = 8;

            if (bytesArray.get(wrongBit - 1) == 0) {
                bytesArray.set(wrongBit - 1, 1);
            } else {
                bytesArray.set(wrongBit - 1, 0);
            }
            decoded.add(bytesArray.get(2));
            decoded.add(bytesArray.get(4));
            decoded.add(bytesArray.get(5));
            decoded.add(bytesArray.get(6));
            bytesArray.subList(0, 8).clear();
        }

        writeFile("decoded.txt", decoded);
        System.out.println("Message successfully decoded! (version 2)");
    }

    /**
     * This method reads a file from path and converts it to binary form
     * which is saved in returned array.
     *
     * @param path - path to the file
     * @return bytesArray - ArrayList with binary content of the file
     * @throws IOException - when file not existing or there is a problem with reading it
     */
    ArrayList<Integer> readFile(String path) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        ArrayList<Integer> singleByteArray = new ArrayList<>();
        ArrayList<Integer> bytesArray = new ArrayList<>();
        byte[] content = fis.readAllBytes();
        fis.close();
        for (int singleByte : content) {
            for (int i = 0; i < 8; i++) {
                singleByteArray.add(singleByte & 0x1);
                singleByte = (byte) (singleByte >> 1);
            }
            Collections.reverse(singleByteArray);
            bytesArray.addAll(singleByteArray);
            singleByteArray.clear();
        }
        return bytesArray;
    }

    /**
     * This method translates binary form from an array to chars and saves them to the file.
     *
     * @param path - path to the file
     * @param bytesArray - ArrayList with binary content of the file
     * @throws IOException - when file not existing or there is a problem with reading it
     */
    void writeFile(String path, ArrayList<Integer> bytesArray) throws IOException {
        PrintWriter pw = new PrintWriter(path); // Only for delete content of the file
        pw.close();
        FileOutputStream fos = new FileOutputStream(new File(path), true);
        int ch = 0;
        int power = 7;
        for (int bit : bytesArray) {
            if (power == 0) {
                ch += bit * ((int) Math.pow(2, power));
                fos.write(ch);
                ch = 0;
                power = 7;
            } else {
                ch += bit * ((int) Math.pow(2, power));
                power--;
            }
        }
        fos.close();
    }
}
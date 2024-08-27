//5L2c6ICF77yaQ2hlbiBaZWxpbi9BbmR5
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter server address: ");
        String serverAddress = stdIn.readLine();
        System.out.print("Enter server port: ");
        int serverPort = Integer.parseInt(stdIn.readLine());

        Socket socket = new Socket(serverAddress, serverPort);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Connected to server");

        System.out.print("Do you want to register (R) or login (L)? ");
        String choice = stdIn.readLine();

        if (choice.equalsIgnoreCase("R")) {
            out.println("REGISTER");
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.equals("USERNAME")) {
                    System.out.print("Enter new username: ");
                    out.println(stdIn.readLine());
                } else if (serverResponse.equals("PASSWORD")) {
                    System.out.print("Enter new password: ");
                    out.println(stdIn.readLine());
                } else if (serverResponse.equals("REG_SUCCESS")) {
                    System.out.println("Registration successful");
                    socket.close();
                    return;
                } else if (serverResponse.equals("REG_FAILED")) {
                    System.out.println("Registration failed");
                    socket.close();
                    return;
                }
            }
        } else if (choice.equalsIgnoreCase("L")) {
            out.println("LOGIN");
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.equals("USERNAME")) {
                    System.out.print("Enter username: ");
                    out.println(stdIn.readLine());
                } else if (serverResponse.equals("PASSWORD")) {
                    System.out.print("Enter password: ");
                    out.println(stdIn.readLine());
                } else if (serverResponse.equals("AUTH_SUCCESS")) {
                    System.out.println("Authentication successful");
                    break;
                } else if (serverResponse.equals("AUTH_FAILED")) {
                    System.out.println("Authentication failed");
                    socket.close();
                    return;
                }
            }

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }

            socket.close();
        }
    }
}

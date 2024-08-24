import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) throws IOException {
        loadUsers();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    private static void loadUsers() throws IOException {
        BufferedReader userReader = new BufferedReader(new FileReader("user.txt"));
        BufferedReader passwdReader = new BufferedReader(new FileReader("passwd.txt"));
        String user;
        while ((user = userReader.readLine()) != null) {
            String passwd = passwdReader.readLine();
            users.put(user, passwd);
        }
        userReader.close();
        passwdReader.close();
    }

    private static void saveUser(String username, String password) throws IOException {
        BufferedWriter userWriter = new BufferedWriter(new FileWriter("user.txt", true));
        BufferedWriter passwdWriter = new BufferedWriter(new FileWriter("passwd.txt", true));
        userWriter.write(username);
        userWriter.newLine();
        passwdWriter.write(password);
        passwdWriter.newLine();
        userWriter.close();
        passwdWriter.close();
        users.put(username, password);
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("COMMAND");
                String command = in.readLine();

                if (command.equals("REGISTER")) {
                    out.println("USERNAME");
                    String newUsername = in.readLine();
                    out.println("PASSWORD");
                    String newPassword = in.readLine();

                    if (!users.containsKey(newUsername)) {
                        saveUser(newUsername, newPassword);
                        out.println("REG_SUCCESS");
                    } else {
                        out.println("REG_FAILED");
                    }
                    socket.close();
                    return;
                }

                if (command.equals("LOGIN")) {
                    out.println("USERNAME");
                    String username = in.readLine();
                    out.println("PASSWORD");
                    String password = in.readLine();

                    if (users.containsKey(username) && users.get(username).equals(password)) {
                        out.println("AUTH_SUCCESS");
                        synchronized (clientWriters) {
                            clientWriters.add(out);
                        }
                        this.username = username;
                        broadcast(username + " has joined the chat");

                        String message;
                        while ((message = in.readLine()) != null) {
                            broadcast(username + ": " + message);
                        }
                    } else {
                        out.println("AUTH_FAILED");
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    broadcast(username + " has left the chat");
                }
                if (out != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}

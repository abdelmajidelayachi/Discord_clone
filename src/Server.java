import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private Socket socket                   = null;
    private ServerSocket server;
    private DataInputStream inputStream     = null;

    public Server(int port) {
        try {

            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("waiting for clients ...");
            while (!server.isClosed()) {
                socket = server.accept();
                System.out.println("New user join the chat!! ✌✌✌");

                ClientHandler client = new ClientHandler(socket);

                Thread thread = new Thread(client);
                thread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
        public static void main(String[] args) {
        Server server1 = new Server(5000);
    }

}

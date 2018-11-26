

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author andrey
 */
public abstract class PlayerThread extends Thread {

    public String name = "Amaze";
    public Socket socket;
    public Cell pos;
    public Cell[] players;
    public Cell[] garbages;
    volatile public boolean dataChanged = false;
    volatile public boolean canFinish = false;

    public PlayerThread() {
        connectToServer();
    }

    private void connectToServer() {
        try {
            Scanner in = new Scanner(new File("E:\\NetbeansProjects\\AmazeBot\\src\\server.txt"));
            String a[] = in.nextLine().split(":");
            socket = new Socket(a[0].trim(), Integer.parseInt(a[1].trim()));
            in.close();
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host");
        } catch (IOException ex) {
            System.out.println("Can not find server.txt");
        }

    }

    @Override
    public void run() {
        long start = 0;

        try {
            Thread reading = new Thread(new AcceptData(socket.getInputStream()));
            reading.start(); // excute accepting thread

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(name);

            boolean first = true;
            while (true) {
                while (!dataChanged && !canFinish); // wait until data is accepted from server
                if (first) {
                    start = System.currentTimeMillis();
                    first = false;
                }

                if (canFinish) {
                    break;
                }

                dataChanged = false;

                Direction d = move();
                out.println(d.toString());
            }

        } catch (IOException ex) {
            canFinish = true;
            System.out.println("Can not read from network");
        } finally {
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - start));
        }
    }

    abstract public Direction move();

    class AcceptData implements Runnable {

        public InputStream input;

        public AcceptData(InputStream in) {
            input = in;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                while (true) {

                    String[] a = in.readLine().split(" "); // get current position
                    pos = new Cell(Integer.parseInt(a[0]), Integer.parseInt(a[1]));

                    int size = Integer.parseInt(in.readLine()); // number of players

                    players = new Cell[size];
                    for (int i = 0; i < size; i++) {
                        a = in.readLine().split(" "); // players coordinates
                        players[i] = new Cell(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
                    }

                    size = Integer.parseInt(in.readLine()); //number of garbages remained

                    garbages = new Cell[size];
                    for (int i = 0; i < size; i++) {
                        a = in.readLine().split(" "); //garbages coordinates
                        garbages[i] = new Cell(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
                    }

                    dataChanged = true; // all data read, write responce!
                }
            } catch (IOException e) {
                System.out.println("Can not read");
                canFinish = true;
                System.exit(0);
                return;
            } catch (NumberFormatException r) {
                System.out.println("Incorrect number");
            } catch (Exception e) {
                System.out.println("No connection");
                canFinish = true;
                System.exit(0);
            }
        }
    }
}

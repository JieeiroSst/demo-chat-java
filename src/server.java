import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;

public class server {
    private static Set<String>names=new HashSet<>();
    private static Set<PrintWriter> writers=new HashSet<>();

    private static class Handler implements Runnable{
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket){
            this.socket=socket;
        }
        @Override
        public void run() {
            try {
                in=new Scanner(socket.getInputStream());
                out=new PrintWriter(socket.getOutputStream(),true);

                while (true){
                    out.println("SUBMITNAME");
                    name=in.nextLine();
                    if(name==null){
                        return;
                    }
                    synchronized (names){
                        if(!name.isBlank() && names.contains(name)){
                            names.add(name);
                            break;
                        }
                    }

                    out.println("NAMEACCEPTED"+name);
                    for (PrintWriter writer: writers){
                        writer.println("MESSAGE"+name+"has joined");
                    }
                    writers.add(out);
                    while (true){
                        String input=in.nextLine();
                        if(input.toLowerCase().startsWith("/quit")){
                            return;
                        }
                        for(PrintWriter writer:writers){
                            writer.println("MESSAGE"+name+":"+input);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (out!=null){
                    writers.remove(out);
                }
                if (name!=null){
                    System.out.println(name+"is leaving");
                    names.remove(name);
                    for(PrintWriter writer:writers){
                        writer.println("MESSAGE"+name+"has left");
                    }
                }
                try {
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args){
        System.out.println("the server running");
        var pool= Executors.newFixedThreadPool(500);
        try (var listener=new ServerSocket(8080)){
            while (true){
                pool.execute(new Handler(listener.accept()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

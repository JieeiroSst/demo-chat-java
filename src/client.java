import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class client {
    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame=new JFrame("Chatter");
    JTextField textField=new JTextField(50);
    JTextArea messageArea=new JTextArea(16,50);

    public client(String serverAddress){
        this.serverAddress=serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea),BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }
    private String getName(){
        return JOptionPane.showInputDialog(frame,"choose name:","screen name selection",JOptionPane.PLAIN_MESSAGE);
    }
    private void run() throws IOException{
        try {
            var socket=new Socket(serverAddress,8080);
            in=new Scanner(socket.getInputStream());
            out=new PrintWriter(socket.getOutputStream());
            while (in.hasNextLine()){
                var line=in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        }finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }
    public static void main(String[] agrs){
        if(agrs.length!=1){
            System.err.println("pass the server ip");
            return;
        }
        var client=new client(agrs[0]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
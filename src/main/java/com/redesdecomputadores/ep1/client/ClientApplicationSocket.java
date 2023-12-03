package com.redesdecomputadores.ep1.client;

import com.redesdecomputadores.ep1.log.DateTimeLog;
import com.redesdecomputadores.ep1.message.MessageClient;
import com.redesdecomputadores.ep1.message.MessageServer;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class ClientApplicationSocket {

    private final String SERVER_ADDRES = "127.0.0.1";
    private Socket client_socket;

    public ClientApplicationSocket(int port) throws IOException {
        this.client_socket = new Socket(SERVER_ADDRES, port);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("  /$$$$$$  /$$                   /$$           /$$$$$$$              /$$    \n" +
                " /$$__  $$| $$                  | $$          | $$__  $$            | $$    \n" +
                "| $$  \\__/| $$$$$$$   /$$$$$$  /$$$$$$        | $$  \\ $$  /$$$$$$  /$$$$$$  \n" +
                "| $$      | $$__  $$ |____  $$|_  $$_/        | $$$$$$$  /$$__  $$|_  $$_/  \n" +
                "| $$      | $$  \\ $$  /$$$$$$$  | $$          | $$__  $$| $$  \\ $$  | $$    \n" +
                "| $$    $$| $$  | $$ /$$__  $$  | $$ /$$      | $$  \\ $$| $$  | $$  | $$ /$$\n" +
                "|  $$$$$$/| $$  | $$|  $$$$$$$  |  $$$$/      | $$$$$$$/|  $$$$$$/  |  $$$$/\n" +
                " \\______/ |__/  |__/ \\_______/   \\___/        |_______/  \\______/    \\___/  \n" +
                "                                                                            \n");

        System.out.println(DateTimeLog.get() + "Iniciando configurações do socket do cliente para realizar conexão com servidor...");
        System.out.println(DateTimeLog.get() + "Por padrão o endereço do servidor será o IP: 127.0.0.1 (localhost) na porta: 4545");
        System.out.println(DateTimeLog.get() + "Iniciando conexão com servidor no endereço: 127.0.0.1/4545");

        ClientApplicationSocket client = new ClientApplicationSocket(4545);
        System.out.println(DateTimeLog.get() + "Solicitação de conexão bem sucedida!");

        ObjectInputStream in = new ObjectInputStream(client.client_socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(client.client_socket.getOutputStream());
        String client_message;
        Scanner scanner;
        System.out.println("------------------------------------------------CHATBOT-------------------------------------------------------");

        do{
            MessageServer messageServer = (MessageServer) in.readObject();
            String[] server_message = messageServer.getContent();
            String client_name = messageServer.getRecipient();
            String server_name = messageServer.getSender();
            boolean close_connection = messageServer.getClose_connection();

            for(int i = 0; i < messageServer.getContentLength(); i++){
                System.out.printf(DateTimeLog.get() + server_message[i]);
            }

            if(close_connection){
                break;
            }

            scanner = new Scanner(System.in);
            client_message = scanner.nextLine();
            MessageClient messageClient = new MessageClient(client_name, server_name, close_connection, client_message);
            out.writeObject(messageClient);

        }while(true);

        client.client_socket.close();
        System.out.println(DateTimeLog.get() + "Conexão com servidor encerrada.");
    }
}

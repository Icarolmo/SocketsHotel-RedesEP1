package com.redesdecomputadores.ep1.server;

import com.redesdecomputadores.ep1.log.DateTimeLog;
import com.redesdecomputadores.ep1.message.MessageClient;
import com.redesdecomputadores.ep1.message.MessageServer;

import java.io.*;
import java.net.Socket;

public class ControllerServer extends Thread {

    private Socket client_socket;
    private static boolean[] calendar;
    private static String[] tickets_calendar;

    public ControllerServer(Socket client_socket, boolean[] calendar, String[] tickets_calendar) {
        this.client_socket = client_socket;
        ControllerServer.calendar = calendar;
        ControllerServer.tickets_calendar = tickets_calendar;
    }

    @Override
    public void run() {
        ObjectOutputStream out;
        ObjectInputStream in;
        try {
            out = new ObjectOutputStream(this.client_socket.getOutputStream());
            in = new ObjectInputStream(this.client_socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String server_name = "Servidor";
        String client_name = "Usuário" + this.client_socket.getPort();
        String client_content_message;
        boolean close_connection = false;

        try {
            String message1 = "ChatBot: Olá, sou o ChatBot que irá te auxiliar na alteração ou realização da sua reserva!\n";
            String message2 = "ChatBot: Primeiramente, como gostaria de ser chamado?\n";
            String message3 = "ChatBot: Lembrando que caso queira finalizar o chat a qualquer momento é só enviar 'FIM'.\n";
            String message_client = client_name + ": Gostaria de ser chamado por: ";
            out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, message3, message_client}));
            MessageClient messageClient = (MessageClient) in.readObject();
            if (messageClient.getContent().equals("FIM")) {
                close_connection = true;
                String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{close_messagem}));
                this.client_socket.close();
                System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + this.client_socket.getPort() +
                        ". " + Thread.currentThread().getName() + " liberada.");
                return;
            }
            client_content_message = messageClient.getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        client_name = client_content_message;


        try {
            String message1 = "ChatBot: Perfeito, prazer em atende-lo " + client_name + "\n";
            String message2 = "ChatBot: Para quais dos serviços abaixo seria seu contato: \n" +
                    "   1 - Realizar reserva.\n" +
                    "   2 - Alterar ou cancelar data da reserva.\n";
            String message3 = "ChatBot: Digite o número da opção que deseja.\n";
            String message_client = client_name + ": Opção número: ";
            out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, message3, message_client}));
            while (true) {
                MessageClient messageClient = (MessageClient) in.readObject();
                if (messageClient.getContent().equals("1") || messageClient.getContent().equals("2") || messageClient.getContent().equals("FIM")) {
                    client_content_message = messageClient.getContent();
                    break;
                }
                String message_erro = "ChatBot: Desculpe, não entendi oque você quis dizer. Escolha umas das opções validas ou envie 'FIM' para finalizar o chat.\n";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message_erro, message2, message3, message_client}));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        switch (client_content_message) {
            case "1":
                try {
                    ReservationService reservationService = new ReservationService(this.client_socket, calendar, tickets_calendar);
                    reservationService.makeReservation(server_name, client_name, close_connection, out, in);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case "2":
                try {
                    ChangeReserveService changeReserveService = new ChangeReserveService(this.client_socket, calendar, tickets_calendar);
                    changeReserveService.alterReservation(server_name, client_name, close_connection, out, in);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case "FIM":
                try{
                    close_connection = true;
                    String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{close_messagem}));
                    this.client_socket.close();
                    System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + this.client_socket.getPort() +
                            ". " + Thread.currentThread().getName() + " liberada.");
                    break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    public static String createCalendar(boolean[] calendar) {
        StringBuilder calendarBuilder = new StringBuilder();

        calendarBuilder.append("\n             Janeiro  de 2024            \n");
        calendarBuilder.append(" Dom   Seg   Ter   Qua   Qui   Sex   Sáb \n");

        int day = 0;
        for (int week = 0; week < 5; week++) {
            for (int week_day = 0; week_day < 7; week_day++) {
                if (day <= calendar.length && day > 0) {
                    if(calendar[day - 1]) {
                        calendarBuilder.append(String.format(" [%02d] ", day));
                    } else {
                        calendarBuilder.append(" [xx] ");
                    }
                } else {
                    calendarBuilder.append(" [xx] ");
                }
                day++;
            }
            calendarBuilder.append("\n");
        }

        calendarBuilder.append("\n");
        return calendarBuilder.toString();
    }
}

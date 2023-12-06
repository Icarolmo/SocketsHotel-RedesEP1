package com.redesdecomputadores.ep1.server;

import com.redesdecomputadores.ep1.log.DateTimeLog;
import com.redesdecomputadores.ep1.message.MessageClient;
import com.redesdecomputadores.ep1.message.MessageServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.redesdecomputadores.ep1.server.ControllerServer.createCalendar;

public class ReservationService {

    private Socket client_socket;
    private static boolean[] calendar;
    private static String[] tickets_calendar;

    public ReservationService(Socket client_socket, boolean[] calendar, String[] tickets_calendar) throws IOException {
        this.client_socket = client_socket;
        ReservationService.calendar = calendar;
        ReservationService.tickets_calendar = tickets_calendar;
    }

    public void makeReservation(String server_name, String client_name, boolean close_connection, ObjectOutputStream output, ObjectInputStream input) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = output;
        ObjectInputStream in = input;

        String message1 = "ChatBot: Ok, você escolheu o serviço de realizar reservas.\n";
        String message2 = "ChatBot: No momento só temos reservas para Janeiro/2024, para qual dia disponível seria o início da reserva?\n";
        String message3 = "ChatBot: As datas marcadas com '[xx]' estão indisponíveis, as demais datas com seu respectivo dia estão disponíveis.\n";
        String message_client = client_name + ": Entendido! O dia de inicio da reserva seria: ";
        out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1,message2, createCalendar(calendar) , message3, message_client}));
        MessageClient messageClient = (MessageClient) in.readObject();

        while(true) {
            try {
                if (messageClient.getContent().equals("FIM")) {
                    close_connection = true;
                    String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{close_messagem}));
                    client_socket.close();
                    System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + client_socket.getPort() +
                            ". " + Thread.currentThread().getName() + " liberada.");
                    return;
                }
                int reserved_day = Integer.parseInt(messageClient.getContent());
                if (reserved_day < 1 || reserved_day > 31) {
                    message1 = "ChatBot: Data para mês de Janeiro inválida. Por favor insira uma data válida ou 'FIM' para finalizar o chat.";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, createCalendar(calendar), message3, message_client}));
                    messageClient = (MessageClient) in.readObject();
                    continue;
                }
                if (this.freeDay(reserved_day)) {
                    String ticket_hash = this.reserveDay(reserved_day);
                    this.setTickets_calendar(ticket_hash, reserved_day);

                    message1 = "ChatBot: Sua reserva para o dia " + reserved_day + " de Janeiro de 2024 foi efetuada com sucesso!\n";
                    message2 = "ChatBot: Seu ticket da reserva é: " + ticket_hash + ". Lembre-se de guarda-lo para caso queira alterar ou cancelar sua reserva.\n";
                    message3 = "ChatBot: Espero te-lo ajudado e o aguardamos em nosso hotel em 0" + reserved_day + "/01/2024. Encerrando ChatBot...\n";
                    close_connection = true;
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, message3}));
                    this.client_socket.close();
                    System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + this.client_socket.getPort() +
                            ". " + Thread.currentThread().getName() + " liberada.");
                    return;
                } else {
                    String error_message = "ChatBot: O dia " + reserved_day + " não está disponível no momento. Por favor, insira uma data disponível: \n";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{error_message, createCalendar(calendar), message_client}));
                    messageClient = (MessageClient) in.readObject();
                }
            } catch (NumberFormatException e) {
                message1 = "ChatBot: Não consegui entender a data que você enviou. Por favor insira uma data válida ou envie 'FIM' para finalizar o chat";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, createCalendar(calendar), message3, message_client}));
                messageClient = (MessageClient) in.readObject();
            }
        }
    }

    private String reserveDay(int day){
        calendar[day - 1] = false;
        return this.generateRandomString(10);
    }

    private boolean freeDay(int day){
        return calendar[day - 1];
    }

    private void setTickets_calendar(String ticket_hash, int day){
        tickets_calendar[day - 1] = ticket_hash;
    }

    private String generateRandomString(int comprimento) {
        // Define os caracteres possíveis para a string
        String caracteresPossiveis = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        // Cria uma lista de caracteres
        List<Character> listaCaracteres = new ArrayList<>();
        for (char c : caracteresPossiveis.toCharArray()) {
            listaCaracteres.add(c);
        }

        // Embaralha a lista para garantir aleatoriedade
        Collections.shuffle(listaCaracteres);

        // Seleciona os primeiros 'comprimento' caracteres da lista
        List<Character> caracteresSelecionados = listaCaracteres.subList(0, comprimento);

        // Converte a lista de caracteres selecionados em uma string
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : caracteresSelecionados) {
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}



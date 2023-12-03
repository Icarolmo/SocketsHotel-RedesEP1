package com.redesdecomputadores.ep1.server;

import com.redesdecomputadores.ep1.log.DateTimeLog;
import com.redesdecomputadores.ep1.message.MessageClient;
import com.redesdecomputadores.ep1.message.MessageServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.redesdecomputadores.ep1.server.ControllerServer.createCalendar;

public class ChangeReserveService {
    private Socket client_socket;
    private static boolean[] calendar;
    private static String[] tickets_calendar;

    public ChangeReserveService(Socket client_socket, boolean[] calendar, String[] tickets_calendar) throws IOException {
        this.client_socket = client_socket;
        ChangeReserveService.calendar = calendar;
        ChangeReserveService.tickets_calendar = tickets_calendar;
    }

    public void alterReservation(String server_name, String client_name, boolean close_connection, ObjectOutputStream output, ObjectInputStream input) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = output;
        ObjectInputStream in = input;

        String message1 = "ChatBot: Ok, você escolheu o serviço de alteração ou cancelamento de reservas. Lembre-se que irá precisar do ticket gerado no momento da reserva.\n";
        String message2 = "ChatBot: No momento só temos reservas para Janeiro/2024, para qual data de Janeiro está agendado sua reserva?\n";
        String message_client = client_name + ": A data da minha reserva é: ";
        out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, createCalendar(calendar), message_client}));
        MessageClient messageClient = (MessageClient) in.readObject();
        int reserved_day;
        while(true) {
            try {
                if (messageClient.getContent().equals("FIM")) {
                    close_connection = true;
                    String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{close_messagem}));
                    client_socket.close();
                    System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + client_socket.getPort() +
                            ". " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId() + " liberada.");
                    return;
                }
                reserved_day = Integer.parseInt(messageClient.getContent());
                if (reserved_day < 1 || reserved_day > 31) {
                    message1 = "ChatBot: Dia inválido. Por favor insira uma data válida ou 'FIM' para finalizar o chat.\n";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, createCalendar(calendar), message_client}));
                    messageClient = (MessageClient) in.readObject();
                    continue;
                }
                if (this.freeDay(reserved_day)) {
                    message1 = "ChatBot: Não existe reserva realizada para a data enviada. Por favor envie a data correta da sua reserva ou 'FIM' para finalizar o chat.\n";
                    out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, createCalendar(calendar), message_client}));
                    messageClient = (MessageClient) in.readObject();
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                message1 = "ChatBot: Não consegui entender a data que você enviou. Por favor insira uma data válida ou envie 'FIM' para finalizar o chat.\n";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, createCalendar(calendar), message_client}));
                messageClient = (MessageClient) in.readObject();
            }
        }

        message1 = "ChatBot: Perfeito, agora precisamos validar sua reserva. Por favor, envie o ticket gerado no momento da reserva feita para " + reserved_day +"/Janeiro/2024.\n";
        message_client = client_name + " Meu ticket: ";
        out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message_client}));
        messageClient = (MessageClient) in.readObject();
        String ticket = messageClient.getContent();
        while(true){
            if(!ticket.equals(tickets_calendar[reserved_day - 1])){
                message1 = "ChatBot: Ticket para reserva em " + reserved_day + "/Janeiro/2024 incorreto. Por favor revise e envie novamente seu Ticket ou envie 'FIM' para finalizar o chat.\n";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message_client}));
                messageClient = (MessageClient) in.readObject();
                ticket = messageClient.getContent();
            } else {
                break;
            }
        }

        message1 = "ChatBot: Ticket validado com sucesso! Agora selecione o serviço que deseja realizar para sua reserva: \n" +
        "       1 - Alterar data da reserva.\n" +
        "       2 - Cancelar reserva.\n";
        message2 = "ChatBot: Por favor, envie o número do serviço que deseja (1 ou 2).\n";
        message_client = client_name + " Opção: ";
        out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, message_client}));
        messageClient = (MessageClient) in.readObject();
        String option;

        while(true){
            option = messageClient.getContent();
            if (option.equals("FIM")) {
                close_connection = true;
                String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{close_messagem}));
                client_socket.close();
                System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + client_socket.getPort() +
                        ". " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId() + " liberada.");
                return;
            }
            if(!option.equals("1") && !option.equals("2")){
                message1 = "ChatBot: Opção inválida, por favor insira uma das opções abaixo ou envie 'FIM' para finalizar o chat: \n" +
                        "       1 - Alterar data da reserva.\n" +
                        "       2 - Cancelar reserva.\n";
                message2 = "ChatBot: Por favor, envie o número do serviço que deseja (1 ou 2).\n";
                message_client = client_name + " Opção: ";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, message_client}));
                messageClient = (MessageClient) in.readObject();
                option = messageClient.getContent();
                continue;
            }
            break;
        }

        switch (option){
            case "1":
                message1 = "ChatBot: Ok, você selecionou a opção de alteração da data de reserva. Insira a nova data para qual deseja alterar a reserva.\n";
                message2 = "ChatBot: As datas marcadas com '[xx]' estão indisponíveis, as demais datas com seu respectivo dia estão disponíveis.\n";
                message_client = client_name + " Desejo alterar a minha reserva para: ";
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, createCalendar(calendar), message2, message_client}));
                messageClient = (MessageClient) in.readObject();
                int new_date;
                while(true){
                        try {
                        if (messageClient.getContent().equals("FIM")) {
                            close_connection = true;
                            String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                            out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{close_messagem}));
                            client_socket.close();
                            System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + client_socket.getPort() +
                                    ". " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId() + " liberada.");
                            return;
                        }
                        new_date = Integer.parseInt(messageClient.getContent());
                        if (new_date < 1 || new_date > 31) {
                            message1 = "ChatBot: Dia inválido. Por favor insira uma data válida ou 'FIM' para finalizar o chat.\n";
                            out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, createCalendar(calendar), message_client}));
                            messageClient = (MessageClient) in.readObject();
                            continue;
                        }
                        if (this.freeDay(new_date)) {
                            message1 = "ChatBot: Perfeito! Reserva do dia " + reserved_day + "/Janeiro/2024 alterada para " + new_date +
                                    "/Janeiro/2024.\n";
                            message2 = "ChatBot: Seu ticket de reserva continua sendo o mesmo da data anterior, lembre-se de guarda-lo.\n";
                            String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                            close_connection = true;
                            out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, close_messagem}));
                            client_socket.close();
                            this.changeData(reserved_day, new_date);
                            System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + client_socket.getPort() +
                                    ". " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId() + " liberada.");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        message1 = "ChatBot: Não consegui entender a data que você enviou. Por favor insira uma data válida ou envie 'FIM' para finalizar o chat.\n";
                        out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, createCalendar(calendar), message_client}));
                        messageClient = (MessageClient) in.readObject();
                    }
                }
            case "2":
                message1 = "ChatBot: Ok, você selecionou a opção de cancelamento de reserva.\n";
                message2 = "ChatBot: Sua reserva para " + reserved_day + "/Janeiro/2024 foi cancelada com sucesso. \n";
                String close_messagem = "ChatBot: Encerrando ChatBot. Espero ve-lo em breve, até!\n";
                close_connection = true;
                out.writeObject(new MessageServer(server_name, client_name, close_connection, new String[]{message1, message2, close_messagem}));
                client_socket.close();
                this.cancelData(reserved_day);
                System.out.println(DateTimeLog.get() + "Encerrada conexão com Cliente: " + client_socket.getPort() +
                        ". " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId() + " liberada.");
                break;
        }
    }

    private void cancelData(int date){
        calendar[date - 1] = true;
        tickets_calendar[date - 1] = "";
    }

    private void changeData(int old_data, int new_data){
        calendar[old_data - 1] = true;
        calendar[new_data - 1] = false;
        tickets_calendar[new_data - 1] = tickets_calendar[old_data - 1];
        tickets_calendar[old_data - 1] = "";
    }

    private boolean freeDay(int day){
        return calendar[day - 1];
    }
}

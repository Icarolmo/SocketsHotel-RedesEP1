package com.redesdecomputadores.ep1.server;

/*
Bibliotecas usadas no projeto.
    java.net.ServerSocket: Classe para criação de Socket do tipo servidor que recebe e estabelece conexão TCP.
    java.net.Socket: Classe para criação de Socket do tipo cliente que solicita conexão TCP.
    java.io.IOException: Classe para tratamento de exceções.
    DateTimeLog: Classe criada tanto para geração de log quanto para informação de dia e hora no ChatBot.
 */

import com.redesdecomputadores.ep1.log.DateTimeLog;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



/*
Classe que representa o servidor que realiza o seguinte fluxo:
    1) Quando criada uma instância da classe inicializa por padrão um ServerSocket na porta 4545.
    2) Inicializa loop aguardando requisições de outros Sockets.
    3) Quando recebe uma requisição de conexão TCP de um cliente realiza o aceite e passa a instância
    do Socket do cliente e ServerSocket para a Classe ControllerServer que realiza a regra de negócio.
    4) Volta a aguardar novas requisições.
 */


public class ServerApplicationSocket{

    private ServerSocket server_socket; // Socket do servidor responsável por receber e realizar conexões com clientes.

    private static boolean[] calendar; // Vetor de booleanos utilizados para gestão de reservas de Janeiro do hotel.

    private static String[] tickets_calendar;

    // Construtor da classe: Recebe a porta e o número máximo da fila requisições de conexões do ServerSocket.
    public ServerApplicationSocket(int port, int connection_queue_size) throws IOException {
        this.server_socket = new ServerSocket(port, connection_queue_size);
        boolean[] inst_calendar = new boolean[31];
        String[] inst_tickets_calendar = new String[31];
        for(int i = 0; i < 31; i++){
            inst_calendar[i] = true;
            inst_tickets_calendar[i] = "";
        }
        inst_calendar[4] = false;
        calendar = inst_calendar;
        tickets_calendar = inst_tickets_calendar;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(" /$$   /$$             /$$               /$$        /$$$$$$                      /$$                   /$$             \n" +
                "| $$  | $$            | $$              | $$       /$$__  $$                    | $$                  | $$             \n" +
                "| $$  | $$  /$$$$$$  /$$$$$$    /$$$$$$ | $$      | $$  \\__/  /$$$$$$   /$$$$$$$| $$   /$$  /$$$$$$  /$$$$$$   /$$$$$$$\n" +
                "| $$$$$$$$ /$$__  $$|_  $$_/   /$$__  $$| $$      |  $$$$$$  /$$__  $$ /$$_____/| $$  /$$/ /$$__  $$|_  $$_/  /$$_____/\n" +
                "| $$__  $$| $$  \\ $$  | $$    | $$$$$$$$| $$       \\____  $$| $$  \\ $$| $$      | $$$$$$/ | $$$$$$$$  | $$   |  $$$$$$ \n" +
                "| $$  | $$| $$  | $$  | $$ /$$| $$_____/| $$       /$$  \\ $$| $$  | $$| $$      | $$_  $$ | $$_____/  | $$ /$$\\____  $$\n" +
                "| $$  | $$|  $$$$$$/  |  $$$$/|  $$$$$$$| $$      |  $$$$$$/|  $$$$$$/|  $$$$$$$| $$ \\  $$|  $$$$$$$  |  $$$$//$$$$$$$/\n" +
                "|__/  |__/ \\______/    \\___/   \\_______/|__/       \\______/  \\______/  \\_______/|__/  \\__/ \\_______/   \\___/ |_______/ \n" +
                "                                                                                                                       ");

        System.out.println(DateTimeLog.get() + "Iniciando Servidor...");

        ServerApplicationSocket server;

        // Verifica se foi passado número de porta para o servidor, se não for passado será inicializado na porta 4545.
        if (args.length > 0) {
            System.out.println(DateTimeLog.get() + "Número de porta para SocketServer fornecido: " + args[0]);
            server = new ServerApplicationSocket(Integer.parseInt(args[0]), 50);
        } else {
            System.out.println(DateTimeLog.get() + "Não foi fornecido número de porta para SocketServer. Subindo servidor na porta: 4545");
            server = new ServerApplicationSocket(4545, 50);
        }

        System.out.println(DateTimeLog.get() + "Servidor inicializado no endereço: " + server.server_socket.getLocalSocketAddress() + ". Aguardando conexões de clientes....");

        // Loop de espera de requisições e quando recebidas e aceitas são passadas para ControllerServer para serem tratadas.
        while(!server.server_socket.isClosed()) {
            try {
                Socket new_client = server.server_socket.accept();
                System.out.println(DateTimeLog.get() + "Nova conexão com cliente - Endereço: " + new_client.getRemoteSocketAddress());
                ControllerServer thread_new_client = new ControllerServer(new_client, calendar, tickets_calendar);
                System.out.println(DateTimeLog.get() + "Reservada " + thread_new_client.getName() + " ID: " + thread_new_client.getId() + " para cliente: " + new_client.getPort());
                thread_new_client.start();
            } catch (IOException e) {
                System.out.println(DateTimeLog.get() + e.toString() + ": Erro ao aceitar conexão de novo cliente.");
            }
        }
        System.out.println(DateTimeLog.get() + "Desconectando servidor da rede local e porta 4545...");
    }
}

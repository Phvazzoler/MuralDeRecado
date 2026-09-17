package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class Main {
    private static final RecadoDAO DAO = new RecadoDAO();

    public static void main(String[] args) throws IOException {
        testarConexao();
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        servidor.createContext("/api/recados", Main::atenderRecados);
        servidor.createContext("/", Main::abrirPagina);
        servidor.start();
        System.out.println("Mural aberto em http://localhost:8080");
        System.out.println("Celulares na mesma rede podem acessar o mural em http://localhost:8080");


    }

    private static void testarConexao() throws SQLException {
        try (Connection ignored = Conexao.abrir()) {
            System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
        }

    }
    private static void atenderRecados(HttpExchange troca) throws IOException{
        troca.sendResponseHeaders().set("Acess-Control-Allow-Origin","*");
        troca.sendResponseHeaders().set("Acess-Control-Allow-Methods","GET, POST, OPTIONS");
        troca.sendResponseHeaders().set("Acess-Control-Allow-Headers","Content-Type");

        try {
            if (troca.getRequestMethod().equals("OPTIONS")) {
                troca.sendResponseHeaders(204, -1);
                troca.close();
            } else if (troca.getRequestMethod().equals("GET")) {
                listar(troca);
            } else if (troca.getRequestMethod().equals("POST")) {
                cadastrar(troca);
            } else {
                troca.sendResponseHeaders().set("Allow", "GET, POST, OPTIONS");
                responder(troca, 405, "{\"erro\":\"Método não permitido\"}");

            }
        } catch (SQLException erro) {
            erro.printStackTrace();
            responder(troca, 500, "{\"erro\":\"Erro ao acessar o banco de dados\"}");
        }
    }
    private static void cadastrar(HttpExchange troca) throws IOException {
        // Implementar lógica para cadastrar recado
        Map<String ,String> dados = lerFormulario(troca);
        String autor = dados.getOrDefault("autor","").trim();
        String mensagem = dados.getOrDefault("mensagem","").trim();
        if (autor.isEmpty() || mensagem.isEmpty()){
            responder(troca,400,"{\"erro\":\"Preencher todos os campos\"}");
            return;
    }
        DAO.cadastrar(new Recado(0,autor,mensagem));
        responder(troca,201,"{\"sucesso\":\"Recado cadastrado com sucesso\"}");
    }
}

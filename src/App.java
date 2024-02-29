import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import compiladores.lexico.Lexico;
import compiladores.lexico.Token;
import compiladores.sintatico.Sintatico;

public class App {

    private static final String MODO_DE_USAR = "Modo de usar: java -jar NomePrograma NomeArquivoCodigo";
    private static final String NOME_PADRAO_ARQUIVO = "teste.pas";
    private static final int NUMERO_ESPACOS_POR_TAB = 4;

    public static void main(String[] args) {
        String nomeArquivo = obterNomeArquivo(args);

        ajustarEspacosTabulacao(nomeArquivo);

        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8))) {
            Lexico lexico = new Lexico(br);
            Sintatico sintatico = new Sintatico(lexico);
            sintatico.analisar();
        } catch (IOException e) {
            handleIOException(nomeArquivo, e);
        }
    }

    private static String obterNomeArquivo(String[] args) {
        if (args.length == 0) {
            System.out.println(MODO_DE_USAR);
            return NOME_PADRAO_ARQUIVO;
        } else {
            return args[0];
        }
    }

    private static void ajustarEspacosTabulacao(String nomeArquivo) {
        Path caminhoArquivoAux = Paths.get(nomeArquivo);
        StringBuilder espacos = new StringBuilder();

        for (int cont = 0; cont < NUMERO_ESPACOS_POR_TAB; cont++) {
            espacos.append(" ");
        }

        try {
            String conteudo = new String(Files.readAllBytes(caminhoArquivoAux), StandardCharsets.UTF_8);
            conteudo = conteudo.replace("\t", espacos.toString());
            Files.write(caminhoArquivoAux, conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            handleIOException(nomeArquivo, e);
        }
    }

    private static void handleIOException(String nomeArquivo, IOException e) {
        System.err.println("Não foi possível abrir ou ler do arquivo: " + nomeArquivo);
        e.printStackTrace();
    }
}

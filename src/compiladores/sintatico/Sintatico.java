package compiladores.sintatico;

import compiladores.lexico.Classe;
import compiladores.lexico.Lexico;
import compiladores.lexico.Token;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Sintatico {

    private Lexico lexico;
    private Token token;
    private boolean flag = false;
    private String codigo;
    private FileWriter fw;
    private Scanner scanner;
    private List<String> variaveis = new ArrayList<>();
    private List<String> parametros = new ArrayList<>();
    public Sintatico(Lexico lexico) {

        this.lexico = lexico;
        scanner = new Scanner(System.in);
        try {
            codigo = "#include <stdio.h>\n#include <stdlib.h>\n#include <string.h>\n#include <math.h>\nint main(){\n";
            fw = new FileWriter(new File("saida.c"));
        } catch (IOException e) {
            System.out.println("Erro ao criar arquivo saida.c");
            e.printStackTrace();
        }
    }

    public void analisar() {
        try {
            token = lexico.nextToken();
            programa();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // <programa> ::= program <id> {A01} ; <corpo> • {A45}
    private void programa() {
        if (palavraReservada(token, "program")) {
            token = lexico.nextToken();

            if (token.getClasse() == Classe.cId) {
                token = lexico.nextToken();

                // Verifica se há ponto e vírgula após o nome do programa
                if (token.getClasse() == Classe.cPontoVirg) {
                    token = lexico.nextToken();
                    corpo();

                    // Verifica se há ponto final no final do programa
                    if (token.getClasse() != Classe.cPonto) {
                        token = lexico.nextToken();
                        // {A45}
                    } else {
                        exibirErro(", Ponto Final (.) esperado");
                    }
                } else {
                    exibirErro(", Ponto e vírgula (;) esperado após o nome do programa");
                }
            } else {
                exibirErro(", Nome do Programa esperado");
            }
        } else {
            exibirErro(", 'program' esperado");
        }
    }


    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    private void corpo() {
        declara();
        gerarVariaveis();

        if (palavraReservada(token, "begin")) {
            token = lexico.nextToken();
            sentencas();

            // Geração do código para encerrar o bloco
            codigo += "return 0;\n";
            codigo += "}";

            if (palavraReservada(token, "end")) {
                token = lexico.nextToken();
                gerarCorpo();  // Não ficou claro o que é gerarCorpo(), ajuste conforme necessário
            } else {
                exibirErro(", end esperado");
            }
        } else {
            exibirErro(", begin esperado");
        }
    }


    // <declara> ::= var <dvar> <mais_dc> | ε
    private void declara() {
        if (palavraReservada(token, "var")) {
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }

    // <mais_dc> ::= ; <cont_dc>

    private void mais_dc() {
        if (token.getClasse() == Classe.cPontoVirg) {
            token = lexico.nextToken();
            cont_dc();
        }
    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    private void cont_dc() {
        if (token.getClasse() == Classe.cId) {
            if(!palavraReservada(token, "begin")){
                dvar();
                mais_dc();
            }
        }

    }

    private void dvar() {
        variaveis();
        if (token.getClasse() == Classe.cDoisPontos) {
            token = lexico.nextToken();
            tipo_var();
        } else {
            exibirErro( ", (:) esperado");
        }

    }

    // <tipo_var> ::= integer
    private void tipo_var() {
        if (palavraReservada(token, "integer")) {
            token = lexico.nextToken();
        } else {
            exibirErro(", tipo integer não especificado.");
        }
    }

    // <variaveis> ::= <id> {A03} <mais_var>
    private void variaveis() {
        if (token.getClasse() == Classe.cId) {
            variaveis.add(token.getValor().getTexto());
            token = lexico.nextToken();
            // {A03}
            mais_var();
        } else {
            exibirErro(", Nome esperado");
        }

    }

    // <mais_var> ::= , <variaveis> | ε
    private void mais_var() {
        if (token.getClasse() == Classe.cVirg) {
            token = lexico.nextToken();
            variaveis();
        }

    }

    // <sentencas> ::= <comando> <mais_sentencas>
    private void sentencas() {
        comando();
        mais_sentencas();
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    private void mais_sentencas() {
        if (token.getClasse() == Classe.cPontoVirg) {
            token = lexico.nextToken();
            codigo += ";\n";
            cont_sentencas();
        } else {
            exibirErro( ", (;) 1 esperado");
        }

    }

    // <cont_sentencas> ::= <sentencas> | ε
    private void cont_sentencas() {
        if ((token.getClasse() == Classe.cPalavraReservada && isPalavraReservadaValida(token)) || (token.getClasse() == Classe.cId)) {
            sentencas();
        }
    }

    private boolean isPalavraReservadaValida(Token token) {
        String[] palavrasReservadas = {"read", "write", "writeln", "for", "repeat", "while", "if"};
        return palavraReservada(token, palavrasReservadas);
    }

    private boolean palavraReservada(Token token, String[] palavrasReservadas) {
        String textoToken = token.getValor().getTexto();
        for (String palavra : palavrasReservadas) {
            if (textoToken.equals(palavra)) {
                return true;
            }
        }
        return false;
    }

    // <var_read> ::= <id> {A08} <mais_var_read>
    private void var_read() {
        if (token.getClasse() == Classe.cId) {
            String varNome = token.getValor().getTexto();

            // Verifica se a variável já foi declarada
            if (chck_var(varNome)) {
                parametros.add(varNome);
                codigo += "%d ";
            } else {
                exibirErro("Variável não declarada: " + varNome);
            }

            token = lexico.nextToken();
            // {AO8}
            mais_var_read();
        } else {
            exibirErro("Nome esperado");
        }
    }

    // <mais_var_read> ::= , <var_read> | ε
    private void mais_var_read() {
        while (token.getClasse() == Classe.cId) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.cVirg) {
                token = lexico.nextToken();
            } else {
                break;
            }
        }
    }

    // <exp_write> ::= <id> {A09} <mais_exp_write> | <string> {A59} <mais_exp_write>
    // | <intnum> {A43} <mais_exp_write>
    private void exp_write() {
        if ((token.getClasse() == Classe.cId)) {
            parametros.add(token.getValor().getTexto());
            token = lexico.nextToken();
            codigo += "%d ";
            // {A09}
            mais_exp_write();
        } else if ((token.getClasse() == Classe.cString)) {

            codigo += token.getValor().getTexto();

            token = lexico.nextToken();

            // {A59}
            mais_exp_write();
        } else if ((token.getClasse() == Classe.cInt)) {

            codigo += String.valueOf(token.getValor().getInteiro());

            token = lexico.nextToken();
            // {A43}
            mais_exp_write();
        } else {
            exibirErro( ", Esperado (nome), (int), ou (string)");
        }
    }

    // <mais_exp_write> ::= , <exp_write> | ε
    private void mais_exp_write() {
        if (token.getClasse() == Classe.cVirg) {
            token = lexico.nextToken();
            exp_write();
        }
    }

    // comando> ::=
    // read ( <var_read> ) |
    // write ( <exp_write> ) |
    // writeln ( <exp_write> ) {A61} |
    // for <id> {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas>
    // end {A13} |
    // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
    // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
    // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa>
    // {A21} |
    // <id> {A49} := <expressao> {A22}
    // | ε
    private void comando() {
        if (palavraReservada(token, "read")) {
            processarRead();
        } else if (palavraReservada(token, "write")) {
            processarWrite(false);
        } else if (palavraReservada(token, "writeln")) {
            processarWrite(true);
        } else if (palavraReservada(token, "for")) {
            processarFor();
        } else if (palavraReservada(token, "repeat")) {
            processarRepeat();
        } else if (palavraReservada(token, "while")) {
            processarWhile();
        } else if (palavraReservada(token, "if")) {
            processarIf();
        } else if (token.getClasse() == Classe.cId) {
            processarAtribuicao();
        }
    }

    private void processarRead() {
        token = lexico.nextToken();
        if (token.getClasse() == Classe.cParEsq) {
            token = lexico.nextToken();
            codigo += "scanf(\"";
            var_read();
            gerarParametros(true);
            if (token.getClasse() == Classe.cParDir) {
                codigo += ")";
                token = lexico.nextToken();
            } else {
                exibirErro(", ) esperado");
            }
        } else {
            exibirErro(", ( esperado");
        }
    }

    private void processarWrite(boolean isWriteln) {
        token = lexico.nextToken();
        if (token.getClasse() == Classe.cParEsq) {
            token = lexico.nextToken();
            codigo += "printf(\"";
            exp_write();
            gerarParametros(false);
            if (token.getClasse() == Classe.cParDir) {
                token = lexico.nextToken();
                codigo += isWriteln ? ");\n" : ")";
                if(isWriteln){
                    codigo += "printf(\"\\n\")";
                }
            } else {
                exibirErro(", ) esperado");
            }
        } else {
            exibirErro(", ( esperado");
        }
    }

    private void processarFor() {
        token = lexico.nextToken();
        if (token.getClasse() == Classe.cId) {
            token = lexico.nextToken();
            codigo += "for(";
            if (token.getClasse() == Classe.cAtribuicao) {
                token = lexico.nextToken();
                expressao();
                if (palavraReservada(token, "to")) {
                    token = lexico.nextToken();
                    expressao();
                    if (palavraReservada(token, "do")) {
                        token = lexico.nextToken();
                        if (palavraReservada(token, "begin")) {
                            token = lexico.nextToken();
                            sentencas();
                            if (palavraReservada(token, "end")) {
                                token = lexico.nextToken();
                            } else {
                                exibirErro(", end esperado");
                            }
                        } else {
                            exibirErro(", (begin) esperado");
                        }
                    } else {
                        exibirErro(", (do) esperado");
                    }
                } else {
                    exibirErro(", (to) esperado");
                }
            } else {
                exibirErro(", (:=) esperado");
            }
        } else {
            exibirErro(", (id) esperado");
        }
    }

    private void processarRepeat() {
        token = lexico.nextToken();
        sentencas();
        if (palavraReservada(token, "until")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.cParEsq) {
                token = lexico.nextToken();
                expressao_logica();
                if (token.getClasse() == Classe.cParDir) {
                    token = lexico.nextToken();
                } else {
                    exibirErro(", ) esperado");
                }
            } else {
                exibirErro(", ( esperado");
            }
        } else {
            exibirErro(", until esperado");
        }
    }

    private void processarWhile() {
        codigo += "while";
        token = lexico.nextToken();
        if (token.getClasse() == Classe.cParEsq) {
            codigo += "(";
            token = lexico.nextToken();
            expressao_logica();
            codigo += ")";
            if (token.getClasse() == Classe.cParDir) {
                token = lexico.nextToken();
                if (palavraReservada(token, "do")) {
                    codigo += "{\n";
                    token = lexico.nextToken();
                    if (palavraReservada(token, "begin")) {
                        token = lexico.nextToken();
                        sentencas();
                        codigo += "}";
                        if (palavraReservada(token, "end")) {
                            token = lexico.nextToken();
                        } else {
                            exibirErro(", end esperado");
                        }
                    } else {
                        exibirErro(", (begin) esperado");
                    }
                } else {
                    exibirErro(", (do) esperado");
                }
            } else {
                exibirErro(", ) esperado");
            }
        } else {
            exibirErro(", ( esperado");
        }
    }

    private void processarIf() {
        token = lexico.nextToken();
        if (token.getClasse() == Classe.cParEsq) {
            token = lexico.nextToken();
            expressao_logica();
            if (token.getClasse() == Classe.cParDir) {
                token = lexico.nextToken();
                if (palavraReservada(token, "then")) {
                    token = lexico.nextToken();
                    if (palavraReservada(token, "begin")) {
                        token = lexico.nextToken();
                        sentencas();
                        if (palavraReservada(token, "end")) {
                            token = lexico.nextToken();
                            pfalsa();
                        } else {
                            exibirErro(", end esperado");
                        }
                    } else {
                        exibirErro(", (begin) esperado");
                    }
                } else {
                    exibirErro(", (then) esperado");
                }
            } else {
                exibirErro(", ) esperado");
            }
        }
    }

    private void processarAtribuicao() {
        String aux = token.getValor().getTexto();
        if (chck_var(aux)) {
            codigo += aux;
        } else {
            System.out.println(aux);
            exibirErro("Variável não declarada.");
        }

        token = lexico.nextToken();
        if (token.getClasse() == Classe.cAtribuicao) {
            codigo += " = ";
            token = lexico.nextToken();
            expressao();
        } else {
            exibirErro(", (:=) esperado");
        }
    }


    // <pfalsa> ::= else {A25} begin <sentencas> end | ε
    private void pfalsa() {
        if (palavraReservada(token, "else")) {
            codigo += "}else{\n";
            token = lexico.nextToken();
            // {A25}
            if (palavraReservada(token, "begin")) {
                token = lexico.nextToken();
                sentencas();
                if (palavraReservada(token, "end")) {
                    token = lexico.nextToken();
                    // {A25}
                } else {
                    exibirErro( ", end esperado");
                }
            } else {
                exibirErro( ", begin esperado");
            }
        }
    }

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressao_logica() {
        termo_logico();
        mais_expr_logica();
    }

    // <mais_expr_logica> ::= or <termo_logico> <mais_expr_logica> {A26} | ε
    private void mais_expr_logica() {
        if (palavraReservada(token, "or")) {
            codigo += token.getValor().getTexto();
            token = lexico.nextToken();
            termo_logico();
            mais_expr_logica();
            // {A26}
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termo_logico() {
        fator_logico();
        mais_termo_logico();
    }

    // <mais_termo_logico> ::= and <fator_logico> <mais_termo_logico> {A27} | ε
    private void mais_termo_logico() {
        if (palavraReservada(token, "and")) {
            codigo += token.getValor().getTexto();
            token = lexico.nextToken();
            fator_logico();
            mais_termo_logico();
            // {A27}
        }
    }

    /*
     * <fator_logico> ::= <relacional> |
     * ( <expressao_logica> ) |
     * not <fator_logico> {A28} |
     * true {A29} |
     * false {A30}
     */
    private void fator_logico() {
        int cont = 0;

        if (token.getClasse() == Classe.cId || token.getClasse() == Classe.cInt || token.getClasse() == Classe.cParEsq) {
            relacional();
            cont++;
        }

        if (token.getClasse() == Classe.cParEsq && cont == 0) {
            codigo += "(";
            cont++;
            token = lexico.nextToken();
            expressao_logica();

            if (token.getClasse() == Classe.cParDir) {
                codigo += ")";
                token = lexico.nextToken();
            } else {
                exibirErro(", ) esperado");
            }
        } else if (palavraReservada(token, "not")) {
            cont++;
            codigo += "!";
            token = lexico.nextToken();
            fator_logico();
            // {A28}
        } else if (palavraReservada(token, "true") || palavraReservada(token, "false")) {
            codigo += token.getValor().getTexto();
            cont++;
            token = lexico.nextToken();
            // {A29} e {A30}
        }

        if (cont == 0) {
            exibirErro(", (relacional), (, (not), (true), ou (false) esperado");
        }
    }


    /*
     * <relacional> ::= <expressao> = <expressao> {A31} |
     * <expressao> > <expressao> {A32} |
     * <expressao> >= <expressao> {A33} |
     * <expressao> < <expressao> {A34} |
     * <expressao> <= <expressao> {A35} |
     * <expressao> <> <expressao> {A36}
     */
    private void relacional() {
        expressao();

        switch (token.getClasse()) {
            case cIgual:
                codigo += " = ";
                break;
            case cMaior:
                codigo += " > ";
                break;
            case cMaiorIgual:
                codigo += " >= ";
                break;
            case cMenor:
                codigo += " < ";
                break;
            case cMenorIgual:
                codigo += " <= ";
                break;
            case cDiferente:
                codigo += " != ";
                break;
            default:
                exibirErro("(>), (>=), (<), (<=), (=), ou (<>) esperado");
                return;
        }

        token = lexico.nextToken();
        expressao();
        // {A31}, {A32}, {A33}, {A34}, {A35}, {A36}
    }


    // <expressao> ::= <termo> <mais_expressao>
    private void expressao() {
        termo();
        mais_expressao();
    }
    /*
     * <mais_expressao> ::= + <termo> <mais_expressao> {A37} |
     * - <termo> <mais_expressao> {A38} | ε
     */
    private void mais_expressao() {
        if (token.getClasse() == Classe.cAdicao || token.getClasse() == Classe.cSubtracao) {
            if (token.getClasse() == Classe.cAdicao) {
                codigo += " "+ token.getValor().getTexto() + " ";
            } else if (token.getClasse() == Classe.cSubtracao) {
                codigo += " "+ token.getValor().getTexto() + " ";
            }

            token = lexico.nextToken();
            termo();
            mais_expressao();
            // {A37}
        }
    }


    // <termo> ::= <fator> <mais_termo>
    private void termo() {
        fator();
        mais_termo();
    }
    /*
     * <mais_termo> ::= * <fator> <mais_termo> {A39} |
     * / <fator> <mais_termo> {A40} | ε
     */
    private void mais_termo() {
        if (token.getClasse() == Classe.cMultiplicacao || token.getClasse() == Classe.cDivisao) {
            if (token.getClasse() == Classe.cMultiplicacao) {
                codigo += " && ";
            } else if (token.getClasse() == Classe.cDivisao) {
                codigo += " / ";
            }

            token = lexico.nextToken();
            fator();
            mais_termo();
            // {A39}, {A40}
        }
    }


    // <fator> ::= <id> {A55} | <intnum> {A41} | ( <expressao> )
    private void fator() {
        if (token.getClasse() == Classe.cId) {
            codigo += token.getValor().getTexto();
            token = lexico.nextToken();
            // {A55}
        } else if (token.getClasse() == Classe.cInt) {
            codigo += token.getValor().getInteiro();
            token = lexico.nextToken();
            // {A41}
        } else if (token.getClasse() == Classe.cParEsq) {
            token = lexico.nextToken();
            expressao();

            if (token.getClasse() == Classe.cParDir) {
                token = lexico.nextToken();
            } else {
                exibirErro(", ) esperado");
            }
        } else {
            exibirErro(", (, (id), ou (int) esperado");
        }
    }

    private void exibirErro(String mensagem) {
        throw new RuntimeException("Linha:" + token.getLinha() + ", Coluna" + token.getColuna() + ": " + mensagem);
    }

    private boolean palavraReservada(Token token, String palavra) {
        return token.getClasse() == Classe.cPalavraReservada && token.getValor().getTexto().equals(palavra);
    }
    private Boolean chck_var(String var){
        if (variaveis.contains(var)) {
            return true;
        }
        return false;
    }
    private void gerarCorpo() {
        try {
            fw.write(codigo);
        } catch (IOException e) {
            System.out.println("Erro ao escrever arquivo de saida");
            e.printStackTrace();
        }
    }
    private void gerarVariaveis() {
        codigo += "int " + String.join(", ", variaveis) + ";\n";
    }

    private void gerarParametros(boolean leitura) {
        codigo += (parametros.isEmpty()) ? "\"" : "\", ";

        for (int i = 0; i < parametros.size(); i++) {
            codigo += (leitura) ? "&" + parametros.get(i) : parametros.get(i);

            if (i < parametros.size() - 1) {
                codigo += ", ";
            }
        }

        parametros.clear();
    }

}
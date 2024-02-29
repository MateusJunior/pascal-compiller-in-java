package compiladores.lexico;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexico {

    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna;
    private String[] idArray = {
            "and", "array", "begin", "case", "const", "div", "do", "downto", "else", "end",
            "file", "for", "function", "goto", "if", "in", "label", "mod", "nil", "not",
            "of", "or", "packed", "procedure", "program", "record", "repeat", "set",
            "then", "to", "type", "until", "var", "while", "with", "integer", "program", "read",
            "write","writeln", "for","repeat", "while","if"
    };
    List<String> idList = new ArrayList<>(Arrays.asList(idArray));
    public Lexico(BufferedReader br) {
        this.br = br;
        linha = 1;
        coluna = 0;
        caractere = nextChar();
    }

    public Token nextToken() {
        while (caractere != 65535) {
            if (Character.isLetter(caractere)) {
                return processIdentifier();
            } else if (Character.isDigit(caractere)) {
                return processInteger();
            } else if (Character.isWhitespace(caractere)) {
                processWhitespace();
            } else if (caractere == '.') {
                processDot();
            } else if (caractere == '\'') {
                return processString();
            } else if (caractere == '(' || caractere == ')') {
                return processParentheses();
            } else if (caractere == ',') {
                return processComma();
            } else if (caractere == ';') {
                return processSemicolon();
            } else if (caractere == '+') {
                return processPlus();
            } else if (caractere == '-') {
                return processMinus();
            } else if (caractere == '*') {
                return processMultiply();
            } else if (caractere == '/') {
                return processDivide();
            } else if (caractere == ':') {
                return processAssignOrColon();
            } else if (caractere == '<') {
                return processLessThan();
            } else if (caractere == '>') {
                return processGreaterThan();
            } else if (caractere == '=') {
                return processEqual();
            } else if (caractere == '{') {
                processComment();
            } else{
                processOtherCharacter();
            }
        }
        return createEOFToken();
    }

    private Token processIdentifier() {
        StringBuilder lexema = new StringBuilder();
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        while (Character.isLetterOrDigit(caractere)) {
            lexema.append(caractere);
            caractere = nextChar();
        }

        String identifier = lexema.toString().toLowerCase();

        if (idList.contains(identifier)) {
            token.setClasse(Classe.cPalavraReservada);
        } else {
            token.setClasse(Classe.cId);
        }

        token.setValor(new Valor(identifier));
        return token;
    }

    private Token processInteger() {
        StringBuilder lexema = new StringBuilder();
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        while (Character.isDigit(caractere)) {
            lexema.append(caractere);
            caractere = nextChar();
        }

        token.setClasse(Classe.cInt);
        token.setValor(new Valor(Integer.parseInt(lexema.toString())));
        return token;
    }

    private void processWhitespace() {
        while (Character.isWhitespace(caractere)) {
            if (caractere == '\n') {
                linha++;
                coluna = 0;
            }
            caractere = nextChar();
        }
    }

    private Token processDot() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cPonto);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private void processOtherCharacter() {
        System.out.println("Outra coisa: " + caractere);
        caractere = nextChar();
    }
    private Token processParentheses() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        if (caractere == '(') {
            token.setClasse(Classe.cParEsq);
        } else { // caractere == ')'
            token.setClasse(Classe.cParDir);
        }
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }
    private void processComment() {
        // Ignore characters until '}' is encountered or end of file
        while (caractere != 65535 && caractere != '}') {
            if (caractere == '\n') {
                linha++;
                coluna = 0;
            }
            caractere = nextChar();
        }

        // Skip the closing '}' character if found
        if (caractere == '}') {
            caractere = nextChar();
        } else {
            // Handle error: unterminated comment
            System.out.println("Erro: Comentário não terminado corretamente");
        }
    }
    private Token createEOFToken() {
        Token token = new Token();
        token.setClasse(Classe.cEOF);
        token.setLinha(linha);
        token.setColuna(coluna - 1);
        return token;
    }

    private Token processString() {
        StringBuilder lexema = new StringBuilder();
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        lexema.append(caractere);

        caractere = nextChar();
        while (caractere != 65535 && caractere != '\'') {
            lexema.append(caractere);
            caractere = nextChar();
        }
        if (caractere == '\'') {
            lexema.append(caractere);
            caractere = nextChar();
        } else {
            System.out.println("Erro: String não terminada corretamente");
            return null;
        }

        token.setClasse(Classe.cString);
        token.setValor(new Valor(lexema.toString()));
        return token;
    }
    private Token processComma() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cVirg);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private Token processSemicolon() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cPontoVirg);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private Token processPlus() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cAdicao);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private Token processMinus() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cSubtracao);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private Token processMultiply() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cMultiplicacao);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private Token processDivide() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cDivisao);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }

    private Token processAssignOrColon() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        caractere = nextChar();
        if (caractere == '=') {
            token.setClasse(Classe.cAtribuicao);
            token.setValor(new Valor(":="));
            caractere = nextChar();
        } else {
            token.setClasse(Classe.cDoisPontos);
            token.setValor(new Valor(":"));
        }
        return token;
    }

    private Token processLessThan() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        caractere = nextChar();
        if (caractere == '=') {
            token.setClasse(Classe.cMenorIgual);
            token.setValor(new Valor("<="));
            caractere = nextChar();
        } else if (caractere == '>') {
            token.setClasse(Classe.cDiferente);
            token.setValor(new Valor("<>"));
            caractere = nextChar();
        } else {
            token.setClasse(Classe.cMenor);
            token.setValor(new Valor("<"));
        }

        return token;
    }

    private Token processGreaterThan() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);

        caractere = nextChar();
        if (caractere == '=') {
            token.setClasse(Classe.cMaiorIgual);
            token.setValor(new Valor(">="));
            caractere = nextChar();
        } else {
            token.setClasse(Classe.cMaior);
            token.setValor(new Valor(">"));
        }

        return token;
    }

    private Token processEqual() {
        Token token = new Token();
        token.setLinha(linha);
        token.setColuna(coluna);
        token.setClasse(Classe.cIgual);
        token.setValor(new Valor(Character.toString(caractere)));
        caractere = nextChar();
        return token;
    }


    private char nextChar() {
        try {
            coluna++;
            return (char) br.read();
        } catch (IOException e) {
            return ' ';
        }
    }


}

oie	entao		10		3.1415		xpt421z
em outra linha			depois de tabulacoes
		monte de espaços.

public class App {

	public static void main(String[] args) {

		String nomeArquivo = "teste.pas";
		Path caminhoArquivo = Paths.get(nomeArquivo);
		int numeroEspacosPorTab = 4;
		StringBuilder juntando = new StringBuilder();
		String espacos;
        
		for (int cont = 0; cont < numeroEspacosPorTab; cont++) {
			juntando.append(" ");
		}
		espacos = juntando.toString();

		String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
		conteudo = conteudo.replace("\t", espacos);
		Files.write(caminhoArquivo, conteudo.getBytes(StandardCharsets.UTF_8));
	}
}



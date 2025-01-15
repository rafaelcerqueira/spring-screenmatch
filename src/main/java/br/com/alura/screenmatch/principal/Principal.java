package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.Config;
import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private ConsumoApi consumo = new ConsumoApi();
    private static final String API_KEY = Config.getApiKey();
    private Scanner leitura = new Scanner(System.in);
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu() {
        System.out.println("Digite o nome da Série para busca: ");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

		List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodio> episodiosTemorada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemorada.size(); j++) {
//                System.out.println(episodiosTemorada.get(j).titulo());
//            }
//        }

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("\nTop 5 episódios:");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.temporada(), d))
                ).collect(Collectors.toList());

        int opcao;
        do {
            System.out.println("Deseja filtrar por ano? 0 - Não ### 1 - Sim");
            opcao = leitura.nextInt();
            if (opcao == 1) {
                System.out.println("Filtrar por ano: ");
                var ano = leitura.nextInt();
                leitura.nextLine();
                LocalDate dataBusca = LocalDate.of(ano, 1, 1);

                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                episodios.stream()
                        .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                        .forEach(e -> System.out.println(
                                "Temporada: " + e.getTemporada() +
                                        " Espisódio: " + e.getTitulo() +
                                        " Data de lançamento: " + e.getDataLancamento().format(formatador)
                        ));
            }

        } while (opcao != 0);



    }
}

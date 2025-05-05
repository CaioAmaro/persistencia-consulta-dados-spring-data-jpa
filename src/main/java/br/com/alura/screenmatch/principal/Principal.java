package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConsumoGemini;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=afd46234";
    private SerieRepository serieRepository;
    private List<Serie> serieList = new ArrayList<>();
    Optional<Serie> serieEncontrada;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {

        var opcao = -1;

        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar Series Buscadas
                    4 - Buscar Série por titulo
                    5 - Buscar Série por ator
                    6 - Top 5 Série
                    7 - Buscar por Gênero
                    8 - Filtrar Séries
                    9 - Buscar Episodio por Trecho
                    10 - Top Episodios Por Séries
                    11 - Buscar depois de um ano.
                    
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarPorCategoria();
                    break;
                case 8:
                    filtrarSeries();
                    break;
                case 9:
                    BuscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSeries();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dadosSerie = getDadosSerie();
        Serie serie = new Serie(dadosSerie);
        serieRepository.save(serie);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.print("Escolha um série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                            .map(e -> new Episodio(t.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);

            serieRepository.save(serieEncontrada);

        }else{
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas(){
        serieList = serieRepository.findAll();

        serieList.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo(){
        System.out.print("Escolha um série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieEncontrada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieEncontrada.isPresent()){
            System.out.println("Dados da Série: " + serieEncontrada.get());
        }else{
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriePorAtor(){
        System.out.print("Escolha um série pelo nome: ");
        var nomeAtor = leitura.nextLine();
        System.out.print("Avaliações a partir de qual valor: ");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries onde " + nomeAtor + " Trabalhou");
        seriesEncontradas.forEach(s ->
                System.out.println("Nome: "+ s.getTitulo() + ", Classificação: "+ s.getAvaliacao()));
    }

    private void buscarTop5Series(){
        List<Serie> series = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        series.forEach(s ->
                System.out.println("Nome: "+ s.getTitulo() + ", Classificação: "+ s.getAvaliacao()));
    }

    private void buscarPorCategoria(){
        System.out.print("Digite categoria: ");
        var string = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(string);
        List<Serie> seriesEncontradas = serieRepository.findByGenero(categoria);
        System.out.println("Séria com gênero: " + categoria);
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " " + s.getGenero()));
    }

    private void filtrarSeries(){
        System.out.print("Até quantas temporadas: ");
        int temporadasLimite = leitura.nextInt();
        System.out.print("Avaliações a cima de: ");
        double avaliacao = leitura.nextDouble();
        //Derived Queries
        //List<Serie> series = serieRepository.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(temporadasLimite,avaliacao);
        //JPQL -> JPA
        List<Serie> series = serieRepository.seriesPorTemporadasEAvaliacao(temporadasLimite,avaliacao);
        series.forEach(s-> System.out.println(s.getTitulo() + " " + s.getAvaliacao()));
    }

    private void BuscarEpisodioPorTrecho(){
        System.out.print("Trecho do episodio: ");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void topEpisodiosPorSeries(){
        buscarSeriePorTitulo();
        if(serieEncontrada.isPresent()){
            Serie serie = serieEncontrada.get();
            List<Episodio> topEpisodios = serieRepository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s Avaliação %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao() ));
        }
    }

    private void buscarEpisodiosDepoisDeUmaData(){
        buscarSeriePorTitulo();
        if(serieEncontrada.isPresent()){
            Serie serie = serieEncontrada.get();
            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = serieRepository.episodiosPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }
}
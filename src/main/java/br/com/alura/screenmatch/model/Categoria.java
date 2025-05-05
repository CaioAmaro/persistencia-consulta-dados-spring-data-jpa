package br.com.alura.screenmatch.model;

public enum Categoria {
    ACAO("Action", "Ação"),
    ROMANCE("Romance", "Romance"),
    COMEDIA("Comedy", "Comedia"),
    DRAMA("Drama", "Drama"),
    CRIME("Crime", "Crime");

    private String categoriaOmdb;
    private String cateoriaPortugues;

    Categoria(String categoriaOmdb, String portugues){
        this.categoriaOmdb = categoriaOmdb;
        this.cateoriaPortugues = portugues;
    }

    public static Categoria fromString(String text){
        for(Categoria categoria : Categoria.values()){
            if(categoria.categoriaOmdb.equalsIgnoreCase(text)){
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma Categoria encontrada para a string fornecida: ");
    }

    public static Categoria fromPortugues(String text){
        for(Categoria categoria : Categoria.values()){
            if(categoria.cateoriaPortugues.equalsIgnoreCase(text)){
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma Categoria encontrada para a string fornecida: ");
    }
}

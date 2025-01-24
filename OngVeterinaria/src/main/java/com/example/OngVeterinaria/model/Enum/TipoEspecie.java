package com.example.OngVeterinaria.model.Enum;

public enum TipoEspecie {
    GATO(RacaGato.class),
    CACHORRO(RacaCachorro.class);


    //Este é um wildcard genérico que indica que racaEnum pode ser qualquer classe que estenda (ou seja, seja um subtipo de) Enum<?>.
    // Isso significa que racaEnum pode ser qualquer enum, mas não se sabe qual específico, garantindo flexibilidade para trabalhar com diferentes enum.
    private Class<? extends Enum<?>> racaEnum;


    //Construtor - monta e acha a determinada EnumRaca que for passada pelo o cliente
    TipoEspecie(Class<? extends Enum<?>> racaEnum) {
        this.racaEnum = racaEnum;
    }

    //Metodo Get
    public Class<? extends Enum<?>> getRacaEnum() {
        return racaEnum;
    }


    public enum RacaGato{
        SIAMES,
        PERSA,
        AMERICAN,
        MAINE_COON

    }
    public enum RacaCachorro{
        PUG,
        BULDOGUE,
        SALSICHA,
        PASTOR_ALEMAO,
        POODLE,
        ROTTWEILER,
        LABRADROR,
        PINSCHER,
        GOLDEN_RETRIEVER,
        PITTBULL,
        BULL_TERRIR
    }

}

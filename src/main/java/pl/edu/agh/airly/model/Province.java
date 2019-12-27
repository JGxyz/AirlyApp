package pl.edu.agh.airly.model;

import java.util.EnumSet;

public enum Province {
    MALOPOLSKIE("Małopolskie"),
    SLASKIE("Śląskie"),
    PODKARPACKIE("Podkarpackie"),
    WIELKOPOLSKIE("Wielopolskie"),
    OPOLSKIE("Opolskie"),
    KUJAWSKO_POMORSKIE("Kujawsko-Pomorskie"),
    ZACHODNIO_POMORSKIE("Zachodnio-Pomorskie"),
    WARMINSKO_MAZURSKIE("Warminsko-Mazurskie"),
    POMORSKIE("Pomorskie"),
    PODLASKIE("Podlaskie"),
    LODZKIE("Łódzkie"),
    LUBUSKIE("Lubuskie"),
    DOLNOSLASKIE("Dolnośląskie"),
    LUBELSKIE("Lubelskie"),
    SWIETOKRZYSKIE("Świętokrzyskie"),
    MAZOWIECKIE("Mazowieckie");

    String name;

    Province(String name) {
        this.name = name;
    }

    public static EnumSet<Province> getAll() {
        return EnumSet.allOf(Province.class);
    }
}

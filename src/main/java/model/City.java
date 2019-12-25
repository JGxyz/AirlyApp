package model;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static model.Province.*;

public enum City {
    WARSZAWA(52.237049, 21.017532, "Warszawa", "Warszawa", MAZOWIECKIE),
    KRAKOW(50.062006, 19.940984, "Kraków", "Krakow", MALOPOLSKIE),
    LODZ(51.759445, 19.457216, "Łódź", "Łodz", LODZKIE),
    WROCLAW(51.107883, 17.038538, "Wrocław", "Wroclaw", DOLNOSLASKIE),
    POZNAN(52.409538, 16.931992, "Poznań", "Poznan", WIELKOPOLSKIE),
    GDANSK(54.372158, 18.638306, "Gdańsk", "Gdansk", POMORSKIE),
    SZCZECIN(53.428543, 14.552812, "Szczecin", "Szczecin", ZACHODNIO_POMORSKIE),
    BYDGOSZCZ(53.123482, 18.008438, "Bydgoszcz", "Bydgoszcz", KUJAWSKO_POMORSKIE),
    TORUN(53.013790, 18.598444, "Toruń", "Torun", KUJAWSKO_POMORSKIE),
    BIALYSTOK(53.13333, 23.16433, "Białystok", "Bialystok", PODLASKIE),
    KATOWICE(50.270908, 19.039993, "Katowice", "Katowice", SLASKIE),
    RZESZOW(50.041187, 21.999121, "Rzeszów", "Rzeszow", PODKARPACKIE),
    GORZOW_WIELKOPOLSKI(52.73679, 15.22878, "Gorzów-Wielkopolski", "Gorzow-Wielkopolski", LUBUSKIE),
    ZIELONA_GORA(51.935619, 15.506186, "Zielona-Góra", "Zielona-Gora", LUBUSKIE),
    OLSZTYN(53.770226, 20.490189, "Olsztyn", "Olsztyn", WARMINSKO_MAZURSKIE),
    KIELCE(50.866077, 20.628569, "Kielce", "Kilece", SWIETOKRZYSKIE),
    OPOLE(50.671062, 17.926126, "Opole", "Opole", OPOLSKIE),
    LUBLIN(51.246452, 22.568445, "Lublin", "Lublin", LUBELSKIE);

    private double latitude;
    private double longitude;
    private String name;
    private String nameWithoutPolishSigns;
    private Province province;

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Province getProvince() {
        return province;
    }

    public String getNameWithoutPolishSigns() {return nameWithoutPolishSigns;}

    City(double latitude, double longitude, String name, String nameWithoutPolishSigns, Province province) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.nameWithoutPolishSigns = nameWithoutPolishSigns;
        this.province = province;
    }

    public static Set<City> getByProvince(Province province) {
        return getAll().stream()
                .filter(city -> city.getProvince().equals(province))
                .collect(Collectors.toSet());
    }

    public static EnumSet<City> getAll() {
        return EnumSet.allOf(City.class);
    }

}

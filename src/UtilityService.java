public enum UtilityService {

    HOT_WATER_SUPPLY("гвс"),
    COLD_WATER_SUPPLY("хвс"),
    GAS_SUPPLY("газоснабжение"),
    INTERCOM("домофон"),
    OVERHAUL("капремонт"),
    RENT("квартплата"),
    MUNICIPAL_SOLID_WASTE("тбо"),
    HEAT_SUPPLY("теплоснабжение"),
    POWER_SUPPLY("электроснабжение");

    private final String name;

    UtilityService(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

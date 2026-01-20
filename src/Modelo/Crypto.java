
package Modelo;


public class Crypto {
    
    private final String symbol;
    private final String name;
    private final double price;
    private final double change1h;
    private final double change24h;
    private final double change7d;
    private final double marketCap;
    private final double volume24h;
    private final double circulatingSupply;

    public Crypto(String symbol, String name, double price, double change1h, double change24h, double change7d, double marketCap, double volume24h, double circulatingSupply) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change1h = change1h;
        this.change24h = change24h;
        this.change7d = change7d;
        this.marketCap = marketCap;
        this.volume24h = volume24h;
        this.circulatingSupply = circulatingSupply;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getChange1h() {
        return change1h;
    }

    public double getChange24h() {
        return change24h;
    }

    public double getChange7d() {
        return change7d;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public double getVolume24h() {
        return volume24h;
    }

    public double getCirculatingSupply() {
        return circulatingSupply;
    }
    
    
    
}

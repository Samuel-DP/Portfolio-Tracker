
package Modelo;

public class Stock {
    
    private final String ticker;    
    private final String company;   
    private final double price;     
    private final double change24h; 
    private final double marketCap; 
    

    public Stock(String ticker, String company, double price, double change24h, double marketCap) {
        this.ticker = ticker;
        this.company = company;
        this.price = price;
        this.change24h = change24h;
        this.marketCap = marketCap;      
    }

    public String getTicker() {
        return ticker;
    }

    public String getCompany() {
        return company;
    }

    public double getPrice() {
        return price;
    }

    public double getChange24h() {
        return change24h;
    }

    public double getMarketCap() {
        return marketCap;
    }


}
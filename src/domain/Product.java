package domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product implements EntityInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36)
    private UUID uuid;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price")
    private Float price;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_price")
    private Date datePrice;

    @Column(name = "store")
    private String store;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Price> historicalPrice = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ProductLink> links = new ArrayList<>();

    public Product() {
    }

    public Product(String sku, String name, Float price) {
        this.sku = sku;
        this.name = name;
        this.price = price;
    }

    public Product(UUID uuid, String sku, String name, Float price) {
        this.uuid = uuid;
        this.sku = sku;
        this.name = name;
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        if (this.price != null && this.datePrice != null) {
            Price oldPrice = new Price(this.price, this.datePrice, this.store);
            oldPrice.setProduct(this);
            historicalPrice.add(oldPrice);
        }

        this.price = price;
        this.datePrice = new Date();
    }

    public void updateBestPrice(Float price, String store) {
        if (this.price != null && this.datePrice != null) {
            Price oldPrice = new Price(this.price, this.datePrice, this.store);
            oldPrice.setProduct(this);
            historicalPrice.add(oldPrice);
        }

        this.price = price;
        this.store = store;
        this.datePrice = new Date();
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public List<ProductLink> getLinks() {
        return links;
    }

    public void setLinks(List<ProductLink> links) {
        this.links = links;
    }

    public void addLink(ProductLink link) {
        links.add(link);
        link.setProduct(this);
    }

    public Date getDatePrice() {
        return datePrice;
    }

    public void setDatePrice(Date datePrice) {
        this.datePrice = datePrice;
    }

    public List<Price> getHistoricalPrice() {
        return historicalPrice;
    }

    public void setHistoricalPrice(List<Price> historicalPrice) {
        this.historicalPrice = historicalPrice;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String toString() {
        return "Product{" +
                "UUID='" + uuid.toString() +'\'' +
                ", Sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", store='" + store + '\'' +
                ", datePrice=" + datePrice +
                ", links=" + links +
                ", historicalPrice=" + historicalPrice +
                '}';
    }
}
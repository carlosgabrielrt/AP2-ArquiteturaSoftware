package domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "product")

public class Price implements EntityInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuui", length = 36)
    private UUID uuid;

    @Column(name = "price")
    private Float price;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_price")
    private Date date;

    @ManyToOne
    @JoinColumn(name = "produto_id")
   private Product product;

    public Price() {
    }

 public Price(Float price, Date date) {

        this.price = price;
        this.date = date;
    }

    public Price(UUID uuid, Float price, Date date) {
        this.uuid = uuid;
        this.price = price;
        this.date = date;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String toString() {
        return "Price{" +
                "price=" + price +
                ", date=" + date +
                '}';
    }
}

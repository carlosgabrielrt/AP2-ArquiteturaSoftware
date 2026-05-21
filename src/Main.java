import adapter.DatabaseStorage;
import domain.Product;
import domain.ProductLink;
import service.CrawlerService;

public class Main {
    public static void main(String[] args) {
        DatabaseStorage<Product> productStorage = new DatabaseStorage<>(Product.class);

        boolean hasPs5 = false;
        for (domain.EntityInterface e : productStorage.listAll()) {
            Product p = (Product) e;
            if ("PS5-01".equals(p.getSku())) {
                hasPs5 = true;
                break;
            }
        }

        if (!hasPs5) {
            Product ps5 = new Product("PS5-01", "PlayStation 5", null);
            productStorage.save(ps5);
            System.out.println("Produto PlayStation 5 de teste criado.");
        }





        CrawlerService crawler = new CrawlerService();
        crawler.runCrawler();
        crawler.printProductsJson();
    }
}
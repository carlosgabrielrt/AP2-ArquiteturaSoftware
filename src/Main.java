import domain.Price;
import domain.Product;
import service.PriceService;
import service.ProductService;

void main() {
    ProductService productService = new ProductService();

    Product produto = new Product("SKU", "asas", 2f);
    produto.setPrice(3f);
    produto.setPrice(4f);
    productService.create(produto);

    PriceService priceService = new PriceService();

    priceService.listAll();

    Price price = new Price(25.0f, new Date());

    productService.listAll();

}

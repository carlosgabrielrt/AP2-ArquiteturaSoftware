package service;

import adapter.DatabaseStorage;
import domain.Product;

public class ProductService extends BaseService {
    public ProductService() {
        this.armazenamento = new DatabaseStorage<>(Product.class);
    }
}
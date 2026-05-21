package service;

import adapter.DatabaseStorage;
import domain.Price;

public class PriceService extends BaseService {
    public PriceService() {
        this.armazenamento = new DatabaseStorage<>(Price.class);
    }
}
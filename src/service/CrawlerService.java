package service;

import adapter.DatabaseStorage;
import domain.Product;
import domain.ProductLink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;

public class CrawlerService {

    private final DatabaseStorage<Product> productStorage;

    public CrawlerService() {
        this.productStorage = new DatabaseStorage<>(Product.class);
    }

    public void runCrawler() {
        System.out.println("Iniciando Crawler de Preços...");
        ArrayList<domain.EntityInterface> entities = productStorage.listAll();

        for (domain.EntityInterface entity : entities) {
            Product product = (Product) entity;
            System.out.println("Processando produto: " + product.getName() + " (SKU: " + product.getSku() + ")");

            Float lowestPrice = null;
            String lowestPriceStore = null;

            String nameForUrlKabum = product.getName().toLowerCase().replace(" ", "-");
            String kabumUrl = "https://www.kabum.com.br/busca/" + nameForUrlKabum;

            String nameForUrlMl = product.getName().toLowerCase().replace(" ", "-");
            String mlUrl = "https://lista.mercadolivre.com.br/" + nameForUrlMl;

            String[] stores = {"Kabum", "Mercado Livre"};
            String[] urls = {kabumUrl, mlUrl};

            for (int i = 0; i < stores.length; i++) {
                String storeName = stores[i];
                String url = urls[i];

                System.out.println("  Buscando na loja: " + storeName + " - " + url);
                Float currentPrice = fetchPrice(storeName, url);

                if (currentPrice != null) {
                    System.out.println("    Preço encontrado: R$ " + currentPrice);
                    if (lowestPrice == null || currentPrice < lowestPrice) {
                        lowestPrice = currentPrice;
                        lowestPriceStore = storeName;
                    }
                } else {
                    System.out.println("    Não foi possível obter o preço nesta loja.");
                }
            }

            if (lowestPrice != null) {
                // Atualiza o produto se for diferente
                boolean shouldUpdate = false;
                if (product.getPrice() == null) {
                    shouldUpdate = true;
                } else if (!product.getPrice().equals(lowestPrice) || !lowestPriceStore.equals(product.getStore())) {
                    shouldUpdate = true;
                }

                if (shouldUpdate) {
                    System.out.println("  => Atualizando menor preço: R$ " + lowestPrice + " na loja " + lowestPriceStore);
                    product.updateBestPrice(lowestPrice, lowestPriceStore);
                    productStorage.save(product);
                } else {
                    System.out.println("  => Preço não mudou. Menor preço continua sendo R$ " + product.getPrice() + " na loja " + product.getStore());
                }
            }
        }
        System.out.println("Crawler finalizado.");

        System.out.println("\n--- Resumo dos Menores Preços ---");
        for (domain.EntityInterface entity : entities) {
            Product product = (Product) entity;
            if (product.getPrice() != null) {
                System.out.println(product.getName());
                System.out.printf("Menor preço atual: R$ %.2f\n", product.getPrice());
                System.out.println("Loja: " + (product.getStore() != null ? product.getStore() : "Não informada (Sem links)"));
                System.out.println("---------------------------------");
            }
        }
    }

    private Float fetchPrice(String storeName, String url) {
        try {
            // Adiciona user agent para evitar alguns bloqueios simples
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            String storeNameLower = storeName.toLowerCase();

            if (storeNameLower.contains("kabum")) {
                return parseKabum(doc);
            } else if (storeNameLower.contains("mercado livre") || storeNameLower.contains("mercadolivre")) {
                return parseMercadoLivre(doc);
            } else {
                System.out.println("    [Aviso] Loja não suportada pelo parser: " + storeName);
            }

        } catch (IOException e) {
            System.out.println("    [Erro] Falha ao acessar a URL: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("    [Erro] Falha ao fazer o parsing do preço: " + e.getMessage());
        }
        return null;
    }

    private Float parseKabum(Document doc) {
        // Exemplo de seletor para Kabum busca:
        Element priceElement = doc.selectFirst(".priceCard");
        if (priceElement == null) {
            priceElement = doc.selectFirst("span[class*=priceCard]");
        }
        if (priceElement != null) {
            String text = priceElement.text();
            return parseMoneyText(text);
        }
        return null;
    }

    private Float parseMercadoLivre(Document doc) {
        // Exemplo de seletor Mercado Livre na busca
        Element priceElement = doc.selectFirst(".ui-search-price__second-line .andes-money-amount__fraction");
        if (priceElement == null) {
            priceElement = doc.selectFirst(".andes-money-amount__fraction");
        }
        if (priceElement != null) {
            String text = priceElement.text();
            return parseMoneyText(text);
        }
        return null;
    }

    private Float parseMoneyText(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        // Remove "R$" e espaços
        String cleanText = text.replaceAll("[R$\\s\\u00A0]", "");

        // Formato brasileiro: 1.500,00 -> 1500.00
        cleanText = cleanText.replace(".", "").replace(",", ".");

        try {
            return Float.parseFloat(cleanText);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void printProductsJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<domain.EntityInterface> entities = productStorage.listAll();

            for (domain.EntityInterface entity : entities) {
                Product product = (Product) entity;

                ObjectNode productNode = mapper.createObjectNode();
                productNode.put("nome", product.getName());

                String nameForUrlKabum = product.getName().toLowerCase().replace(" ", "-");
                String kabumUrl = "https://www.kabum.com.br/busca/" + nameForUrlKabum;

                String nameForUrlMl = product.getName().toLowerCase().replace(" ", "-");
                String mlUrl = "https://lista.mercadolivre.com.br/" + nameForUrlMl;

                ArrayNode linksArray = productNode.putArray("links");

                ObjectNode kabumNode = mapper.createObjectNode();
                kabumNode.put("loja", "Kabum");
                kabumNode.put("url", kabumUrl);
                linksArray.add(kabumNode);

                ObjectNode mlNode = mapper.createObjectNode();
                mlNode.put("loja", "Mercado Livre");
                mlNode.put("url", mlUrl);
                linksArray.add(mlNode);

                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(productNode);
                System.out.println(json);
            }
        } catch (Exception e) {
            System.out.println("Erro ao gerar JSON: " + e.getMessage());
        }
    }
}

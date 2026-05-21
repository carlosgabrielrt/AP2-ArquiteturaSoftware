import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TestScraper {
    public static void main(String[] args) throws Exception {
        System.out.println("Testando Kabum...");
        Document docKabum = Jsoup.connect("https://www.kabum.com.br/busca/playstation-5")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();
        System.out.println("Titulo Kabum: " + docKabum.title());
        for (Element e : docKabum.getElementsMatchingOwnText("R\\$")) {
             System.out.println("Possivel preco Kabum: " + e.text() + " - Classes: " + e.className());
             break; // pega apenas o primeiro para ver
        }

        System.out.println("\nTestando Mercado Livre...");
        Document docMl = Jsoup.connect("https://lista.mercadolivre.com.br/playstation-5")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();
        System.out.println("Titulo ML: " + docMl.title());
        for (Element e : docMl.getElementsMatchingOwnText("R\\$")) {
             System.out.println("Possivel preco ML: " + e.text() + " - Classes: " + e.className());
             break; // pega apenas o primeiro
        }
    }
}

package admin;

import model.Produs;

import java.util.List;

import static java.lang.Integer.parseInt;

public class CheckProducts {
    private final List<Produs> listOfProducts;

    public CheckProducts(List<Produs> listOfProducts) {
        this.listOfProducts = listOfProducts;
    }

    public boolean checkProducts() {
        return listOfProducts.stream().anyMatch(currentProduct -> parseInt(currentProduct.getCantitateProdus()) <= 3);
    }
}

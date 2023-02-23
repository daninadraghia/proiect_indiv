package managers;

import model.Produs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProductsManager {
    private final List<Produs> listOfProducts;
    private final static String SELECT_ALL_QUERY = "SELECT * FROM public.\"STOC_PRODUSE\";";
    private final static Logger LOGGER = Logger.getLogger(ProductsManager.class.getName());
    public ProductsManager() {
        listOfProducts = new ArrayList<>();
    }

    public List<Produs> getAllProducts(Connection sqlConnection) {
        try {
            Statement sqlStatement = sqlConnection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(SELECT_ALL_QUERY);
            while (resultSet.next()) {
                listOfProducts.add(new Produs(resultSet.getString("ID_PRODUS").trim(),
                        resultSet.getString("NUME_PRODUS").trim(),resultSet.getString("PRET_PRODUS").trim(),
                        resultSet.getString("CANTITATE_PRODUS").trim()));
            }
        } catch (SQLException | NullPointerException | ClassCastException
                 | IllegalArgumentException | UnsupportedOperationException exceptionObject) {
            LOGGER.severe(exceptionObject.getMessage());
        }
        return listOfProducts;
    }
}

package forms;

import managers.database.ConexiuneDB;
import managers.database.DatabaseManager;
import model.Produs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve", "CommentedOutCode"})
public class FereastraCosCumparaturi extends JFrame implements ActionListener {
    private final List<JButton> listaButoane;
    private final List<Produs> listaProduseComandate;

    private final Map<Produs, Integer> mapOfProducts;

    private final FereastraUtilizatorAutentificat obiectFereastra;
    private JButton butonPlaseazaComanda, butonAnulare, butonStergeProdus;
    private JLabel labelSumaProduse, labelInformativ, labelPrimaParte, labelADouaParte, labelAvertizare;
    private JPanel panouPrincipal;
    private JList<Produs> JListProduseComandate;

    private final static Logger LOGGER = Logger.getLogger(FereastraCosCumparaturi.class.getName());

    public FereastraCosCumparaturi(String titluFereastra, FereastraUtilizatorAutentificat obiectFereastra) {
        super(titluFereastra);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(1400, 580));
        this.setLocationRelativeTo(null);

        listaProduseComandate = new ArrayList<>();
        mapOfProducts = new HashMap<>();
        modificariEsteticeJList();
        listaButoane = creareListaSiAdaugareFunctionalitate(); // creează lista de butoane și aduce fiecărui buton unele modificări și funcționalitate
        this.obiectFereastra = obiectFereastra;
        adaugaProduseInListaSiAfiseaza(); // preia produsele din baza de date și le adaugă în List și JList
        labelAvertizare.setVisible(false);


        this.add(panouPrincipal);
        this.setVisible(true);
    }

    public JLabel getLabelAvertizare() {
        return labelAvertizare;
    }

    public JButton getButonPlaseazaComanda() {
        return butonPlaseazaComanda;
    }

    public JButton getButonAnulare() {
        return butonAnulare;
    }

    public JButton getButonStergeProdus() {
        return butonStergeProdus;
    }

    public Map<Produs, Integer> getProducts() {
        return mapOfProducts;
    }

    private void modificariEsteticeJList() {
        JListProduseComandate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        var defaultListCellRenderer = (DefaultListCellRenderer) JListProduseComandate.getCellRenderer();
        defaultListCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    // TODO: fix
    private void adaugaProduseInListaSiAfiseaza() {
        /*
            try {
                ConexiuneDB conexiuneDB = new ConexiuneDB();
                Statement statement = conexiuneDB.getSQLConnection().createStatement();
                ResultSet resultSet = statement.executeQuery(creareStringSelect());
                if (!resultSet.next()) {
                    afisareCosCumparaturiGol(); // dacă nu avem produse în coșul de afișăm corespunzător situației
                    labelADouaParte.setVisible(false);
                } else {
                    listaProduseComandate.add(creareProdus(resultSet)); // creăm produsul și-l adăugăm în listă
                    while (resultSet.next())
                        listaProduseComandate.add(creareProdus(resultSet)); // și tot adăugăm până când nu mai avem produse de adăugat
                    actualizeazaProduseInCos(); // mai apoi actualizăm produsele în listă ( și le și afișăm )
                    actualizareSoldProduse(); // și actualizăm și soldul
                    this.obiectFereastra.getLabelNrProduseInCos().setText(valueOf(listaProduseComandate.size()));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        */
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.getDatabaseConnection().ifPresentOrElse(sqlConnection -> {
            try {
                Statement sqlStatement = sqlConnection.createStatement();
                ResultSet resultSet = sqlStatement.executeQuery(creareStringSelect());
                if (!resultSet.next()) {
                    afisareCosCumparaturiGol();
                    labelADouaParte.setVisible(false);
                } else {
                    mapOfProducts.put(creareProdus(resultSet), resultSet.getInt("CANTITATE_PRODUS"));
                    while (resultSet.next()) {
                        mapOfProducts.put(creareProdus(resultSet), resultSet.getInt("CANTITATE_PRODUS"));
                    }
                    actualizeazaProduseInCos();
//                    actualizareSoldProduse();
                }
            } catch (SQLException sqlException) {
                LOGGER.severe(sqlException.getMessage());
            }
        }, () -> LOGGER.severe("Can't establish connection to SQL Database!"));
    }

    @Contract("_ -> new")
    @NotNull
    private Produs creareProdus(@NotNull ResultSet resultSet) throws SQLException {
        return new Produs(resultSet.getString("ID_PRODUS").trim(), resultSet.getString("NUME_PRODUS").trim(),
                resultSet.getString("PRET_PRODUS").trim(), resultSet.getString("CANTITATE_PRODUS").trim());
    }

    @Contract(pure = true)
    private @NotNull String creareStringSelect() {
        /* TODO: aici bubuie 100% */
        // ne returneaza selectul folosit pentru a "uni" fiecare Utilizator cu Produsele sale din Coșul de Cumpărături
        return "SELECT T1.\"ID_UTILIZATOR\", T2.\"ID_PRODUS\", T2.\"NUME_PRODUS\", T2.\"PRET_PRODUS\", T1.\"CANTITATE_PRODUS\" " +
                "FROM \"COS_CUMPARATURI\" T1 JOIN \"STOC_PRODUSE\" T2 ON T1.\"ID_PRODUS\" = T2.\"ID_PRODUS\" " +
                "WHERE T1.\"ID_UTILIZATOR\" = " + obiectFereastra.getUtilizatorAutentificat().getIdUtilizator();
    }

    // dacă coșul este gol vom afișa astfel
    private void afisareCosCumparaturiGol() {
        labelInformativ.setText("Momentan nu ai niciun produs în coșul de cumpărături!");
        afisareLabele(labelInformativ, labelPrimaParte, labelSumaProduse, false);
    }

    // după fiecare modificare adusă vom apela funcția aceasta pentru a actualiza produsele din JList
    void actualizeazaProduseInCos() {
        /*
            DefaultListModel<Produs> defaultListModel = new DefaultListModel<>();
            listaProduseComandate.forEach(defaultListModel::addElement);
            JListProduseComandate.setModel(defaultListModel);
            labelInformativ.setText("Produsele din stoc sunt :");
            afisareLabele(labelPrimaParte, labelSumaProduse, labelADouaParte, true);
            actualizareSoldProduse();
        */
        DefaultListModel<Produs> defaultListModel = new DefaultListModel<>();
        mapOfProducts.forEach((currentProduct, productQuantity) -> defaultListModel.addElement(currentProduct));
        JListProduseComandate.setModel(defaultListModel);
        labelInformativ.setText("Produsele din stoc sunt:");
        afisareLabele(labelPrimaParte, labelSumaProduse, labelADouaParte, true);
        actualizareSoldProduse();
    }

    private void afisareLabele(@NotNull JLabel labelPrimaParte, @NotNull JLabel labelSumaProduse, @NotNull JLabel labelADouaParte, boolean aFlag) {
        labelPrimaParte.setVisible(true);
        labelSumaProduse.setVisible(aFlag);
        labelADouaParte.setVisible(aFlag);
    }

    void actualizareSoldProduse() {
        /*
            if (listaProduseComandate.size() > 0) {
                int sumaProduseDinCos = listaProduseComandate.stream()
                        .mapToInt(produsCurent -> Integer.parseInt(produsCurent.getPretProdus()))
                        .sum();
                labelSumaProduse.setText(String.valueOf(sumaProduseDinCos));
            }
        */
        int totalCost = mapOfProducts.keySet().stream().mapToInt(currentProduct -> {
            try {
                int productPrice = parseInt(currentProduct.getPretProdus());
                int productQuantity = mapOfProducts.get(currentProduct);
//                LOGGER.severe("currentProduct -> " + currentProduct);
//                LOGGER.severe("productsQuantity -> " + productQuantity);
                return productPrice * productQuantity;
            } catch (NumberFormatException numberFormatException) {
                LOGGER.severe(numberFormatException.getMessage());
            }
            return 0;
        }).sum();
        labelSumaProduse.setText(valueOf(totalCost));
    }

    public List<Produs> getListaProduseComandate() {
        return listaProduseComandate;
    }

    private @NotNull List<JButton> creareListaSiAdaugareFunctionalitate() {
        List<JButton> listaButoane = new ArrayList<>(List.of(butonPlaseazaComanda, butonAnulare, butonStergeProdus));
        listaButoane.forEach(butonCurent -> {
            butonCurent.addActionListener(this);
            butonCurent.setFocusable(false);
            butonCurent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        });
        return listaButoane;
    }

    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        /*
            if (listaProduseComandate.size() > 0) {
                if (e.getSource().equals(butonPlaseazaComanda)) {
                    if (!(parseInt(obiectFereastra.getUtilizatorAutentificat().getSoldUtilizator()) >= parseInt(labelSumaProduse.getText())))
                        afisareMesajAvertizare("Nu poți plasa comanda deoarece momentan nu ai bani suficienți, soldul tău : " + obiectFereastra.getUtilizatorAutentificat().getSoldUtilizator());
                    else {
                        StringBuilder stringBuilder = creareStringComanda();
                        try {
                            ConexiuneDB conexiuneDB = new ConexiuneDB();
                            Statement sqlStatement = conexiuneDB.getSQLConnection().createStatement();
                            sqlStatement.executeUpdate("INSERT INTO \"ISTORIC_COMENZI\" (\"ID_UTILIZATOR\", \"MESAJ_COMANDA\") VALUES ("
                                    + obiectFereastra.getUtilizatorAutentificat().getIdUtilizator() + ", '" + stringBuilder + "')");
                            listaProduseComandate.forEach(produsCurent -> {
                                try {
                                    if (parseInt(produsCurent.getCantitateProdus()) - 1 >= 0)
                                        sqlStatement.executeUpdate("UPDATE \"STOC_PRODUSE\" SET \"CANTITATE_PRODUS\" = " +
                                                "(SELECT \"CANTITATE_PRODUS\" FROM \"STOC_PRODUSE\" WHERE \"ID_PRODUS\" = "
                                                + produsCurent.getIdProdus() + ") - 1 WHERE \"ID_PRODUS\" = " + produsCurent.getIdProdus());
                                    sqlStatement.executeUpdate("DELETE FROM \"COS_CUMPARATURI\" WHERE \"ID_PRODUS\" = " + produsCurent.getIdProdus());
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            });
                            listaProduseComandate.clear();
                            obiectFereastra.getLabelNrProduseInCos().setText("0");
                            obiectFereastra.getLabelSuma().setText(valueOf(parseInt(obiectFereastra.getLabelSuma().getText()) - parseInt(labelSumaProduse.getText())));
                            obiectFereastra.setSaModificatSoldul(true);
                            actualizeazaProduseInCos();
                            afisareCosCumparaturiGol();
                            labelADouaParte.setVisible(false);
                            afisareMesajAvertizare("Comanda a fost plasata cu succes!");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } else afisareMesajAvertizare("Nu poti plasa comanda deoarece cosul tau de cumparaturi este gol");
            if (e.getSource().equals(butonStergeProdus)) {
                if (listaProduseComandate.size() == 0) // dacă coșul este gol nu mai putem șterge produse
                    afisareMesajAvertizare("Nu mai poți șterge produse din coș, acesta este gol!");
                else if (JListProduseComandate.getSelectedValuesList().size() == 0) // dacă nu am selectat nimic pentru șters
                    afisareMesajAvertizare("Trebuie să selectezi un produs înainte de a-l șterge!");
                else {
                    for (Produs produsPentruSters : JListProduseComandate.getSelectedValuesList()) { // preluăm produsele selectate de către utilizator
                        try {
                            ConexiuneDB conexiuneDB = new ConexiuneDB();
                            Statement sqlStatement = conexiuneDB.getSQLConnection().createStatement();
                            sqlStatement.executeUpdate("DELETE FROM \"COS_CUMPARATURI\" WHERE \"ID_UTILIZATOR\" = " + obiectFereastra.getUtilizatorAutentificat().getIdUtilizator() +
                                    " AND \"ID_PRODUS\" = " + produsPentruSters.getIdProdus()); // ștergem fiecare produs selectat din baza de date
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        listaProduseComandate.removeIf(produsCurent -> produsCurent.getIdProdus().equals(produsPentruSters.getIdProdus())); // în același timp le ștergem și din listă
                        obiectFereastra.getLabelNrProduseInCos().setText(valueOf(listaProduseComandate.size()));
                    }
                    if (listaProduseComandate.size() == 0) {
                        actualizeazaProduseInCos(); // ștergem produsele din JList
                        afisareCosCumparaturiGol(); // apelăm această funcție de afișare deoarece coșul nostru de cumpărături este acuma gol !
                        labelADouaParte.setVisible(false);
                    } else {
                        actualizeazaProduseInCos(); // ștergem produsele selectate
                        actualizareSoldProduse(); // acutalizăm prețul ( prețul total - suma preturilor produselor șterge ... )
                    }

                }
            }
            if (e.getSource().equals(butonAnulare)) {
                this.setVisible(false);
                labelAvertizare.setVisible(false);
                obiectFereastra.setVisible(true);
            }
        */
        if (e.getSource().equals(butonAnulare)) {
            this.setVisible(false);
            labelAvertizare.setVisible(false);
            obiectFereastra.setVisible(true);
        }
        String userId = obiectFereastra.getUtilizatorAutentificat().getIdUtilizator();
        if (e.getSource().equals(butonStergeProdus)) {
            if (mapOfProducts.size() == 0) {
                afisareMesajAvertizare("Nu poți șterge produse din coș, acesta este gol!");
            }
            List<Produs> selectedProducts = JListProduseComandate.getSelectedValuesList();
            if (selectedProducts.size() == 0) {
                afisareMesajAvertizare("Trebuie să selectezi un produs înainte de a-l șterge!");
            } else {
                selectedProducts.forEach(currentProduct -> {
                    DatabaseManager databaseManager = new DatabaseManager();
                    databaseManager.getDatabaseConnection().ifPresentOrElse(sqlConnection -> {
                        try {
                            Statement sqlStatement = sqlConnection.createStatement();
                            String productId = currentProduct.getIdProdus();
                            sqlStatement.executeUpdate("DELETE FROM public.\"COS_CUMPARATURI\" WHERE " +
                                    "\"ID_PRODUS\" = " + productId + " AND \"ID_UTILIZATOR\" = " + userId + ";");
                            mapOfProducts.remove(currentProduct);
                            actualizeazaProduseInCos();
                            obiectFereastra.updateCartQuantity();
                        } catch (SQLException sqlException) {
                            LOGGER.severe(sqlException.getMessage());
                        }

                    }, () -> LOGGER.severe("Can't connect to Database!"));
                });
            }
        }
        if (e.getSource().equals(butonPlaseazaComanda)) {
            int userMoney = parseInt(obiectFereastra.getUtilizatorAutentificat().getSoldUtilizator());
            int totalCost = parseInt(labelSumaProduse.getText());
            if (userMoney < totalCost) {
                afisareMesajAvertizare("Nu poți plasa comanda deoarece momentan nu ai bani suficienți, soldul tău : " + userMoney);
            } else {
                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.getDatabaseConnection().ifPresentOrElse(sqlConnection -> {
                    try {
                        Statement sqlStatement = sqlConnection.createStatement();
                        StringBuilder stringBuilder = new StringBuilder();
                        mapOfProducts.forEach((currentProduct, productQuantity) -> {
                            String productId = currentProduct.getIdProdus();
                            try {
                                sqlStatement.executeUpdate("DELETE FROM public.\"COS_CUMPARATURI\" WHERE \"ID_PRODUS\" = " +
                                        productId + " AND \"ID_UTILIZATOR\" = " + userId + ";");
                                sqlStatement.executeUpdate("UPDATE public.\"UTILIZATORI\" SET \"SOLD_UTILIZATOR\" = \"SOLD_UTILIZATOR\" - " +
                                        totalCost + ";");
                                // TODO: check if there are enough products in stock ( sau cand adaugi in cos ) !
                                sqlStatement.executeUpdate("UPDATE public.\"STOC_PRODUSE\" SET \"CANTITATE_PRODUS\" = \"CANTITATE_PRODUS\" - " +
                                        productQuantity + " WHERE \"ID_PRODUS\" = " + productId + ";");
                                stringBuilder.append(currentProduct).append("\n");
                            } catch (SQLException sqlException) {
                               LOGGER.severe(sqlException.getMessage());
                            }
                        });
                        sqlStatement.executeUpdate("INSERT INTO public.\"ISTORIC_COMENZI\" (\"MESAJ_COMANDA\", \"ID_UTILIZATOR\") VALUES ('" +
                                stringBuilder.substring(stringBuilder.indexOf(".") + 1).trim() + "', " + userId + ");");
                    } catch (SQLException sqlException) {
                        LOGGER.severe(sqlException.getMessage());
                    }
                    mapOfProducts.clear();
                }, () -> LOGGER.severe("Can't connect to Database!"));
                JLabel userMoneyLabel = obiectFereastra.getLabelSuma();
                userMoneyLabel.setText(valueOf(parseInt(userMoneyLabel.getText()) - totalCost));
                obiectFereastra.getLabelNrProduseInCos().setText("0");
                obiectFereastra.setSaModificatSoldul(true);
                obiectFereastra.getUtilizatorAutentificat().setSoldUtilizator(userMoneyLabel.getText());
                actualizeazaProduseInCos();
                afisareCosCumparaturiGol();
                labelADouaParte.setVisible(false);
            }
        }
    }

    @NotNull
    private StringBuilder creareStringComanda() {
        StringBuilder stringBuilder = new StringBuilder();
        listaProduseComandate.forEach(produscurent -> stringBuilder.append("\t").append(" ").append(produscurent).append("\n"));
        stringBuilder.append("In valoare de ").append(labelSumaProduse.getText()).append(" RON\n");
        return stringBuilder;
    }

    private void afisareMesajAvertizare(String continutMesaj) {
        labelAvertizare.setText(continutMesaj);
        if (!labelAvertizare.isVisible()) labelAvertizare.setVisible(true);
    }

}

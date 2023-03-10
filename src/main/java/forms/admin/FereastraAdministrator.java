package forms.admin;

import admin.CheckProducts;
import managers.database.DatabaseManager;
import forms.FereastraPrincipala;
import model.Produs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import managers.ProductsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.*;

/**
 * Clasa administrator din care putem naviga spre {@link FereastraAdaugaProdus}, {@link FereastraModificaProdus} sau {@link FereastraStergeProdus}
 *
 * @author Mercescu Ionut
 */
public class FereastraAdministrator extends JFrame implements ActionListener {
    private JButton butonDeconectare, butonIesire, butonAdaugaProdus, butonStergeProdus, butonModificaProdus;
    private JPanel panouPrincipal;

    private final List<JButton> listaButoane;

    /**
     * Constructorul clasei
     *
     * @param titluFereastra titlul ferestrei
     */
    public FereastraAdministrator(String titluFereastra) {
        super(titluFereastra);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 400));
        this.setLocationRelativeTo(null);
        listaButoane = creareListaButoane();
        modificareButoaneDinLista(listaButoane);

        this.add(panouPrincipal);
        this.setVisible(true);

        if (this.isVisible()) verificaProduse();
    }

    private void verificaProduse() {
        DatabaseManager databaseManager = new DatabaseManager();
        ProductsManager productsManager = new ProductsManager();
        databaseManager.getDatabaseConnection().ifPresent(sqlConnection -> {
            List<Produs> allProducts = productsManager.getAllProducts(sqlConnection);
            CheckProducts checkProducts = new CheckProducts(allProducts);
            if (checkProducts.checkProducts()) {
                StringBuilder stringBuilder = new StringBuilder("Urmatoarele produse au cantitatea <= 3:\n");
                allProducts.stream()
                        .filter(currentProduct -> Integer.parseInt(currentProduct.getCantitateProdus()) <= 3)
                        .map(Produs::getNumeProdus)
                        .forEach(numeCurent -> stringBuilder.append("\t").append(numeCurent).append("\n"));
                showMessageDialog(panouPrincipal, stringBuilder.toString(), "Avertizare - Stoc Insuficient", WARNING_MESSAGE);
            }
        });
    }

    /**
     * Metod?? ce se ocup?? de ad??ugarea func??ionalit????ii ??i a unor elemente de design butoanelor din list??
     *
     * @param listaButoane lista de butoane c??rora le aducem modific??rile
     */
    private void modificareButoaneDinLista(@NotNull List<JButton> listaButoane) {
        listaButoane.forEach(butonCurent -> {
            butonCurent.setBorderPainted(false);
            butonCurent.setFocusable(false);
            butonCurent.addActionListener(this);
            butonCurent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        });
    }

    /**
     * Metod?? ce ne creaz?? list?? cu butoanele prezente ??n clas??
     *
     * @return lista cu butoane
     */
    @Contract(" -> new")
    private @NotNull List<JButton> creareListaButoane() {
        return new ArrayList<>(List.of(butonDeconectare, butonIesire, butonAdaugaProdus, butonStergeProdus, butonModificaProdus));
    }

    /**
     * ??n func??ie de butonul pe care-l alegem se pot ??nt??mpla urm??toarele:
     * <pre>
     *     1. Dac?? alegem {@link FereastraAdministrator#butonAdaugaProdus} ni se va deschide fereastra de tipul {@link FereastraAdaugaProdus}
     *     2. Dac?? alegem {@link FereastraAdministrator#butonModificaProdus} ni se va deschide fereastra de tipul {@link FereastraModificaProdus}
     *     3. Dac?? alegem {@link FereastraAdministrator#butonStergeProdus} ni se va deschide fereastra de tipul {@link FereastraStergeProdus}
     *     4. Dac?? alegem {@link FereastraAdministrator#butonDeconectare} ne vom ??ntoarce ??napoi la fereastra principal??
     *     5. Dac?? alegem {@link FereastraAdministrator#butonIesire} vom ie??i din aplica??ie
     * </pre>
     *
     * @param e eventul primit ??n urma select??rii unui buton
     */
    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        if (e.getSource().equals(butonAdaugaProdus)) {
            FereastraAdaugaProdus fereastraAdaugaProdus = new FereastraAdaugaProdus("Aplicatie Gestionare Stocuri - Administrator", this);
            this.setVisible(false);
        }
        if (e.getSource().equals(butonModificaProdus)) {
            FereastraModificaProdus fereastraModificaProdus = new FereastraModificaProdus("Aplicatie Gestionare Stocuri - Administrator", this);
            this.setVisible(false);
        }
        if (e.getSource().equals(butonStergeProdus)) {
            FereastraStergeProdus fereastraStergeProdus = new FereastraStergeProdus("Aplicatie Gestionare Stocuri - Administrator", this);
            this.setVisible(false);
        }
        if (e.getSource().equals(butonDeconectare)) {
            this.dispose();
            FereastraPrincipala fereastraPrincipala = new FereastraPrincipala("Aplicatie Gestionare Stocuri - BETA");
        }
        if (e.getSource().equals(butonIesire)) this.dispose();
    }

    /**
     * Getter pentru {@link FereastraAdministrator#butonDeconectare}
     *
     * @return butonul de deconectare
     */
    public JButton getButonDeconectare() {
        return butonDeconectare;
    }

    /**
     * Getter pentru {@link FereastraAdministrator#butonIesire}
     *
     * @return butonul de iesire
     */
    public JButton getButonIesire() {
        return butonIesire;
    }

    /**
     * Getter pentru {@link FereastraAdministrator#listaButoane}
     *
     * @return lista de butoane
     */
    public List<JButton> getListaButoane() {
        return listaButoane;
    }

}

package modele;

import java.math.BigDecimal;

public class Benefice {

    private long nombrePrets;
    private BigDecimal totalPrete;
    private BigDecimal beneficeTotal;


    public Benefice() {
    }


    public long getNombrePrets() {
        return nombrePrets;
    }

    public void setNombrePrets(long nombrePrets) {
        this.nombrePrets = nombrePrets;
    }


    public BigDecimal getTotalPrete() {
        return totalPrete;
    }

    public void setTotalPrete(BigDecimal totalPrete) {
        this.totalPrete = totalPrete;
    }


    public BigDecimal getBeneficeTotal() {
        return beneficeTotal;
    }

    public void setBeneficeTotal(BigDecimal beneficeTotal) {
        this.beneficeTotal = beneficeTotal;
    }


    @Override
    public String toString() {

        return "Nombre prêts : " + nombrePrets +
               " | Total prêté : " + totalPrete +
               " Ar | Bénéfice banque : " + beneficeTotal + " Ar";
    }
}
package pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import modele.Client;
import modele.Virement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Génère l'avis de virement en PDF, dans le format demandé par le sujet :
 *
 *   Nom du Banque                      Date : 23/04/2023
 *              AVIS DE VIREMENT N°005
 *   N° de compte : 200 543                 A          N° de compte : 202 908
 *   RAKOTO Bernard                                     RANDRIA Barthelemy
 *   Solde actuel : 15.000.000 Ar
 *                       Montant : 2.000.000 Ar
 */
public class AvisVirementPDF {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat FORMAT_MONTANT = new DecimalFormat("#,##0.00");

    /**
     * Construit le PDF et l'enregistre à l'emplacement indiqué.
     *
     * @param virement     le virement déjà exécuté (soldes déjà à jour en base)
     * @param envoyeur     le client envoyeur (avec son solde APRÈS le virement)
     * @param beneficiaire le client bénéficiaire
     * @param cheminFichier chemin complet du fichier .pdf à créer
     */
    public static void genererAvis(Virement virement, Client envoyeur, Client beneficiaire, String cheminFichier)
            throws IOException {

        Document document = new Document(PageSize.A5);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            Font policeTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font policeSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font policeNormale = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font policeGras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // En-tête : nom de la banque + date
            PdfPTable enTete = new PdfPTable(2);
            enTete.setWidthPercentage(100);

            PdfPCell celluleBanque = new PdfPCell(new Paragraph("Banque BankSys", policeTitre));
            celluleBanque.setBorder(0);
            enTete.addCell(celluleBanque);

            PdfPCell celluleDate = new PdfPCell(new Paragraph(
                    "Date : " + virement.getDateTransfert().format(FORMAT_DATE), policeNormale));
            celluleDate.setBorder(0);
            celluleDate.setHorizontalAlignment(Element.ALIGN_RIGHT);
            enTete.addCell(celluleDate);

            document.add(enTete);

            // Titre centré
            Paragraph titre = new Paragraph("AVIS DE VIREMENT N°" + virement.getNumVirement(), policeSousTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingBefore(10);
            titre.setSpacingAfter(20);
            document.add(titre);

            // Bloc envoyeur / bénéficiaire, avec "A" au centre
            PdfPTable blocComptes = new PdfPTable(new float[]{5, 1, 5});
            blocComptes.setWidthPercentage(100);

            PdfPCell celluleEnvoyeur = new PdfPCell();
            celluleEnvoyeur.setBorder(0);
            celluleEnvoyeur.addElement(new Paragraph("N° de compte : " + virement.getNumCompteEnvoyeur(), policeGras));
            celluleEnvoyeur.addElement(new Paragraph(envoyeur.getNom() + " " + nvl(envoyeur.getPrenoms()), policeNormale));
            celluleEnvoyeur.addElement(new Paragraph(
                    "Solde actuel : " + FORMAT_MONTANT.format(envoyeur.getSoldeActuel()) + " Ar", policeNormale));
            blocComptes.addCell(celluleEnvoyeur);

            PdfPCell celluleFleche = new PdfPCell(new Paragraph("A", policeGras));
            celluleFleche.setBorder(0);
            celluleFleche.setHorizontalAlignment(Element.ALIGN_CENTER);
            celluleFleche.setVerticalAlignment(Element.ALIGN_MIDDLE);
            blocComptes.addCell(celluleFleche);

            PdfPCell celluleBeneficiaire = new PdfPCell();
            celluleBeneficiaire.setBorder(0);
            celluleBeneficiaire.addElement(new Paragraph("N° de compte : " + virement.getNumCompteBeneficiaire(), policeGras));
            celluleBeneficiaire.addElement(new Paragraph(beneficiaire.getNom() + " " + nvl(beneficiaire.getPrenoms()), policeNormale));
            blocComptes.addCell(celluleBeneficiaire);

            document.add(blocComptes);

            // Montant
            Paragraph montant = new Paragraph(
                    "Montant : " + FORMAT_MONTANT.format(virement.getMontant()) + " Ar", policeSousTitre);
            montant.setAlignment(Element.ALIGN_CENTER);
            montant.setSpacingBefore(25);
            document.add(montant);

        } catch (com.lowagie.text.DocumentException e) {
            throw new IOException("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    private static String nvl(String texte) {
        return texte == null ? "" : texte;
    }
}

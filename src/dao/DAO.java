package dao;

import java.util.List;

/**
 * Interface générique définissant les opérations CRUD de base.
 * Chaque DAO concret (ClientDAO, VirementDAO, PretDAO, RenduDAO)
 * implémente cette interface avec du JDBC pur.
 *
 * T = le type de l'objet métier (Client, Virement, Pret, Rendu)
 * ID = le type de la clé primaire (toujours String dans ce projet)
 */
public interface DAO<T, ID> {

    boolean ajouter(T objet);

    boolean modifier(T objet);

    boolean supprimer(ID id);

    T rechercherParId(ID id);

    List<T> listerTous();
}

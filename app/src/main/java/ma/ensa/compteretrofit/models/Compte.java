package ma.ensa.compteretrofit.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "compte")
public class Compte {

    @JacksonXmlProperty(localName = "id")
    private Long id;

    @JacksonXmlProperty(localName = "solde")
    private double solde;

    @JacksonXmlProperty(localName = "date")
    private String dateCreation;

    @JacksonXmlProperty(localName = "type")
    private String type;

    // Getters et setters

    public Long getId() {
        return id;
    }

    public Compte(Long id, double solde, String dateCreation, String type) {
        this.id = id;
        this.solde = solde;
        this.dateCreation = dateCreation;
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

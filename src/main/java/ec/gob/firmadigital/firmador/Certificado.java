/**
 *
 * @author jdc
 */
package ec.gob.firmadigital.firmador;

import java.util.Date;

public class Certificado {
    private String issuedTo;
    private String issuedBy;
    private Date validFrom;
    private Date validTo;
    private Date generated;
    private Boolean validated;

    public Certificado() {
    }

    public Certificado(String issuedTo, String issuedBy, Date validFrom, Date validTo, Date generated, Boolean validated) {
        this.issuedTo = issuedTo;
        this.issuedBy = issuedBy;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.generated = generated;
        this.validated = validated;
    }

    public String getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(String issuedTo) {
        this.issuedTo = issuedTo;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Date getGenerated() {
        return generated;
    }

    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }    
}

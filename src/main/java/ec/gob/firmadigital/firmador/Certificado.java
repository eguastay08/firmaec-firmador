/**
 *
 * @author jdc
 */
package ec.gob.firmadigital.firmador;

import java.util.Calendar;

public class Certificado {
    private String issuedTo;
    private String issuedBy;
    private Calendar validFrom;
    private Calendar validTo;
    private Calendar generated;
    private Boolean validated;
    private Boolean revocated;

    public Certificado() {
    }

    public Certificado(String issuedTo, String issuedBy, Calendar validFrom, Calendar validTo, Calendar generated, Boolean validated, Boolean revocated) {
        this.issuedTo = issuedTo;
        this.issuedBy = issuedBy;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.generated = generated;
        this.validated = validated;
        this.revocated = revocated;
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

    public Calendar getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Calendar validFrom) {
        this.validFrom = validFrom;
    }

    public Calendar getValidTo() {
        return validTo;
    }

    public void setValidTo(Calendar validTo) {
        this.validTo = validTo;
    }

    public Calendar getGenerated() {
        return generated;
    }

    public void setGenerated(Calendar generated) {
        this.generated = generated;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

	public Boolean getRevocated() {
		return revocated;
	}

	public void setRevocated(Boolean revocated) {
		this.revocated = revocated;
	}    
}

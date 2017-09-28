/*
 * Copyright (C) 2017 FirmaEC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ec.gob.firmadigital.utils;

import ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException;

import io.rubrica.certificate.ec.CertificadoFuncionarioPublico;
import io.rubrica.certificate.ec.CertificadoMiembroEmpresa;
import io.rubrica.certificate.ec.CertificadoPersonaJuridica;
import io.rubrica.certificate.ec.CertificadoPersonaNatural;
import io.rubrica.certificate.ec.CertificadoRepresentanteLegal;
import io.rubrica.certificate.ec.bce.BceSubCert;
import io.rubrica.certificate.ec.bce.CertificadoBancoCentral;
import io.rubrica.certificate.ec.bce.CertificadoBancoCentralFactory;
import io.rubrica.certificate.ec.cj.CertificadoConsejoJudicatura;
import io.rubrica.certificate.ec.cj.CertificadoConsejoJudicaturaDataFactory;
import io.rubrica.certificate.ec.cj.CertificadoDepartamentoEmpresaConsejoJudicatura;
import io.rubrica.certificate.ec.cj.CertificadoEmpresaConsejoJudicatura;
import io.rubrica.certificate.ec.cj.CertificadoMiembroEmpresaConsejoJudicatura;
import io.rubrica.certificate.ec.cj.CertificadoPersonaJuridicaPrivadaConsejoJudicatura;
import io.rubrica.certificate.ec.cj.CertificadoPersonaJuridicaPublicaConsejoJudicatura;
import io.rubrica.certificate.ec.cj.CertificadoPersonaNaturalConsejoJudicatura;
import io.rubrica.certificate.ec.cj.ConsejoJudicaturaSubCert;
import io.rubrica.certificate.ec.securitydata.CertificadoSecurityData;
import io.rubrica.certificate.ec.securitydata.CertificadoSecurityDataFactory;
import io.rubrica.certificate.ec.securitydata.SecurityDataSubCaCert;
import io.rubrica.sign.cms.DatosUsuario;
import java.security.cert.X509Certificate;

/**
 *
 * @author FirmaEC
 */
public class CertificadoEcUtils {
    public static X509Certificate getRootCertificate(X509Certificate cert) throws EntidadCertificadoraNoValidaException{
        String entidadCertStr = getNombreCA(cert);
        switch(entidadCertStr){
            case "Banco Central del Ecuador":
                return new BceSubCert();
            case "Consejo de la Judicatura":
                return new ConsejoJudicaturaSubCert();
            case "SecurityData":
                return new SecurityDataSubCaCert();
            default:
                throw new EntidadCertificadoraNoValidaException("Entidad Certificador no reconocida");
        }
    }

    //TODO poner los nombres como constantes
    public static String getNombreCA(X509Certificate certificado) {
        if (CertificadoBancoCentralFactory.esCertificadoDelBancoCentral(certificado)) {
            return "Banco Central del Ecuador";
        }

        if (CertificadoConsejoJudicaturaDataFactory.esCertificadoDelConsejoJudicatura(certificado)) {
            return "Consejo de la Judicatura";
        }

        if (CertificadoSecurityDataFactory.esCertificadoDeSecurityData(certificado)) {
            return "SecurityData";
        }

        /**
         * ****************************************
         * TODO comentar esto para produccion
         */
        if (CertificadoBancoCentralFactory.estTestCa(certificado)) {
            return "Banco Central del Ecuador Test, de prueba DEFINITIVAMENTE NO OFICIAL!!!!!!!!!!";
        }
        /**
         * Fin de cambio para produccion
         */
        return "Entidad no reconocidad: " + certificado.getIssuerDN().getName();
    }
    
    
    //TODO poner los nombres como constantes
    public static DatosUsuario getDatosUsuarios(X509Certificate certificado) {
        if (CertificadoBancoCentralFactory.esCertificadoDelBancoCentral(certificado)) {
            DatosUsuario datosUsuario = new DatosUsuario();
            
            CertificadoBancoCentral certificadoBancoCentral = CertificadoBancoCentralFactory.construir(certificado);
            if (certificadoBancoCentral instanceof CertificadoFuncionarioPublico) {
                CertificadoFuncionarioPublico certificadoFuncionarioPublico = (CertificadoFuncionarioPublico) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoFuncionarioPublico.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoFuncionarioPublico.getNombres());
                datosUsuario.setApellido(certificadoFuncionarioPublico.getPrimerApellido() + " "
                        + certificadoFuncionarioPublico.getSegundoApellido());
                datosUsuario.setInstitucion(certificadoFuncionarioPublico.getInstitucion());
                datosUsuario.setCargo(certificadoFuncionarioPublico.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoMiembroEmpresa) {
                CertificadoMiembroEmpresa certificadoMiembroEmpresa = (CertificadoMiembroEmpresa) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoMiembroEmpresa.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoMiembroEmpresa.getNombres());
                datosUsuario.setApellido(certificadoMiembroEmpresa.getPrimerApellido() + " "
                        + certificadoMiembroEmpresa.getSegundoApellido());
                datosUsuario.setCargo(certificadoMiembroEmpresa.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoPersonaJuridica) {
                CertificadoPersonaJuridica certificadoPersonaJuridica = (CertificadoPersonaJuridica) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoPersonaJuridica.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaJuridica.getNombres());
                datosUsuario.setApellido(certificadoPersonaJuridica.getPrimerApellido() + " "
                        + certificadoPersonaJuridica.getSegundoApellido());
                datosUsuario.setCargo(certificadoPersonaJuridica.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoPersonaNatural) {
                CertificadoPersonaNatural certificadoPersonaNatural = (CertificadoPersonaNatural) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoPersonaNatural.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaNatural.getNombres());
                datosUsuario.setApellido(certificadoPersonaNatural.getPrimerApellido() + " "
                        + certificadoPersonaNatural.getSegundoApellido());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoRepresentanteLegal) {
                CertificadoRepresentanteLegal certificadoRepresentanteLegal = (CertificadoRepresentanteLegal) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoRepresentanteLegal.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoRepresentanteLegal.getNombres());
                datosUsuario.setApellido(certificadoRepresentanteLegal.getPrimerApellido() + " "
                        + certificadoRepresentanteLegal.getSegundoApellido());
                datosUsuario.setCargo(certificadoRepresentanteLegal.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            return datosUsuario;
        }

        if (CertificadoConsejoJudicaturaDataFactory.esCertificadoDelConsejoJudicatura(certificado)) {
            DatosUsuario datosUsuario = new DatosUsuario();
            CertificadoConsejoJudicatura certificadoConsejoJudicatura;
            certificadoConsejoJudicatura = CertificadoConsejoJudicaturaDataFactory.construir(certificado);
            
            if (certificadoConsejoJudicatura instanceof CertificadoDepartamentoEmpresaConsejoJudicatura) {
                CertificadoDepartamentoEmpresaConsejoJudicatura certificadoDepartamentoEmpresaConsejoJudicatura;
                certificadoDepartamentoEmpresaConsejoJudicatura = (CertificadoDepartamentoEmpresaConsejoJudicatura) certificadoConsejoJudicatura;
                
                datosUsuario.setCedula(certificadoDepartamentoEmpresaConsejoJudicatura.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoDepartamentoEmpresaConsejoJudicatura.getNombres());
                datosUsuario.setApellido(certificadoDepartamentoEmpresaConsejoJudicatura.getPrimerApellido() + " "
                        + certificadoDepartamentoEmpresaConsejoJudicatura.getSegundoApellido());
                datosUsuario.setCargo(certificadoDepartamentoEmpresaConsejoJudicatura.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoConsejoJudicatura instanceof CertificadoEmpresaConsejoJudicatura) {
                CertificadoEmpresaConsejoJudicatura certificadoEmpresaConsejoJudicatura = (CertificadoEmpresaConsejoJudicatura) certificadoConsejoJudicatura;
                datosUsuario.setCedula(certificadoEmpresaConsejoJudicatura.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoEmpresaConsejoJudicatura.getNombres());
                datosUsuario.setApellido(certificadoEmpresaConsejoJudicatura.getPrimerApellido() + " "
                        + certificadoEmpresaConsejoJudicatura.getSegundoApellido());
                datosUsuario.setCargo(certificadoEmpresaConsejoJudicatura.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            
            if (certificadoConsejoJudicatura instanceof CertificadoMiembroEmpresaConsejoJudicatura) {
                CertificadoMiembroEmpresaConsejoJudicatura certificadoMiembroEmpresaConsejoJudicatura = (CertificadoMiembroEmpresaConsejoJudicatura) certificadoConsejoJudicatura;
                datosUsuario.setCedula(certificadoMiembroEmpresaConsejoJudicatura.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoMiembroEmpresaConsejoJudicatura.getNombres());
                datosUsuario.setApellido(certificadoMiembroEmpresaConsejoJudicatura.getPrimerApellido() + " "
                        + certificadoMiembroEmpresaConsejoJudicatura.getSegundoApellido());
                datosUsuario.setCargo(certificadoMiembroEmpresaConsejoJudicatura.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoConsejoJudicatura instanceof CertificadoPersonaJuridicaPrivadaConsejoJudicatura) {
                CertificadoPersonaJuridicaPrivadaConsejoJudicatura certificadoPersonaJuridicaPrivadaConsejoJudicatura = (CertificadoPersonaJuridicaPrivadaConsejoJudicatura) certificadoConsejoJudicatura;
                datosUsuario.setCedula(certificadoPersonaJuridicaPrivadaConsejoJudicatura.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaJuridicaPrivadaConsejoJudicatura.getNombres());
                datosUsuario.setApellido(certificadoPersonaJuridicaPrivadaConsejoJudicatura.getPrimerApellido() + " "
                        + certificadoPersonaJuridicaPrivadaConsejoJudicatura.getSegundoApellido());
                datosUsuario.setCargo(datosUsuario.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoConsejoJudicatura instanceof CertificadoPersonaJuridicaPublicaConsejoJudicatura) {
                CertificadoPersonaJuridicaPublicaConsejoJudicatura certificadoPersonaJuridicaPublicaConsejoJudicatura = (CertificadoPersonaJuridicaPublicaConsejoJudicatura) certificadoConsejoJudicatura;
                datosUsuario.setCedula(certificadoPersonaJuridicaPublicaConsejoJudicatura.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaJuridicaPublicaConsejoJudicatura.getNombres());
                datosUsuario.setApellido(certificadoPersonaJuridicaPublicaConsejoJudicatura.getPrimerApellido() + " "
                        + certificadoPersonaJuridicaPublicaConsejoJudicatura.getSegundoApellido());
                datosUsuario.setCargo(certificadoPersonaJuridicaPublicaConsejoJudicatura.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoConsejoJudicatura instanceof CertificadoPersonaNaturalConsejoJudicatura) {
                CertificadoPersonaNaturalConsejoJudicatura certificadoPersonaNaturalConsejoJudicatura = (CertificadoPersonaNaturalConsejoJudicatura) certificadoConsejoJudicatura;
                datosUsuario.setCedula(certificadoPersonaNaturalConsejoJudicatura.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaNaturalConsejoJudicatura.getNombres());
                datosUsuario.setApellido(certificadoPersonaNaturalConsejoJudicatura.getPrimerApellido() + " "
                        + certificadoPersonaNaturalConsejoJudicatura.getSegundoApellido());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            return datosUsuario;
        }

        if (CertificadoSecurityDataFactory.esCertificadoDeSecurityData(certificado)) {
            System.out.println("Certificados es SecurityData");
            DatosUsuario datosUsuario = new DatosUsuario();
            CertificadoSecurityData certificadoSecurityData = CertificadoSecurityDataFactory.construir(certificado);
            
            if (certificadoSecurityData instanceof CertificadoFuncionarioPublico) {
                CertificadoFuncionarioPublico certificadoFuncionarioPublico = (CertificadoFuncionarioPublico) certificadoSecurityData;
                
                datosUsuario.setCedula(certificadoFuncionarioPublico.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoFuncionarioPublico.getNombres());
                datosUsuario.setApellido(certificadoFuncionarioPublico.getPrimerApellido() + " "
                        + certificadoFuncionarioPublico.getSegundoApellido());
                datosUsuario.setCargo(certificadoFuncionarioPublico.getCargo());
                datosUsuario.setInstitucion(certificadoFuncionarioPublico.getInstitucion());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoSecurityData instanceof CertificadoPersonaJuridica) {
                CertificadoPersonaJuridica certificadoPersonaJuridica = (CertificadoPersonaJuridica) certificadoSecurityData;
                datosUsuario.setCedula(certificadoPersonaJuridica.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaJuridica.getNombres());
                datosUsuario.setApellido(certificadoPersonaJuridica.getPrimerApellido() + " "
                        + certificadoPersonaJuridica.getSegundoApellido());
                datosUsuario.setCargo(certificadoPersonaJuridica.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            
            if (certificadoSecurityData instanceof CertificadoPersonaNatural) {
                CertificadoPersonaNatural certificadoPersonaNatural = (CertificadoPersonaNatural) certificadoSecurityData;
                datosUsuario.setCedula(certificadoPersonaNatural.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaNatural.getNombres());
                datosUsuario.setApellido(certificadoPersonaNatural.getPrimerApellido() + " "
                        + certificadoPersonaNatural.getSegundoApellido());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            return datosUsuario;
        }

        /**
         * ****************************************
         * TODO comentar esto para produccion
         */
        if (CertificadoBancoCentralFactory.estTestCa(certificado)) {
            DatosUsuario datosUsuario = new DatosUsuario();
            
            CertificadoBancoCentral certificadoBancoCentral = CertificadoBancoCentralFactory.construir(certificado);
            if (certificadoBancoCentral instanceof CertificadoFuncionarioPublico) {
                CertificadoFuncionarioPublico certificadoFuncionarioPublico = (CertificadoFuncionarioPublico) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoFuncionarioPublico.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoFuncionarioPublico.getNombres());
                datosUsuario.setApellido(certificadoFuncionarioPublico.getPrimerApellido() + " "
                        + certificadoFuncionarioPublico.getSegundoApellido());
                datosUsuario.setInstitucion(certificadoFuncionarioPublico.getInstitucion());
                datosUsuario.setCargo(certificadoFuncionarioPublico.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoMiembroEmpresa) {
                CertificadoMiembroEmpresa certificadoMiembroEmpresa = (CertificadoMiembroEmpresa) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoMiembroEmpresa.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoMiembroEmpresa.getNombres());
                datosUsuario.setApellido(certificadoMiembroEmpresa.getPrimerApellido() + " "
                        + certificadoMiembroEmpresa.getSegundoApellido());
                datosUsuario.setCargo(certificadoMiembroEmpresa.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoPersonaJuridica) {
                CertificadoPersonaJuridica certificadoPersonaJuridica = (CertificadoPersonaJuridica) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoPersonaJuridica.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaJuridica.getNombres());
                datosUsuario.setApellido(certificadoPersonaJuridica.getPrimerApellido() + " "
                        + certificadoPersonaJuridica.getSegundoApellido());
                datosUsuario.setCargo(certificadoPersonaJuridica.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoPersonaNatural) {
                CertificadoPersonaNatural certificadoPersonaNatural = (CertificadoPersonaNatural) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoPersonaNatural.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoPersonaNatural.getNombres());
                datosUsuario.setApellido(certificadoPersonaNatural.getPrimerApellido() + " "
                        + certificadoPersonaNatural.getSegundoApellido());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            if (certificadoBancoCentral instanceof CertificadoRepresentanteLegal) {
                CertificadoRepresentanteLegal certificadoRepresentanteLegal = (CertificadoRepresentanteLegal) certificadoBancoCentral;
                datosUsuario.setCedula(certificadoRepresentanteLegal.getCedulaPasaporte());
                datosUsuario.setNombre(certificadoRepresentanteLegal.getNombres());
                datosUsuario.setApellido(certificadoRepresentanteLegal.getPrimerApellido() + " "
                        + certificadoRepresentanteLegal.getSegundoApellido());
                datosUsuario.setCargo(certificadoRepresentanteLegal.getCargo());
                datosUsuario.setSerial(certificado.getSerialNumber().toString());
            }
            return datosUsuario;
        }
        /**
         * Fin de cambio para produccion
         */
        System.out.println("Llego a nulo");
        return null;
    }
}

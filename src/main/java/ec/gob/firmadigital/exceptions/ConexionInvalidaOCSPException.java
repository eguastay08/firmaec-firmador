/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.exceptions;

/**
 *
 * @author jdc
 */
public class ConexionInvalidaOCSPException extends Exception{
    private static final long serialVersionUID = 1L;

	public ConexionInvalidaOCSPException(String msg){
		super(msg);
	}
}

package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.PrivateKey;
import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

@NotNullByDefault
interface Signature {

	/**
	 * @see java.security.Signature#initSign(java.security.PrivateKey)
	 */
	void initSign(PrivateKey k) throws GeneralSecurityException;

	/**
	 * @see java.security.Signature#initVerify(java.security.PublicKey)
	 */
	void initVerify(PublicKey k) throws GeneralSecurityException;

	/**
	 * @see java.security.Signature#update(byte)
	 */
	void update(byte b) throws GeneralSecurityException;

	/**
	 * @see java.security.Signature#update(byte[])
	 */
	void update(byte[] b) throws GeneralSecurityException;

	/**
	 * @see java.security.Signature#update(byte[], int, int)
	 */
	void update(byte[] b, int off, int len) throws GeneralSecurityException;

	/**
	 * @see java.security.Signature#sign()}
	 */
	byte[] sign() throws GeneralSecurityException;

	/**
	 * @see java.security.Signature#verify(byte[])
	 */
	boolean verify(byte[] signature) throws GeneralSecurityException;
}

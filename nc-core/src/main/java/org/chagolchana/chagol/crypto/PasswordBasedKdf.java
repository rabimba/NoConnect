package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.SecretKey;

interface PasswordBasedKdf {

	int chooseCostParameter();

	SecretKey deriveKey(String password, byte[] salt, int cost);
}

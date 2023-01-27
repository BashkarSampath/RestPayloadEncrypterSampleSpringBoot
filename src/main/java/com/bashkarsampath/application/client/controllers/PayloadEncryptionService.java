package com.bashkarsampath.application.client.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;

import lombok.Data;

public class PayloadEncryptionService {
	private KeyStore pkcs12KeyStore;
	@NotNull
	private PrivateKey privateKey;
	@NotNull
	private RSAPublicKey publicKey;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public PayloadEncryptionService(InputStream pkcs12KeystoreInputStream, String keystorePassword)
			throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		this.pkcs12KeyStore = KeyStore.getInstance("PKCS12");
		this.pkcs12KeyStore.load(pkcs12KeystoreInputStream, keystorePassword.toCharArray());
	}

	/**
	 * Load a Certificate out of a PKCS#12 container.
	 */
	public Certificate getCertificate(String certificatename) throws KeyStoreException {
		return this.pkcs12KeyStore.getCertificate(certificatename);
	}

	/**
	 * Load a RSA signing key out of a PKCS#12 container.
	 */
	public PrivateKey getSigningKey(String signingKeyAlias, String signingKeyPassword)
			throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		return (PrivateKey) this.pkcs12KeyStore.getKey(signingKeyAlias, signingKeyPassword.toCharArray());
	}

	/**
	 * Converts PEM file content to RSAPublicKey
	 */
	public RSAPublicKey getRSAPublicKey(String certificateName) throws KeyStoreException {
		Certificate cf = this.getCertificate(certificateName);
		return (RSAPublicKey) cf.getPublicKey();
	}

	// Make sure you are reading and passing the correct keyId from credentials.
	// This is required and is passed in headers.
	public EncryptedResponse getEncryptedPayload(RSAPublicKey publicKey, Object payload, String keyId)
			throws JOSEException, IOException {
		String plainText = payload == null ? "" : OBJECT_MAPPER.writeValueAsString(payload);
		JWEHeader.Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_512,
				EncryptionMethod.A256CBC_HS512);
		headerBuilder.keyID(keyId);
		headerBuilder.customParam("iat", System.currentTimeMillis());
		JWEObject jweObject = new JWEObject(headerBuilder.build(), new Payload(plainText));
		jweObject.encrypt(new RSAEncrypter(publicKey));
		EncryptedResponse encryptedResponse = new EncryptedResponse();
		encryptedResponse.setEncData(jweObject.serialize());
		return encryptedResponse;
	}

	public <T> T getDecryptedPayload(PrivateKey privateKey, EncryptedResponse encryptedPayload, Class<T> returnType)
			throws ParseException, JOSEException, IOException {
		String response = encryptedPayload.getEncData();
		JWEObject jweObject = JWEObject.parse(response);
		jweObject.decrypt(new RSADecrypter(privateKey));
		response = jweObject.getPayload().toString();
		return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsBytes(response), returnType);
	}
}

@Data
class EncryptedResponse {
	private String encData;
}
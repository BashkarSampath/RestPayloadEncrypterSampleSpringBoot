package com.bashkarsampath.application.client.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bashkarsampath.application.client.configuration.SwaggerDocumentation;
import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.shaded.gson.JsonSyntaxException;

@RestController
@SwaggerDocumentation
public class AppController {

	private final PayloadEncryptionService payloadEncryptionService;
	private final PrivateKey privateKey;
	private final RSAPublicKey publicKey;

	public AppController(@Autowired ResourceLoader resourceLoader) throws IOException, GeneralSecurityException {
		this.payloadEncryptionService = new PayloadEncryptionService(
				resourceLoader.getResource("classpath:ClientKeystore.p12").getInputStream(), "bas2023");
		this.privateKey = this.payloadEncryptionService.getSigningKey("privatekey", "bas2023");
		this.publicKey = this.payloadEncryptionService.getRSAPublicKey("server-public-cert.bashkarsampath.com");
	}

	@PostMapping({ "/data/encrypted" })
	public ResponseEntity<EncryptedResponse> returnEncryptedModel(@RequestBody JsonNode requestPayload)
			throws IOException, JOSEException {
		EncryptedResponse encryptedData = this.payloadEncryptionService.getEncryptedPayload(this.publicKey,
				requestPayload, "123");
		return ResponseEntity.ok(encryptedData);
	}

	@PostMapping({ "/data/decrypt" })
	public ResponseEntity<String> decrypt(@RequestBody EncryptedResponse encryptedPayload)
			throws JsonSyntaxException, ParseException, JOSEException, IOException {
		String decryptedPayload = this.payloadEncryptionService.getDecryptedPayload(this.privateKey, encryptedPayload,
				String.class);
		return ResponseEntity.ok(decryptedPayload);
	}
}
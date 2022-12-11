/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.manager;

import static org.bouncycastle.asn1.x500.style.BCStyle.CN;
import static pt.ulisboa.tecnico.surespace.prover.R.raw.export;
import static pt.ulisboa.tecnico.surespace.prover.util.Utils.toast;

import android.content.res.Resources;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.manager.EntityManager;
import pt.ulisboa.tecnico.surespace.common.manager.KeyStoreManager;
import pt.ulisboa.tecnico.surespace.common.manager.NonceManager;
import pt.ulisboa.tecnico.surespace.common.manager.exception.EntityManagerException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.KeyStoreManagerException;
import pt.ulisboa.tecnico.surespace.common.message.MessageValidator;
import pt.ulisboa.tecnico.surespace.common.message.SignedMessageValidator;
import pt.ulisboa.tecnico.surespace.common.message.exception.MessageValidatorException;
import pt.ulisboa.tecnico.surespace.long_term_ca.client.LongTermCAClient;
import pt.ulisboa.tecnico.surespace.long_term_ca.client.LongTermCAClientException;
import pt.ulisboa.tecnico.surespace.long_term_ca.common.message.SignedRegisterEntityRequest;
import pt.ulisboa.tecnico.surespace.long_term_ca.common.message.SignedRegisterEntityRequest.RegisterEntityRequest;
import pt.ulisboa.tecnico.surespace.long_term_ca.common.message.SignedRegisterEntityResponse;

public final class ProverKeyStoreManager extends KeyStoreManager {
  private final ProverManager manager;

  public ProverKeyStoreManager(ProverManager manager) throws KeyStoreManagerException {
    super(manager.property().get("keystore", "key").asCharArray(), manager.property());
    this.manager = manager;

    beforeLoading();
    afterLoading();
  }

  public static boolean correctKeyStoreCredentials(
      ProverPropertyManager propertyManager, Entity prover, String password) {
    try {
      File file = keyStoreFile(propertyManager, prover);
      KeyStore keyStore = KeyStore.getInstance(keyStoreExtension(propertyManager));

      keyStore.load(new FileInputStream(file), password.toCharArray());
      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private static String keyStoreExtension(ProverPropertyManager propertyManager) {
    return propertyManager.get("keystore", "instance").asString();
  }

  private static File keyStoreFile(ProverPropertyManager propertyManager, Entity prover) {
    File root = propertyManager.activity().getFilesDir();
    String extension = keyStoreExtension(propertyManager);

    return new File(root, prover.getDescriptiveUId() + "." + extension);
  }

  private static void removeKeyStoreCredentials(
      ProverPropertyManager propertyManager, Entity prover) throws KeyStoreManagerException {
    if (!keyStoreFile(propertyManager, prover).delete())
      throw new KeyStoreManagerException("Could not delete keystore");
  }

  @Override
  public void beforeLoading() throws KeyStoreManagerException {
    super.beforeLoading();
    Resources resources = manager.activity().getResources();

    try {
      File keyStoreFile = keyStoreFile();
      final char[] newPassword = propertyManager().get("password").asCharArray();

      if (!keyStoreFile.exists()) {
        logManager().info("[-] Keystore file not found.");

        if (!keyStoreFile.createNewFile()) {
          logManager().error("[-] Could not create keystore.");
          throw new KeyStoreManagerException("Could not create keystore.");
        }

        try (InputStream inputStream = resources.openRawResource(export);
            OutputStream outputStream = new FileOutputStream(keyStoreFile)) {
          // Copy existing keystore in res folder to Android storage.
          ByteStreams.copy(inputStream, outputStream);

          // The keystore is loaded with the old password and stored with the new one.
          load();
          store(newPassword);

          logManager().info("[+] Successfully created keystore from resources file.");
        }

      } else logManager().info("[+] Using existing keystore file.");

      // Update passwords.
      updateKeyStorePassword(newPassword);

    } catch (EntityManagerException e) {
      logManager().error("[-] Could not create keystore.");
      throw new KeyStoreManagerException(e.getMessage());

    } catch (IOException e) {
      logManager().error("[-] IOException.");
    }
  }

  private EntityManager entityManager() {
    return manager.entity();
  }

  private X500Name getProverX500Name(Entity prover) {
    return new X500NameBuilder().addRDN(CN, prover.getName()).build();
  }

  private File keyStoreFile() throws EntityManagerException {
    return keyStoreFile(manager.property(), manager.entity().current());
  }

  @Override
  protected InputStream keyStoreInputStream() throws KeyStoreManagerException {
    try {
      return new FileInputStream(keyStoreFile());

    } catch (EntityManagerException e) {
      throw new KeyStoreManagerException(e.getMessage());

    } catch (FileNotFoundException e) {
      throw new KeyStoreManagerException("File not found");
    }
  }

  @Override
  protected OutputStream keyStoreOutputStream() throws KeyStoreManagerException {
    try {
      return new FileOutputStream(keyStoreFile());

    } catch (EntityManagerException e) {
      throw new KeyStoreManagerException(e.getMessage());

    } catch (FileNotFoundException e) {
      throw new KeyStoreManagerException("File not found");
    }
  }

  private ProverLogManager logManager() {
    return manager.log();
  }

  private NonceManager nonceManager() {
    return manager.nonce();
  }

  private ProverPropertyManager propertyManager() {
    return manager.property();
  }

  public void registerToLtca(Entity prover) throws KeyStoreManagerException {
    try {
      Entity ltca = entityManager().getByPath("surespace://rca/ltca");

      // Generate a CSR.
      KeyPair keyPair = generateKeyPair();
      PKCS10CertificationRequest csr = generateCsr(keyPair, getProverX500Name(prover));

      // Create the request.
      RegisterEntityRequest request =
          RegisterEntityRequest.newBuilder()
              .setSender(prover)
              .setReceiver(ltca)
              .setNonce(nonceManager())
              .setCsr(csr.getEncoded())
              .build();

      // Sign the request.
      SignedRegisterEntityRequest signedRequest =
          SignedRegisterEntityRequest.newBuilder()
              .setMessage(request)
              .setSignature(this, keyPair.getPrivate())
              .build();

      LongTermCAClient client = manager.getLtcaClient();
      SignedRegisterEntityResponse signedResponse = client.registerEntity(signedRequest);

      // Validate received message.
      new MessageValidator(manager)
          .init(signedResponse.getMessage())
          .assertReceiver(prover)
          .assertSender(ltca)
          .assertCertificateValid()
          .assertNonceValid()
          .validate();

      new SignedMessageValidator(manager).init(signedResponse).assertSignature().validate();

      // By now, everything is OK.
      byte[][] chainBytes = signedResponse.getMessage().getCertificateBytesChain();
      Certificate[] certificateChain = certificateChainFromBytes(chainBytes);
      Certificate certificate = certificateChain[0];

      if (isValidCertificate(certificate)) {
        setCertificateEntry(prover, certificate);
        setKeyEntry(prover, keyPair.getPrivate(), certificateChain);
        toast(manager.activity(), "Successfully registered '%s'.", prover);

      } else toast(manager.activity(), "Received an invalid certificate");

    } catch (LongTermCAClientException | EntityManagerException | MessageValidatorException e) {

      e.printStackTrace();
      removeKeyStoreCredentials(propertyManager(), prover);
      throw new KeyStoreManagerException(e.getMessage());

    } catch (IOException e) {
      removeKeyStoreCredentials(propertyManager(), prover);
      e.printStackTrace();
    }
  }
}

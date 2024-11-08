package anuznomii.lol.apihyperledgerfabricspring.services;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi.ecDSARipeMD160;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/* 
This service will use used in order to register and enroll the user Fabric CA
 */
@Service
@Slf4j
public class FabricCAService {

    @Value("${fabric.ca.org1.caUrl}")
    private String org1CaUrl;
    @Value("${fabric.ca.org1.certificatePath}")
    private String org1CertificatePath;
    @Value("${fabric.wallet.config-path}")
    private String walletPath;
    private Wallet wallet;

    @Value("${fabric.ca.tls.enabled}")
    private Boolean tlsEnabled;

    // Only for testing
    @Value("${fabric.ca.admin.username}")
    private String adminUsername;
    @Value("${fabric.ca.admin.password}")
    private String adminPassword;

    @PostConstruct
    public void init() throws Exception {
        // create wallet instance
        this.wallet = Wallets.newFileSystemWallet(
                Paths.get(walletPath));
        // create admin first, in order to register and enroll the user
        createAdminUserOrg1();
    }

    private void createAdminUserOrg1() throws Exception {

        // 1. create enrollment request
        var enrollmentRequest = new EnrollmentRequest();
        // enrollmentRequest.addAttrReq(); // used in future for attributes
        enrollmentRequest.setProfile("tls");
        enrollmentRequest.addHost("localhost");

        // 2. Setup HFCAClient
        var props = new Properties();
        if (tlsEnabled) {
            // we will configure the tls properties for the HFCA client
            // 2.1. configure tls
            File pemFile = new File(org1CertificatePath);
            if (!pemFile.exists()) {
                throw new Exception("Certificate org1 CA file does not exist");
            }
            // verify the type of the certificate && validity of the certificate
            props.setProperty("pemFile", org1CertificatePath);
            props.setProperty("allowAllHostNames", "true");
            // 2.2. configure the timeout
            props.put("connectTimeout", "30000");
            props.put("readTimeout", "30000");
        }
        var caClient = HFCAClient.createNewInstance(
                org1CaUrl,
                props);
        caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        var enrollment = caClient.enroll(
                adminUsername,
                adminPassword,
                enrollmentRequest);

        var certificate = Identities.readX509Certificate(
                enrollment.getCert());
        var adminIdentity = Identities.newX509Identity(
                "Org1MSP", certificate, enrollment.getKey());

        wallet.put("admin", adminIdentity);
        log.info("Successfully store the identity to the wallet !  ");
    }

}

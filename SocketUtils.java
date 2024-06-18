import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.joda.time.DateTime;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;


public class SocketUtils {
    private static final String BC = org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider.PROVIDER_NAME;

    public static X509Certificate getSelfCertificate(KeyPair keyPair, String organisation, String orgUnit, String commonName, Date issueDate,
                                                     long validForSeconds,
                                                     String signatureAlgorithm)
            throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException {
        try {
            Security.addProvider(new org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider());

            X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
            builder.addRDN(BCStyle.OU, orgUnit);
            builder.addRDN(BCStyle.O, organisation);
            builder.addRDN(BCStyle.CN, commonName);


            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

            Date notAfter = new DateTime(issueDate).plusSeconds((int) validForSeconds).toDate();
            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(builder.build(),
                    serial, issueDate, notAfter, builder.build(), keyPair.getPublic());
            ContentSigner sigGen = new JcaContentSignerBuilder(signatureAlgorithm)
                    .setProvider(BC).build(keyPair.getPrivate());
            X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC)
                    .getCertificate(certGen.build(sigGen));
            cert.checkValidity(new Date());
            cert.verify(cert.getPublicKey());

            return cert;
        } catch (OperatorCreationException ioe) {
            throw new CertificateEncodingException("Error during creation of self-signed Certificate: " + ioe.getMessage(), ioe);
        }
    }
}

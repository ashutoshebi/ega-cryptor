/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.egacryptor.cryptography.pgp;

import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PGPUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PGPCryptography.class);

    private static final int KEY_FLAGS = 27;
    private static final List<Integer> MASTER_KEY_CERTIFICATION_TYPES = Arrays.asList(PGPSignature.POSITIVE_CERTIFICATION,
            PGPSignature.CASUAL_CERTIFICATION,
            PGPSignature.NO_CERTIFICATION,
            PGPSignature.DEFAULT_CERTIFICATION);

    public static PGPPublicKey readPublicKey(final InputStream inputStream) throws IOException, PGPException {

        LOGGER.trace("Searching for public key in keyring");

        final PGPPublicKeyRingCollection pgpPublicKeyRings = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(inputStream),
                new BcKeyFingerprintCalculator());

        // just loop through the collection till we find a key suitable for encryption
        PGPPublicKey publicKey = null;

        // iterate through the key rings.
        final Iterator<PGPPublicKeyRing> publicKeyRingIterator = pgpPublicKeyRings.getKeyRings();

        while (publicKey == null && publicKeyRingIterator.hasNext()) {
            final PGPPublicKeyRing pgpPublicKeyRing = publicKeyRingIterator.next();
            final Iterator<PGPPublicKey> publicKeyIterator = pgpPublicKeyRing.getPublicKeys();
            while (publicKey == null && publicKeyIterator.hasNext()) {
                PGPPublicKey key = publicKeyIterator.next();
                if (key.isEncryptionKey()) {
                    publicKey = key;
                }
            }
        }

        if (publicKey == null) {
            throw new IllegalArgumentException("Can't find public key in the key ring.");
        }

        if (!isForEncryption(publicKey)) {
            throw new IllegalArgumentException("KeyID " + publicKey.getKeyID() + " not flagged for encryption.");
        }
        LOGGER.trace("Public key found for encryption with KeyID {}", publicKey.getKeyID());
        return publicKey;
    }

    public static PGPEncryptedDataGenerator getEncryptedGenerator(final PGPPublicKey pgpPublicKey) {
        final PGPEncryptedDataGenerator pgpEncryptedDataGenerator = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).
                        setWithIntegrityPacket(true).
                        setSecureRandom(new SecureRandom()).
                        setProvider("BC"));
        pgpEncryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(pgpPublicKey).setProvider("BC"));
        return pgpEncryptedDataGenerator;
    }

    private static boolean isForEncryption(final PGPPublicKey key) {
        if (key.getAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN ||
                key.getAlgorithm() == PublicKeyAlgorithmTags.DSA ||
                key.getAlgorithm() == PublicKeyAlgorithmTags.ECDSA) {
            return false;
        }
        return hasKeyFlags(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
    }

    @SuppressWarnings(value = "unchecked")
    private static boolean hasKeyFlags(final PGPPublicKey encKey, final int keyUsage) {
        if (encKey.isMasterKey()) {
            for (final int masterKeyCertType : MASTER_KEY_CERTIFICATION_TYPES) {
                final Iterator<PGPSignature> pgpSignatureIterator = encKey.getSignaturesOfType(masterKeyCertType);
                while (pgpSignatureIterator.hasNext()) {
                    final PGPSignature pgpSignature = pgpSignatureIterator.next();
                    if (isNotMatchingUsage(pgpSignature, keyUsage)) {
                        return false;
                    }
                }
            }
        } else {
            final Iterator<PGPSignature> signaturesOfType = encKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING);
            while (signaturesOfType.hasNext()) {
                final PGPSignature pgpSignature = signaturesOfType.next();
                if (isNotMatchingUsage(pgpSignature, keyUsage)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isNotMatchingUsage(final PGPSignature pgpSignature, final int keyUsage) {
        if (pgpSignature.hasSubpackets()) {
            final PGPSignatureSubpacketVector pgpSignatureSubpacketVector = pgpSignature.getHashedSubPackets();
            if (pgpSignatureSubpacketVector.hasSubpacket(KEY_FLAGS)) {
                return pgpSignatureSubpacketVector.getKeyFlags() == 0 && keyUsage == 0;
            }
        }
        return false;
    }
}

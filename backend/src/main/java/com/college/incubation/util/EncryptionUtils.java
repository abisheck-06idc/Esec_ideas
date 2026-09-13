package com.college.incubation.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";

    private static final String TRANSFORMATION =
            "AES/GCM/NoPadding";

    private static final int IV_LENGTH = 12;

    private static final int TAG_LENGTH = 128;

    @Value("${app.encryption.key}")
    private String configuredKey;

    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {

        byte[] keyBytes =
                Base64.getDecoder()
                        .decode(configuredKey);

        if (keyBytes.length != 32) {

            throw new IllegalStateException(
                    "Encryption key must be exactly 32 bytes"
            );
        }

        secretKey =
                new SecretKeySpec(
                        keyBytes,
                        ALGORITHM
                );
    }

    public String encrypt(
            String plainText
    ) {

        try {

            byte[] iv =
                    new byte[IV_LENGTH];

            SecureRandom random =
                    new SecureRandom();

            random.nextBytes(iv);

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec spec =
                    new GCMParameterSpec(
                            TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    spec
            );

            byte[] encrypted =
                    cipher.doFinal(
                            plainText.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            byte[] result =
                    new byte[
                            IV_LENGTH +
                            encrypted.length
                    ];

            System.arraycopy(
                    iv,
                    0,
                    result,
                    0,
                    IV_LENGTH
            );

            System.arraycopy(
                    encrypted,
                    0,
                    result,
                    IV_LENGTH,
                    encrypted.length
            );

            return Base64.getEncoder()
                    .encodeToString(result);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Encryption failed",
                    e
            );
        }
    }

    public String decrypt(
            String encryptedText
    ) {

        try {

            byte[] combined =
                    Base64.getDecoder()
                            .decode(encryptedText);

            byte[] iv =
                    new byte[IV_LENGTH];

            byte[] encrypted =
                    new byte[
                            combined.length -
                            IV_LENGTH
                    ];

            System.arraycopy(
                    combined,
                    0,
                    iv,
                    0,
                    IV_LENGTH
            );

            System.arraycopy(
                    combined,
                    IV_LENGTH,
                    encrypted,
                    0,
                    encrypted.length
            );

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec spec =
                    new GCMParameterSpec(
                            TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    spec
            );

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Decryption failed",
                    e
            );
        }
    }
}
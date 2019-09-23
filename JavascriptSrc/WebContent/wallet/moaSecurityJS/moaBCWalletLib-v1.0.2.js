const MoaWallet = (function () {
    const ENCRYPT = 10000000;
    const DECRYPT = 20000000;
    const TYPE_RESTORE = "R";
    const TYPE_NORMAL = "";

    class Options {
        constructor() {
            this.VERSION_INFO = 1;
            this.OS_NAME = "WebBrowser";
            this.SYMMETRIC_ALG = "AES/CTR/NoPadding";
            this.SYMMETRIC_KEY_SIZE = 256;
            this.HASH_ALG = "SHA256";
            this.SIGNATURE_ALG = "SHA256withECDSA";
            this.ECC_CURVE = "secp256r1";
            this.MAC_ALG = "HmacSHA256";
            this.ITERATION_COUNT = 4096;

            switch (this.HASH_ALG) {
                case "SHA3-256":
                case "SHA256":
                    this.SALT_SIZE = 32;
                    break;
                case "SHA3-384":
                case "SHA384":
                    this.SALT_SIZE = 48;
                    break;
                case "SHA3-512":
                case "SHA512":
                    this.SALT_SIZE = 64;
                    break;
            }
        }
    }
    const options = new Options();

    // private byte[] BlockchainEncryptPrk(byte[] privateKeyByte, String passwordStr, byte[] saltByte, int iterationCount)
    const getPrk = (mode, data) => {
        let result;
        if (mode == ENCRYPT) {
            const firstEnc = getAESData(ENCRYPT, data);
            data.target = firstEnc.ciphertext;
            result = getPBEData(ENCRYPT, data);
        } else if (mode == DECRYPT) {
            const firstDec = getPBEData(DECRYPT, data);
            data.target = firstDec.toString(CryptoJS.enc.Base64);
            result = getAESData(DECRYPT, data);
        }
        return result;
    }

    const getPBKDF2 = (data) => {
        const derivationKey = CryptoJS.PBKDF2(
            data.psw,
            data.salt,
            {
                keySize: 384 / 32,
                hasher: CryptoJS.algo.SHA384,
                iterations: data.cnt == undefined ?
                    options.ITERATION_COUNT : data.cnt
            }
        );
        return derivationKey.toString(CryptoJS.enc.Hex);
    }

    const getAESData = (mode, data) => {
        const pbkdf2 = getPBKDF2(data);
        const key = CryptoJS.enc.Hex.parse(
            pbkdf2.substr(0, 32 * 2)
        );
        const iv = CryptoJS.enc.Hex.parse(
            pbkdf2.substr(32 * 2, 16 * 2)
        );
        let result;
        if (mode == ENCRYPT) {
            result = CryptoJS.AES.encrypt(
                CryptoJS.enc.Hex.parse(data.target),
                key,
                {
                    mode: CryptoJS.mode.CTR,
                    iv: iv,
                    padding: CryptoJS.pad.NoPadding
                }
            );
        } else if (mode == DECRYPT) {
            result = CryptoJS.AES.decrypt(
                data.target,
                key,
                {
                    mode: CryptoJS.mode.CTR,
                    iv: iv,
                    padding: CryptoJS.pad.NoPadding
                }
            );
        }
        return result;
    }

    const getPBEData = (mode, data) => {
        let result;
        if (mode == ENCRYPT) {
            result = CryptoJS.lib.PasswordBasedCipher.encrypt(
                CryptoJS.algo.AES,
                data.target,
                data.psw,
                {
                    format: CryptoJS.format.OpenSSL
                }
            );
        } else if (mode == DECRYPT) {
            result = CryptoJS.lib.PasswordBasedCipher.decrypt(
                CryptoJS.algo.AES,
                data.target,
                data.psw,
                {
                    format: CryptoJS.format.OpenSSL
                }
            );
        }
        return result;
    }

    // public byte[] EthereumWalletAddrGen(String hashAlg, byte[] publicKeyBytes)
    const generateAddr = (puk) => {
        const hash = CryptoJS.SHA256(puk).toString(CryptoJS.enc.Hex);
        const address = hash.substr(12 * 2, 32 * 2);
        return CryptoJS.enc.Hex.parse(address);
    }

    const generateMAC = (dataForMAC) => {
        const hash = CryptoJS.SHA256(dataForMAC.saltAndPsw);
        const combineMAC = dataForMAC.version
            + dataForMAC.os
            + dataForMAC.type
            + dataForMAC.saltAndPsw.toString(CryptoJS.enc.Hex)
                .substr(0, options.SALT_SIZE * 2)
            + dataForMAC.iterationCnt
            + CryptoJS.enc.Base64.parse(dataForMAC.target.toString())
                .toString(CryptoJS.enc.Hex)
            + dataForMAC.puk.toString()
            + dataForMAC.address.toString(CryptoJS.enc.Hex);
        return CryptoJS.HmacSHA256(CryptoJS.enc.Utf8.parse(combineMAC), hash);
    }

    const saveData = (dataForSave) => {
        const properties = getWalletProperties(dataForSave.type);
        if (localStorage.getItem(properties.version) != null) {
            alert("wallet already exist!");
            return;
        }
        localStorage.setItem("Wallet.Type", dataForSave.type);
        localStorage.setItem(properties.version, options.VERSION_INFO);
        localStorage.setItem(properties.os, options.OS_NAME);
        localStorage.setItem(properties.salt,
            dataForSave.salt.toString(CryptoJS.enc.Hex));
        localStorage.setItem(properties.iterationCnt, options.ITERATION_COUNT);
        localStorage.setItem(properties.cipheredData,
            CryptoJS.enc.Base64.parse(dataForSave.target.toString())
                .toString(CryptoJS.enc.Hex));
        localStorage.setItem(properties.walletPuk, dataForSave.puk);
        localStorage.setItem(properties.walletAddr,
            dataForSave.address.toString(CryptoJS.enc.Hex));
        localStorage.setItem(properties.macData,
            dataForSave.mac.toString(CryptoJS.enc.Hex));
    }

    const getWalletProperties = (type) => {
        let version = "Version.Info";
        let os = "OS.Info";
        let salt = "Salt.Value";
        let iterationCnt = "Iteration.Count";
        let cipheredData = "Ciphered.Data";
        let walletPuk = "Wallet.PublicKey";
        let walletAddr = "Wallet.Addr";
        let macData = "MAC.Data";
        if (type.indexOf("R") > -1) {
            const prefix = "R.";
            version = prefix + version;
            os = prefix + os;
            salt = prefix + salt;
            iterationCnt = prefix + iterationCnt;
            cipheredData = prefix + cipheredData;
            walletPuk = prefix + walletPuk;
            walletAddr = prefix + walletAddr;
            macData = prefix + macData;
        }
        return walletProperties = {
            version: version,
            os: os,
            salt: salt,
            iterationCnt: iterationCnt,
            cipheredData: cipheredData,
            walletPuk: walletPuk,
            walletAddr: walletAddr,
            macData: macData,
        };
    }

    const verifyMAC = (type, password) => {
        const properties = getWalletProperties(type);
        const dataForMAC = {
            version: localStorage.getItem(properties.version),
            os: localStorage.getItem(properties.os),
            type: type,
            saltAndPsw: localStorage.getItem(properties.salt)
                + CryptoJS.enc.Utf8.parse(password)
                    .toString(CryptoJS.enc.Hex),
            iterationCnt: localStorage.getItem(properties.iterationCnt),
            target: CryptoJS.enc.Hex.parse(
                localStorage.getItem(properties.cipheredData)
            ).toString(CryptoJS.enc.Base64),
            puk: localStorage.getItem(properties.walletPuk),
            address: localStorage.getItem(properties.walletAddr)
        }
        const mac = localStorage.getItem(properties.macData);
        if (mac.indexOf(generateMAC(dataForMAC).toString()) == -1) {
            alert("[#] moaWallet Data Integrity Verify Error!");
            return false;
        } else {
            return true;
        }
    }

    const wordToByteArray = (wordArray) => {
        const byteArray = [];
        let word;
        for (let i = 0; i < wordArray.length; ++i) {
            word = wordArray[i];
            for (let j = 3; j >= 0; --j) {
                byteArray.push((word >> 8 * j) & 0xFF);
            }
        }
        return byteArray;
    }

    const generateHmacPsw = (psw) => {
        const hash = CryptoJS.SHA256(psw);
        const hmac = CryptoJS.HmacSHA256(CryptoJS.enc.Utf8.parse(psw), hash)
            .toString(CryptoJS.enc.Hex);
        if (wordToByteArray(hash.words)[0] % 2 == 0) {
            return hmac.substr(14 * 2, 14 * 2);
        } else {
            return hmac.substr(0, 14 * 2);
        }
    }

    const decimalToHexString = (number) => {
        if (number < 0) {
            number = 0xFF + number + 1;
        }
        return ("00" + number.toString(16)).substr(-2);
    }

    return {
        TYPE_RESTORE: "R",
        TYPE_NORMAL: "",

        // public void NonRestoreBlockchainWalletIssuing(String alias, String password)
        createNonRestoreWallet: password => {
            const type = "";
            let keyPair = MoaECDSACoreAPI.doGenerate(options.ECC_CURVE);
            const salt = CryptoJS.lib.WordArray.random(options.SALT_SIZE);
            const encPrk = getPrk(
                ENCRYPT,
                {
                    target: keyPair.ecprvhex,
                    psw: password,
                    salt: salt
                }
            );
            keyPair.ecprvhex = null;
            const address = generateAddr(keyPair.ecpubhex);
            const combineSaltWithPsw = salt.toString(CryptoJS.enc.Hex)
                + CryptoJS.enc.Utf8.parse(password)
                    .toString(CryptoJS.enc.Hex);
            password = null;
            const mac = generateMAC({
                version: options.VERSION_INFO,
                os: options.OS_NAME,
                type: type,
                saltAndPsw: combineSaltWithPsw,
                iterationCnt: options.ITERATION_COUNT,
                target: encPrk,
                puk: keyPair.ecpubhex,
                address: address
            });
            saveData({
                type: type,
                salt: salt,
                target: encPrk,
                puk: keyPair.ecpubhex,
                address: address,
                mac: mac
            });
        },

        // public String RestoreBlockchainWalletInfoGen(String alias, String passwordStr)
        generateRestoreDataFormat: password => {
            let keyPair = MoaECDSACoreAPI.doGenerate(options.ECC_CURVE);
            const salt = CryptoJS.lib.WordArray.random(options.SALT_SIZE);
            let dataForEnc = {
                target: keyPair.ecprvhex,
                psw: password,
                salt: salt
            };
            const encPrk = getAESData(ENCRYPT, dataForEnc);
            dataForEnc.target = keyPair.ecpubhex;
            const encPuk = getAESData(ENCRYPT, dataForEnc);
            const hmacEncryptedPuk = CryptoJS.HmacSHA256(
                CryptoJS.enc.Base64.parse(encPuk.toString()), password
            );
            return hmacEncryptedPuk.toString(CryptoJS.enc.Base64) + "%"
                + encPrk.toString() + "$"
                + encPuk.toString() + "$"
                + salt.toString(CryptoJS.enc.Base64);
        },

        // public void RestoreBlockchainWallet(String passwordStr, String encWPrkBase64AndencWPukBase64AndwSaltBase64)
        createRestoreWallet: (password, data) => {
            if (data == null) {
                return;
            }
            const type = "R";
            const restoreData = data.split('$');
            let prk = CryptoJS.enc.Hex.parse(CryptoJS.enc.Base64.parse(restoreData[0])
                .toString(CryptoJS.enc.Hex));
            let puk = restoreData[1];
            const salt = CryptoJS.enc.Base64.parse(restoreData[2]);
            prk = getPBEData(
                ENCRYPT,
                {
                    target: prk,
                    psw: password,
                    salt: salt
                }
            );
            puk = getAESData(
                DECRYPT,
                {
                    target: puk,
                    psw: password,
                    salt: salt
                }
            );
            const address = generateAddr(puk);
            const combineSaltWithPsw = salt.toString(CryptoJS.enc.Hex)
                + CryptoJS.enc.Utf8.parse(password)
                    .toString(CryptoJS.enc.Hex);

            password = null;
            const mac = generateMAC({
                version: options.VERSION_INFO,
                os: options.OS_NAME,
                type: type,
                saltAndPsw: combineSaltWithPsw,
                iterationCnt: options.ITERATION_COUNT,
                target: prk,
                puk: puk,
                address: address
            });
            saveData({
                type: type,
                salt: salt,
                target: prk,
                puk: puk,
                address: address,
                mac: mac
            });
        },

        // public String GetBlockchainPulicKey() throws FileNotFoundException, IOException {
        getPublicKey: () => {
            if (localStorage.getItem("Wallet.Type").indexOf("R") > -1) {
                return localStorage.getItem("R.Wallet.PublicKey");
            } else {
                return localStorage.getItem("Wallet.PublicKey");
            }
        },

        // public String GetBlockchainWalletAddr() throws FileNotFoundException, IOException {
        getWalletAddr: () => {
            if (localStorage.getItem("Wallet.Type").indexOf("R") > -1) {
                return localStorage.getItem("R.Wallet.Addr");
            } else {
                return localStorage.getItem("Wallet.Addr");
            }
        },

        // public byte[] BlockchainTransactionSign(String alias, String trasctionData, String password)
        generateTransactionSign: (transaction, password) => {
            const type = localStorage.getItem("Wallet.Type");
            if (!verifyMAC(type, password)) {
                return;
            }
            let target;
            let salt;
            if (type.indexOf("R") > -1) {
                target = CryptoJS.enc.Hex.parse(localStorage.getItem("R.Ciphered.Data"))
                    .toString(CryptoJS.enc.Base64);
                salt = CryptoJS.enc.Hex.parse(localStorage.getItem("R.Salt.Value"));
            } else {
                target = CryptoJS.enc.Hex.parse(localStorage.getItem("Ciphered.Data"))
                    .toString(CryptoJS.enc.Base64);
                salt = CryptoJS.enc.Hex.parse(localStorage.getItem("Salt.Value"));
            }
            const prk = getPrk(
                DECRYPT,
                {
                    target: target,
                    psw: password,
                    salt: salt
                }
            );
            return MoaECDSACoreAPI.doSign(
                options.ECC_CURVE,
                options.SIGNATURE_ALG,
                transaction,
                prk.toString()
            );
        },

        // public boolean BlockchainTransactionVerify(byte[] trasctionDataBytes, byte[] signatureBytes, byte[] publicKeyBytes)
        verifyTransactionSign: (transaction, sign, puk) => {
            return MoaECDSACoreAPI.doVerify(
                options.ECC_CURVE,
                options.SIGNATURE_ALG,
                transaction,
                sign,
                puk
            );
        },

        setType: (type) => {
            if (type.indexOf(TYPE_NORMAL) == -1 && type.indexOf(TYPE_RESTORE) == -1) {
                return;
            }
            localStorage.setItem("Wallet.Type", type);
        },

        convertRestoreDataFormat: (msg) => {
            const eachMsg = msg.split('$');
            const base64Prk = eachMsg[eachMsg.length - 2];
            const pukHmacPukSaltHexStr = CryptoJS.enc.Base64
                .parse(eachMsg[eachMsg.length - 1])
                .toString();
            const base64Puk = CryptoJS.enc.Hex
                .parse(pukHmacPukSaltHexStr.substr(0, pukHmacPukSaltHexStr.length - 128))
                .toString(CryptoJS.enc.Base64);
            const base64Salt = CryptoJS.enc.Hex
                .parse(pukHmacPukSaltHexStr.substr(pukHmacPukSaltHexStr.length - 64, 64))
                .toString(CryptoJS.enc.Base64);
            return base64Prk.concat('$')
                .concat(base64Puk)
                .concat('$')
                .concat(base64Salt);
        },

        getHmacPsw: (psw) => {
            return generateHmacPsw(psw);
        },

        getEncryptedHmacPsw: (id, psw, dateOfBirth) => {
            // hmac 생성
            const hmac = generateHmacPsw(psw);

            // 암호화된 hmac 생성
            const derivationKey = getPBKDF2(
                data = {
                    psw: dateOfBirth,
                    salt: id,
                    cnt: 10
                }
            );
            const encryptedHmac = CryptoJS.AES.encrypt(
                CryptoJS.enc.Hex.parse(CryptoJS.lib.WordArray.random(1) + hmac),
                CryptoJS.enc.Hex.parse(derivationKey.substr(0, 32 * 2)), // dbk
                {
                    mode: CryptoJS.mode.CBC,
                    iv: CryptoJS.enc.Hex.parse(derivationKey.substr(32 * 2, 16 * 2)),
                    padding: CryptoJS.pad.Pkcs7
                }
            );

            // hmac 생성 (암호화된 hmac 기반)
            const hmacEncryptedHmac = CryptoJS.HmacSHA256(
                CryptoJS.enc.Base64.parse(encryptedHmac.toString()), dateOfBirth
            ).toString();

            // 절반 크기의 hmac 생성            
            let halfHmacEncryptedHmac = "";
            for (let i = 0; i < hmacEncryptedHmac.length / 2; i += 2) {
                halfHmacEncryptedHmac +=
                    decimalToHexString(
                        parseInt(hmacEncryptedHmac.substr(i, 2), 16) ^
                        parseInt(
                            hmacEncryptedHmac.substr(hmacEncryptedHmac.length / 2 + i, 2
                            ), 16)
                    );
            }
            return CryptoJS.enc.Base64.parse(encryptedHmac.toString()).toString(CryptoJS.enc.Hex)
                + halfHmacEncryptedHmac;
        },

        // msg [Header$State$E(prk)$E(puk)+Hmac(E(puk))+salt]
        verifyPsw: (psw, msg) => {
            const firstSplit = msg.split('$');
            const encryptedPuk = CryptoJS.enc.Base64.parse(firstSplit[3])
                .toString(CryptoJS.enc.Hex)
                .substr(0, 65 * 2);
            const hmacEncryptedPuk = CryptoJS.enc.Base64.parse(firstSplit[3])
                .toString(CryptoJS.enc.Hex)
                .substr(65 * 2, 32 * 2);
            const newHmacEncryptedPuk = CryptoJS.HmacSHA256(
                CryptoJS.enc.Hex.parse(encryptedPuk), psw
            ).toString(CryptoJS.enc.Hex);
            return hmacEncryptedPuk.indexOf(newHmacEncryptedPuk) == 0;
        }
    }
})();
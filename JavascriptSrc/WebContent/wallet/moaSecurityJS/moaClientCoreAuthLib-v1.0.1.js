const MoaClientCoreAuthLib = (function () {
    // Define about MOA Manager ID Parsing for ID & Password : ID/Psw Regist 
    const moaCORE_IDExist_STR = "4D4F410001";
    const moaCORE_IDExistAck_STR = "4D4F410002";
    const moaCORE_IdPswRegReq_STR = "4D4F410003";
    const moaCORE_IdPswRegRes_STR = "4D4F410004";

    const moaMANAGER_IdPswRegReq_STR = "4D4F410013";    // Only Manager 
    const moaMANAGER_IdPswRegRes_STR = "4D4F410014";    // Only Manager 

    // Define about MOA Manager ID Parsing for ID & Password : ID/Psw LogIn
    const moaCORE_IdPswLogInStartReq_STR = "4D4F412001";
    const moaCORE_IdPswLogInSTartRes_STR = "4D4F412002";
    const moaCORE_IdPswLogInReq_STR = "4D4F412003";
    const moaCORE_IdPswLogInRes_STR = "4D4F412004";

    const moaMANAGER_IdPswLogInReq_STR = "4D4F412013";    // Only Manager 
    const moaMANAGER_IdPswLogInRes_STR = "4D4F412014";    // Only Manager 

    // Define about MOA Manager ID Parsing for ID & Password : Change Psw (After LogIn)
    const moaCORE_ChagePswReq_STR = "4D4F413001";
    const moaCORE_ChagePswRes_STR = "4D4F413002";

    // Define about MOA Manager ID Parsing for ID & Password : UnRegist (After LogIn)
    const moaCORE_UnRegistReq_STR = "4D4F413007";
    const moaCORE_UnRegistRes_STR = "4D4F413008";

    const moaCORE_BCWalletRegistReq_STR = "4D4F414003";
    const moaCORE_BCWalletRegistRes_STR = "4D4F414004";
    const moaCORE_BCWalletRestoreReq_STR = "4D4F414007";
    const moaCORE_BCWalletRestoreRes_STR = "4D4F414008";

    const moaCORE_BCWalletRegistSuccess_STR = "6080";
    const moaCORE_BCWalletRestoreSuccess_STR = "6084";

    // Declaration about Initial Vector of Symmetric Alg 
    const iv = "00FF0000FF00FF000000FFFF000000FF";

    const checkHeader = (header) => {
        if (header.indexOf(moaCORE_IDExistAck_STR) == 0 ||				//	Header Parging & Check of "4D4F410002$...$...$" MSG 
            header.indexOf(moaCORE_IdPswRegRes_STR) == 0 ||			//	Header Parging & Check of "4D4F410004$...$...$" MSG 
            header.indexOf(moaMANAGER_IdPswRegRes_STR) == 0 ||		//	Header Parging & Check of "4D4F410014$...$...$" MSG [Only Manager]
            header.indexOf(moaCORE_IdPswLogInSTartRes_STR) == 0 ||	//	Header Parging & Check of "4D4F412002$...$...$" MSG 
            header.indexOf(moaCORE_IdPswLogInRes_STR) == 0 ||			//	Header Parging & Check of "4D4F412004$...$...$" MSG 
            header.indexOf(moaMANAGER_IdPswLogInRes_STR) == 0 ||	//	Header Parging & Check of "4D4F412014$...$...$" MSG [Only Manager]
            header.indexOf(moaCORE_ChagePswRes_STR) == 0 ||			//	Header Parging & Check of "4D4F413007$...$...$" MSG 
            header.indexOf(moaCORE_UnRegistRes_STR) == 0)				// Header Parging & Check of "4D4F413008$...$...$" MSG 
            return true;
        else
            return false;
    }

    const getEncryptedMergeMsg = (id, psw) => {
        const hashEncryptedPsw = CryptoJS.SHA256(
            CryptoJS.AES.encrypt(
                psw,
                CryptoJS.enc.Hex.parse(
                    CryptoJS.SHA256(id).toString()
                        .substring(0, iv.length)),
                { iv: CryptoJS.enc.Hex.parse(iv) }
            ).ciphertext
        );
        const hmacId = CryptoJS.HmacSHA256(id, hashEncryptedPsw);
        return CryptoJS.enc.Utf8.parse(id).toString(CryptoJS.enc.Base64).concat('$')
            .concat(hashEncryptedPsw.toString(CryptoJS.enc.Base64)).concat('$')
            .concat(hmacId.toString(CryptoJS.enc.Base64));
    }

    return {
        MANAGER_ADMIN: "0x61",
        MANAGER_TOP: "0x62",
        MANAGER_GENERAL: "0x63",

        coreMsgPacketParser: message => {
            const headerIndex = message.indexOf('$');
            if (headerIndex === -1) {
                return '';
            }
            if (checkHeader(message.substring(0, headerIndex).toUpperCase())) {
                return message.substring(headerIndex + 1, message.length);
            } else {
                return '';
            }
        },

        coreIdExistRegistRequestMsgGenProcess: id => {
            return moaCORE_IDExist_STR.concat('$')
                .concat(CryptoJS.enc.Utf8.parse(id).toString(CryptoJS.enc.Base64));
        },

        coreIdPswRegistRequestMsgGenProcess: (managerLevel, id, psw) => {
            if (managerLevel === null) {
                return moaCORE_IdPswRegReq_STR.concat('$')
                    .concat(getEncryptedMergeMsg(id, psw));
            } else {
                return moaMANAGER_IdPswRegReq_STR.concat('$')
                    .concat(managerLevel).concat('$')
                    .concat(getEncryptedMergeMsg(id, psw));
            }
        },

        coreIdPswLogInStartRequestMsgGenProcess: () => {
            return moaCORE_IdPswLogInStartReq_STR;
        },

        coreIdPswLogInRequestMsgGenProcess: (id, psw, nonce, isManager) => {
            if (isManager) {
                return moaMANAGER_IdPswLogInReq_STR.concat('$')
                    .concat(getEncryptedMergeMsg(id, psw)).concat('$')
                    .concat(nonce);
            } else {
                return moaCORE_IdPswLogInReq_STR.concat('$')
                    .concat(getEncryptedMergeMsg(id, psw)).concat('$')
                    .concat(nonce);
            }
        },

        coreIdPswChangePswRequestMsgGenProcess: (id, psw, newPsw) => {
            const cipheredNewPswMsg = getEncryptedMergeMsg(id, newPsw);
            return moaCORE_ChagePswReq_STR.concat('$')
                .concat(getEncryptedMergeMsg(id, psw)).concat('$')
                .concat(cipheredNewPswMsg.substring(cipheredNewPswMsg.indexOf('$') + 1, cipheredNewPswMsg.length));
        },

        coreIdPswUnRegistRequestMsgGenProcess: id => {
            return moaCORE_UnRegistReq_STR.concat('$')
                .concat(CryptoJS.enc.Utf8.parse(id).toString(CryptoJS.enc.Base64));
        },

        generateSign: message => {
            const curve = "secp256r1";
            const signAlg = "SHA256withECDSA";
            const keyPairObj = MoaECDSACoreAPI.doGenerate(curve);
            return MoaECDSACoreAPI.doSign(
                curve,
                signAlg,
                message,
                keyPairObj.ecprvhex
            );
        },

        generateWalletRegistMsg: (id, psw, restoreMsg) => {
            const firstSplit = restoreMsg.split('%');
            const secondSplit = firstSplit[1].split('$');
            const originHmacEncryptedPuk = CryptoJS.enc.Hex.parse(
                CryptoJS.enc.Base64.parse(firstSplit[0])
                    .toString(CryptoJS.enc.Hex)
            );
            const originPuk = CryptoJS.enc.Hex.parse(
                CryptoJS.enc.Base64.parse(secondSplit[1])
                    .toString(CryptoJS.enc.Hex)
            );
            const originSalt = CryptoJS.enc.Hex.parse(
                CryptoJS.enc.Base64.parse(secondSplit[2])
                    .toString(CryptoJS.enc.Hex)
            );
            const msg = moaCORE_BCWalletRegistReq_STR.concat('$')
                .concat(CryptoJS.enc.Utf8.parse(id).toString(CryptoJS.enc.Base64)).concat('$')
                .concat(CryptoJS.enc.Hex.parse(psw).toString(CryptoJS.enc.Base64)).concat('$')
                .concat(secondSplit[0]).concat('$')
                .concat(originPuk.concat(originHmacEncryptedPuk).concat(originSalt).toString(CryptoJS.enc.Base64));
            return msg;
        },

        verifyRegisterWalletMsg: (msg) => {
            const verifyMsg = msg.split('$');
            if (verifyMsg[0].indexOf(moaCORE_BCWalletRegistRes_STR) > -1 &&
                verifyMsg[1].indexOf(moaCORE_BCWalletRegistSuccess_STR) > -1)
                return true;
            else
                return false;
        },

        generateRestoreWalletMsg: (id) => {
            return moaCORE_BCWalletRestoreReq_STR.concat('$')
                .concat(CryptoJS.enc.Utf8.parse(id).toString(CryptoJS.enc.Base64));
        },

        verifyRestoreWalletMsg: (msg) => {
            const verifyMsg = msg.split('$');
            if (verifyMsg[0].indexOf(moaCORE_BCWalletRestoreRes_STR) > -1 &&
                verifyMsg[1].indexOf(moaCORE_BCWalletRestoreSuccess_STR) > -1)
                return true;
            else
                return false;
        }
    }
})();
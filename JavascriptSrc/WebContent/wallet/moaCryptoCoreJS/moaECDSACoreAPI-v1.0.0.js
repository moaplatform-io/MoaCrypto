const MoaECDSACoreAPI = (function () {
	return {
		doGenerate: function(ecCurve) {
			const ec = new KJUR.crypto.ECDSA({"curve": ecCurve});
			const keyPair = ec.generateKeyPairHex();
			return keyPair; 			
		},

		doSign: function(ecCurve, hash, msg, privateKey) {
			const sig = new KJUR.crypto.Signature({"alg": hash});
			sig.init({d: privateKey, curve: ecCurve});
			sig.updateString(msg);
			return sig.sign();
		},

		doVerify: function(ecCurve, hash, msg, sign, publicKey) {			
			const sig = new KJUR.crypto.Signature({"alg": hash, "prov": "cryptojs/jsrsa"});
			sig.init({xy: publicKey, curve: ecCurve});
			sig.updateString(msg);
			return sig.verify(sign);			
		}
	}
	
}());
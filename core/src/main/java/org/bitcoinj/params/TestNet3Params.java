/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.params;

import org.bitcoinj.core.CoinDefinition;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.net.discovery.HttpDiscovery;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the testnet, a separate public instance of Bitcoin that has relaxed rules suitable for development
 * and testing of applications and new Bitcoin versions.
 */
public class TestNet3Params extends NetworkParameters {
    public TestNet3Params() {
        super();
        id = ID_TESTNET;
        packetMagic = CoinDefinition.testnetPacketMagic;
        port = CoinDefinition.TestPort;
        addressHeader = CoinDefinition.testnetAddressHeader;
        p2shHeader = CoinDefinition.testnetp2shHeader;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = CoinDefinition.proofOfWorkLimit;
        dumpedPrivateKeyHeader = 128;
        genesisBlock.setTime(CoinDefinition.testnetGenesisBlockTime);
        genesisBlock.setDifficultyTarget(CoinDefinition.testnetGenesisBlockDifficultyTarget);
        genesisBlock.setNonce(CoinDefinition.testnetGenesisBlockNonce);
        spendableCoinbaseDepth = CoinDefinition.spendableCoinbaseDepth;
        mergedMiningEnabled = CoinDefinition.testnetMergedMineEnable;
        mergedMineStartTime = CoinDefinition.testnetMergedMineStartTime;
        mergedMineChainID = CoinDefinition.testnetMergedMineChainID;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals(CoinDefinition.testnetGenesisHash));
        CoinDefinition.initCheckpoints(checkpoints);

        dnsSeeds = CoinDefinition.testnetDnsSeeds;
        httpSeeds = new HttpDiscovery.Details[]{};
        alertSigningKey = Utils.HEX.decode(CoinDefinition.TESTNET_SATOSHI_KEY);
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;
    }

    private static TestNet3Params instance;
    public static synchronized TestNet3Params get() {
        if (instance == null) {
            instance = new TestNet3Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }
}

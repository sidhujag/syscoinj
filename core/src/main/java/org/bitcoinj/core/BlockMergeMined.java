/**
 * Copyright 2011 Google Inc.
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

package org.bitcoinj.core;

import com.google.common.primitives.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * <p>A block is a group of transactions, and is one of the fundamental data structures of the Bitcoin system.
 * It records a set of {@link org.bitcoinj.core.Transaction}s together with some data that links it into a place in the global block
 * chain, and proves that a difficult calculation was done over its contents. See
 * <a href="http://www.bitcoin.org/bitcoin.pdf">the Bitcoin technical paper</a> for
 * more detail on blocks. <p/>
 *
 * To get a block, you can either build one from the raw bytes you can get from another implementation, or request one
 * specifically using {@link org.bitcoinj.core.Peer#getBlock(org.bitcoinj.core.Sha256Hash)}, or grab one from a downloaded {@link org.bitcoinj.core.BlockChain}.
 */
public class BlockMergeMined {
    private static final Logger log = LoggerFactory.getLogger(BlockMergeMined.class);

    public static final long BLOCK_VERSION_AUXPOW = (1 << 8);
    public static final long BLOCK_VERSION_CHAIN_START = (1 << 16);

    public static final byte pchMergedMiningHeader[] = { (byte)0xfa, (byte)0xbe, 'm', 'm' } ;
    // Fields defined as part of the protocol format.
    // modifiers

    private transient NetworkParameters params;
    public transient Block block;
    public transient BlockMergeMinedPayload payload;


    /** Constructs a block object from the Bitcoin wire format. */
    public BlockMergeMined(NetworkParameters netparams, byte[] payloadBytes, int cursor, Block block) throws ProtocolException {
        params = netparams;

        payload = new BlockMergeMinedPayload(this.params, payloadBytes, cursor, block);
        setBlock(block);
    }
    private void setBlock(Block block)
    {
        this.block = block;
        if(this.payload != null && this.payload.block == null)
            this.payload.block = block;
    }
    public long GetChainID(long ver)
    {
        return ver / BLOCK_VERSION_CHAIN_START;
    }
    public long GetChainID()
    {
        return block.getVersion() / BLOCK_VERSION_CHAIN_START;
    }
    public boolean IsValid()
    {
        return payload != null && payload.IsValid();
    }
    public Sha256Hash getParentBlockHash()
    {
        Sha256Hash blockHash;
        switch (CoinDefinition.coinPOWHash)
        {
            case scrypt:
                blockHash = payload.parentBlockHeader.getScryptHash();
                break;
            case SHA256:
                blockHash = payload.parentBlockHeader.getHash();
                break;
            default:  //use the normal getHash() method.
                blockHash = payload.parentBlockHeader.getHash();
                break;
        }
        return blockHash;
    }
    /** Returns the version of the block data structure as defined by the Bitcoin protocol. */
    public long getVersion() {
        return block.getVersion();
    }

    /**
     * Returns the nonce, an arbitrary value that exists only to make the hash of the block header fall below the
     * difficulty target.
     */
    public long getNonce() {
        return block.getNonce();
    }
    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */

    public String toString() {
        StringBuilder s = new StringBuilder("");
        s.append("      version: v");
        s.append(block.getVersion());
        s.append("\n");
        s.append("      time: [");
        s.append(block.getTime());
        s.append("] ");
        s.append(new Date(block.getTimeSeconds() * 1000));
        s.append("\n");
        s.append("      difficulty target (nBits): ");
        s.append(block.getDifficultyTarget());
        s.append("\n");
        s.append("      nonce: ");
        s.append(block.getNonce());
        s.append("\n");
        if(payload != null)
        {
            s.append("\n");
            s.append(payload.toString());
        }
        return s.toString();
    }

    public int getCursor()
    {
        if(payload != null)
            return payload.cursor;
        else
            return 0;
    }
    public int getMessageSize()
    {
        if(payload != null)
            return payload.length;
        else
            return 0;
    }
    /** Returns true if the hash of the block is OK (lower than difficulty target). */

    protected boolean checkProofOfWork(boolean throwException) throws VerificationException {
        if(GetChainID() != params.mergedMineChainID)
        {
            throw new VerificationException("Merged-mine block does not have the correct chain ID required for Devcoin blocks, Current ID: " + GetChainID() + " Expected: " + params.mergedMineChainID);
        }
        if(GetChainID(payload.parentBlockHeader.getVersion()) == params.mergedMineChainID)
        {
            throw new VerificationException("Merged-mine block Aux POW parent has our chain ID: " + params.mergedMineChainID);
        }
        TransactionInput coinbaseInput = payload.parentBlockCoinBaseTx.getInput(0);
        byte[] scriptBytes = coinbaseInput.getScriptBytes();
        int headerIndex = Bytes.indexOf(scriptBytes, this.pchMergedMiningHeader);
        if(headerIndex > 0)
        {
            if((scriptBytes.length - headerIndex) >= headerIndex)
            {
                byte[] remainingBytes = java.util.Arrays.copyOfRange(scriptBytes, headerIndex, scriptBytes.length - headerIndex);
                headerIndex = Bytes.indexOf(remainingBytes, this.pchMergedMiningHeader);
                if(headerIndex > 0)
                {
                    throw new VerificationException("Multiple merged mining headers in coinbase");
                }
            }
            if(!coinbaseInput.isCoinBase())
            {
                throw new VerificationException("Parent coinbase transaction not an actual coinbase transaction!");
            }
        }
        return true;
    }


    public boolean equals(Object o) {
        if (!(o instanceof BlockMergeMined))
            return false;
        BlockMergeMined other = (BlockMergeMined) o;
        return block.getTimeSeconds() == other.block.getTimeSeconds();
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node. This
     * is measured in seconds since the UNIX epoch (midnight Jan 1st 1970).
     */
    public long getTimeSeconds() {
        return block.getTimeSeconds();
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node.
     */
    public Date getTime() {
        return new Date(getTimeSeconds()*1000);
    }



}
/**
 * Reed-Solomon Coding over 8-bit values.
 *
 * Copyright 2015, Backblaze, Inc.
 */

package com.csp;

/**
 * Reed-Solomon Coding over 8-bit values.
 */
public class Secureconding {

    private final int dataCount;
    private final int parityCount;
    private final int totalCount;
    private final Dataset matrix;

    /**
     * Rows from the matrix for encoding parity, each one as its own
     * byte array to allow for efficient access while encoding.
     */
    private final byte [] [] parityRows;

    /**
     * Initializes a new encoder/decoder.
     */
    public Secureconding(int dataShardCount, int parityShardCount) {
        this.dataCount = dataShardCount;
        this.parityCount = parityShardCount;
        this.totalCount = dataShardCount + parityShardCount;
        matrix = buildMatrix(dataShardCount, this.totalCount);
        parityRows = new byte [parityShardCount] [];
        for (int i = 0; i < parityShardCount; i++) {
            parityRows[i] = matrix.getRow(dataShardCount + i);
        }
    }

    /**
     * Returns the number of data shards.
     */
    public int getDataShardCount() {
        return dataCount;
    }

    /**
     * Returns the number of parity shards.
     */
    public int getParityShardCount() {
        return parityCount;
    }

    /**
     * Returns the total number of shards.
     */
    public int getTotalShardCount() {
        return totalCount;
    }

    
    public void encodeParity(byte[][] shards, int offset, int byteCount) {
        // Check arguments.
        checkBuffersAndSizes(shards, offset, byteCount);

        // Build the array of output buffers.
        byte [] [] outputs = new byte [parityCount] [];
        for (int i = 0; i < parityCount; i++) {
            outputs[i] = shards[dataCount + i];
        }

        // Do the coding.
        codeSomeShards(parityRows, shards, outputs, parityCount,
                offset, byteCount);
    }

    public boolean isParityCorrect(byte[][] shards, int firstByte, int byteCount) {
        // Check arguments.
        checkBuffersAndSizes(shards, firstByte, byteCount);

        // Build the array of buffers being checked.
        byte [] [] toCheck = new byte [parityCount] [];
        for (int i = 0; i < parityCount; i++) {
            toCheck[i] = shards[dataCount + i];
        }

        // Do the checking.
        return checkSomeShards(parityRows, shards, toCheck, parityCount,
                firstByte, byteCount);
    }


    public void decodeMissing(byte [] [] shards,
                              boolean [] shardPresent,
                              final int offset,
                              final int byteCount) {
        // Check arguments.
        checkBuffersAndSizes(shards, offset, byteCount);

        int numberPresent = 0;
        for (int i = 0; i < totalCount; i++) {
            if (shardPresent[i]) {
                numberPresent += 1;
            }
        }
        if (numberPresent == totalCount) {
           
            return;
        }

        // More complete sanity check
        if (numberPresent < dataCount) {
            throw new IllegalArgumentException("Not enough shards present");
        }

        
        Dataset subMatrix = new Dataset(dataCount, dataCount);
        byte [] [] subShards = new byte [dataCount] [];
        {
            int subMatrixRow = 0;
            for (int matrixRow = 0; matrixRow < totalCount && subMatrixRow < dataCount; matrixRow++) {
                if (shardPresent[matrixRow]) {
                    for (int c = 0; c < dataCount; c++) {
                        subMatrix.set(subMatrixRow, c, matrix.get(matrixRow, c));
                    }
                    subShards[subMatrixRow] = shards[matrixRow];
                    subMatrixRow += 1;
                }
            }
        }

        Dataset dataDecodeMatrix = subMatrix.invert();

        byte [] [] outputs = new byte [parityCount] [];
        byte [] [] matrixRows = new byte [parityCount] [];
        int outputCount = 0;
        for (int iShard = 0; iShard < dataCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                matrixRows[outputCount] = dataDecodeMatrix.getRow(iShard);
                outputCount += 1;
            }
        }
        codeSomeShards(matrixRows, subShards, outputs, outputCount, offset, byteCount);

        
        outputCount = 0;
        for (int iShard = dataCount; iShard < totalCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                matrixRows[outputCount] = parityRows[iShard - dataCount];
                outputCount += 1;
            }
        }
        codeSomeShards(matrixRows, shards, outputs, outputCount, offset, byteCount);
    }

    private void checkBuffersAndSizes(byte [] [] shards, int offset, int byteCount) {
       if (shards.length != totalCount) {
            throw new IllegalArgumentException("wrong number of shards: " + shards.length);
        }
int shardLength = shards[0].length;
        for (int i = 1; i < shards.length; i++) {
            if (shards[i].length != shardLength) {
                throw new IllegalArgumentException("Shards are different sizes");
            }
        }

        // The offset and byteCount must be non-negative and fit in the buffers.
        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative: " + offset);
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount is negative: " + byteCount);
        }
        if (shardLength < offset + byteCount) {
            throw new IllegalArgumentException("buffers to small: " + byteCount + offset);
        }
    }

  
    private void codeSomeShards(final byte [] [] matrixRows,
                                final byte [] [] inputs,
                                final byte [] [] outputs,
                                final int outputCount,
                                final int offset,
                                final int byteCount) {

      
        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < outputCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < dataCount; c++) {
                    value ^= LogData.multiply(matrixRow[c], inputs[c][iByte]);
                }
                outputs[iRow][iByte] = (byte) value;
            }
        }
    }

    
    private boolean checkSomeShards(final byte [] [] matrixRows,
                                    final byte [] [] inputs,
                                    final byte [] [] toCheck,
                                    final int checkCount,
                                    final int offset,
                                    final int byteCount) {

      
        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < checkCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < dataCount; c++) {
                    value ^= LogData.multiply(matrixRow[c], inputs[c][iByte]);
                }
                if (toCheck[iRow][iByte] != (byte) value) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Dataset buildMatrix(int dataShards, int totalShards) {
        
        Dataset vandermonde = vandermonde(totalShards, dataShards);
        Dataset top = vandermonde.submatrix(0, 0, dataShards, dataShards);
        return vandermonde.times(top.invert());
    }

    private static Dataset vandermonde(int rows, int cols) {
        Dataset result = new Dataset(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result.set(r, c, LogData.exp((byte) r, c));
            }
        }
        return result;
    }
}

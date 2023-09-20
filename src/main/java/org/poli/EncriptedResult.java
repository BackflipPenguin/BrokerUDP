package org.poli;

public class EncriptedResult {
    private byte[] iv;

    private byte[] message;

    public EncriptedResult(byte[] iv, byte[] message) {
        this.iv = iv;
        this.message = message;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }
}
